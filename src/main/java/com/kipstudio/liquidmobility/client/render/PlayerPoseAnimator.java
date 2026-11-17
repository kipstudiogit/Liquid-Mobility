// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.client.render;

import com.kipstudio.liquidmobility.capability.IMobilityData;
import com.kipstudio.liquidmobility.capability.MobilityCapability;
import com.kipstudio.liquidmobility.client.render.anim.PlayerAnimState;
import com.kipstudio.liquidmobility.client.render.anim.PlayerAnimStateRegistry;
import com.kipstudio.liquidmobility.config.MobilityConfig;
import com.kipstudio.liquidmobility.core.WallDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class PlayerPoseAnimator {
    private PlayerPoseAnimator() {
    }

    @SuppressWarnings("null")
    public static void applyPose(PlayerEntity player, PlayerModel<?> model, float ageInTicks, float headPitch) {
        if (!MobilityConfig.CLIENT.enableParkourPlayerPose.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.screen != null) {
            return;
        }

        PlayerAnimState state = PlayerAnimStateRegistry.getUpdatedState(player);
        IMobilityData cap = MobilityCapability.get(player);
        if (state == null || cap == null) {
            return;
        }

        float headX = model.head.xRot;
        float bodyX = model.body.xRot;
        float bodyY = model.body.yRot;

        float lArmX = model.leftArm.xRot;
        float lArmY = model.leftArm.yRot;
        float lArmZ = model.leftArm.zRot;

        float rArmX = model.rightArm.xRot;
        float rArmY = model.rightArm.yRot;
        float rArmZ = model.rightArm.zRot;

        float lLegX = model.leftLeg.xRot;
        float lLegY = model.leftLeg.yRot;
        float lLegZ = model.leftLeg.zRot;

        float rLegX = model.rightLeg.xRot;
        float rLegY = model.rightLeg.yRot;
        float rLegZ = model.rightLeg.zRot;

        boolean modified = false;

        float airborneW = MobilityConfig.CLIENT.enableAirbornePose.get() ? state.getAirborneWeight() : 0.0F;
        if (airborneW > 0.005F) {
            lArmX = MathHelper.lerp(airborneW, lArmX, -0.35F);
            lArmZ = MathHelper.lerp(airborneW, lArmZ, -0.32F);
            rArmX = MathHelper.lerp(airborneW, rArmX, -0.55F);
            rArmZ = MathHelper.lerp(airborneW, rArmZ, 0.35F);
            lLegX = MathHelper.lerp(airborneW, lLegX, -0.35F);
            rLegX = MathHelper.lerp(airborneW, rLegX, 0.25F);
            modified = true;
        }

        float squash = Math.max(0.0F, state.getLandingSquash());
        if (squash > 0.005F) {
            float w = MathHelper.clamp(squash, 0.0F, 1.0F);
            bodyX = MathHelper.lerp(w, bodyX, 0.24F);
            lLegX = MathHelper.lerp(w, lLegX, -0.55F);
            rLegX = MathHelper.lerp(w, rLegX, 0.45F);
            lArmX = MathHelper.lerp(w, lArmX, 0.35F);
            rArmX = MathHelper.lerp(w, rArmX, 0.35F);
            lArmZ = MathHelper.lerp(w, lArmZ, -0.62F);
            rArmZ = MathHelper.lerp(w, rArmZ, 0.62F);
            modified = true;
        }

        float clingW = state.getWallClingWeight();
        if (clingW > 0.005F) {
            WallDirection dir = cap.getWallDirection();
            if (dir == WallDirection.LEFT) {
                lArmX = MathHelper.lerp(clingW, lArmX, -1.85F);
                lArmZ = MathHelper.lerp(clingW, lArmZ, -0.55F);
                rArmX = MathHelper.lerp(clingW, rArmX, 0.25F);
                rArmZ = MathHelper.lerp(clingW, rArmZ, 0.45F);
            } else if (dir == WallDirection.RIGHT) {
                rArmX = MathHelper.lerp(clingW, rArmX, -1.85F);
                rArmZ = MathHelper.lerp(clingW, rArmZ, 0.55F);
                lArmX = MathHelper.lerp(clingW, lArmX, 0.25F);
                lArmZ = MathHelper.lerp(clingW, lArmZ, -0.45F);
            } else {
                lArmX = MathHelper.lerp(clingW, lArmX, -1.85F);
                rArmX = MathHelper.lerp(clingW, rArmX, -1.85F);
                lArmZ = MathHelper.lerp(clingW, lArmZ, -0.25F);
                rArmZ = MathHelper.lerp(clingW, rArmZ, 0.25F);
            }
            lLegX = MathHelper.lerp(clingW, lLegX, -0.75F);
            rLegX = MathHelper.lerp(clingW, rLegX, -0.45F);
            modified = true;
        }

        float wallLeftW = state.getWallLeftWeight();
        if (wallLeftW > 0.005F) {
            float stride = MathHelper.sin(state.getStridePhase());
            lArmX = MathHelper.lerp(wallLeftW, lArmX, -0.85F);
            lArmZ = MathHelper.lerp(wallLeftW, lArmZ, -0.78F);
            rArmX = MathHelper.lerp(wallLeftW, rArmX, 0.45F - stride * 0.55F);
            rArmZ = MathHelper.lerp(wallLeftW, rArmZ, 0.65F);
            lLegX = MathHelper.lerp(wallLeftW, lLegX, stride * 0.95F);
            rLegX = MathHelper.lerp(wallLeftW, rLegX, -stride * 0.95F);
            modified = true;
        }

        float wallRightW = state.getWallRightWeight();
        if (wallRightW > 0.005F) {
            float stride = MathHelper.sin(state.getStridePhase());
            rArmX = MathHelper.lerp(wallRightW, rArmX, -0.85F);
            rArmZ = MathHelper.lerp(wallRightW, rArmZ, 0.78F);
            lArmX = MathHelper.lerp(wallRightW, lArmX, 0.45F + stride * 0.55F);
            lArmZ = MathHelper.lerp(wallRightW, lArmZ, -0.65F);
            lLegX = MathHelper.lerp(wallRightW, lLegX, stride * 0.95F);
            rLegX = MathHelper.lerp(wallRightW, rLegX, -stride * 0.95F);
            modified = true;
        }

        float wallClimbW = state.getWallClimbWeight();
        if (wallClimbW > 0.005F) {
            float armSwing = MathHelper.sin(state.getStridePhase() * 0.95F);
            rArmX = MathHelper.lerp(wallClimbW, rArmX, -1.75F + armSwing * 0.72F);
            lArmX = MathHelper.lerp(wallClimbW, lArmX, -1.75F - armSwing * 0.72F);
            rArmZ = MathHelper.lerp(wallClimbW, rArmZ, 0.22F);
            lArmZ = MathHelper.lerp(wallClimbW, lArmZ, -0.22F);
            rLegX = MathHelper.lerp(wallClimbW, rLegX, -0.75F - armSwing * 0.55F);
            lLegX = MathHelper.lerp(wallClimbW, lLegX, -0.75F + armSwing * 0.55F);
            bodyY = MathHelper.lerp(wallClimbW, bodyY, armSwing * 0.22F);
            modified = true;
        }

        float dashW = state.getDashWeight();
        if (dashW > 0.005F) {
            lArmX = MathHelper.lerp(dashW, lArmX, 1.15F);
            rArmX = MathHelper.lerp(dashW, rArmX, 1.15F);
            lArmZ = MathHelper.lerp(dashW, lArmZ, -0.42F);
            rArmZ = MathHelper.lerp(dashW, rArmZ, 0.42F);
            lLegX = MathHelper.lerp(dashW, lLegX, -0.75F);
            rLegX = MathHelper.lerp(dashW, rLegX, 0.85F);
            headX = MathHelper.lerp(dashW, headX, -0.35F);
            modified = true;
        }

        float slideW = state.getSlideWeight();
        if (slideW > 0.005F) {
            lLegX = MathHelper.lerp(slideW, lLegX, -1.35F);
            lLegY = MathHelper.lerp(slideW, lLegY, 0.15F);
            lLegZ = MathHelper.lerp(slideW, lLegZ, -0.08F);
            rLegX = MathHelper.lerp(slideW, rLegX, 1.15F);
            rLegZ = MathHelper.lerp(slideW, rLegZ, 0.25F);
            rArmX = MathHelper.lerp(slideW, rArmX, -0.65F);
            rArmZ = MathHelper.lerp(slideW, rArmZ, 0.45F);
            lArmX = MathHelper.lerp(slideW, lArmX, 0.95F);
            lArmZ = MathHelper.lerp(slideW, lArmZ, -0.55F);
            headX = MathHelper.lerp(slideW, headX, (headPitch * ((float) Math.PI / 180.0F)) + 0.55F);
            modified = true;
        }

        float diveW = state.getDiveWeight();
        if (diveW > 0.005F) {
            lArmX = MathHelper.lerp(diveW, lArmX, -2.65F);
            rArmX = MathHelper.lerp(diveW, rArmX, -2.65F);
            lArmZ = MathHelper.lerp(diveW, lArmZ, -0.35F);
            rArmZ = MathHelper.lerp(diveW, rArmZ, 0.35F);
            lLegX = MathHelper.lerp(diveW, lLegX, 0.25F);
            rLegX = MathHelper.lerp(diveW, rLegX, 0.25F);
            lLegZ = MathHelper.lerp(diveW, lLegZ, -0.12F);
            rLegZ = MathHelper.lerp(diveW, rLegZ, 0.12F);
            headX = MathHelper.lerp(diveW, headX, -0.65F);
            modified = true;
        }

        float flipW = state.getFlipWeight();
        if (flipW > 0.005F) {
            lLegX = MathHelper.lerp(flipW, lLegX, -1.15F);
            rLegX = MathHelper.lerp(flipW, rLegX, -1.05F);
            lArmX = MathHelper.lerp(flipW, lArmX, -1.25F);
            rArmX = MathHelper.lerp(flipW, rArmX, -1.25F);
            lArmZ = MathHelper.lerp(flipW, lArmZ, -0.25F);
            rArmZ = MathHelper.lerp(flipW, rArmZ, 0.25F);
            headX = MathHelper.lerp(flipW, headX, 0.55F);
            modified = true;
        }

        float rollW = state.getSkillRollWeight();
        if (rollW > 0.005F) {
            lLegX = MathHelper.lerp(rollW, lLegX, -1.28F);
            rLegX = MathHelper.lerp(rollW, rLegX, -1.18F);
            lArmX = MathHelper.lerp(rollW, lArmX, -1.42F);
            rArmX = MathHelper.lerp(rollW, rArmX, -1.42F);
            lArmZ = MathHelper.lerp(rollW, lArmZ, -0.18F);
            rArmZ = MathHelper.lerp(rollW, rArmZ, 0.18F);
            headX = MathHelper.lerp(rollW, headX, 0.68F);
            modified = true;
        }

        if (modified) {
            model.head.xRot = headX;
            model.body.xRot = bodyX;
            model.body.yRot = bodyY;

            model.leftArm.xRot = lArmX;
            model.leftArm.yRot = lArmY;
            model.leftArm.zRot = lArmZ;

            model.rightArm.xRot = rArmX;
            model.rightArm.yRot = rArmY;
            model.rightArm.zRot = rArmZ;

            model.leftLeg.xRot = lLegX;
            model.leftLeg.yRot = lLegY;
            model.leftLeg.zRot = lLegZ;

            model.rightLeg.xRot = rLegX;
            model.rightLeg.yRot = rLegY;
            model.rightLeg.zRot = rLegZ;

            model.leftSleeve.copyFrom(model.leftArm);
            model.rightSleeve.copyFrom(model.rightArm);
            model.leftPants.copyFrom(model.leftLeg);
            model.rightPants.copyFrom(model.rightLeg);
            model.jacket.copyFrom(model.body);
            model.hat.copyFrom(model.head);
        }
    }
}