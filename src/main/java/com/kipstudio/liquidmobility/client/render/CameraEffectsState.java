// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.util.math.MathHelper;

public final class CameraEffectsState {
    private static final float SKILL_ROLL_DURATION = 0.36F;

    private static WallDirection activeWallDirection = WallDirection.NONE;
    private static boolean sliding;
    private static boolean fastDropping;
    private static float jumpShake;
    private static float dashImpulse;
    private static float dashStrafeSign;
    private static float dashForwardSign;
    private static float doubleJumpImpulse;
    private static float vaultImpulse;
    private static float skillRollRemaining;
    private static float hardLandImpulse;
    private static float flowLevel;
    private static boolean routeVisionActive = true;
    private static float pendingFallDistance;
    private static float pendingCameraLandingDip;
    private static float pendingHandLandingDip;

    private CameraEffectsState() {
    }

    public static WallDirection getActiveWallDirection() {
        return activeWallDirection;
    }

    public static void setActiveWallDirection(WallDirection direction) {
        activeWallDirection = direction != null ? direction : WallDirection.NONE;
    }

    public static boolean isSliding() {
        return sliding;
    }

    public static void setSliding(boolean value) {
        sliding = value;
    }

    public static boolean isFastDropping() {
        return fastDropping;
    }

    public static void setFastDropping(boolean value) {
        fastDropping = value;
    }

    public static float getJumpShake() {
        return jumpShake;
    }

    public static void addShake(float amount) {
        jumpShake = Math.min(12.0F, Math.max(jumpShake, amount));
    }

    public static void triggerDash(float forward, float strafe) {
        dashImpulse = 1.0F;
        dashForwardSign = forward;
        dashStrafeSign = strafe;
    }

    public static void triggerDoubleJump() {
        doubleJumpImpulse = 1.0F;
    }

    public static void triggerVault() {
        vaultImpulse = 1.0F;
    }

    public static void triggerSkillRoll() {
        skillRollRemaining = SKILL_ROLL_DURATION;
    }

    public static void triggerHardLanding() {
        hardLandImpulse = 1.0F;
        addShake(7.5F);
    }

    public static void recordFallImpact(float fallDistance) {
        if (fallDistance > pendingFallDistance) {
            pendingFallDistance = fallDistance;
        }
        if (fallDistance > pendingCameraLandingDip) {
            pendingCameraLandingDip = fallDistance;
        }
        if (fallDistance > pendingHandLandingDip) {
            pendingHandLandingDip = fallDistance;
        }
    }

    public static float consumePendingFallDistance() {
        float value = pendingFallDistance;
        pendingFallDistance = 0.0F;
        return value;
    }

    public static float consumeCameraLandingDip() {
        float value = pendingCameraLandingDip;
        pendingCameraLandingDip = 0.0F;
        return value;
    }

    public static float consumeHandLandingDip() {
        float value = pendingHandLandingDip;
        pendingHandLandingDip = 0.0F;
        return value;
    }

    public static float getDashImpulse() {
        return dashImpulse;
    }

    public static float getDashStrafeSign() {
        return dashStrafeSign;
    }

    public static float getDashForwardSign() {
        return dashForwardSign;
    }

    public static float getDoubleJumpImpulse() {
        return doubleJumpImpulse;
    }

    public static float getVaultImpulse() {
        return vaultImpulse;
    }

    public static float getSkillRollProgress() {
        if (skillRollRemaining <= 0.0F) {
            return 0.0F;
        }
        return MathHelper.clamp(1.0F - (skillRollRemaining / SKILL_ROLL_DURATION), 0.0F, 1.0F);
    }

    public static boolean isSkillRolling() {
        return skillRollRemaining > 0.0F;
    }

    public static float getHardLandImpulse() {
        return hardLandImpulse;
    }

    public static float getFlowLevel() {
        return flowLevel;
    }

    public static void setFlowLevel(float value) {
        flowLevel = MathHelper.clamp(value, 0.0F, 1.0F);
    }

    public static boolean isRouteVisionActive() {
        return routeVisionActive;
    }

    public static void setRouteVisionActive(boolean active) {
        routeVisionActive = active;
    }

    public static void tickImpulses(float dt) {
        if (jumpShake > 0.0F) {
            jumpShake *= (float) Math.exp(-9.0F * dt);
            if (jumpShake < 0.05F) {
                jumpShake = 0.0F;
            }
        }
        if (dashImpulse > 0.0F) {
            dashImpulse *= (float) Math.exp(-7.5F * dt);
            if (dashImpulse < 0.01F) {
                dashImpulse = 0.0F;
            }
        }
        if (doubleJumpImpulse > 0.0F) {
            doubleJumpImpulse *= (float) Math.exp(-6.5F * dt);
            if (doubleJumpImpulse < 0.01F) {
                doubleJumpImpulse = 0.0F;
            }
        }
        if (vaultImpulse > 0.0F) {
            vaultImpulse *= (float) Math.exp(-8.0F * dt);
            if (vaultImpulse < 0.01F) {
                vaultImpulse = 0.0F;
            }
        }
        if (skillRollRemaining > 0.0F) {
            skillRollRemaining = Math.max(0.0F, skillRollRemaining - dt);
        }
        if (hardLandImpulse > 0.0F) {
            hardLandImpulse *= (float) Math.exp(-6.0F * dt);
            if (hardLandImpulse < 0.01F) {
                hardLandImpulse = 0.0F;
            }
        }
    }

    public static void reset() {
        activeWallDirection = WallDirection.NONE;
        sliding = false;
        fastDropping = false;
        jumpShake = 0.0F;
        dashImpulse = 0.0F;
        dashStrafeSign = 0.0F;
        dashForwardSign = 0.0F;
        doubleJumpImpulse = 0.0F;
        vaultImpulse = 0.0F;
        skillRollRemaining = 0.0F;
        hardLandImpulse = 0.0F;
        flowLevel = 0.0F;
        pendingFallDistance = 0.0F;
        pendingCameraLandingDip = 0.0F;
        pendingHandLandingDip = 0.0F;
    }
}