// Copyright (C) 2026 Kettle Ignition Power Studio (K. I. P. Studio)
// SPDX-License-Identifier: GPL-3.0-only
package com.kipstudio.liquidmobility.mixin;

import com.kipstudio.liquidmobility.client.render.PlayerPoseAnimator;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> {
    @Inject(method = "setupAnim", at = @At("TAIL"))
    public void liquidmobility$injectSetupAnim(
            T entityIn,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (entityIn instanceof PlayerEntity) {
            PlayerPoseAnimator.applyPose((PlayerEntity) entityIn, (PlayerModel<?>) (Object) this, ageInTicks, headPitch);
        }
    }
}