// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.input;

import com.kipstudio.liquidmobility.core.KeybindRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;

public class ClientInputState {
    private boolean jumpDown;
    private boolean wasJumpDown;
    private boolean sneakDown;
    private boolean wasSneakDown;
    private boolean dashDown;
    private boolean wasDashDown;
    private boolean quickTurnDown;
    private boolean wasQuickTurnDown;
    private boolean routeVisionDown;
    private boolean wasRouteVisionDown;
    private float forwardAxis;
    private float strafeAxis;
    private boolean sprintIntent;
    private int sprintGraceTicks;

    public void sample(Minecraft mc, ClientPlayerEntity player) {
        wasJumpDown = jumpDown;
        wasSneakDown = sneakDown;
        wasDashDown = dashDown;
        wasQuickTurnDown = quickTurnDown;
        wasRouteVisionDown = routeVisionDown;

        jumpDown = mc.options.keyJump.isDown();
        sneakDown = mc.options.keyShift.isDown();
        dashDown = KeybindRegistry.DASH_KEY.isDown();
        quickTurnDown = KeybindRegistry.QUICK_TURN_KEY.isDown();
        routeVisionDown = KeybindRegistry.ROUTE_VISION_KEY.isDown();

        forwardAxis = mc.options.keyUp.isDown() ? 1.0F : (mc.options.keyDown.isDown() ? -1.0F : 0.0F);
        strafeAxis = mc.options.keyLeft.isDown() ? 1.0F : (mc.options.keyRight.isDown() ? -1.0F : 0.0F);

        if (player.isSprinting() || (mc.options.keySprint.isDown() && forwardAxis > 0.0F)) {
            sprintIntent = true;
            sprintGraceTicks = 3;
        } else if (forwardAxis > 0.0F && sprintGraceTicks > 0) {
            sprintGraceTicks--;
            sprintIntent = true;
        } else {
            sprintGraceTicks = 0;
            sprintIntent = false;
        }
    }

    public void reset(Minecraft mc) {
        jumpDown = mc.options.keyJump.isDown();
        wasJumpDown = jumpDown;
        sneakDown = mc.options.keyShift.isDown();
        wasSneakDown = sneakDown;
        dashDown = KeybindRegistry.DASH_KEY.isDown();
        wasDashDown = dashDown;
        quickTurnDown = KeybindRegistry.QUICK_TURN_KEY.isDown();
        wasQuickTurnDown = quickTurnDown;
        routeVisionDown = KeybindRegistry.ROUTE_VISION_KEY.isDown();
        wasRouteVisionDown = routeVisionDown;
        forwardAxis = 0.0F;
        strafeAxis = 0.0F;
        sprintIntent = false;
        sprintGraceTicks = 0;
    }

    public boolean isJumpDown() {
        return jumpDown;
    }

    public boolean isJumpJustPressed() {
        return jumpDown && !wasJumpDown;
    }

    public boolean isSneakDown() {
        return sneakDown;
    }

    public boolean isSneakJustPressed() {
        return sneakDown && !wasSneakDown;
    }

    public boolean isDashJustPressed() {
        return dashDown && !wasDashDown;
    }

    public boolean isQuickTurnJustPressed() {
        return quickTurnDown && !wasQuickTurnDown;
    }

    public boolean isRouteVisionJustPressed() {
        return routeVisionDown && !wasRouteVisionDown;
    }

    public float getForwardAxis() {
        return forwardAxis;
    }

    public float getStrafeAxis() {
        return strafeAxis;
    }

    public boolean hasSprintIntent() {
        return sprintIntent;
    }
}