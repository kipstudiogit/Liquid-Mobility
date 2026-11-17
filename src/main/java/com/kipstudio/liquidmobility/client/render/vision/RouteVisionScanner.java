// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render.vision;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class RouteVisionScanner {
    private static final int MAX_HIGHLIGHTS = 48;

    private RouteVisionScanner() {
    }

    public static List<RouteHighlight> scan(PlayerEntity player, int range) {
        List<RouteHighlight> candidates = new ArrayList<>();
        World level = player.level;
        BlockPos origin = player.blockPosition();
        Vector3d look = player.getLookAngle();
        Vector3d eyePos = new Vector3d(player.getX(), player.getY() + 1.5D, player.getZ());

        int r = Math.max(4, Math.min(range, 20));
        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r) {
                    continue;
                }
                for (int dy = -4; dy <= 4; dy++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);

                    double toX = (cursor.getX() + 0.5D) - eyePos.x;
                    double toY = (cursor.getY() + 0.5D) - eyePos.y;
                    double toZ = (cursor.getZ() + 0.5D) - eyePos.z;
                    double distSq = toX * toX + toY * toY + toZ * toZ;
                    if (distSq < 1.2D || distSq > r * r) {
                        continue;
                    }
                    double invDist = 1.0D / Math.sqrt(distSq);
                    double dot = (toX * look.x + toY * look.y + toZ * look.z) * invDist;
                    if (dot < 0.25D) {
                        continue;
                    }

                    BlockState state = level.getBlockState(cursor);
                    if (state.getCollisionShape(level, cursor).isEmpty()) {
                        continue;
                    }

                    if (isSoftCushion(state) && isPassable(level, cursor.above())) {
                        candidates.add(new RouteHighlight(
                                new AxisAlignedBB(
                                        cursor.getX() + 0.02D,
                                        cursor.getY() + 0.94D,
                                        cursor.getZ() + 0.02D,
                                        cursor.getX() + 0.98D,
                                        cursor.getY() + 1.02D,
                                        cursor.getZ() + 0.98D
                                ),
                                RouteHighlight.Category.SOFT_CUSHION,
                                distSq
                        ));
                        continue;
                    }

                    if (dy == 0
                            && isPassable(level, cursor.above(1))
                            && isPassable(level, cursor.above(2))
                            && !isPassable(level, cursor.below())
                            && hasExposedSide(level, cursor)) {
                        candidates.add(new RouteHighlight(
                                new AxisAlignedBB(
                                        cursor.getX(),
                                        cursor.getY() + 0.88D,
                                        cursor.getZ(),
                                        cursor.getX() + 1.0D,
                                        cursor.getY() + 1.01D,
                                        cursor.getZ() + 1.0D
                                ),
                                RouteHighlight.Category.VAULT_LEDGE,
                                distSq
                        ));
                        continue;
                    }

                    if (dy >= 1 && dy <= 3
                            && isPassable(level, cursor.above(1))
                            && isPassable(level, cursor.above(2))
                            && hasExposedSide(level, cursor)) {
                        candidates.add(new RouteHighlight(
                                new AxisAlignedBB(
                                        cursor.getX(),
                                        cursor.getY() + 0.88D,
                                        cursor.getZ(),
                                        cursor.getX() + 1.0D,
                                        cursor.getY() + 1.01D,
                                        cursor.getZ() + 1.0D
                                ),
                                RouteHighlight.Category.CLIMB_LEDGE,
                                distSq
                        ));
                        continue;
                    }

                    if (dy == 1
                            && !isPassable(level, cursor.below())
                            && !isPassable(level, cursor.above())
                            && hasExposedSide(level, cursor)) {
                        candidates.add(new RouteHighlight(
                                new AxisAlignedBB(
                                        cursor.getX() - 0.005D,
                                        cursor.getY(),
                                        cursor.getZ() - 0.005D,
                                        cursor.getX() + 1.005D,
                                        cursor.getY() + 1.0D,
                                        cursor.getZ() + 1.005D
                                ),
                                RouteHighlight.Category.WALL_RUN,
                                distSq
                        ));
                    }
                }
            }
        }

        if (candidates.size() > MAX_HIGHLIGHTS) {
            candidates.sort((a, b) -> Double.compare(a.getDistanceSq(), b.getDistanceSq()));
            return new ArrayList<>(candidates.subList(0, MAX_HIGHLIGHTS));
        }
        return candidates;
    }

    private static boolean hasExposedSide(World level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (dir != null && isPassable(level, pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPassable(World level, @Nullable BlockPos pos) {
        if (pos == null) {
            return true;
        }
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isSoftCushion(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.HAY_BLOCK
                || block == Blocks.SLIME_BLOCK
                || state.getMaterial() == Material.WOOL;
    }
}