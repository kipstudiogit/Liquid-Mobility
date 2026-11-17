// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.movement;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonMovementHandler {
    public static boolean isMobilityBlocked(PlayerEntity player) {
        if (player.isSpectator() || player.abilities.flying) {
            return true;
        }
        return player.isCreative() && !MobilityConfig.COMMON.allowInCreativeWhenNotFlying.get();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        PlayerEntity player = event.player;
        IMobilityData cap = MobilityCapability.get(player);
        if (cap == null) {
            return;
        }

        if (isMobilityBlocked(player)) {
            cap.resetAll();
            return;
        }

        cap.tick();

        if (player.isInWater() || player.isInLava() || player.isFallFlying() || player.onClimbable()) {
            cap.setSlideTicks(0);
            cap.setWallRunTicks(0);
            cap.setWallDirection(WallDirection.NONE);
            cap.setFastDropping(false);
            return;
        }

        if (!player.level.isClientSide && cap.getWallDirection() != WallDirection.NONE && cap.getWallRunTicks() > 0) {
            player.fallDistance = 0.0F;
        }

        if (player.isOnGround() && player.getDeltaMovement().y <= 0.0D) {
            if (cap.hasDoubleJumped()) {
                cap.setDoubleJumped(false);
            }
            if (cap.getWallDirection() != WallDirection.NONE || cap.getWallRunTicks() > 0) {
                cap.setWallDirection(WallDirection.NONE);
                cap.setWallRunTicks(0);
            }
            if (!player.level.isClientSide && cap.isFastDropping()) {
                cap.setFastDropping(false);
            }
        }
    }
}