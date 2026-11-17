// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

public class FrameDeltaTracker {
    private long lastNanos;

    public float sampleDeltaSeconds() {
        long now = System.nanoTime();
        if (lastNanos == 0L) {
            lastNanos = now;
            return 1.0F / 60.0F;
        }
        float dt = (now - lastNanos) * 1.0E-9F;
        lastNanos = now;
        if (dt < 0.0001F) {
            return 0.0001F;
        }
        return Math.min(dt, 0.05F);
    }

    public void reset() {
        lastNanos = 0L;
    }

    public static float expLerp(float current, float target, float speed, float dt) {
        float alpha = 1.0F - (float) Math.exp(-speed * dt);
        return current + (target - current) * alpha;
    }
}