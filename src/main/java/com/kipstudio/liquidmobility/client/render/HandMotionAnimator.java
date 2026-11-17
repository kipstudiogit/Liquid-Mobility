// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

@SuppressWarnings("null")
public class HandMotionAnimator {
    private final FrameDeltaTracker frameDelta = new FrameDeltaTracker();

    private float slideWeight;
    private float diveWeight;
    private float wallLeftWeight;
    private float wallRightWeight;
    private float wallClimbWeight;
    private float dashWeight;

    private float prevYaw;
    private float prevPitch;
    private boolean hasPrevLook;
    private float swayX;
    private float swayY;

    private float velocityInertiaY;
    private float jumpDip;
    private float jumpDipVelocity;
    private boolean wasOnGround = true;
    private float peakAirFallDistance;

    private float sprintStridePhase;
    private float sprintSwingWeight;
    private float climbPhase;
    private float breathPhase;

    public void reset() {
        frameDelta.reset();
        hasPrevLook = false;
        peakAirFallDistance = 0.0F;
    }

    public void updateFrame(PlayerEntity player) {
        float dt = frameDelta.sampleDeltaSeconds();

        if (!hasPrevLook) {
            prevYaw = player.yRot;
            prevPitch = player.xRot;
            hasPrevLook = true;
        }

        float yawDiff = MathHelper.clamp(player.yRot - prevYaw, -15.0F, 15.0F);
        float pitchDiff = MathHelper.clamp(player.xRot - prevPitch, -15.0F, 15.0F);
        prevYaw = player.yRot;
        prevPitch = player.xRot;

        swayX = FrameDeltaTracker.expLerp(swayX, -yawDiff * 0.014F, 22.0F, dt);
        swayY = FrameDeltaTracker.expLerp(swayY, -pitchDiff * 0.014F, 22.0F, dt);

        boolean isOnGround = player.isOnGround();
        Vector3d motion = player.getDeltaMovement();
        double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        if (!isOnGround && player.fallDistance > peakAirFallDistance) {
            peakAirFallDistance = player.fallDistance;
        }

        float impactFall = CameraEffectsState.consumeHandLandingDip();
        float targetVelY = !isOnGround ? (float) MathHelper.clamp(-motion.y * 0.22D, -0.28D, 0.36D) : 0.0F;
        if (isOnGround && !wasOnGround) {
            float effectiveFall = Math.max(impactFall, peakAirFallDistance);
            if (effectiveFall > 0.5F) {
                jumpDipVelocity += Math.min(effectiveFall * 2.2F, 8.5F);
            }
            peakAirFallDistance = 0.0F;
        } else if (isOnGround) {
            peakAirFallDistance = 0.0F;
        }
        wasOnGround = isOnGround;

        float dipAccel = -210.0F * jumpDip - 19.0F * jumpDipVelocity;
        jumpDipVelocity += dipAccel * dt;
        jumpDip += jumpDipVelocity * dt;
        jumpDip = MathHelper.clamp(jumpDip, -0.15F, 0.4F);

        velocityInertiaY = FrameDeltaTracker.expLerp(velocityInertiaY, targetVelY, 11.0F, dt);

        breathPhase += dt * 2.2F;
        if (breathPhase > (float) (Math.PI * 200.0D)) {
            breathPhase -= (float) (Math.PI * 200.0D);
        }

        boolean activeSprint = isOnGround && horizSpeed > 0.18D && !CameraEffectsState.isSliding();
        sprintSwingWeight = FrameDeltaTracker.expLerp(sprintSwingWeight, activeSprint ? 1.0F : 0.0F, 12.0F, dt);
        if (activeSprint) {
            sprintStridePhase += dt * (float) (6.0D + horizSpeed * 22.0D);
        }

        IMobilityData cap = MobilityCapability.get(player);
        if (cap != null) {
            boolean wallRunning = cap.getWallRunTicks() > 0;
            WallDirection wallDir = cap.getWallDirection();

            slideWeight = FrameDeltaTracker.expLerp(slideWeight, cap.getSlideTicks() > 0 ? 1.0F : 0.0F, 18.0F, dt);
            diveWeight = FrameDeltaTracker.expLerp(diveWeight, CameraEffectsState.isFastDropping() ? 1.0F : 0.0F, 13.0F, dt);
            dashWeight = FrameDeltaTracker.expLerp(dashWeight, cap.getDashTicks() > 0 ? 1.0F : 0.0F, 26.0F, dt);
            wallLeftWeight = FrameDeltaTracker.expLerp(wallLeftWeight, (wallRunning && wallDir == WallDirection.LEFT) ? 1.0F : 0.0F, 14.0F, dt);
            wallRightWeight = FrameDeltaTracker.expLerp(wallRightWeight, (wallRunning && wallDir == WallDirection.RIGHT) ? 1.0F : 0.0F, 14.0F, dt);
            wallClimbWeight = FrameDeltaTracker.expLerp(wallClimbWeight, (wallRunning && wallDir == WallDirection.FRONT) ? 1.0F : 0.0F, 16.0F, dt);

            if (wallRunning && wallDir == WallDirection.FRONT) {
                climbPhase += dt * 13.5F;
            }
        }
    }

