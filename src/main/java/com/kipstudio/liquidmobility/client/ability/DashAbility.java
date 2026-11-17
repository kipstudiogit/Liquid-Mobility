// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.ability;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.physics.ClientParkourContext;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.network.MobilityNetwork;
import com.kipstudio.liquidmobility.network.PacketDash;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

@SuppressWarnings("null")
public class DashAbility implements IParkourAbility {
    private static final Random RANDOM = new Random();

    @Override
    public void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx) {
        if (!MobilityConfig.COMMON.enableDash.get()) {
            return;
        }
        if (!input.isDashJustPressed() || data.getDashCooldown() > 0) {
            return;
        }

        float forward = input.getForwardAxis();
        float strafe = input.getStrafeAxis();
        if (forward == 0.0F && strafe == 0.0F) {
            forward = 1.0F;
        }

        float yawRad = player.yRot * ((float) Math.PI / 180.0F);
        double fwdX = -Math.sin(yawRad);
        double fwdZ = Math.cos(yawRad);
        double leftX = Math.cos(yawRad);
        double leftZ = Math.sin(yawRad);

        double dashX = forward * fwdX + strafe * leftX;
        double dashZ = forward * fwdZ + strafe * leftZ;
        double dashSpeed = MobilityConfig.COMMON.dashSpeed.get();
        double vertBoost = MobilityConfig.COMMON.dashVerticalBoost.get();

        Vector3d dashVec = new Vector3d(dashX, 0.0D, dashZ).normalize().scale(dashSpeed);
        player.setDeltaMovement(dashVec.x, vertBoost, dashVec.z);
        player.fallDistance = 0.0F;

        data.setDashCooldown(MobilityConfig.COMMON.dashCooldownTicks.get());
        data.setDashTicks(MobilityConfig.COMMON.dashAnimTicks.get());
        data.setFastDropping(false);
        ctx.setGroundPoundArmed(false);
        ctx.resetAirFallDistance();
        CameraEffectsState.consumePendingFallDistance();

        CameraEffectsState.triggerDash(forward, strafe);
        CameraEffectsState.addShake(2.2F);

        MobilityNetwork.CHANNEL.sendToServer(new PacketDash());
        player.playSound(SoundEvents.TRIDENT_THROW, 1.0F, 1.3F + RANDOM.nextFloat() * 0.4F);
        for (int i = 0; i < 15; i++) {
            player.level.addParticle(
                    ParticleTypes.POOF,
                    player.getX() + (RANDOM.nextDouble() - 0.5D),
                    player.getY() + 1.0D,
                    player.getZ() + (RANDOM.nextDouble() - 0.5D),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
}