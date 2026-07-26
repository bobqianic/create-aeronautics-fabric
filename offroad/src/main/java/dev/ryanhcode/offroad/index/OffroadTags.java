package dev.ryanhcode.offroad.index;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.ryanhcode.offroad.Offroad;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class OffroadTags {
	public static void addGenerators() {
		Offroad.getRegistrate().addDataGenerator(ProviderType.BLOCK_TAGS, BlockTags::genBlockTags);
		Offroad.getRegistrate().addDataGenerator(ProviderType.ITEM_TAGS, ItemTags::genItemTags);
	}

	public static class BlockTags {
		private static TagKey<Block> create(final String path) {
			return TagKey.create(Registries.BLOCK, Offroad.path(path));
		}

		private static void genBlockTags(final RegistrateTagsProvider<Block> provIn) {
			final TagGen.CreateTagsProvider<Block> prov = new TagGen.CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);
		}
	}

	public static class ItemTags {
		private static TagKey<Item> create(final String path) {
			return TagKey.create(Registries.ITEM, Offroad.path(path));
		}

		public static void genItemTags(final RegistrateItemTagsProvider provIn) {
			final TagGen.CreateTagsProvider<Item> prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);
		}
	}
}
