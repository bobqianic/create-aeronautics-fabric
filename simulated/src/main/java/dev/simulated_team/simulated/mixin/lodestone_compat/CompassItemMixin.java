package dev.simulated_team.simulated.mixin.lodestone_compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability.LodestoneTrackingMap;
import dev.simulated_team.simulated.index.SimDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin extends Item {
	public CompassItemMixin(final Properties properties) {
		super(properties);
	}

	@Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
	private void simulated$checkID(final ItemStack stack, final ServerLevel level, final Entity entity, final EquipmentSlot slot, final CallbackInfo ci) {
		if (stack.has(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER)) {
			final UUID trackerID = stack.get(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER);
			final LodestoneTrackingMap map = LodestoneTrackingMap.getOrLoad(level);
			if (map != null && entity instanceof final ServerPlayer sp) {
				map.sendUpdateForPlayer(trackerID, sp);
			}
		}
	}

	@WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	public <T> T simulated$setLodestoneData(final ItemStack instance, final DataComponentType<? super T> component, final T value, final Operation<T> original, @Local(argsOnly = true) final UseOnContext context) {
		final BlockPos pos = context.getClickedPos();
		final LodestoneTrackingMap map = LodestoneTrackingMap.getOrLoad(context.getLevel());
		if (map != null) {
			final UUID uuid = map.addOrGetLodestoneTrackingPoint(pos);
			if (uuid != null) {
				instance.set(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER, uuid);
			}
		}

		return original.call(instance, component, value);
	}
}
