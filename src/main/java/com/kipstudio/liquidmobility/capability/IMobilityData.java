// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.capability;

import com.kipstudio.liquidmobility.core.WallDirection;

public interface IMobilityData {
    boolean hasDoubleJumped();
    void setDoubleJumped(boolean doubleJumped);

    int getSlideTicks();
    void setSlideTicks(int ticks);

    int getSlideKickCooldown();
    void setSlideKickCooldown(int ticks);

    int getDashCooldown();
    void setDashCooldown(int ticks);

    int getDashTicks();
    void setDashTicks(int ticks);

    int getWallRunTicks();
    void setWallRunTicks(int ticks);

    WallDirection getWallDirection();
    void setWallDirection(WallDirection direction);

    boolean isFastDropping();
    void setFastDropping(boolean fastDropping);

    int getGroundPoundCooldown();
    void setGroundPoundCooldown(int ticks);

    int getGroundPoundWindowTicks();
    void setGroundPoundWindowTicks(int ticks);

    void resetAll();
    void tick();
}