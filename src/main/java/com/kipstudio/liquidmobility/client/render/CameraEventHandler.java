// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CameraEventHandler {
    private static final FrameDeltaTracker CAMERA_DELTA = new FrameDeltaTracker();
    private static final FrameDeltaTracker FOV_DELTA = new FrameDeltaTracker();

    private static float currentRoll;
    private static float currentPitchOffset;
    private static float currentFovOffset;

    private static float landingDip;
    private static float landingDipVelocity;
    private static boolean wasOnGround = true;
    private static float peakAirFallDistance;

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (mc.isPaused() || player == null) {
            CAMERA_DELTA.reset();
            return;
        }

        float dt = CAMERA_DELTA.sampleDeltaSeconds();
        double timeSec = System.nanoTime() * 1.0E-9D;

        float targetRoll = 0.0F;
        if (MobilityConfig.CLIENT.enableCameraRoll.get()) {
            WallDirection wall = CameraEffectsState.getActiveWallDirection();
            if (wall == WallDirection.LEFT) {
                targetRoll = -MobilityConfig.CLIENT.wallRunCameraRoll.get().floatValue();
            } else if (wall == WallDirection.RIGHT) {
                targetRoll = MobilityConfig.CLIENT.wallRunCameraRoll.get().floatValue();
            } else if (wall == WallDirection.FRONT) {
                targetRoll = (float) Math.sin(timeSec * 14.0D) * 2.4F;
            } else if (CameraEffectsState.isSliding()) {
                targetRoll = -4.2F;
            } else if (!player.isOnGround() && !CommonMovementHandler.isMobilityBlocked(player)) {
                float strafe = mc.options.keyLeft.isDown() ? 1.0F : (mc.options.keyRight.isDown() ? -1.0F : 0.0F);
                targetRoll = strafe * -MobilityConfig.CLIENT.airStrafeCameraRoll.get().floatValue();
            }

            float dashImpulse = CameraEffectsState.getDashImpulse();
            if (dashImpulse > 0.01F) {
                targetRoll += CameraEffectsState.getDashStrafeSign() * -6.5F * dashImpulse;
            }
        }
        currentRoll = FrameDeltaTracker.expLerp(currentRoll, targetRoll, 11.0F, dt);
        if (Math.abs(currentRoll) > 0.05F) {
            event.setRoll(event.getRoll() + currentRoll);
        }

        float targetPitchOffset = 0.0F;
        if (MobilityConfig.CLIENT.enableCameraPitch.get()) {
            if (CameraEffectsState.getActiveWallDirection() == WallDirection.FRONT) {
                targetPitchOffset = -15.0F + (float) Math.cos(timeSec * 14.0D) * 1.8F;
            } else if (CameraEffectsState.isFastDropping()) {
                targetPitchOffset = 25.0F;
            } else if (CameraEffectsState.isSliding()) {
                targetPitchOffset = 7.0F;
            }

            if (MobilityConfig.CLIENT.enableDoubleJumpCameraKick.get()) {
                float dj = CameraEffectsState.getDoubleJumpImpulse();
                if (dj > 0.01F) {
                    targetPitchOffset -= MathHelper.sin(dj * (float) Math.PI) * 10.5F;
                }
            }

            float vault = CameraEffectsState.getVaultImpulse();
            if (vault > 0.01F) {
                targetPitchOffset += MathHelper.sin(vault * (float) Math.PI) * 6.0F;
            }

            float dashImpulse = CameraEffectsState.getDashImpulse();
            if (dashImpulse > 0.01F) {
                targetPitchOffset += CameraEffectsState.getDashForwardSign() * 4.5F * dashImpulse;
            }

            float hardLand = CameraEffectsState.getHardLandImpulse();
            if (hardLand > 0.01F) {
                targetPitchOffset += hardLand * 16.0F;
            }
        }
        currentPitchOffset = FrameDeltaTracker.expLerp(currentPitchOffset, targetPitchOffset, 14.0F, dt);

        boolean isOnGround = player.isOnGround();
        if (!isOnGround) {
            if (player.fallDistance > peakAirFallDistance) {
                peakAirFallDistance = player.fallDistance;
            }
        }

        float impactFall = CameraEffectsState.consumeCameraLandingDip();
        if (MobilityConfig.CLIENT.enableLandingDip.get() && !CommonMovementHandler.isMobilityBlocked(player)) {
            if (isOnGround && !wasOnGround) {
                float effectiveFall = Math.max(impactFall, peakAirFallDistance);
                if (effectiveFall > 0.5F) {
                    landingDipVelocity += Math.min(effectiveFall * 60.0F, 360.0F);
                }
                peakAirFallDistance = 0.0F;
            } else if (isOnGround) {
                peakAirFallDistance = 0.0F;
            }
            float stiffness = 240.0F;
            float damping = 22.0F;
            float accel = -stiffness * landingDip - damping * landingDipVelocity;
            landingDipVelocity += accel * dt;
            landingDip += landingDipVelocity * dt;
        } else {
            landingDip = 0.0F;
            landingDipVelocity = 0.0F;
            if (isOnGround) {
                peakAirFallDistance = 0.0F;
            }
        }
        wasOnGround = isOnGround;

        float finalPitch = currentPitchOffset + landingDip;

        if (MobilityConfig.CLIENT.enableSkillRollCameraFlip.get() && CameraEffectsState.isSkillRolling()) {
            float p = CameraEffectsState.getSkillRollProgress();
            float smoothP = p * p * (3.0F - 2.0F * p);
            finalPitch += 360.0F * smoothP;
        }

        if (MobilityConfig.CLIENT.enableCameraShake.get()) {
            float shake = CameraEffectsState.getJumpShake();
            if (CameraEffectsState.isFastDropping()) {
                shake = Math.max(shake, 0.55F);
            }
            if (shake > 0.0F) {
                finalPitch += (float) (Math.sin(timeSec * 65.0D) * shake);
                event.setYaw(event.getYaw() + (float) (Math.cos(timeSec * 50.0D) * shake * 0.5F));
            }
        }

        if (Math.abs(finalPitch) > 0.05F) {
            event.setPitch(event.getPitch() + finalPitch);
        }

        CameraEffectsState.tickImpulses(dt);
    }

    @SubscribeEvent
    public static void onFOVUpdate(EntityViewRenderEvent.FOVModifier event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null
                || mc.isPaused()
                || CommonMovementHandler.isMobilityBlocked(player)
                || !MobilityConfig.CLIENT.enableDynamicFov.get()) {
            FOV_DELTA.reset();
            return;
        }

        float dt = FOV_DELTA.sampleDeltaSeconds();
        double horizSpeed = Math.sqrt(
                player.getDeltaMovement().x * player.getDeltaMovement().x
                        + player.getDeltaMovement().z * player.getDeltaMovement().z
        );

        float targetFovOffset = 0.0F;
        float maxOffset = MobilityConfig.CLIENT.maxFovOffset.get().floatValue();
        if (horizSpeed > 0.22D) {
            float speedFactor = (float) (horizSpeed - 0.22D);
            targetFovOffset = speedFactor * 50.0F + (speedFactor * speedFactor * 100.0F);
        }

        targetFovOffset += CameraEffectsState.getFlowLevel() * 4.5F;
        targetFovOffset += CameraEffectsState.getDashImpulse() * 8.0F;
        targetFovOffset += CameraEffectsState.getDoubleJumpImpulse() * 3.5F;
        targetFovOffset = MathHelper.clamp(targetFovOffset, 0.0F, maxOffset);

        currentFovOffset = FrameDeltaTracker.expLerp(currentFovOffset, targetFovOffset, 10.0F, dt);
        event.setFOV(event.getFOV() + currentFovOffset);
    }
}