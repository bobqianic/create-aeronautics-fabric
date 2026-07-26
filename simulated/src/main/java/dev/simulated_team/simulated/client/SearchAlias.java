package dev.simulated_team.simulated.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.util.SimCodecUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SearchAlias(List<String> terms, List<ExtraCodecs.TagOrElementLocation> results) {
    public static final Codec<List<String>> TERM_CODEC = SimCodecUtil.withAlternative(
            Codec.STRING.xmap(List::of, List::getFirst),
            Codec.STRING.listOf()
    );

    public static final Codec<SearchAlias> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TERM_CODEC.fieldOf("term").forGetter(SearchAlias::terms),
            ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().fieldOf("results").forGetter(SearchAlias::results)
    ).apply(instance, SearchAlias::new));

    public static List<String> getAliases(ItemStack stack) {
        List<String> aliases = new ArrayList<>();
        for (SearchAlias entry : SimResourceManagers.SEARCH_ALIAS.entries()) {
            if(entry.match(stack)) {
                aliases.addAll(entry.terms());
            }
        }
        return aliases;
    }

    public boolean match(ItemStack stack) {
        for (ExtraCodecs.TagOrElementLocation result : this.results()) {
            if(result.tag()) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, result.id());
                if(stack.is(tag)) {
                    return true;
                }
            } else {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if(key.equals(result.id())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>();
        for (ExtraCodecs.TagOrElementLocation result : this.results()) {
            if(result.tag()) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, result.id());
                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    if(holder.isBound()) {
                        list.add(holder.value().getDefaultInstance());
                    }
                }
            } else {
                BuiltInRegistries.ITEM.getOptional(result.id()).ifPresent(item -> list.add(item.getDefaultInstance()));
            }
        }
        return list;
    }

}
