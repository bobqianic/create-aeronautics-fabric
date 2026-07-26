package dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import dev.simulated_team.simulated.multiloader.inventory.ContainerSlot;
import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MountedPotatoCannonInventory extends SingleSlotContainer {

	private final MountedPotatoCannonBlockEntity be;

	private PotatoCannonProjectileType cachedProjectileType;

	public MountedPotatoCannonInventory(final MountedPotatoCannonBlockEntity be) {
		super(16);

		this.be = be;
	}

	@Override
	public void setChanged() {
		this.be.notifyUpdate();
	}

	@Override
	public void onStackItemChange(final ContainerSlot slot, final ItemStack oldSlotStack, final ItemStack newSlotStack) {
		super.onStackItemChange(slot, oldSlotStack, newSlotStack);

		//if the item type changes, we want to update our cached projectile type
		if (oldSlotStack.getItem() != newSlotStack.getItem()) {
			this.updateCachedType(this.be.getLevel().registryAccess(), newSlotStack);
			this.be.resetAndUpdate();
		}
	}

	public void updateCachedType(final HolderLookup.Provider registries, final ItemStack itemStack) {
		// why does PotatoCannonProjectileType.getTypeForItem() requires RegistryAccess specifically
		this.cachedProjectileType = registries.lookupOrThrow(CreateRegistryKeys.POTATO_PROJECTILE_TYPE)
				.listElements()
				.filter(ref -> ref.value().items().contains(itemStack.getItem().builtInRegistryHolder()))
				.findFirst().map(Holder.Reference::value).orElse(null);
	}

	@Nullable
	public PotatoCannonItem.Ammo getAmmo() {
		final ItemStack currentStack = this.getItem(0);
		if (this.cachedProjectileType != null) {
			return new PotatoCannonItem.Ammo(currentStack, this.cachedProjectileType);
		}

		return null;
	}

	@Override
	public boolean canInsertItem(final ItemInfoWrapper info) {
		return PotatoCannonProjectileType.getTypeForItem(this.be.getLevel().registryAccess(), info.type()).isPresent();
	}
}
