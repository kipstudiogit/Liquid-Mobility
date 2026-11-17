// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.capability;

import com.kipstudio.liquidmobility.core.WallDirection;

public class MobilityData implements IMobilityData {
    private boolean doubleJumped;
    private int slideTicks;
    private int slideKickCooldown;
    private int dashCooldown;
    private int dashTicks;
    private int wallRunTicks;
    private WallDirection wallDirection = WallDirection.NONE;
    private boolean fastDropping;
    private int groundPoundCooldown;
    private int groundPoundWindowTicks;

    @Override
    public boolean hasDoubleJumped() {
        return doubleJumped;
    }

    @Override
    public void setDoubleJumped(boolean doubleJumped) {
        this.doubleJumped = doubleJumped;
    }

    @Override
    public int getSlideTicks() {
        return slideTicks;
    }

    @Override
    public void setSlideTicks(int ticks) {
        this.slideTicks = Math.max(0, ticks);
    }

    @Override
    public int getSlideKickCooldown() {
        return slideKickCooldown;
    }

    @Override
    public void setSlideKickCooldown(int ticks) {
        this.slideKickCooldown = Math.max(0, ticks);
    }

    @Override
    public int getDashCooldown() {
        return dashCooldown;
    }

    @Override
    public void setDashCooldown(int ticks) {
        this.dashCooldown = Math.max(0, ticks);
    }

    @Override
    public int getDashTicks() {
        return dashTicks;
    }

    @Override
    public void setDashTicks(int ticks) {
        this.dashTicks = Math.max(0, ticks);
    }

    @Override
    public int getWallRunTicks() {
        return wallRunTicks;
    }

    @Override
    public void setWallRunTicks(int ticks) {
        this.wallRunTicks = Math.max(0, ticks);
    }

    @Override
    public WallDirection getWallDirection() {
        return wallDirection;
    }

    @Override
    public void setWallDirection(WallDirection direction) {
        this.wallDirection = direction != null ? direction : WallDirection.NONE;
    }

    @Override
    public boolean isFastDropping() {
        return fastDropping;
    }

    @Override
    public void setFastDropping(boolean fastDropping) {
        this.fastDropping = fastDropping;
    }

    @Override
    public int getGroundPoundCooldown() {
        return groundPoundCooldown;
    }

    @Override
    public void setGroundPoundCooldown(int ticks) {
        this.groundPoundCooldown = Math.max(0, ticks);
    }

    @Override
    public int getGroundPoundWindowTicks() {
        return groundPoundWindowTicks;
    }

    @Override
    public void setGroundPoundWindowTicks(int ticks) {
        this.groundPoundWindowTicks = Math.max(0, ticks);
    }

    @Override
    public void resetAll() {
        doubleJumped = false;
        slideTicks = 0;
        slideKickCooldown = 0;
        dashCooldown = 0;
        dashTicks = 0;
        wallRunTicks = 0;
        wallDirection = WallDirection.NONE;
        fastDropping = false;
        groundPoundCooldown = 0;
        groundPoundWindowTicks = 0;
    }

    @Override
    public void tick() {
        if (slideTicks > 0) slideTicks--;
        if (slideKickCooldown > 0) slideKickCooldown--;
        if (dashCooldown > 0) dashCooldown--;
        if (dashTicks > 0) dashTicks--;
        if (wallRunTicks > 0) wallRunTicks--;
        if (groundPoundCooldown > 0) groundPoundCooldown--;
        if (groundPoundWindowTicks > 0) groundPoundWindowTicks--;
    }
}