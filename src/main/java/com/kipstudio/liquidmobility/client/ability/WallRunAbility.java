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
import com.kipstudio.liquidmobility.core.WallDirection;
import com.kipstudio.liquidmobility.network.MobilityNetwork;
import com.kipstudio.liquidmobility.network.PacketParkourJump;
import com.kipstudio.liquidmobility.network.PacketWallState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvents;

import java.util.Random;

@SuppressWarnings("null")
public class WallRunAbility implements IParkourAbility {
    private static final Random RANDOM = new Random();

    private int wallGraceTicks;
    private int quickTurnLockTicks;
    private WallDirection graceWall = WallDirection.NONE;

    @Override
    public void tick(ClientPlayerEntity player, IMobilityData data, ClientInputState input, ClientParkourContext ctx) {
        if (!MobilityConfig.COMMON.enableWallRun.get()) {
            data.setWallDirection(WallDirection.NONE);
            data.setWallRunTicks(0);
            wallGraceTicks = 0;
            quickTurnLockTicks = 0;
            graceWall = WallDirection.NONE;
            CameraEffectsState.setActiveWallDirection(WallDirection.NONE);
            syncIfChanged(data, ctx, false);
            return;
        }

        if (QuickTurnController.isTurning()) {
            if (data.getWallDirection() != WallDirection.NONE || data.getWallRunTicks() > 0) {
                graceWall = data.getWallDirection();
                wallGraceTicks = 10;
            }
            quickTurnLockTicks = 6;
            data.setWallRunTicks(0);
            data.setWallDirection(WallDirection.NONE);
        } else if (quickTurnLockTicks > 0) {
            quickTurnLockTicks--;
        }

        boolean isOnGround = player.isOnGround();
        boolean lockWallAttach = QuickTurnController.isTurning() || quickTurnLockTicks > 0;
        WallDirection wall = (isOnGround || lockWallAttach) ? WallDirection.NONE : WallProbe.detectWall(player);

        float yawRad = player.yRot * ((float) Math.PI / 180.0F);
        double fwdX = -Math.sin(yawRad);
        double fwdZ = Math.cos(yawRad);
        double leftX = Math.cos(yawRad);
        double leftZ = Math.sin(yawRad);
        double rightX = -leftX;
        double rightZ = -leftZ;

        boolean clinging = false;

        if (!isOnGround && wall != WallDirection.NONE && wall != ctx.getLastJumpedWall()) {
            wallGraceTicks = 6;
            graceWall = wall;

            if (input.isSneakDown()) {
                clinging = true;
                data.setWallRunTicks(0);
                data.setWallDirection(wall);
                data.setFastDropping(false);
                ctx.setGroundPoundArmed(false);
                ctx.resetAirFallDistance();
                CameraEffectsState.consumePendingFallDistance();

                double hugX = 0.0D;
                double hugZ = 0.0D;
                if (wall == WallDirection.LEFT) {
                    hugX = leftX * 0.02D;
                    hugZ = leftZ * 0.02D;
                } else if (wall == WallDirection.RIGHT) {
                    hugX = rightX * 0.02D;
                    hugZ = rightZ * 0.02D;
                }

                double descent = -MobilityConfig.COMMON.wallClingDescentSpeed.get();
                player.setDeltaMovement(hugX, descent, hugZ);
                player.fallDistance = 0.0F;
                if (player.tickCount % 10 == 0) {
                    player.playSound(SoundEvents.SAND_HIT, 0.3F, 1.5F);
                }

                if (input.isJumpJustPressed()) {
                    performWallJump(player, data, ctx, wall, fwdX, fwdZ, leftX, leftZ, rightX, rightZ, false);
                    clinging = false;
                }
            } else if (input.hasSprintIntent() && player.getDeltaMovement().y < 0.1D && data.getWallRunTicks() == 0) {
                int duration = (wall == WallDirection.FRONT)
                        ? MobilityConfig.COMMON.wallClimbDurationTicks.get()
                        : MobilityConfig.COMMON.wallRunHorizontalDurationTicks.get();
                data.setWallRunTicks(duration);
                data.setWallDirection(wall);
                data.setFastDropping(false);
                ctx.setGroundPoundArmed(false);
                ctx.resetAirFallDistance();
                CameraEffectsState.consumePendingFallDistance();
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.8F, randomPitch());
            }
        } else {
            data.setWallRunTicks(0);
            data.setWallDirection(WallDirection.NONE);

            if (isOnGround) {
                wallGraceTicks = 0;
                graceWall = WallDirection.NONE;
            } else if (wallGraceTicks > 0) {
                wallGraceTicks--;
                if (input.isJumpJustPressed() && graceWall != WallDirection.NONE && (lockWallAttach || graceWall != ctx.getLastJumpedWall())) {
                    performWallJump(player, data, ctx, WallDirection.NONE, fwdX, fwdZ, leftX, leftZ, rightX, rightZ, false);
                }
            }
        }

