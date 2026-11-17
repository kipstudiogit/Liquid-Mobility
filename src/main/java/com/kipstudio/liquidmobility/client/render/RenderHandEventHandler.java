// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

import com.kipstudio.liquidmobility.LiquidMobility;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderHandEventHandler {
    private static final HandMotionAnimator ANIMATOR = new HandMotionAnimator();

    @SubscribeEvent
    public static void onHandRender(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (player == null || mc.isPaused() || !MobilityConfig.CLIENT.enableHandAnimations.get()) {
            ANIMATOR.reset();
            return;
        }

        if (event.getHand() == Hand.MAIN_HAND) {
            ANIMATOR.updateFrame(player);
        }

        ANIMATOR.applyToHand(event.getMatrixStack(), event.getHand());
    }
}