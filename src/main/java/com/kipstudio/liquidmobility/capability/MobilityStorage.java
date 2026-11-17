// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class MobilityStorage implements Capability.IStorage<IMobilityData> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<IMobilityData> capability, IMobilityData instance, Direction side) {
        return new CompoundNBT();
    }

    @Override
    public void readNBT(Capability<IMobilityData> capability, IMobilityData instance, Direction side, INBT nbt) {
    }
}