        if (data.getWallRunTicks() > 0 && !input.isSneakDown() && !isOnGround && wall != WallDirection.NONE) {
            data.setWallDirection(wall);
            if (wall == WallDirection.FRONT) {
                double climbSpeed = MobilityConfig.COMMON.wallClimbSpeed.get();
                player.setDeltaMovement(0.0D, climbSpeed, 0.0D);
                player.fallDistance = 0.0F;

                if (input.isJumpJustPressed()) {
                    performWallJump(player, data, ctx, wall, fwdX, fwdZ, leftX, leftZ, rightX, rightZ, true);
                }
            } else {
                double hugX = (wall == WallDirection.LEFT ? leftX : rightX) * 0.02D;
                double hugZ = (wall == WallDirection.LEFT ? leftZ : rightZ) * 0.02D;
                int halfDuration = MobilityConfig.COMMON.wallRunHorizontalDurationTicks.get() / 2;
                double motionY = (data.getWallRunTicks() > halfDuration) ? 0.0D : -0.05D;
                double runSpeed = MobilityConfig.COMMON.wallRunSpeed.get();

                player.setDeltaMovement(fwdX * runSpeed + hugX, motionY, fwdZ * runSpeed + hugZ);
                player.fallDistance = 0.0F;

                if (input.isJumpJustPressed()) {
                    performWallJump(player, data, ctx, wall, fwdX, fwdZ, leftX, leftZ, rightX, rightZ, false);
                }
            }
        } else if (!clinging) {
            data.setWallDirection(WallDirection.NONE);
        }

        CameraEffectsState.setActiveWallDirection(data.getWallDirection());
        syncIfChanged(data, ctx, clinging);
    }

    private void performWallJump(
            ClientPlayerEntity player,
            IMobilityData data,
            ClientParkourContext ctx,
            WallDirection wall,
            double fwdX,
            double fwdZ,
            double leftX,
            double leftZ,
            double rightX,
            double rightZ,
            boolean backward
    ) {
        double horizBoost = MobilityConfig.COMMON.wallJumpHorizontalBoost.get();
        double sidePush = MobilityConfig.COMMON.wallJumpSidePush.get();
        double vertBoost = MobilityConfig.COMMON.wallJumpVerticalBoost.get();

        double pushX = 0.0D;
        double pushZ = 0.0D;
        if (wall == WallDirection.LEFT) {
            pushX = rightX * sidePush;
            pushZ = rightZ * sidePush;
        } else if (wall == WallDirection.RIGHT) {
            pushX = leftX * sidePush;
            pushZ = leftZ * sidePush;
        }

        double dirSign = backward ? -1.0D : 1.0D;
        player.setDeltaMovement(fwdX * horizBoost * dirSign + pushX, vertBoost, fwdZ * horizBoost * dirSign + pushZ);
        player.fallDistance = 0.0F;

        data.setWallRunTicks(0);
        data.setWallDirection(WallDirection.NONE);
        data.setDoubleJumped(false);
        ctx.setLastJumpedWall(wall != WallDirection.NONE ? wall : graceWall);
        ctx.addFlow(0.20F);
        ctx.resetAirFallDistance();
        CameraEffectsState.consumePendingFallDistance();
        wallGraceTicks = 0;

        CameraEffectsState.triggerVault();
        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, randomPitch());
        MobilityNetwork.CHANNEL.sendToServer(new PacketParkourJump(JumpActionType.WALL_JUMP));
    }

    private void syncIfChanged(IMobilityData data, ClientParkourContext ctx, boolean clinging) {
        WallDirection currentDir = data.getWallDirection();
        boolean running = data.getWallRunTicks() > 0;
        if (currentDir != ctx.getSyncedWallDirection() || running != ctx.isSyncedWallRunning()) {
            ctx.setSyncedWallDirection(currentDir);
            ctx.setSyncedWallRunning(running);
            MobilityNetwork.CHANNEL.sendToServer(new PacketWallState(currentDir, data.getWallRunTicks(), clinging));
        }
    }

    private float randomPitch() {
        return 1.3F + RANDOM.nextFloat() * 0.4F;
    }
}