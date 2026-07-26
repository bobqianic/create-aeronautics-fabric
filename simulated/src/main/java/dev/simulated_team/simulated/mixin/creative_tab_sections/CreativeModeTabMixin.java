package dev.simulated_team.simulated.mixin.creative_tab_sections;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.simulated_team.simulated.registrate.simulated_tab.SimulatedCreativeTab;
import dev.simulated_team.simulated.service.SimTabService;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {

	@Shadow private Collection<ItemStack> displayItems;

	@Shadow private Set<ItemStack> displayItemsSearchTab;

	@WrapMethod(method = "buildContents")
	private void simulated$buildContents(final CreativeModeTab.ItemDisplayParameters parameters, final Operation<Void> original) {
		final CreativeModeTab self = (CreativeModeTab) (Object) this;
		if(self == SimTabService.INSTANCE.getCreativeTab()) {
			final List<ItemStack> displayItems = new LinkedList<>();
			final Set<ItemStack> searchItems = new LinkedHashSet<>();
			SimulatedCreativeTab.processItems(displayItems::add, searchItems::add);
			this.displayItems = displayItems;
			this.displayItemsSearchTab = searchItems;
			return;
		}
		original.call(parameters);
	}

}
