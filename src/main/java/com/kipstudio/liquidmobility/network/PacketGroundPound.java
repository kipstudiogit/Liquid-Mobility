// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("null")
public class PacketGroundPound {
    public PacketGroundPound() {
    }

    public PacketGroundPound(PacketBuffer buf) {
    }

    public void toBytes(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.isAlive() || CommonMovementHandler.isMobilityBlocked(player)) {
                return;
            }
            if (!MobilityConfig.COMMON.enableFastDrop.get()) {
                return;
            }

            IMobilityData cap = MobilityCapability.get(player);
            if (cap == null || cap.getGroundPoundCooldown() > 0) {
                return;
            }
            if (!cap.isFastDropping() && cap.getGroundPoundWindowTicks() <= 0) {
                return;
            }

            double radius = MobilityConfig.COMMON.groundPoundRadius.get();
            float damage = MobilityConfig.COMMON.groundPoundDamage.get().floatValue();
            float knockback = MobilityConfig.COMMON.groundPoundKnockback.get().floatValue();

            List<LivingEntity> targets = player.level.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(radius)
            );
            for (LivingEntity target : targets) {
                if (target != player && target.isAlive()) {
                    target.hurt(DamageSource.playerAttack(player), damage);
                    double dx = target.getX() - player.getX();
                    double dz = target.getZ() - player.getZ();
                    if (dx * dx + dz * dz < 0.0001D) {
                        dx = 0.1D;
                    }
                    target.knockback(knockback, -dx, -dz);
                }
            }

            cap.setFastDropping(false);
            cap.setGroundPoundWindowTicks(0);
            cap.setGroundPoundCooldown(MobilityConfig.COMMON.groundPoundCooldownTicks.get());
            player.fallDistance = 0.0F;

            MobilityNetwork.CHANNEL.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new PacketSyncMobility(player.getId(), cap)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}