    public void applyToHand(MatrixStack matrix, Hand hand) {
        float handSign = (hand == Hand.MAIN_HAND) ? 1.0F : -1.0F;

        if (MobilityConfig.CLIENT.enableHandSway.get()) {
            matrix.translate(swayX, swayY, 0.0D);
            matrix.mulPose(Vector3f.YP.rotationDegrees(swayX * 48.0F));
            matrix.mulPose(Vector3f.XP.rotationDegrees(swayY * 48.0F));
        }

        float breathY = MathHelper.sin(breathPhase) * 0.006F;
        matrix.translate(0.0D, velocityInertiaY - jumpDip + breathY, 0.0D);

        if (MobilityConfig.CLIENT.enableFirstPersonParkourSwing.get()) {
            if (sprintSwingWeight > 0.01F) {
                float swingX = MathHelper.sin(sprintStridePhase) * 0.032F * handSign * sprintSwingWeight;
                float swingY = Math.abs(MathHelper.cos(sprintStridePhase)) * 0.024F * sprintSwingWeight;
                float swingPitch = MathHelper.cos(sprintStridePhase) * 7.0F * handSign * sprintSwingWeight;
                matrix.translate(swingX, swingY, 0.0D);
                matrix.mulPose(Vector3f.XP.rotationDegrees(swingPitch));
            }

            float dj = CameraEffectsState.getDoubleJumpImpulse();
            if (dj > 0.01F) {
                float arc = MathHelper.sin(dj * (float) Math.PI);
                matrix.translate(0.0D, 0.08D * arc, -0.05D * arc);
                matrix.mulPose(Vector3f.XP.rotationDegrees(-16.0F * arc));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(handSign * 6.0F * arc));
            }

            float vault = CameraEffectsState.getVaultImpulse();
            if (vault > 0.01F) {
                float push = MathHelper.sin(vault * (float) Math.PI);
                matrix.translate(0.0D, -0.12D * push, -0.1D * push);
                matrix.mulPose(Vector3f.XP.rotationDegrees(18.0F * push));
            }
        }

        float dashImpulse = Math.max(dashWeight, CameraEffectsState.getDashImpulse());
        if (dashImpulse > 0.01F) {
            float strafeRoll = CameraEffectsState.getDashStrafeSign() * -10.0F * dashImpulse;
            matrix.translate(0.0D, -dashImpulse * 0.09D, dashImpulse * 0.28D);
            matrix.mulPose(Vector3f.XP.rotationDegrees(dashImpulse * 22.0F));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(strafeRoll));
        }

        if (slideWeight > 0.01F) {
            float scrape = MathHelper.sin(breathPhase * 12.0F) * 0.004F * slideWeight;
            if (hand == Hand.OFF_HAND) {
                matrix.translate(-0.14D * slideWeight, -0.26D * slideWeight + scrape, 0.08D * slideWeight);
                matrix.mulPose(Vector3f.XP.rotationDegrees(18.0F * slideWeight));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(18.0F * slideWeight));
            } else {
                matrix.translate(0.08D * slideWeight, -0.16D * slideWeight + scrape, 0.14D * slideWeight);
                matrix.mulPose(Vector3f.XP.rotationDegrees(12.0F * slideWeight));
                matrix.mulPose(Vector3f.ZP.rotationDegrees(-12.0F * slideWeight));
            }
        }

        if (diveWeight > 0.01F) {
            float windJitter = MathHelper.sin(breathPhase * 18.0F) * 0.006F * diveWeight;
            matrix.translate(windJitter * handSign, 0.18D * diveWeight, -0.16D * diveWeight);
            matrix.mulPose(Vector3f.XP.rotationDegrees(-36.0F * diveWeight));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(handSign * -8.0F * diveWeight));
        }

        if (wallLeftWeight > 0.01F) {
            applyWallHandPose(matrix, hand == Hand.OFF_HAND, wallLeftWeight, -1.0F);
        }

        if (wallRightWeight > 0.01F) {
            applyWallHandPose(matrix, hand == Hand.MAIN_HAND, wallRightWeight, 1.0F);
        }

        if (wallClimbWeight > 0.01F) {
            float phase = climbPhase + (hand == Hand.MAIN_HAND ? 0.0F : (float) Math.PI);
            float reachY = MathHelper.sin(phase) * 0.16F * wallClimbWeight;
            float reachZ = -Math.max(0.0F, MathHelper.cos(phase)) * 0.18F * wallClimbWeight;
            float reachPitch = (-22.0F - MathHelper.sin(phase) * 24.0F) * wallClimbWeight;
            matrix.translate(handSign * -0.04D * wallClimbWeight, reachY, reachZ);
            matrix.mulPose(Vector3f.XP.rotationDegrees(reachPitch));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(handSign * 10.0F * wallClimbWeight));
        }
    }

    private void applyWallHandPose(MatrixStack matrix, boolean isWallSideHand, float weight, float wallSideSign) {
        if (isWallSideHand) {
            matrix.translate(wallSideSign * 0.14D * weight, 0.08D * weight, -0.12D * weight);
            matrix.mulPose(Vector3f.XP.rotationDegrees(-18.0F * weight));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(wallSideSign * -28.0F * weight));
        } else {
            float pump = MathHelper.sin(sprintStridePhase) * 8.0F * weight;
            matrix.translate(wallSideSign * 0.06D * weight, -0.06D * weight, 0.06D * weight);
            matrix.mulPose(Vector3f.XP.rotationDegrees(pump));
            matrix.mulPose(Vector3f.ZP.rotationDegrees(wallSideSign * 10.0F * weight));
        }
    }
}