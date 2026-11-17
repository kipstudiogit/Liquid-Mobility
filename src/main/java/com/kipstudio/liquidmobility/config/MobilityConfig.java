// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MobilityConfig {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue allowInCreativeWhenNotFlying;

        public final ForgeConfigSpec.BooleanValue enableFlowMomentum;
        public final ForgeConfigSpec.DoubleValue flowMaxSpeedBonus;
        public final ForgeConfigSpec.DoubleValue flowBuildRatePerTick;
        public final ForgeConfigSpec.DoubleValue flowDecayRatePerTick;
        public final ForgeConfigSpec.BooleanValue enableHardLandingStagger;
        public final ForgeConfigSpec.DoubleValue hardLandingMinFallDistance;
        public final ForgeConfigSpec.IntValue hardLandingStaggerTicks;
        public final ForgeConfigSpec.BooleanValue enableSoftCushions;

        public final ForgeConfigSpec.BooleanValue enableDoubleJump;
        public final ForgeConfigSpec.DoubleValue doubleJumpVerticalVelocity;
        public final ForgeConfigSpec.DoubleValue doubleJumpHorizontalMultiplier;
        public final ForgeConfigSpec.IntValue coyoteTimeTicks;

        public final ForgeConfigSpec.BooleanValue enableBHop;
        public final ForgeConfigSpec.IntValue bHopWindowTicks;
        public final ForgeConfigSpec.DoubleValue bHopSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue bHopMaxHorizontalSpeed;

        public final ForgeConfigSpec.BooleanValue enableAirStrafe;
        public final ForgeConfigSpec.DoubleValue airStrafeAcceleration;

        public final ForgeConfigSpec.BooleanValue enableWallRun;
        public final ForgeConfigSpec.IntValue wallRunHorizontalDurationTicks;
        public final ForgeConfigSpec.IntValue wallClimbDurationTicks;
        public final ForgeConfigSpec.DoubleValue wallRunSpeed;
        public final ForgeConfigSpec.DoubleValue wallClimbSpeed;
        public final ForgeConfigSpec.DoubleValue wallClingDescentSpeed;
        public final ForgeConfigSpec.DoubleValue wallJumpHorizontalBoost;
        public final ForgeConfigSpec.DoubleValue wallJumpSidePush;
        public final ForgeConfigSpec.DoubleValue wallJumpVerticalBoost;

        public final ForgeConfigSpec.BooleanValue enableSlide;
        public final ForgeConfigSpec.IntValue slideDurationTicks;
        public final ForgeConfigSpec.DoubleValue slideBaseSpeed;
        public final ForgeConfigSpec.DoubleValue slidePeakBoostSpeed;
        public final ForgeConfigSpec.DoubleValue slideKickDamage;
        public final ForgeConfigSpec.DoubleValue slideKickKnockback;
        public final ForgeConfigSpec.IntValue slideKickCooldownTicks;
        public final ForgeConfigSpec.DoubleValue slideJumpHorizontalSpeed;
        public final ForgeConfigSpec.DoubleValue slideJumpVerticalBoost;

        public final ForgeConfigSpec.BooleanValue enableDash;
        public final ForgeConfigSpec.DoubleValue dashSpeed;
        public final ForgeConfigSpec.DoubleValue dashVerticalBoost;
        public final ForgeConfigSpec.IntValue dashCooldownTicks;
        public final ForgeConfigSpec.IntValue dashAnimTicks;

        public final ForgeConfigSpec.BooleanValue enableFastDrop;
        public final ForgeConfigSpec.DoubleValue fastDropVerticalVelocity;
        public final ForgeConfigSpec.DoubleValue groundPoundMinFallDistance;
        public final ForgeConfigSpec.DoubleValue groundPoundRadius;
        public final ForgeConfigSpec.DoubleValue groundPoundDamage;
        public final ForgeConfigSpec.DoubleValue groundPoundKnockback;
        public final ForgeConfigSpec.IntValue groundPoundCooldownTicks;

        public final ForgeConfigSpec.BooleanValue enableFallRoll;
        public final ForgeConfigSpec.DoubleValue rollMinFallDistance;
        public final ForgeConfigSpec.DoubleValue rollFallDamageMultiplier;
        public final ForgeConfigSpec.IntValue rollSlideTicks;

        public final ForgeConfigSpec.BooleanValue enableVault;
        public final ForgeConfigSpec.DoubleValue vaultMinHorizontalSpeed;
        public final ForgeConfigSpec.DoubleValue vaultVerticalBoost;
        public final ForgeConfigSpec.DoubleValue springboardVaultBonus;
        public final ForgeConfigSpec.DoubleValue ledgeVaultVerticalBoost;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            allowInCreativeWhenNotFlying = builder.define("allowInCreativeWhenNotFlying", true);
            builder.pop();

            builder.push("flow_momentum");
            enableFlowMomentum = builder.define("enableFlowMomentum", true);
            flowMaxSpeedBonus = builder.defineInRange("flowMaxSpeedBonus", 0.36D, 0.0D, 1.0D);
            flowBuildRatePerTick = builder.defineInRange("flowBuildRatePerTick", 0.012D, 0.001D, 0.1D);
            flowDecayRatePerTick = builder.defineInRange("flowDecayRatePerTick", 0.025D, 0.001D, 0.2D);
            enableHardLandingStagger = builder.define("enableHardLandingStagger", true);
            hardLandingMinFallDistance = builder.defineInRange("hardLandingMinFallDistance", 5.5D, 3.0D, 20.0D);
            hardLandingStaggerTicks = builder.defineInRange("hardLandingStaggerTicks", 8, 1, 30);
            enableSoftCushions = builder.define("enableSoftCushions", true);
            builder.pop();

            builder.push("jump_and_air");
            enableDoubleJump = builder.define("enableDoubleJump", true);
            doubleJumpVerticalVelocity = builder.defineInRange("doubleJumpVerticalVelocity", 0.45D, 0.1D, 1.5D);
            doubleJumpHorizontalMultiplier = builder.defineInRange("doubleJumpHorizontalMultiplier", 1.1D, 0.5D, 2.0D);
            coyoteTimeTicks = builder.defineInRange("coyoteTimeTicks", 5, 0, 20);
            enableBHop = builder.define("enableBHop", true);
            bHopWindowTicks = builder.defineInRange("bHopWindowTicks", 6, 0, 20);
            bHopSpeedMultiplier = builder.defineInRange("bHopSpeedMultiplier", 1.25D, 1.0D, 2.5D);
            bHopMaxHorizontalSpeed = builder.defineInRange("bHopMaxHorizontalSpeed", 0.95D, 0.2D, 3.0D);
            enableAirStrafe = builder.define("enableAirStrafe", true);
            airStrafeAcceleration = builder.defineInRange("airStrafeAcceleration", 0.015D, 0.0D, 0.2D);
            builder.pop();

            builder.push("wall_mobility");
            enableWallRun = builder.define("enableWallRun", true);
            wallRunHorizontalDurationTicks = builder.defineInRange("wallRunHorizontalDurationTicks", 60, 10, 200);
            wallClimbDurationTicks = builder.defineInRange("wallClimbDurationTicks", 30, 5, 120);
            wallRunSpeed = builder.defineInRange("wallRunSpeed", 0.55D, 0.1D, 2.0D);
            wallClimbSpeed = builder.defineInRange("wallClimbSpeed", 0.35D, 0.1D, 1.5D);
            wallClingDescentSpeed = builder.defineInRange("wallClingDescentSpeed", 0.15D, 0.0D, 1.0D);
            wallJumpHorizontalBoost = builder.defineInRange("wallJumpHorizontalBoost", 0.6D, 0.1D, 2.0D);
            wallJumpSidePush = builder.defineInRange("wallJumpSidePush", 0.5D, 0.1D, 2.0D);
            wallJumpVerticalBoost = builder.defineInRange("wallJumpVerticalBoost", 0.55D, 0.1D, 1.5D);
            builder.pop();

            builder.push("slide");
            enableSlide = builder.define("enableSlide", true);
            slideDurationTicks = builder.defineInRange("slideDurationTicks", 15, 5, 60);
            slideBaseSpeed = builder.defineInRange("slideBaseSpeed", 0.2D, 0.05D, 1.0D);
            slidePeakBoostSpeed = builder.defineInRange("slidePeakBoostSpeed", 0.6D, 0.0D, 2.0D);
            slideKickDamage = builder.defineInRange("slideKickDamage", 4.0D, 0.0D, 40.0D);
            slideKickKnockback = builder.defineInRange("slideKickKnockback", 1.2D, 0.0D, 5.0D);
            slideKickCooldownTicks = builder.defineInRange("slideKickCooldownTicks", 15, 0, 100);
            slideJumpHorizontalSpeed = builder.defineInRange("slideJumpHorizontalSpeed", 1.25D, 0.2D, 3.0D);
            slideJumpVerticalBoost = builder.defineInRange("slideJumpVerticalBoost", 0.5D, 0.1D, 1.5D);
            builder.pop();

            builder.push("dash");
            enableDash = builder.define("enableDash", true);
            dashSpeed = builder.defineInRange("dashSpeed", 1.4D, 0.2D, 4.0D);
            dashVerticalBoost = builder.defineInRange("dashVerticalBoost", 0.15D, 0.0D, 1.0D);
            dashCooldownTicks = builder.defineInRange("dashCooldownTicks", 60, 5, 600);
            dashAnimTicks = builder.defineInRange("dashAnimTicks", 10, 1, 40);
            builder.pop();

            builder.push("fast_drop_and_fall");
            enableFastDrop = builder.define("enableFastDrop", true);
            fastDropVerticalVelocity = builder.defineInRange("fastDropVerticalVelocity", 1.5D, 0.4D, 4.0D);
            groundPoundMinFallDistance = builder.defineInRange("groundPoundMinFallDistance", 3.0D, 1.0D, 20.0D);
            groundPoundRadius = builder.defineInRange("groundPoundRadius", 4.0D, 1.0D, 12.0D);
            groundPoundDamage = builder.defineInRange("groundPoundDamage", 6.0D, 0.0D, 50.0D);
            groundPoundKnockback = builder.defineInRange("groundPoundKnockback", 1.5D, 0.0D, 5.0D);
            groundPoundCooldownTicks = builder.defineInRange("groundPoundCooldownTicks", 20, 0, 200);
            enableFallRoll = builder.define("enableFallRoll", true);
            rollMinFallDistance = builder.defineInRange("rollMinFallDistance", 3.0D, 1.5D, 20.0D);
            rollFallDamageMultiplier = builder.defineInRange("rollFallDamageMultiplier", 0.0D, 0.0D, 1.0D);
            rollSlideTicks = builder.defineInRange("rollSlideTicks", 10, 1, 40);
            builder.pop();

            builder.push("vault");
            enableVault = builder.define("enableVault", true);
            vaultMinHorizontalSpeed = builder.defineInRange("vaultMinHorizontalSpeed", 0.15D, 0.05D, 1.0D);
            vaultVerticalBoost = builder.defineInRange("vaultVerticalBoost", 0.58D, 0.2D, 1.5D);
            springboardVaultBonus = builder.defineInRange("springboardVaultBonus", 0.14D, 0.0D, 0.6D);
            ledgeVaultVerticalBoost = builder.defineInRange("ledgeVaultVerticalBoost", 0.55D, 0.2D, 1.5D);
            builder.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue enableCameraRoll;
        public final ForgeConfigSpec.DoubleValue wallRunCameraRoll;
        public final ForgeConfigSpec.DoubleValue airStrafeCameraRoll;
        public final ForgeConfigSpec.BooleanValue enableCameraPitch;
        public final ForgeConfigSpec.BooleanValue enableLandingDip;
        public final ForgeConfigSpec.BooleanValue enableCameraShake;
        public final ForgeConfigSpec.BooleanValue enableDoubleJumpCameraKick;
        public final ForgeConfigSpec.BooleanValue enableSkillRollCameraFlip;
        public final ForgeConfigSpec.BooleanValue enableDynamicFov;
        public final ForgeConfigSpec.DoubleValue maxFovOffset;

        public final ForgeConfigSpec.BooleanValue enableHandAnimations;
        public final ForgeConfigSpec.BooleanValue enableHandSway;
        public final ForgeConfigSpec.BooleanValue enableFirstPersonParkourSwing;
        public final ForgeConfigSpec.BooleanValue enableBodyLean;
        public final ForgeConfigSpec.BooleanValue enableParkourPlayerPose;
        public final ForgeConfigSpec.BooleanValue enableDoubleJumpFlip;
        public final ForgeConfigSpec.BooleanValue enableAirbornePose;
        public final ForgeConfigSpec.BooleanValue enableLandingSquash;

        public final ForgeConfigSpec.BooleanValue enableRouteVision;
        public final ForgeConfigSpec.IntValue routeVisionRange;
        public final ForgeConfigSpec.DoubleValue routeVisionOpacity;

        public final ForgeConfigSpec.BooleanValue enableParkourHud;
        public final ForgeConfigSpec.BooleanValue enableSpeedLines;
        public final ForgeConfigSpec.BooleanValue enableFlowBar;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("camera");
            enableCameraRoll = builder.define("enableCameraRoll", true);
            wallRunCameraRoll = builder.defineInRange("wallRunCameraRoll", 15.0D, 0.0D, 45.0D);
            airStrafeCameraRoll = builder.defineInRange("airStrafeCameraRoll", 3.5D, 0.0D, 20.0D);
            enableCameraPitch = builder.define("enableCameraPitch", true);
            enableLandingDip = builder.define("enableLandingDip", true);
            enableCameraShake = builder.define("enableCameraShake", true);
            enableDoubleJumpCameraKick = builder.define("enableDoubleJumpCameraKick", true);
            enableSkillRollCameraFlip = builder.define("enableSkillRollCameraFlip", true);
            enableDynamicFov = builder.define("enableDynamicFov", true);
            maxFovOffset = builder.defineInRange("maxFovOffset", 35.0D, 0.0D, 60.0D);
            builder.pop();

            builder.push("rendering");
            enableHandAnimations = builder.define("enableHandAnimations", true);
            enableHandSway = builder.define("enableHandSway", true);
            enableFirstPersonParkourSwing = builder.define("enableFirstPersonParkourSwing", true);
            enableBodyLean = builder.define("enableBodyLean", true);
            enableParkourPlayerPose = builder.define("enableParkourPlayerPose", true);
            enableDoubleJumpFlip = builder.define("enableDoubleJumpFlip", true);
            enableAirbornePose = builder.define("enableAirbornePose", true);
            enableLandingSquash = builder.define("enableLandingSquash", true);
            builder.pop();

            builder.push("route_vision");
            enableRouteVision = builder.define("enableRouteVision", true);
            routeVisionRange = builder.defineInRange("routeVisionRange", 12, 4, 24);
            routeVisionOpacity = builder.defineInRange("routeVisionOpacity", 0.78D, 0.1D, 1.0D);
            builder.pop();

            builder.push("hud");
            enableParkourHud = builder.define("enableParkourHud", true);
            enableSpeedLines = builder.define("enableSpeedLines", true);
            enableFlowBar = builder.define("enableFlowBar", true);
            builder.pop();
        }
    }
}