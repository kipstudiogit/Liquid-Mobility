// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render.anim;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.client.render.FrameDeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerAnimStateRegistry {
    private static final Map<Integer, PlayerAnimState> STATES = new HashMap<>();
    private static final FrameDeltaTracker FRAME_DELTA = new FrameDeltaTracker();
    private static long currentFrameId;
    private static float currentFrameDt = 1.0F / 60.0F;

    private PlayerAnimStateRegistry() {
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) {
            FRAME_DELTA.reset();
            return;
        }
        currentFrameId++;
        currentFrameDt = FRAME_DELTA.sampleDeltaSeconds();
        if (STATES.size() > 128) {
            STATES.clear();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) {
            STATES.clear();
            FRAME_DELTA.reset();
        }
    }

    @Nullable
    public static PlayerAnimState getUpdatedState(@Nullable PlayerEntity player) {
        if (player == null) {
            return null;
        }
        IMobilityData cap = MobilityCapability.get(player);
        if (cap == null) {
            return null;
        }
        PlayerAnimState state = STATES.computeIfAbsent(player.getId(), id -> new PlayerAnimState());
        state.updateOncePerFrame(currentFrameId, currentFrameDt, player, cap);
        return state;
    }
}