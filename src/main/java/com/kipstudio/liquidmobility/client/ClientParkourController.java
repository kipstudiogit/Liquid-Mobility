// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.client.ability.AirMobilityAbility;
import com.kipstudio.liquidmobility.client.ability.DashAbility;
import com.kipstudio.liquidmobility.client.ability.IParkourAbility;
import com.kipstudio.liquidmobility.client.ability.QuickTurnController;
import com.kipstudio.liquidmobility.client.ability.SlideAbility;
import com.kipstudio.liquidmobility.client.ability.VaultAbility;
import com.kipstudio.liquidmobility.client.ability.WallRunAbility;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.physics.ClientParkourContext;
import com.kipstudio.liquidmobility.client.physics.FlowMomentumController;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.movement.CommonMovementHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("null")
@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientParkourController {
    private static final ClientInputState INPUT = new ClientInputState();
    private static final ClientParkourContext CONTEXT = new ClientParkourContext();
    private static final List<IParkourAbility> ABILITIES = Arrays.asList(
            new WallRunAbility(),
            new SlideAbility(),
            new AirMobilityAbility(),
            new DashAbility(),
            new VaultAbility()
    );
    private static boolean loggedMissingCapability;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            CONTEXT.reset();
            CameraEffectsState.reset();
            QuickTurnController.cancel();
            loggedMissingCapability = false;
            return;
        }

        if (mc.isPaused() || mc.screen != null) {
            INPUT.reset(mc);
            CameraEffectsState.reset();
            QuickTurnController.cancel();
            return;
        }

        INPUT.sample(mc, player);

        if (INPUT.isRouteVisionJustPressed()) {
            CONTEXT.toggleRouteVision();
            boolean enabled = CONTEXT.isRouteVisionEnabled();
            CameraEffectsState.setRouteVisionActive(enabled);
            player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.35F, enabled ? 1.5F : 1.0F);
        }

        if (CommonMovementHandler.isMobilityBlocked(player)
                || player.isFallFlying()
                || player.isInWater()
                || player.isInLava()
                || player.onClimbable()) {
            CONTEXT.updateGroundHistory(player.isOnGround());
            CONTEXT.resetAirFallDistance();
            CameraEffectsState.reset();
            QuickTurnController.cancel();
            return;
        }

        if (INPUT.isQuickTurnJustPressed()) {
            QuickTurnController.trigger(player);
        }

        IMobilityData data = MobilityCapability.get(player);
        if (data == null) {
            if (!loggedMissingCapability) {
                loggedMissingCapability = true;
                LiquidMobility.LOGGER.warn("Mobility capability is not attached to client player; parkour ticks skipped.");
            }
            return;
        }

        for (IParkourAbility ability : ABILITIES) {
            ability.tick(player, data, INPUT, CONTEXT);
        }

        FlowMomentumController.tick(player, data, INPUT, CONTEXT);
        CONTEXT.updateGroundHistory(player.isOnGround());
    }
}