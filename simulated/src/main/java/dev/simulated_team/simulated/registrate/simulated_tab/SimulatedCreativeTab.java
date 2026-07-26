package dev.simulated_team.simulated.registrate.simulated_tab;

import com.mojang.blaze3d.platform.Window;
import dev.simulated_team.simulated.client.sections.SimulatedSection;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.mixin.accessor.CreativeModeInventoryScreenAccessor;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.color.Colorc;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SimulatedCreativeTab {
	public static int CURRENT_ROW = 0;
	public static final Object2IntOpenHashMap<ResourceLocation> SECTION_Y_VALUES = new Object2IntOpenHashMap<>();

	public static void renderBanners(final CreativeModeInventoryScreen screen, final GuiGraphics graphics, int mouseX, int mouseY) {
		final Matrix3x2fStack ps = graphics.pose();
		ps.pushMatrix();
		int left = ((CreativeModeInventoryScreenAccessor) screen).getLeftPos() + 8;
		int top = ((CreativeModeInventoryScreenAccessor) screen).getTopPos() + 17;
		ps.translate(left, top);

		final List<SimulatedSection> sections = SimResourceManagers.SIMULATED_SECTION.sortedEntries();

		for (final SimulatedSection section : sections) {
			ResourceLocation id = SimResourceManagers.SIMULATED_SECTION.getId(section);
			int yValue = SECTION_Y_VALUES.getInt(id);
			final int sectionRow = (yValue - CURRENT_ROW);
			if(sectionRow < 0 || sectionRow > 4) continue;

			Font font = Minecraft.getInstance().font;
			int x = 0;
			int y = sectionRow * 18;
			int w = 162;
			int h = 18;

			ResourceLocation bannerTexture = section.sprite();

			if(section.animateOnHover()) {
				boolean isHovering =
						mouseX >= left + x &&
						mouseX <= left + x + w &&
						mouseY >= top + y &&
						mouseY <= top + y + h;
				setPlaying(bannerTexture, isHovering);
			}

			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bannerTexture, x, y, w, h);

			Component text = section.title().text();
			int textWidth = font.width(text);

			Colorc background = section.title().background();
			graphics.fill(x + 2, y + 2, x + textWidth + 8, y + h - 2, background.argb());

			Colorc light = section.title().color();
			Colorc dark = section.title().secondaryColor()
					.orElse(light.darken(0.2f, new Color()));
			drawAuraText(graphics, text, dark.argb(), light.argb(), x + 5, y + 5);
		}
		ps.popMatrix();
	}

	public static void drawAuraText(GuiGraphics graphics, Component text, int color1, int color2, int x, int y) {
		Font font = Minecraft.getInstance().font;
		graphics.drawString(font, text, x, y, color1, true);

		graphics.enableScissor(x, y, x + font.width(text), y + (int) (font.lineHeight / 1.8f));

		graphics.drawString(font, text, x, y, color2, false);

		graphics.disableScissor();

	}

	public static void processItems(final Consumer<ItemStack> displayItems, final Consumer<ItemStack> searchItems) {
		final Map<SimulatedSection, List<ItemStack>> sectionMap = new HashMap<>();
		final List<Item> tabItems = SimulatedRegistrate.TAB_ITEMS.stream()
				.map(item -> item.get())
				.sorted(Comparator.comparingInt(item -> item instanceof BlockItem ? 0 : 1))
				.toList();

		for (final Item item : tabItems) {
			final ItemStack stack = item.getDefaultInstance();

			final ResourceLocation sectionId = SimulatedRegistrate.sectionOf(item);
			if(sectionId == null)
				continue;

			final SimulatedSection section = SimResourceManagers.SIMULATED_SECTION.get(sectionId);
			sectionMap.computeIfAbsent(section, (s) -> new LinkedList<>()).add(stack);
		}

		for (int i = 0; i < 9; i++) {
			displayItems.accept(ItemStack.EMPTY);
		}

		int y = 0;
		final List<SimulatedSection> sectionKeys = sectionMap.keySet().stream().sorted().toList();
		for (final SimulatedSection key : sectionKeys) {

			int itemCount = 0;
			final List<ItemStack> sectionItems = sectionMap.get(key);
			for (ItemStack item : sectionItems) {
				item = CreativeTabItemTransforms.applyTransform(item);

				if(CreativeTabItemTransforms.VisibilityType.SEARCH_ONLY.has(item.getItem())) {
					searchItems.accept(item);
				} else if(!CreativeTabItemTransforms.VisibilityType.INVISIBLE.has(item.getItem())) {
					displayItems.accept(item);
					searchItems.accept(item);
					itemCount++;
				}
			}

			ResourceLocation id = SimResourceManagers.SIMULATED_SECTION.getId(key);
			SECTION_Y_VALUES.put(id, y);
			final int rowCount = (int) Math.ceil(itemCount / 9.0f);
			y += rowCount + 1;

			if(key != null && key.equals(sectionKeys.getLast())) {
				break;
			}

			int padding = 9 - itemCount % 9;
			if(padding < 9) {
				padding += 9;
			}
			for (int i = 0; i < padding; i++) {
				displayItems.accept(ItemStack.EMPTY);
			}
		}
	}

    public static void setPlaying(ResourceLocation resourceLocation, boolean playing) {
        // Animated creative-tab sprites are left to the vanilla ticker on Fabric.
    }

}
