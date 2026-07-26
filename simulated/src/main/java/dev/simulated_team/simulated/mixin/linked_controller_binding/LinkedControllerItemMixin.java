package dev.simulated_team.simulated.mixin.linked_controller_binding;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LinkedControllerItem.class)
public abstract class LinkedControllerItemMixin {
	@WrapOperation(method = "onItemUseFirst", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 1))
	private static boolean simulated$onItemUseFirst(final BlockState state, final Block block, final Operation<Boolean> original) {
		return original.call(state, block) || SimBlocks.MODULATING_LINKED_RECEIVER.has(state) || SimBlocks.DIRECTIONAL_LINKED_RECEIVER.has(state);
	}
}
