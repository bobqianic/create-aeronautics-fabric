package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import java.util.function.Function;

public final class TagGen {
    private TagGen() {
    }

    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() { return b -> b; }
    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() { return b -> b; }
    public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() { return b -> b; }

    public static class CreateTagsProvider<T> {
        private final RegistrateTagsProvider<T> provider;
        private final Function<T, Holder<T>> holderGetter;
        public CreateTagsProvider(RegistrateTagsProvider<T> provider, Function<T, Holder<T>> holderGetter) {
            this.provider = provider;
            this.holderGetter = holderGetter;
        }
        public CreateTagAppender<T> tag(TagKey<T> key) {
            return new CreateTagAppender<>(provider.addTag(key), provider.registry(), holderGetter);
        }
    }

    public static class CreateTagAppender<T> {
        private final TagAppender<ResourceKey<T>, T> delegate;
        private final ResourceKey<? extends Registry<T>> registry;
        private final Function<T, Holder<T>> holderGetter;

        private CreateTagAppender(TagAppender<ResourceKey<T>, T> delegate,
                                  ResourceKey<? extends Registry<T>> registry,
                                  Function<T, Holder<T>> holderGetter) {
            this.delegate = delegate;
            this.registry = registry;
            this.holderGetter = holderGetter;
        }

        @SafeVarargs
        public final CreateTagAppender<T> add(T... values) {
            for (T value : values) {
                delegate.add(holderGetter.apply(value).unwrapKey().orElseThrow());
            }
            return this;
        }

        public CreateTagAppender<T> addOptional(ResourceLocation id) {
            delegate.addOptional(ResourceKey.create(registry, id));
            return this;
        }

        public CreateTagAppender<T> addTag(TagKey<T> tag) {
            delegate.addTag(tag);
            return this;
        }

        public CreateTagAppender<T> addOptionalTag(TagKey<T> tag) {
            delegate.addOptionalTag(tag);
            return this;
        }
    }
}
