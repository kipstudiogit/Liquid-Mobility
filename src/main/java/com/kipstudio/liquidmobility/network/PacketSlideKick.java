// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.network;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

@SuppressWarnings("null")
public class PacketSlideKick {
    private final int targetId;

    public PacketSlideKick(int targetId) {
        this.targetId = targetId;
    }

    public PacketSlideKick(PacketBuffer buf) {
        this.targetId = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(targetId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.isAlive() || CommonMovementHandler.isMobilityBlocked(player)) {
                return;
            }
            if (!MobilityConfig.COMMON.enableSlide.get()) {
                return;
            }

            IMobilityData cap = MobilityCapability.get(player);
            if (cap == null || cap.getSlideTicks() <= 0 || cap.getSlideKickCooldown() > 0) {
                return;
            }

            Entity target = player.level.getEntity(targetId);
            if (target instanceof LivingEntity && target != player && target.isAlive() && player.distanceTo(target) < 5.0D) {
                float damage = MobilityConfig.COMMON.slideKickDamage.get().floatValue();
                float knockback = MobilityConfig.COMMON.slideKickKnockback.get().floatValue();

                target.hurt(DamageSource.playerAttack(player), damage);
                float yawRad = player.yRot * ((float) Math.PI / 180.0F);
                double ratioX = Math.sin(yawRad);
                double ratioZ = -Math.cos(yawRad);
                ((LivingEntity) target).knockback(knockback, ratioX, ratioZ);

                cap.setSlideTicks(0);
                cap.setSlideKickCooldown(MobilityConfig.COMMON.slideKickCooldownTicks.get());

                MobilityNetwork.CHANNEL.send(
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                        new PacketSyncMobility(player.getId(), cap)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}