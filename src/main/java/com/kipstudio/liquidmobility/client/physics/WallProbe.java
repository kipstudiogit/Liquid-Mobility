// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.physics;

import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class WallProbe {
    private static final double WALL_CHECK_DISTANCE = 0.7D;

    private WallProbe() {
    }

    public static WallDirection detectWall(PlayerEntity player) {
        float yawRad = player.yRot * ((float) Math.PI / 180.0F);
        double leftX = Math.cos(yawRad) * WALL_CHECK_DISTANCE;
        double leftZ = Math.sin(yawRad) * WALL_CHECK_DISTANCE;
        double rightX = -Math.cos(yawRad) * WALL_CHECK_DISTANCE;
        double rightZ = -Math.sin(yawRad) * WALL_CHECK_DISTANCE;
        double fwdX = -Math.sin(yawRad) * WALL_CHECK_DISTANCE;
        double fwdZ = Math.cos(yawRad) * WALL_CHECK_DISTANCE;

        double y = player.getY() + 1.0D;
        World level = player.level;

        if (isSolidBlock(level, new BlockPos(player.getX() + leftX, y, player.getZ() + leftZ))) {
            return WallDirection.LEFT;
        }
        if (isSolidBlock(level, new BlockPos(player.getX() + rightX, y, player.getZ() + rightZ))) {
            return WallDirection.RIGHT;
        }
        if (isSolidBlock(level, new BlockPos(player.getX() + fwdX, y, player.getZ() + fwdZ))) {
            return WallDirection.FRONT;
        }
        return WallDirection.NONE;
    }

    public static boolean canStepVault(PlayerEntity player, Vector3d motion) {
        double dirX;
        double dirZ;
        double horizSq = motion.x * motion.x + motion.z * motion.z;
        if (horizSq >= 0.0025D) {
            double len = Math.sqrt(horizSq);
            dirX = motion.x / len;
            dirZ = motion.z / len;
        } else {
            float yawRad = player.yRot * ((float) Math.PI / 180.0F);
            dirX = -Math.sin(yawRad);
            dirZ = Math.cos(yawRad);
        }

        return hasStepObstacleAt(player, dirX, dirZ, 0.85D) || hasStepObstacleAt(player, dirX, dirZ, 1.25D);
    }

    private static boolean hasStepObstacleAt(PlayerEntity player, double dirX, double dirZ, double distance) {
        BlockPos frontPos = new BlockPos(
                player.getX() + dirX * distance,
                player.getY() + 0.5D,
                player.getZ() + dirZ * distance
        );
        World level = player.level;

        return isSolidBlock(level, frontPos)
                && !isSolidBlock(level, frontPos.above(1))
                && !isSolidBlock(level, frontPos.above(2));
    }

    @Nullable
    public static Vector3d detectLedgeDirection(PlayerEntity player) {
        Vector3d look = player.getLookAngle();
        double horizSq = look.x * look.x + look.z * look.z;
        if (horizSq < 0.0001D) {
            return null;
        }

        double len = Math.sqrt(horizSq);
        double normX = (look.x / len) * WALL_CHECK_DISTANCE;
        double normZ = (look.z / len) * WALL_CHECK_DISTANCE;

        BlockPos chestPos = new BlockPos(player.getX() + normX, player.getY() + 0.8D, player.getZ() + normZ);
        BlockPos headPos = new BlockPos(player.getX() + normX, player.getY() + 1.9D, player.getZ() + normZ);
        World level = player.level;

        if (isSolidBlock(level, chestPos) && !isSolidBlock(level, headPos)) {
            return new Vector3d(look.x / len, 0.0D, look.z / len);
        }
        return null;
    }

    private static boolean isSolidBlock(World level, @Nullable BlockPos pos) {
        if (pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }
}