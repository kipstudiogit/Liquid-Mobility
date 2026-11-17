// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.movement;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FallImpactHandler {
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (CommonMovementHandler.isMobilityBlocked(player)) {
            return;
        }

        float fallDistance = event.getDistance();
        if (player.level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CameraEffectsState.recordFallImpact(fallDistance));
        }

        IMobilityData cap = MobilityCapability.get(player);
        if (cap == null) {
            return;
        }

        if (cap.isFastDropping() && MobilityConfig.COMMON.enableFastDrop.get()) {
            if (fallDistance >= MobilityConfig.COMMON.groundPoundMinFallDistance.get()) {
                event.setDamageMultiplier(0.0F);
                event.setDistance(0.0F);
                if (!player.level.isClientSide) {
                    cap.setGroundPoundWindowTicks(8);
                    cap.setFastDropping(false);
                }
                return;
            }
            if (!player.level.isClientSide) {
                cap.setFastDropping(false);
            }
        }

        if (MobilityConfig.COMMON.enableSoftCushions.get()
                && fallDistance >= 3.0F
                && isLandingOnSoftCushion(player)) {
            event.setDamageMultiplier(0.2F);
            if (player.level.isClientSide) {
                player.playSound(SoundEvents.WOOL_FALL, 1.0F, 0.9F);
            }
            return;
        }

        if (MobilityConfig.COMMON.enableFallRoll.get()
                && player.isCrouching()
                && fallDistance >= MobilityConfig.COMMON.rollMinFallDistance.get()) {
            float damageMultiplier = MobilityConfig.COMMON.rollFallDamageMultiplier.get().floatValue();
            event.setDamageMultiplier(damageMultiplier);
            if (damageMultiplier <= 0.0F) {
                event.setDistance(0.0F);
            }

            int rollTicks = MobilityConfig.COMMON.rollSlideTicks.get();
            cap.setSlideTicks(rollTicks);

            if (player.level.isClientSide) {
                float yawRad = player.yRot * ((float) Math.PI / 180.0F);
                double fwdX = -Math.sin(yawRad);
                double fwdZ = Math.cos(yawRad);
                double boost = Math.min(2.2D, 0.95D + (fallDistance * 0.08D));

                player.setDeltaMovement(fwdX * boost, player.getDeltaMovement().y, fwdZ * boost);
                player.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 0.85F, 0.85F);
                player.playSound(SoundEvents.SAND_HIT, 0.85F, 1.25F);

                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    CameraEffectsState.triggerSkillRoll();
                    CameraEffectsState.addShake(2.5F);
                });

                for (int i = 0; i < 8; i++) {
                    player.level.addParticle(
                            ParticleTypes.CLOUD,
                            player.getX() + (player.level.random.nextDouble() - 0.5D),
                            player.getY() + 0.1D,
                            player.getZ() + (player.level.random.nextDouble() - 0.5D),
                            0.0D,
                            0.08D,
                            0.0D
                    );
                }
            }
            return;
        }

        if (MobilityConfig.COMMON.enableHardLandingStagger.get()
                && fallDistance >= MobilityConfig.COMMON.hardLandingMinFallDistance.get()
                && player.level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CameraEffectsState::triggerHardLanding);
        }
    }

    private static boolean isLandingOnSoftCushion(PlayerEntity player) {
        BlockPos below = new BlockPos(player.getX(), player.getY() - 0.2D, player.getZ());
        BlockState state = player.level.getBlockState(below);
        Block block = state.getBlock();
        return block == Blocks.HAY_BLOCK
                || block == Blocks.SLIME_BLOCK
                || state.getMaterial() == Material.WOOL;
    }
}