// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.ability;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.client.input.ClientInputState;
import com.kipstudio.liquidmobility.client.physics.ClientParkourContext;
import com.kipstudio.liquidmobility.client.physics.WallProbe;
import com.kipstudio.liquidmobility.client.render.CameraEffectsState;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.JumpActionType;
import com.kipstudio.liquidmobility.network.MobilityNetwork;
import com.kipstudio.liquidmobility.network.PacketParkourJump;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

@SuppressWarnings("null")
public class VaultAbility implements IParkourAbility {
    private static final Random RANDOM = new Random();

    @Override
    public void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx) {
        if (!MobilityConfig.COMMON.enableVault.get()) {
            return;
        }

        Vector3d motion = player.getDeltaMovement();
        double horizSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        if (player.isOnGround()
                && input.hasSprintIntent()
                && input.getForwardAxis() > 0.0F
                && (horizSpeed > MobilityConfig.COMMON.vaultMinHorizontalSpeed.get() || player.horizontalCollision)
                && WallProbe.canStepVault(player, motion)) {
            boolean springboard = input.isJumpDown();
            double vertBoost = MobilityConfig.COMMON.vaultVerticalBoost.get();
            if (springboard) {
                vertBoost += MobilityConfig.COMMON.springboardVaultBonus.get();
            }

            double horizMult = springboard ? 1.24D : 1.12D;
            float yawRad = player.yRot * ((float) Math.PI / 180.0F);
            double fwdX = -Math.sin(yawRad) * 0.4D;
            double fwdZ = Math.cos(yawRad) * 0.4D;
            double nextX = Math.abs(motion.x) > 0.05D ? motion.x * horizMult : fwdX;
            double nextZ = Math.abs(motion.z) > 0.05D ? motion.z * horizMult : fwdZ;

            player.setDeltaMovement(nextX, vertBoost, nextZ);
            player.setSprinting(true);
            player.fallDistance = 0.0F;
            ctx.resetAirFallDistance();
            CameraEffectsState.consumePendingFallDistance();
            ctx.addFlow(springboard ? 0.22F : 0.16F);
            CameraEffectsState.triggerVault();
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.55F, randomPitch());
            MobilityNetwork.CHANNEL.sendToServer(new PacketParkourJump(JumpActionType.VAULT));
            return;
        }

        if (player.horizontalCollision && !player.isOnGround() && input.getForwardAxis() > 0.0F) {
            Vector3d ledgeDir = WallProbe.detectLedgeDirection(player);
            if (ledgeDir != null) {
                boolean springboard = input.isJumpDown();
                double ledgeBoost = MobilityConfig.COMMON.ledgeVaultVerticalBoost.get();
                if (springboard) {
                    ledgeBoost += MobilityConfig.COMMON.springboardVaultBonus.get() * 0.8D;
                }

                data.setWallRunTicks(0);
                player.setDeltaMovement(ledgeDir.x * 0.48D, ledgeBoost, ledgeDir.z * 0.48D);
                player.fallDistance = 0.0F;
                ctx.resetAirFallDistance();
                CameraEffectsState.consumePendingFallDistance();
                ctx.addFlow(0.18F);
                CameraEffectsState.triggerVault();
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.7F, randomPitch());
                MobilityNetwork.CHANNEL.sendToServer(new PacketParkourJump(JumpActionType.VAULT));
            }
        }
    }

    private float randomPitch() {
        return 1.3F + RANDOM.nextFloat() * 0.4F;
    }
}