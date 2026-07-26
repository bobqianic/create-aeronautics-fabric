package dev.eriksonn.aeronautics.mixin.shears_break_envelopes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.Tool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {
	@WrapOperation(method = "createToolProperties", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"))
	private static <E> List<E> aeronautics$createToolProperties(E e1, E e2, E e3, E e4, Operation<List<E>> original) {
		ArrayList<E> newList = new ArrayList<>(original.call(e1, e2, e3, e4));
		newList.add((E) Tool.Rule.overrideSpeed(
				BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK)
						.getOrThrow(AeroTags.BlockTags.ENVELOPE),
				5.0f));
		return newList;
	}
}
