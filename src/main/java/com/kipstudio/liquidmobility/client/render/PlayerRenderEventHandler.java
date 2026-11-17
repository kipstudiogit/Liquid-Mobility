// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.client.render.anim.PlayerAnimState;
import com.kipstudio.liquidmobility.client.render.anim.PlayerAnimStateRegistry;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Deque;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerRenderEventHandler {
    private static final Deque<Boolean> PUSH_STACK = new ArrayDeque<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.isCanceled()) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.screen != null) {
            PUSH_STACK.push(Boolean.FALSE);
            return;
        }

        PlayerAnimState state = PlayerAnimStateRegistry.getUpdatedState(player);
        IMobilityData cap = MobilityCapability.get(player);
        if (state == null || cap == null) {
            PUSH_STACK.push(Boolean.FALSE);
            return;
        }

        Vector3d motion = player.getDeltaMovement();
        double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        float slideW = state.getSlideWeight();
        float diveW = state.getDiveWeight();
        float dashW = state.getDashWeight();
        float wallLeftW = state.getWallLeftWeight();
        float wallRightW = state.getWallRightWeight();
        float wallClimbW = state.getWallClimbWeight();
        float wallClingW = state.getWallClingWeight();
        float flipW = state.getFlipWeight();
        float rollW = state.getSkillRollWeight();
        float squash = state.getLandingSquash();

        boolean hasParkourTransform = MobilityConfig.CLIENT.enableParkourPlayerPose.get()
                && (slideW > 0.005F
                || diveW > 0.005F
                || dashW > 0.005F
                || wallLeftW > 0.005F
                || wallRightW > 0.005F
                || wallClimbW > 0.005F
                || wallClingW > 0.005F
                || flipW > 0.005F
                || rollW > 0.005F
                || Math.abs(squash) > 0.005F);

        boolean hasLean = MobilityConfig.CLIENT.enableBodyLean.get()
                && !player.isFallFlying()
                && speed > 0.12D;

        if (!hasParkourTransform && !hasLean) {
            PUSH_STACK.push(Boolean.FALSE);
            return;
        }

        MatrixStack matrix = event.getMatrixStack();
        matrix.pushPose();
        PUSH_STACK.push(Boolean.TRUE);

        float partial = event.getPartialRenderTick();
        float bodyYaw = MathHelper.lerp(partial, player.yBodyRotO, player.yBodyRot);

        if (MobilityConfig.CLIENT.enableParkourPlayerPose.get() && Math.abs(squash) > 0.005F) {
            float scaleY = MathHelper.clamp(1.0F - squash * 0.18F, 0.78F, 1.06F);
            float scaleXZ = 1.0F + (1.0F - scaleY) * 0.45F;
            matrix.scale(scaleXZ, scaleY, scaleXZ);
        }

        if (MobilityConfig.CLIENT.enableParkourPlayerPose.get() && slideW > 0.005F) {
            matrix.translate(0.0D, -0.52D * slideW, 0.0D);
        }

        if (MobilityConfig.CLIENT.enableParkourPlayerPose.get() && rollW > 0.005F) {
            matrix.translate(0.0D, -0.35D * rollW, 0.0D);
        }

        float pivotY = 0.85F;
        matrix.translate(0.0D, pivotY, 0.0D);
        matrix.mulPose(Vector3f.YP.rotationDegrees(-bodyYaw));

        float wallSuppression = 1.0F - MathHelper.clamp(wallLeftW + wallRightW + wallClimbW + wallClingW, 0.0F, 1.0F);
        float leanSuppression = (1.0F - slideW) * (1.0F - diveW) * wallSuppression * (1.0F - flipW) * (1.0F - rollW);

        if (hasLean && leanSuppression > 0.01F) {
            float movementYaw = (float) (MathHelper.atan2(motion.z, motion.x) * (180.0D / Math.PI)) - 90.0F;
            float yawDiff = MathHelper.wrapDegrees(movementYaw - bodyYaw);
            float leanForward = (float) (Math.cos(yawDiff * Math.PI / 180.0D) * speed * 16.0D) * leanSuppression;
            float leanSide = (float) (Math.sin(yawDiff * Math.PI / 180.0D) * speed * 13.0D) * leanSuppression;

            matrix.mulPose(Vector3f.XP.rotationDegrees(MathHelper.clamp(leanForward, -16.0F, 16.0F)));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.clamp(leanSide, -13.0F, 13.0F)));
        }

        if (MobilityConfig.CLIENT.enableParkourPlayerPose.get()) {
            if (squash > 0.01F) {
                matrix.mulPose(Vector3f.XP.rotationDegrees(14.0F * squash));
            }

            if (flipW > 0.005F) {
                float p = state.getFlipProgress();
                float smoothP = p * p * (3.0F - 2.0F * p);
                matrix.mulPose(Vector3f.XP.rotationDegrees(360.0F * smoothP * flipW));
            }

            if (rollW > 0.005F) {
                float p = state.getSkillRollProgress();
                float smoothP = p * p * (3.0F - 2.0F * p);
                matrix.mulPose(Vector3f.XP.rotationDegrees(360.0F * smoothP * rollW));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(12.0F * rollW));
            }

            if (dashW > 0.005F) {
                float dashYaw = (float) (MathHelper.atan2(state.getDashMotionZ(), state.getDashMotionX()) * (180.0D / Math.PI)) - 90.0F;
                float dashDiff = MathHelper.wrapDegrees(dashYaw - bodyYaw);
                float dashFwd = (float) Math.cos(dashDiff * Math.PI / 180.0D) * 32.0F * dashW;
                float dashSide = (float) Math.sin(dashDiff * Math.PI / 180.0D) * 26.0F * dashW;
                matrix.mulPose(Vector3f.XP.rotationDegrees(dashFwd));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(dashSide));
            }

            if (slideW > 0.005F) {
                matrix.mulPose(Vector3f.XP.rotationDegrees(-52.0F * slideW));
                matrix.mulPose(Vector3f.YP.rotationDegrees(22.0F * slideW));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(-8.0F * slideW));
            }

            if (diveW > 0.005F) {
                float windRoll = (float) Math.sin(System.nanoTime() * 1.0E-9D * 26.0D) * 2.5F;
                matrix.mulPose(Vector3f.XP.rotationDegrees(68.0F * diveW));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(windRoll * diveW));
            }

            if (wallLeftW > 0.005F) {
                matrix.translate(-0.18D * wallLeftW, 0.0D, 0.0D);
                matrix.mulPose(Vector3f.ZP.rotationDegrees(28.0F * wallLeftW));
                matrix.mulPose(Vector3f.XP.rotationDegrees(14.0F * wallLeftW));
                matrix.mulPose(Vector3f.YP.rotationDegrees(-10.0F * wallLeftW));
            }

            if (wallRightW > 0.005F) {
                matrix.translate(0.18D * wallRightW, 0.0D, 0.0D);
                matrix.mulPose(Vector3f.ZP.rotationDegrees(-28.0F * wallRightW));
                matrix.mulPose(Vector3f.XP.rotationDegrees(14.0F * wallRightW));
                matrix.mulPose(Vector3f.YP.rotationDegrees(10.0F * wallRightW));
            }

            if (wallClimbW > 0.005F) {
                float rock = MathHelper.sin(state.getStridePhase() * 0.9F) * 4.5F * wallClimbW;
                matrix.translate(0.0D, 0.0D, -0.18D * wallClimbW);
                matrix.mulPose(Vector3f.XP.rotationDegrees(-12.0F * wallClimbW));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(rock));
            }

            if (wallClingW > 0.005F) {
                WallDirection wallDir = cap.getWallDirection();
                if (wallDir == WallDirection.LEFT) {
                    matrix.translate(-0.22D * wallClingW, 0.0D, 0.0D);
                    matrix.mulPose(Vector3f.ZP.rotationDegrees(12.0F * wallClingW));
                } else if (wallDir == WallDirection.RIGHT) {
                    matrix.translate(0.22D * wallClingW, 0.0D, 0.0D);
                    matrix.mulPose(Vector3f.ZP.rotationDegrees(-12.0F * wallClingW));
                } else if (wallDir == WallDirection.FRONT) {
                    matrix.translate(0.0D, 0.0D, -0.22D * wallClingW);
                    matrix.mulPose(Vector3f.XP.rotationDegrees(-8.0F * wallClingW));
                }
            }
        }

        matrix.mulPose(Vector3f.YP.rotationDegrees(bodyYaw));
        matrix.translate(0.0D, -pivotY, 0.0D);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        if (!PUSH_STACK.isEmpty() && Boolean.TRUE.equals(PUSH_STACK.pop())) {
            event.getMatrixStack().popPose();
        }
    }
}