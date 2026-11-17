// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.ability;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.physics.ClientParkourContext;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.network.MobilityNetwork;
import com.kipstudio.liquidmobility.network.PacketSlideKick;
import com.kipstudio.liquidmobility.network.PacketSlideState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Random;

@SuppressWarnings("null")
public class SlideAbility implements IParkourAbility {
    private static final Random RANDOM = new Random();

    @Override
    public void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx) {
        if (!MobilityConfig.COMMON.enableSlide.get()) {
            data.setSlideTicks(0);
            CameraEffectsState.setSliding(false);
            syncIfChanged(data, ctx);
            return;
        }

        boolean isOnGround = player.isOnGround();
        Vector3d motion = player.getDeltaMovement();
        double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        if (input.isSneakJustPressed()
                && isOnGround
                && data.getSlideTicks() == 0
                && (player.isSprinting() || horizSpeed > 0.15D)) {
            data.setSlideTicks(MobilityConfig.COMMON.slideDurationTicks.get());
            ctx.addFlow(0.14F);
            player.playSound(SoundEvents.SAND_HIT, 1.0F, 1.3F + RANDOM.nextFloat() * 0.4F);
        }

        if (data.getSlideTicks() > 0) {
            if (data.getSlideKickCooldown() == 0) {
                List<Entity> targets = player.level.getEntities(player, player.getBoundingBox().inflate(0.5D));
                for (Entity target : targets) {
                    if (target instanceof LivingEntity && target.isAlive()) {
                        MobilityNetwork.CHANNEL.sendToServer(new PacketSlideKick(target.getId()));
                        player.setDeltaMovement(-motion.x * 0.5D, 0.3D, -motion.z * 0.5D);
                        player.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
                        player.level.addParticle(
                                ParticleTypes.CRIT,
                                target.getX(),
                                target.getY() + 1.0D,
                                target.getZ(),
                                0.0D,
                                0.0D,
                                0.0D
                        );
                        data.setSlideTicks(0);
                        data.setSlideKickCooldown(MobilityConfig.COMMON.slideKickCooldownTicks.get());
                        break;
                    }
                }
            }

            if (data.getSlideTicks() > 0) {
                if (!input.isSneakDown() || !isOnGround) {
                    data.setSlideTicks(0);
                } else {
                    float yawRad = player.yRot * ((float) Math.PI / 180.0F);
                    double fwdX = -Math.sin(yawRad);
                    double fwdZ = Math.cos(yawRad);

                    int maxTicks = Math.max(1, MobilityConfig.COMMON.slideDurationTicks.get());
                    float factor = Math.min(1.0F, (float) data.getSlideTicks() / (float) maxTicks);
                    double baseSpeed = MobilityConfig.COMMON.slideBaseSpeed.get();
                    double peakBoost = MobilityConfig.COMMON.slidePeakBoostSpeed.get() * (1.0D + ctx.getFlowLevel() * 0.25D);
                    double speed = baseSpeed + (peakBoost * factor * factor);

                    player.setDeltaMovement(fwdX * speed, player.getDeltaMovement().y, fwdZ * speed);

                    if (data.getSlideTicks() % 2 == 0) {
                        player.level.addParticle(
                                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                player.getX(),
                                player.getY() + 0.1D,
                                player.getZ(),
                                0.0D,
                                0.02D,
                                0.0D
                        );
                    }
                }
            }
        }

        CameraEffectsState.setSliding(data.getSlideTicks() > 0);
        syncIfChanged(data, ctx);
    }

    private void syncIfChanged(IMobilityData data, ClientParkourContext ctx) {
        boolean sliding = data.getSlideTicks() > 0;
        if (sliding != ctx.isSyncedSlideActive()) {
            ctx.setSyncedSlideActive(sliding);
            MobilityNetwork.CHANNEL.sendToServer(new PacketSlideState(sliding));
        }
    }
}