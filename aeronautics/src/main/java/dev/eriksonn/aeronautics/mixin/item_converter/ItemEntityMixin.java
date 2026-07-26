package dev.eriksonn.aeronautics.mixin.item_converter;

import dev.eriksonn.aeronautics.content.components.Converter;
import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.eriksonn.aeronautics.index.AeroDataComponents;
import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void aeronautics$tick(final CallbackInfo ci) {
		final ItemEntity entity = (ItemEntity) (Object) this;
		if(entity.getItem().isEmpty() || entity.isRemoved()) return;

		final ItemStack item = entity.getItem();
		final Level level = entity.level();
		if(item.has(AeroDataComponents.CONVERTER)) {
			final Converter converter = item.get(AeroDataComponents.CONVERTER);
			Converter.tick(level, entity, item, converter);
		}

		if(level.dimension().equals(Level.OVERWORLD) && item.is(AeroTags.ItemTags.CONVERTS_TO_CLOUD_SKIPPER)) {
			// magic cloud number, i have no idea where its actually defined
			if(entity.getY() >= 192 && entity.getY() <= 196 && !item.has(AeroDataComponents.CONVERTER)) {
				final DataComponentPatch patch = DataComponentPatch.builder()
						.set(AeroDataComponents.CONVERTER, Converter.cloudSkipper())
						.set(AeroDataComponents.LEVITATING, Levitating.DEFAULT)
						.build();
				item.applyComponents(patch);
				entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5f));
			}
		}
	}
}
