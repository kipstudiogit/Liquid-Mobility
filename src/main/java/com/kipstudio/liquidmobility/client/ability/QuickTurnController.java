// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.ability;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.client.render.FrameDeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QuickTurnController {
    private static final float TURN_DURATION_SECONDS = 0.16F;
    private static final FrameDeltaTracker FRAME_DELTA = new FrameDeltaTracker();

    private static float elapsedSeconds;
    private static float previousSmoothProgress;
    private static boolean turning;

    private QuickTurnController() {
    }

    public static boolean isTurning() {
        return turning;
    }

    public static void trigger(ClientPlayerEntity player) {
        if (turning) {
            return;
        }
        turning = true;
        elapsedSeconds = 0.0F;
        previousSmoothProgress = 0.0F;
        FRAME_DELTA.reset();
        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.45F, 1.85F);
    }

    public static void cancel() {
        turning = false;
        elapsedSeconds = 0.0F;
        previousSmoothProgress = 0.0F;
        FRAME_DELTA.reset();
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !turning) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.isPaused() || mc.screen != null) {
            cancel();
            return;
        }

        float dt = FRAME_DELTA.sampleDeltaSeconds();
        elapsedSeconds += dt;
        float linear = MathHelper.clamp(elapsedSeconds / TURN_DURATION_SECONDS, 0.0F, 1.0F);
        float smooth = linear * linear * (3.0F - 2.0F * linear);
        float deltaYaw = 180.0F * (smooth - previousSmoothProgress);
        previousSmoothProgress = smooth;

        player.yRot += deltaYaw;
        player.yRotO += deltaYaw;
        player.yHeadRot += deltaYaw;

        if (linear >= 1.0F) {
            turning = false;
        }
    }
}