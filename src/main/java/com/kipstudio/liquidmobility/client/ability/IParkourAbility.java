// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.ability;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.physics.ClientParkourContext;
import net.minecraft.client.entity.player.ClientPlayerEntity;

public interface IParkourAbility {
    void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx);
}