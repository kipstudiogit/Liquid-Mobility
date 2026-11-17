// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render.vision;

import net.minecraft.util.math.AxisAlignedBB;

public final class RouteHighlight {
    public enum Category {
        VAULT_LEDGE,
        CLIMB_LEDGE,
        WALL_RUN,
        SOFT_CUSHION
    }

    private final AxisAlignedBB box;
    private final Category category;
    private final double distanceSq;

    public RouteHighlight(AxisAlignedBB box, Category category, double distanceSq) {
        this.box = box;
        this.category = category;
        this.distanceSq = distanceSq;
    }

    public AxisAlignedBB getBox() {
        return box;
    }

    public Category getCategory() {
        return category;
    }

    public double getDistanceSq() {
        return distanceSq;
    }
}