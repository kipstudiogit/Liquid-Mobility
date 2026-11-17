// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class MobilityCapability {
    @CapabilityInject(IMobilityData.class)
    public static Capability<IMobilityData> CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(IMobilityData.class, new MobilityStorage(), MobilityData::new);
    }

    @Nullable
    public static IMobilityData get(@Nullable PlayerEntity player) {
        Capability<IMobilityData> cap = CAPABILITY;
        if (player == null || cap == null) {
            return null;
        }
        LazyOptional<IMobilityData> lazy = player.getCapability(cap);
        if (!lazy.isPresent()) {
            return null;
        }
        return lazy.orElseThrow(IllegalStateException::new);
    }
}