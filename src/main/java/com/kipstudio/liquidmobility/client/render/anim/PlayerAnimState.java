// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render.anim;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.client.render.FrameDeltaTracker;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class PlayerAnimState {
    private static final float FLIP_DURATION_SECONDS = 0.42F;
    private static final float SKILL_ROLL_DURATION_SECONDS = 0.36F;

    private long lastFrameId = -1L;

    private float slideWeight;
    private float diveWeight;
    private float dashWeight;
    private float wallLeftWeight;
    private float wallRightWeight;
    private float wallClimbWeight;
    private float wallClingWeight;
    private float airborneWeight;
    private float flipWeight;
    private float flipProgress;
    private float flipRemainingSeconds;
    private float skillRollWeight;
    private float skillRollProgress;
    private float skillRollRemainingSeconds;

    private float landingSquash;
    private float landingSquashVelocity;

    private float stridePhase;
    private float dashMotionX;
    private float dashMotionZ;

    private boolean initialized;
    private boolean prevOnGround = true;
    private boolean prevDoubleJumped;
    private boolean prevDashing;
    private double prevVelY;
    private double prevPosY;
    private float peakAirFallDistance;

    public void updateOncePerFrame(long frameId, float dt, PlayerEntity player, IMobilityData cap) {
        if (lastFrameId == frameId) {
            return;
        }
        lastFrameId = frameId;

        boolean onGround = player.isOnGround();
        Vector3d motion = player.getDeltaMovement();
        double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        if (!initialized) {
            initialized = true;
            prevOnGround = onGround;
            prevDoubleJumped = cap.hasDoubleJumped();
            prevDashing = cap.getDashTicks() > 0;
            prevVelY = motion.y;
            prevPosY = player.getY();
            return;
        }

        double deltaY = player.getY() - prevPosY;
        double effectiveVelY = (Math.abs(deltaY) < 8.0D) ? Math.min(motion.y, deltaY / Math.max(dt * 20.0D, 0.05D)) : motion.y;
        prevPosY = player.getY();

        if (!onGround && player.fallDistance > peakAirFallDistance) {
            peakAirFallDistance = player.fallDistance;
        }

        stridePhase += dt * (float) (4.5D + horizSpeed * 22.0D);
        if (stridePhase > (float) (Math.PI * 200.0D)) {
            stridePhase -= (float) (Math.PI * 200.0D);
        }

        boolean isLocalPlayer = player == Minecraft.getInstance().player;
        if (isLocalPlayer && CameraEffectsState.isSkillRolling()) {
            skillRollRemainingSeconds = (1.0F - CameraEffectsState.getSkillRollProgress()) * SKILL_ROLL_DURATION_SECONDS;
        } else if (!prevOnGround && onGround && cap.getSlideTicks() > 0 && prevVelY < -0.45D) {
            skillRollRemainingSeconds = SKILL_ROLL_DURATION_SECONDS;
        } else if (skillRollRemainingSeconds > 0.0F) {
            skillRollRemainingSeconds = Math.max(0.0F, skillRollRemainingSeconds - dt);
        }

        boolean rolling = skillRollRemainingSeconds > 0.0F;
        skillRollProgress = rolling ? 1.0F - (skillRollRemainingSeconds / SKILL_ROLL_DURATION_SECONDS) : 0.0F;
        skillRollWeight = blendDirectional(skillRollWeight, rolling ? 1.0F : 0.0F, 30.0F, 18.0F, dt);

        boolean isSliding = cap.getSlideTicks() > 0 && !rolling;
        boolean isDiving = !onGround && motion.y < -1.0D && cap.getWallDirection() == WallDirection.NONE;
        boolean isDashing = cap.getDashTicks() > 0;
        boolean isWallRunning = !onGround && cap.getWallRunTicks() > 0;
        WallDirection wallDir = cap.getWallDirection();
        boolean isWallClinging = !onGround && cap.getWallRunTicks() == 0 && wallDir != WallDirection.NONE;
        boolean isAirborne = !onGround
                && !player.isFallFlying()
                && !player.isInWater()
                && !isWallRunning
                && !isWallClinging
                && !isDiving
                && !isSliding;

        slideWeight = blendDirectional(slideWeight, isSliding ? 1.0F : 0.0F, 24.0F, 14.0F, dt);
        diveWeight = blendDirectional(diveWeight, isDiving ? 1.0F : 0.0F, 13.0F, 18.0F, dt);
        dashWeight = blendDirectional(dashWeight, isDashing ? 1.0F : 0.0F, 30.0F, 12.0F, dt);
        wallLeftWeight = blendDirectional(wallLeftWeight, (isWallRunning && wallDir == WallDirection.LEFT) ? 1.0F : 0.0F, 18.0F, 14.0F, dt);
        wallRightWeight = blendDirectional(wallRightWeight, (isWallRunning && wallDir == WallDirection.RIGHT) ? 1.0F : 0.0F, 18.0F, 14.0F, dt);
        wallClimbWeight = blendDirectional(wallClimbWeight, (isWallRunning && wallDir == WallDirection.FRONT) ? 1.0F : 0.0F, 18.0F, 14.0F, dt);
        wallClingWeight = blendDirectional(wallClingWeight, isWallClinging ? 1.0F : 0.0F, 18.0F, 14.0F, dt);
        airborneWeight = blendDirectional(airborneWeight, isAirborne ? 1.0F : 0.0F, 8.0F, 18.0F, dt);

        if (isDashing && !prevDashing) {
            if (horizSpeed > 0.05D) {
                dashMotionX = (float) (motion.x / horizSpeed);
                dashMotionZ = (float) (motion.z / horizSpeed);
            } else {
                float yawRad = player.yRot * ((float) Math.PI / 180.0F);
                dashMotionX = (float) -Math.sin(yawRad);
                dashMotionZ = (float) Math.cos(yawRad);
            }
        }
        prevDashing = isDashing;

        if (MobilityConfig.CLIENT.enableDoubleJumpFlip.get()
                && !prevDoubleJumped
                && cap.hasDoubleJumped()
                && !onGround) {
            flipRemainingSeconds = FLIP_DURATION_SECONDS;
        }
        prevDoubleJumped = cap.hasDoubleJumped();

        if (onGround) {
            flipRemainingSeconds = 0.0F;
        } else if (flipRemainingSeconds > 0.0F) {
            flipRemainingSeconds = Math.max(0.0F, flipRemainingSeconds - dt);
        }

        boolean flipping = flipRemainingSeconds > 0.0F;
        flipProgress = flipping ? 1.0F - (flipRemainingSeconds / FLIP_DURATION_SECONDS) : 0.0F;
        flipWeight = blendDirectional(flipWeight, flipping ? 1.0F : 0.0F, 28.0F, 16.0F, dt);

        if (MobilityConfig.CLIENT.enableLandingSquash.get() && !rolling) {
            if (!prevOnGround && onGround && !player.isInWater()) {
                float fallImpact = Math.max(peakAirFallDistance * 0.22F, (float) Math.max(0.0D, -prevVelY * 0.85D));
                if (fallImpact > 0.25F) {
                    landingSquashVelocity += MathHelper.clamp(fallImpact * 7.5F, 2.0F, 11.0F);
                }
                peakAirFallDistance = 0.0F;
            } else if (onGround) {
                peakAirFallDistance = 0.0F;
            }
            float stiffness = 220.0F;
            float damping = 20.0F;
            float accel = -stiffness * landingSquash - damping * landingSquashVelocity;
            landingSquashVelocity += accel * dt;
            landingSquash += landingSquashVelocity * dt;
            if (Math.abs(landingSquash) < 0.002F && Math.abs(landingSquashVelocity) < 0.01F) {
                landingSquash = 0.0F;
                landingSquashVelocity = 0.0F;
            }
        } else {
            if (onGround) {
                peakAirFallDistance = 0.0F;
            }
            if (!MobilityConfig.CLIENT.enableLandingSquash.get()) {
                landingSquash = 0.0F;
                landingSquashVelocity = 0.0F;
            }
        }

        prevOnGround = onGround;
        prevVelY = effectiveVelY;
    }

    private static float blendDirectional(float current, float target, float attackSpeed, float releaseSpeed, float dt) {
        float speed = target > current ? attackSpeed : releaseSpeed;
        float result = FrameDeltaTracker.expLerp(current, target, speed, dt);
        return MathHelper.clamp(result, 0.0F, 1.0F);
    }

    public float getSlideWeight() {
        return slideWeight;
    }

    public float getDiveWeight() {
        return diveWeight;
    }

    public float getDashWeight() {
        return dashWeight;
    }

    public float getWallLeftWeight() {
        return wallLeftWeight;
    }

    public float getWallRightWeight() {
        return wallRightWeight;
    }

    public float getWallClimbWeight() {
        return wallClimbWeight;
    }

    public float getWallClingWeight() {
        return wallClingWeight;
    }

    public float getAirborneWeight() {
        return airborneWeight;
    }

    public float getFlipWeight() {
        return flipWeight;
    }

    public float getFlipProgress() {
        return flipProgress;
    }

    public float getSkillRollWeight() {
        return skillRollWeight;
    }

    public float getSkillRollProgress() {
        return skillRollProgress;
    }

    public float getLandingSquash() {
        return MathHelper.clamp(landingSquash, -0.25F, 1.0F);
    }

    public float getStridePhase() {
        return stridePhase;
    }

    public float getDashMotionX() {
        return dashMotionX;
    }

    public float getDashMotionZ() {
        return dashMotionZ;
    }
}