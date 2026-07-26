package dev.simulated_team.simulated.mixin.silence_jei;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.service.SimTabService;
import mezz.jei.library.plugins.vanilla.ingredients.ItemStackListFactory;
import net.minecraft.world.item.CreativeModeTab;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = ItemStackListFactory.class, remap = false)
public class ItemStackListFactoryMixin {

	@WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 0))
	private static void simulated$error(final Logger instance, final String string, final Object o, final Object o1, final Operation<Void> original, @Local final CreativeModeTab tab) {
		if(tab != SimTabService.INSTANCE.getCreativeTab()) {
			original.call(instance, string, o, o1);
		}
	}

	@WrapOperation(method = "addFromTab", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 0))
	private static void simulated$error(final Logger instance, final String string, final Object o, final Object o1, final Object o2, final Operation<Void> original, @Local(argsOnly = true) final CreativeModeTab tab) {
		if(tab != SimTabService.INSTANCE.getCreativeTab()) {
			original.call(instance, string, o, o1, o2);
		}
	}
}
