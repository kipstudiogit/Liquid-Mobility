// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.physics;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

public final class FlowMomentumController {
    private static final double BASE_SPRINT_SPEED = 0.28D;

    private FlowMomentumController() {
    }

    public static void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx) {
        if (!MobilityConfig.COMMON.enableFlowMomentum.get()) {
            ctx.setFlowLevel(0.0F);
            CameraEffectsState.setFlowLevel(0.0F);
            return;
        }

        if (ctx.getHardLandStaggerTicks() > 0) {
            ctx.setHardLandStaggerTicks(ctx.getHardLandStaggerTicks() - 1);
            Vector3d motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * 0.55D, motion.y, motion.z * 0.55D);
            ctx.setFlowLevel(0.0F);
            CameraEffectsState.setFlowLevel(0.0F);
            return;
        }

        Vector3d motion = player.getDeltaMovement();
        double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        float buildRate = MobilityConfig.COMMON.flowBuildRatePerTick.get().floatValue();
        float decayRate = MobilityConfig.COMMON.flowDecayRatePerTick.get().floatValue();

        boolean activeWall = data.getWallRunTicks() > 0 && data.getWallDirection() != WallDirection.NONE;
        boolean activeSlide = data.getSlideTicks() > 0;
        boolean sprintingForward = input.hasSprintIntent() && input.getForwardAxis() > 0.0F;

        if (player.horizontalCollision && !activeWall && !activeSlide) {
            ctx.addFlow(-decayRate * 4.0F);
        } else if (activeWall || activeSlide) {
            ctx.addFlow(buildRate * 1.4F);
        } else if (sprintingForward && horizSpeed > 0.18D) {
            float targetCap = player.isOnGround() ? 0.65F : 0.85F;
            if (ctx.getFlowLevel() < targetCap) {
                ctx.setFlowLevel(Math.min(targetCap, ctx.getFlowLevel() + buildRate));
            }
        } else if (horizSpeed < 0.11D) {
            ctx.addFlow(-decayRate * 2.0F);
        } else {
            ctx.addFlow(-decayRate * 0.65F);
        }

        if (player.isOnGround()
                && sprintingForward
                && !activeSlide
                && !player.isCrouching()
                && ctx.getFlowLevel() > 0.05F
                && horizSpeed > 0.14D) {
            double maxBonus = MobilityConfig.COMMON.flowMaxSpeedBonus.get();
            double targetSpeed = BASE_SPRINT_SPEED * (1.0D + ctx.getFlowLevel() * maxBonus);
            if (horizSpeed < targetSpeed) {
                double nextSpeed = Math.min(targetSpeed, horizSpeed + 0.018D);
                double scale = nextSpeed / horizSpeed;
                player.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
            }
        }

        CameraEffectsState.setFlowLevel(ctx.getFlowLevel());
    }
}