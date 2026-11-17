// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render.vision;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RouteVisionRenderer {
    private static List<RouteHighlight> cachedHighlights = Collections.emptyList();
    private static int lastScanTick = -100;

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null
                || mc.level == null
                || !MobilityConfig.CLIENT.enableRouteVision.get()
                || !CameraEffectsState.isRouteVisionActive()
                || CommonMovementHandler.isMobilityBlocked(player)) {
            cachedHighlights = Collections.emptyList();
            return;
        }

        if (Math.abs(player.tickCount - lastScanTick) >= 2) {
            lastScanTick = player.tickCount;
            cachedHighlights = RouteVisionScanner.scan(player, MobilityConfig.CLIENT.routeVisionRange.get());
        }

        if (cachedHighlights.isEmpty()) {
            return;
        }

        MatrixStack matrix = event.getMatrixStack();
        Vector3d camPos = mc.gameRenderer.getMainCamera().getPosition();
        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
        IVertexBuilder builder = buffer.getBuffer(RenderType.lines());

        float alpha = MobilityConfig.CLIENT.routeVisionOpacity.get().floatValue();

        matrix.pushPose();
        matrix.translate(-camPos.x, -camPos.y, -camPos.z);

        for (RouteHighlight highlight : cachedHighlights) {
            AxisAlignedBB box = highlight.getBox();
            switch (highlight.getCategory()) {
                case VAULT_LEDGE:
                case CLIMB_LEDGE:
                    WorldRenderer.renderLineBox(
                            matrix, builder,
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            0.94F, 0.08F, 0.14F, alpha
                    );
                    break;
                case WALL_RUN:
                    WorldRenderer.renderLineBox(
                            matrix, builder,
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            0.85F, 0.10F, 0.16F, alpha * 0.55F
                    );
                    break;
                case SOFT_CUSHION:
                    WorldRenderer.renderLineBox(
                            matrix, builder,
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ,
                            0.96F, 0.96F, 0.98F, alpha * 0.9F
                    );
                    break;
            }
        }

        matrix.popPose();
        buffer.endBatch(RenderType.lines());
    }
}