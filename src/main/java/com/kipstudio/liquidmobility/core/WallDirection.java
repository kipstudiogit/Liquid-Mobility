// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.core;

public enum WallDirection {
    NONE((byte) 0),
    LEFT((byte) 1),
    RIGHT((byte) 2),
    FRONT((byte) 3);

    private final byte id;

    WallDirection(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static WallDirection fromId(byte id) {
        for (WallDirection dir : values()) {
            if (dir.id == id) {
                return dir;
            }
        }
        return NONE;
    }
}