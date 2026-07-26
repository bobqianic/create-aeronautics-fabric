package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Compatibility facade for the tag handles used by the ported modules. */
public final class AllTags {
    private AllTags() {
    }

    public static TagKey<Item> commonItemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static final class TagRef<T> {
        public final TagKey<T> tag;

        private TagRef(TagKey<T> tag) {
            this.tag = tag;
        }

        @SuppressWarnings("unchecked")
        public boolean matches(ItemStack stack) {
            return stack.is((TagKey<Item>) (TagKey<?>) tag);
        }
    }

    public static final class AllBlockTags {
        public static final TagRef<Block> BRITTLE = block("brittle");
        public static final TagRef<Block> SAFE_NBT = block("safe_nbt");
        public static final TagRef<Block> NON_MOVABLE = block("non_movable");
        public static final TagRef<Block> WINDMILL_SAILS = block("windmill_sails");
        public static final TagRef<Block> FAN_TRANSPARENT = block("fan_transparent");

        private static TagRef<Block> block(String path) {
            return new TagRef<>(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("create", path)));
        }
    }

    public static final class AllItemTags {
        public static final TagRef<Item> CHAIN_RIDEABLE = item("chain_rideable");

        private static TagRef<Item> item(String path) {
            return new TagRef<>(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("create", path)));
        }
    }
}
