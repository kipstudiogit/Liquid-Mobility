// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.core;

import com.kipstudio.liquidmobility.LiquidMobility;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = LiquidMobility.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeybindRegistry {
    public static final KeyBinding DASH_KEY = new KeyBinding(
            "key.liquidmobility.dash",
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.liquidmobility.main"
    );

    public static final KeyBinding QUICK_TURN_KEY = new KeyBinding(
            "key.liquidmobility.quick_turn",
            GLFW.GLFW_KEY_X,
            "category.liquidmobility.main"
    );

    public static final KeyBinding ROUTE_VISION_KEY = new KeyBinding(
            "key.liquidmobility.route_vision",
            GLFW.GLFW_KEY_V,
            "category.liquidmobility.main"
    );

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(DASH_KEY);
        ClientRegistry.registerKeyBinding(QUICK_TURN_KEY);
        ClientRegistry.registerKeyBinding(ROUTE_VISION_KEY);
    }
}