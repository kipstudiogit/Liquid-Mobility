// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.core;

public enum JumpActionType {
    DOUBLE_JUMP((byte) 0),
    COYOTE_JUMP((byte) 1),
    WALL_JUMP((byte) 2),
    SLIDE_JUMP((byte) 3),
    VAULT((byte) 4);

    private final byte id;

    JumpActionType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    public static JumpActionType fromId(byte id) {
        for (JumpActionType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return DOUBLE_JUMP;
    }
}