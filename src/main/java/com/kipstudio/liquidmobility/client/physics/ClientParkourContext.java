// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.physics;

import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.util.math.MathHelper;

public class ClientParkourContext {
    private boolean wasOnGround = true;
    private int ticksSinceOnGround;
    private int bHopWindow;
    private WallDirection lastJumpedWall = WallDirection.NONE;
    private WallDirection syncedWallDirection = WallDirection.NONE;
    private boolean syncedWallRunning;
    private boolean syncedSlideActive;
    private boolean syncedFastDropping;
    private boolean groundPoundArmed;
    private float flowLevel;
    private int hardLandStaggerTicks;
    private boolean routeVisionEnabled = true;
    private float lastAirFallDistance;

    public void updateGroundHistory(boolean isOnGround) {
        wasOnGround = isOnGround;
    }

    public void reset() {
        wasOnGround = true;
        ticksSinceOnGround = 0;
        bHopWindow = 0;
        lastJumpedWall = WallDirection.NONE;
        syncedWallDirection = WallDirection.NONE;
        syncedWallRunning = false;
        syncedSlideActive = false;
        syncedFastDropping = false;
        groundPoundArmed = false;
        flowLevel = 0.0F;
        hardLandStaggerTicks = 0;
        lastAirFallDistance = 0.0F;
    }

    public boolean wasOnGround() {
        return wasOnGround;
    }

    public int getTicksSinceOnGround() {
        return ticksSinceOnGround;
    }

    public void setTicksSinceOnGround(int ticksSinceOnGround) {
        this.ticksSinceOnGround = Math.max(0, ticksSinceOnGround);
    }

    public int getBHopWindow() {
        return bHopWindow;
    }

    public void setBHopWindow(int bHopWindow) {
        this.bHopWindow = Math.max(0, bHopWindow);
    }

    public WallDirection getLastJumpedWall() {
        return lastJumpedWall;
    }

    public void setLastJumpedWall(WallDirection lastJumpedWall) {
        this.lastJumpedWall = lastJumpedWall != null ? lastJumpedWall : WallDirection.NONE;
    }

    public WallDirection getSyncedWallDirection() {
        return syncedWallDirection;
    }

    public void setSyncedWallDirection(WallDirection syncedWallDirection) {
        this.syncedWallDirection = syncedWallDirection != null ? syncedWallDirection : WallDirection.NONE;
    }

    public boolean isSyncedWallRunning() {
        return syncedWallRunning;
    }

    public void setSyncedWallRunning(boolean syncedWallRunning) {
        this.syncedWallRunning = syncedWallRunning;
    }

    public boolean isSyncedSlideActive() {
        return syncedSlideActive;
    }

    public void setSyncedSlideActive(boolean syncedSlideActive) {
        this.syncedSlideActive = syncedSlideActive;
    }

    public boolean isSyncedFastDropping() {
        return syncedFastDropping;
    }

    public void setSyncedFastDropping(boolean syncedFastDropping) {
        this.syncedFastDropping = syncedFastDropping;
    }

    public boolean isGroundPoundArmed() {
        return groundPoundArmed;
    }

    public void setGroundPoundArmed(boolean groundPoundArmed) {
        this.groundPoundArmed = groundPoundArmed;
    }

    public float getFlowLevel() {
        return flowLevel;
    }

    public void setFlowLevel(float flowLevel) {
        this.flowLevel = MathHelper.clamp(flowLevel, 0.0F, 1.0F);
    }

    public void addFlow(float delta) {
        setFlowLevel(this.flowLevel + delta);
    }

    public int getHardLandStaggerTicks() {
        return hardLandStaggerTicks;
    }

    public void setHardLandStaggerTicks(int hardLandStaggerTicks) {
        this.hardLandStaggerTicks = Math.max(0, hardLandStaggerTicks);
    }

    public boolean isRouteVisionEnabled() {
        return routeVisionEnabled;
    }

    public void toggleRouteVision() {
        this.routeVisionEnabled = !this.routeVisionEnabled;
    }

    public float getLastAirFallDistance() {
        return lastAirFallDistance;
    }

    public void updateAirFallDistance(float fallDistance) {
        if (fallDistance > this.lastAirFallDistance) {
            this.lastAirFallDistance = fallDistance;
        }
    }

    public void resetAirFallDistance() {
        this.lastAirFallDistance = 0.0F;
    }
}