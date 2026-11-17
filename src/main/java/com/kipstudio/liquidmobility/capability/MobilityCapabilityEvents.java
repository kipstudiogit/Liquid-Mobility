// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.capability;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.network.MobilityNetwork;
import com.kipstudio.liquidmobility.network.PacketSyncMobility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobilityCapabilityEvents {
    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(LiquidMobility.MOD_ID, "mobility_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            MobilityProvider provider = new MobilityProvider();
            event.addCapability(CAPABILITY_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncToSelf(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncToSelf(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncToSelf(event.getPlayer());
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayerEntity && event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity target = (ServerPlayerEntity) event.getTarget();
            ServerPlayerEntity tracker = (ServerPlayerEntity) event.getPlayer();
            IMobilityData cap = MobilityCapability.get(target);
            if (cap != null) {
                MobilityNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> tracker),
                        new PacketSyncMobility(target.getId(), cap)
                );
            }
        }
    }

    private static void syncToSelf(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            IMobilityData cap = MobilityCapability.get(serverPlayer);
            if (cap != null) {
                MobilityNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new PacketSyncMobility(serverPlayer.getId(), cap)
                );
            }
        }
    }
}