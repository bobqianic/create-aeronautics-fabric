package com.zurrtum.create.foundation.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface CustomArmPoseItem {
    default HumanoidModel.ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        return HumanoidModel.ArmPose.EMPTY;
    }
}
