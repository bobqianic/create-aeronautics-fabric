package dev.ryanhcode.offroad.data;

import com.zurrtum.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.ryanhcode.offroad.Offroad;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class OffroadTags {

    public static void addGenerators() {
        OffroadTags.Blocks.addGenerators();
    }

    public static class Blocks {

        public static final TagKey<Block> BOREHEAD_EFFECTIVE = create("borehead_effective");
        public static final TagKey<Block> BOREHEAD_SUPER_EFFECTIVE = create("borehead_super_effective");

        private static TagKey<Block> create(final String path) {
            return TagKey.create(Registries.BLOCK, Offroad.path(path));
        }

        private static TagKey<Block> create(final String namespace, final String path) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
        }

        protected static void addGenerators() {
            Offroad.getRegistrate().addDataGenerator(ProviderType.BLOCK_TAGS, OffroadTags.Blocks::genBlockTags);
        }

        private static void genBlockTags(final RegistrateTagsProvider<Block> provIn) {
            final TagGen.CreateTagsProvider<Block> prov = new TagGen.CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);
            prov.tag(BOREHEAD_EFFECTIVE)
                    .addTag(create("c", "ores"));
            prov.tag(BOREHEAD_SUPER_EFFECTIVE)
                    .add(net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS);
        }
    }
}
