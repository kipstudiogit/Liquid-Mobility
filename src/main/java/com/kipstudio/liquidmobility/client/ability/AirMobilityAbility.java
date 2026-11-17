// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.ability;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.physics.ClientParkourContext;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.JumpActionType;
import com.kipstudio.liquidmobility.core.WallDirection;
import com.kipstudio.liquidmobility.network.MobilityNetwork;
import com.kipstudio.liquidmobility.network.PacketFastDrop;
import com.kipstudio.liquidmobility.network.PacketGroundPound;
import com.kipstudio.liquidmobility.network.PacketParkourJump;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

@SuppressWarnings("null")
public class AirMobilityAbility implements IParkourAbility {
    private static final Random RANDOM = new Random();

    @Override
    public void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx) {
        boolean isOnGround = player.isOnGround();
        WallDirection wall = data.getWallDirection();

        if (isOnGround) {
            ctx.setTicksSinceOnGround(0);
            ctx.setLastJumpedWall(WallDirection.NONE);

            if (!ctx.wasOnGround()) {
                ctx.setBHopWindow(MobilityConfig.COMMON.bHopWindowTicks.get());
                float impactFallDistance = Math.max(ctx.getLastAirFallDistance(), CameraEffectsState.consumePendingFallDistance());
                ctx.resetAirFallDistance();

                if (CameraEffectsState.isSkillRolling()) {
                    ctx.addFlow(0.24F);
                } else if (CameraEffectsState.getHardLandImpulse() > 0.85F) {
                    ctx.setHardLandStaggerTicks(MobilityConfig.COMMON.hardLandingStaggerTicks.get());
                    ctx.setFlowLevel(0.0F);
                }

                if (ctx.isGroundPoundArmed()
                        && MobilityConfig.COMMON.enableFastDrop.get()
                        && data.getGroundPoundCooldown() == 0
                        && impactFallDistance >= MobilityConfig.COMMON.groundPoundMinFallDistance.get()) {
                    triggerGroundPoundImpact(player, data);
                } else if (impactFallDistance > 1.5F && !CameraEffectsState.isSkillRolling()) {
                    CameraEffectsState.addShake(Math.min(impactFallDistance * 0.5F, 4.0F));
                    player.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 0.5F, 0.7F);
                }

                ctx.setGroundPoundArmed(false);
                data.setFastDropping(false);
            } else {
                CameraEffectsState.consumePendingFallDistance();
                ctx.resetAirFallDistance();
            }
        } else {
            ctx.updateAirFallDistance(player.fallDistance);
            if (player.getDeltaMovement().y > 0.1D) {
                ctx.setTicksSinceOnGround(999);
            } else {
                ctx.setTicksSinceOnGround(ctx.getTicksSinceOnGround() + 1);
            }
            if (ctx.getBHopWindow() > 0) {
                ctx.setBHopWindow(ctx.getBHopWindow() - 1);
            }
        }

        if (MobilityConfig.COMMON.enableBHop.get()
                && ctx.wasOnGround()
                && !isOnGround
                && player.getDeltaMovement().y > 0.25D
                && ctx.getBHopWindow() > 0) {
            applyBHopBoost(player, ctx);
        }

        boolean inFreeAir = !isOnGround && data.getWallRunTicks() == 0 && wall == WallDirection.NONE;
        float yawRad = player.yRot * ((float) Math.PI / 180.0F);
        double fwdX = -Math.sin(yawRad);
        double fwdZ = Math.cos(yawRad);
        double leftX = Math.cos(yawRad);
        double leftZ = Math.sin(yawRad);

        if (inFreeAir) {
            if (MobilityConfig.COMMON.enableAirStrafe.get()
                    && (input.getForwardAxis() != 0.0F || input.getStrafeAxis() != 0.0F)) {
                double accel = MobilityConfig.COMMON.airStrafeAcceleration.get();
                double strafeX = input.getForwardAxis() * fwdX + input.getStrafeAxis() * leftX;
                double strafeZ = input.getForwardAxis() * fwdZ + input.getStrafeAxis() * leftZ;
                Vector3d motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x + strafeX * accel, motion.y, motion.z + strafeZ * accel);
            }

            if (MobilityConfig.COMMON.enableFastDrop.get()
                    && input.isSneakJustPressed()
                    && player.fallDistance < 2.5F
                    && player.getDeltaMovement().y > -0.65D) {
                Vector3d motion = player.getDeltaMovement();
                double dropVel = -MobilityConfig.COMMON.fastDropVerticalVelocity.get();
                player.setDeltaMovement(motion.x, dropVel, motion.z);
                data.setFastDropping(true);
                ctx.setGroundPoundArmed(true);
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);
            }
        }

        if (input.isJumpJustPressed()) {
            if (MobilityConfig.COMMON.enableSlide.get() && data.getSlideTicks() > 0) {
                data.setSlideTicks(0);
                double slideJumpHoriz = MobilityConfig.COMMON.slideJumpHorizontalSpeed.get();
                double slideJumpVert = MobilityConfig.COMMON.slideJumpVerticalBoost.get();
                player.setDeltaMovement(fwdX * slideJumpHoriz, slideJumpVert, fwdZ * slideJumpHoriz);
                player.fallDistance = 0.0F;
                ctx.resetAirFallDistance();
                CameraEffectsState.consumePendingFallDistance();
                ctx.addFlow(0.16F);
                CameraEffectsState.triggerVault();
                player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 0.5F, 1.5F);
                player.level.addParticle(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 0.0D, 0.0D, 0.0D);
                MobilityNetwork.CHANNEL.sendToServer(new PacketParkourJump(JumpActionType.SLIDE_JUMP));
            } else if (inFreeAir) {
                if (player.getDeltaMovement().y < 0.1D
                        && ctx.getTicksSinceOnGround() <= MobilityConfig.COMMON.coyoteTimeTicks.get()) {
                    Vector3d motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.42D, motion.z);
                    player.fallDistance = 0.0F;
                    ctx.setTicksSinceOnGround(999);
                    ctx.resetAirFallDistance();
                    CameraEffectsState.consumePendingFallDistance();
                    ctx.addFlow(0.12F);
                    CameraEffectsState.triggerVault();
                    player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.5F, randomPitch());
                    MobilityNetwork.CHANNEL.sendToServer(new PacketParkourJump(JumpActionType.COYOTE_JUMP));
                } else if (MobilityConfig.COMMON.enableDoubleJump.get() && !data.hasDoubleJumped()) {
                    data.setDoubleJumped(true);
                    data.setFastDropping(false);
                    ctx.setGroundPoundArmed(false);
                    Vector3d motion = player.getDeltaMovement();
                    double horizMult = MobilityConfig.COMMON.doubleJumpHorizontalMultiplier.get();
                    double vertVel = MobilityConfig.COMMON.doubleJumpVerticalVelocity.get();
                    player.setDeltaMovement(motion.x * horizMult, vertVel, motion.z * horizMult);
                    player.fallDistance = 0.0F;
                    ctx.resetAirFallDistance();
                    CameraEffectsState.consumePendingFallDistance();
                    ctx.addFlow(0.12F);
                    CameraEffectsState.triggerDoubleJump();
                    player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, randomPitch());
                    player.level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.1D, player.getZ(), 0.0D, -0.1D, 0.0D);
                    MobilityNetwork.CHANNEL.sendToServer(new PacketParkourJump(JumpActionType.DOUBLE_JUMP));
                }
            }
        }

        CameraEffectsState.setFastDropping(!isOnGround && player.getDeltaMovement().y < -1.0D && wall == WallDirection.NONE);
        syncFastDropIfChanged(data, ctx);
    }

    private void applyBHopBoost(ClientPlayerEntity player, ClientParkourContext ctx) {
        Vector3d motion = player.getDeltaMovement();
        double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizSpeed > 0.12D) {
            double mult = MobilityConfig.COMMON.bHopSpeedMultiplier.get();
            double maxSpeed = MobilityConfig.COMMON.bHopMaxHorizontalSpeed.get();
            double targetSpeed = Math.min(maxSpeed, horizSpeed * mult);
            double scale = targetSpeed / horizSpeed;

            player.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
            ctx.addFlow(0.14F);
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.6F, 2.0F);
            for (int i = 0; i < 5; i++) {
                player.level.addParticle(
                        ParticleTypes.POOF,
                        player.getX() + (RANDOM.nextDouble() - 0.5D),
                        player.getY(),
                        player.getZ() + (RANDOM.nextDouble() - 0.5D),
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
        ctx.setBHopWindow(0);
    }

    private void triggerGroundPoundImpact(ClientPlayerEntity player, IMobilityData data) {
        CameraEffectsState.addShake(8.0F);
        player.playSound(SoundEvents.ANVIL_LAND, 0.5F, 0.8F);
        player.playSound(SoundEvents.GENERIC_EXPLODE, 0.5F, 1.2F);
        for (int i = 0; i < 36; i++) {
            double angle = i * 10.0D * (Math.PI / 180.0D);
            player.level.addParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    player.getX() + Math.cos(angle) * 2.0D,
                    player.getY() + 0.2D,
                    player.getZ() + Math.sin(angle) * 2.0D,
                    Math.cos(angle) * 0.3D,
                    0.1D,
                    Math.sin(angle) * 0.3D
            );
        }
        data.setGroundPoundCooldown(MobilityConfig.COMMON.groundPoundCooldownTicks.get());
        MobilityNetwork.CHANNEL.sendToServer(new PacketGroundPound());
    }

    private void syncFastDropIfChanged(IMobilityData data, ClientParkourContext ctx) {
        boolean dropping = data.isFastDropping();
        if (dropping != ctx.isSyncedFastDropping()) {
            ctx.setSyncedFastDropping(dropping);
            MobilityNetwork.CHANNEL.sendToServer(new PacketFastDrop(dropping));
        }
    }

    private float randomPitch() {
        return 1.3F + RANDOM.nextFloat() * 0.4F;
    }
}