package dev.simulated_team.simulated.mixin.creative_tab_sections;

import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.client.sections.SimulatedSection;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.registrate.simulated_tab.SimulatedCreativeTab;
import dev.simulated_team.simulated.service.SimTabService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin {
	@Shadow private static CreativeModeTab selectedTab;

	@Inject(method = "render", at = @At("TAIL"))
	private void simulated$render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick, final CallbackInfo ci) {
		if (selectedTab == SimTabService.INSTANCE.getCreativeTab()) {
			SimulatedCreativeTab.renderBanners((CreativeModeInventoryScreen) (Object) this, guiGraphics, mouseX, mouseY);
		}
	}

	@Inject(method = "getTooltipFromContainerItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;tabs()Ljava/util/List;"))
	private void simulated$getTooltipFromContainerItem(final ItemStack stack, final CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 1) final List<Component> list1, @Local final int i) {
		final ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
		final ResourceLocation id = SimulatedRegistrate.ITEM_TO_SECTION.get(key);
		if(id != null) {
			final SimulatedSection section = SimResourceManagers.SIMULATED_SECTION.get(id);
			if(section != null) {
				list1.add(i, section.title().text().copy().withStyle(ChatFormatting.BLUE));
			}
		}
	}
}
