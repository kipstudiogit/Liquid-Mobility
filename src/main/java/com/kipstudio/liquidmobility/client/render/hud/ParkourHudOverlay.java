// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render.hud;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.client.render.FrameDeltaTracker;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ParkourHudOverlay {
    private static final FrameDeltaTracker HUD_DELTA = new FrameDeltaTracker();
    private static float smoothedFlow;
    private static float streakIntensity;

    private ParkourHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null
                || mc.options.hideGui
                || mc.screen != null
                || !MobilityConfig.CLIENT.enableParkourHud.get()
                || CommonMovementHandler.isMobilityBlocked(player)) {
            HUD_DELTA.reset();
            return;
        }

        float dt = HUD_DELTA.sampleDeltaSeconds();
        smoothedFlow = FrameDeltaTracker.expLerp(smoothedFlow, CameraEffectsState.getFlowLevel(), 10.0F, dt);

        float targetStreaks = 0.0F;
        if (smoothedFlow > 0.42F) {
            targetStreaks = (smoothedFlow - 0.42F) / 0.58F;
        }
        targetStreaks = Math.max(targetStreaks, CameraEffectsState.getDashImpulse() * 0.9F);
        if (CameraEffectsState.isFastDropping()) {
            targetStreaks = Math.max(targetStreaks, 0.85F);
        }
        streakIntensity = FrameDeltaTracker.expLerp(streakIntensity, MathHelper.clamp(targetStreaks, 0.0F, 1.0F), 12.0F, dt);

        MatrixStack matrix = event.getMatrixStack();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        if (MobilityConfig.CLIENT.enableSpeedLines.get()
                && mc.options.getCameraType() == PointOfView.FIRST_PERSON
                && streakIntensity > 0.03F) {
            renderSpeedStreaks(matrix, width, height, streakIntensity);
        }

        if (MobilityConfig.CLIENT.enableFlowBar.get() && smoothedFlow > 0.02F) {
            renderFlowBar(matrix, width, height, smoothedFlow);
        }
    }

    private static void renderSpeedStreaks(MatrixStack matrix, int width, int height, float intensity) {
        double time = System.nanoTime() * 1.0E-9D;
        int alpha = MathHelper.clamp((int) (intensity * 105.0F), 0, 120);
        if (alpha <= 2) {
            return;
        }
        int color = (alpha << 24) | 0xF2F6FA;

        for (int i = 0; i < 6; i++) {
            float cycle = (float) ((time * (2.6D + (i % 3) * 0.7D) + i * 0.37D) % 1.0D);
            int y = MathHelper.clamp((int) (height * (0.12F + (i * 0.145F))), 4, Math.max(5, height - 6));
            int len = 18 + (int) (intensity * 42.0F);
            int inset = (int) ((1.0F - cycle) * 34.0F);

            AbstractGui.fill(matrix, inset, y, inset + len, y + 1, color);
            AbstractGui.fill(matrix, width - inset - len, y + 3, width - inset, y + 4, color);
        }

        for (int i = 0; i < 4; i++) {
            float cycle = (float) ((time * (2.9D + i * 0.5D) + i * 0.29D) % 1.0D);
            int x = MathHelper.clamp((int) (width * (0.18F + i * 0.21F)), 4, Math.max(5, width - 6));
            int len = 12 + (int) (intensity * 26.0F);
            int inset = (int) ((1.0F - cycle) * 24.0F);

            AbstractGui.fill(matrix, x, inset, x + 1, inset + len, color);
            AbstractGui.fill(matrix, x + 4, height - inset - len, x + 5, height - inset, color);
        }
    }

    private static void renderFlowBar(MatrixStack matrix, int width, int height, float flow) {
        int barWidth = 44;
        int x = (width / 2) - (barWidth / 2);
        int y = (height / 2) + 14;

        float fadeAlpha = MathHelper.clamp(flow * 3.0F, 0.0F, 1.0F);
        int bgAlpha = (int) (fadeAlpha * 95.0F);
        int fgAlpha = (int) (fadeAlpha * 225.0F);

        int bgColor = (bgAlpha << 24) | 0x101216;
        AbstractGui.fill(matrix, x, y, x + barWidth, y + 2, bgColor);

        int fillWidth = MathHelper.clamp((int) Math.round(barWidth * flow), 0, barWidth);
        if (fillWidth > 0) {
            float crimsonMix = MathHelper.clamp((flow - 0.55F) / 0.30F, 0.0F, 1.0F);
            int r = (int) MathHelper.lerp(crimsonMix, 245.0F, 236.0F);
            int g = (int) MathHelper.lerp(crimsonMix, 245.0F, 24.0F);
            int b = (int) MathHelper.lerp(crimsonMix, 248.0F, 38.0F);
            int fgColor = (fgAlpha << 24) | (r << 16) | (g << 8) | b;
            AbstractGui.fill(matrix, x, y, x + fillWidth, y + 2, fgColor);
        }
    }
}