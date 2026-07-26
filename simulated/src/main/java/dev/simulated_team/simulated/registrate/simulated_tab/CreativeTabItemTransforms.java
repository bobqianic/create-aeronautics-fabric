package dev.simulated_team.simulated.registrate.simulated_tab;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class for easy invisibility marking and custom NBT in creative tabs
 *
 * @author Cyvack
 */
public class CreativeTabItemTransforms {

    private static final HashMap<Item, VisibilityType> ITEM_VISIBILITY = new HashMap<>();
    private static final HashMap<Item, Function<Item, ItemStack>> STACK_TRANSFORM = new HashMap<>();

    public static ItemStack applyTransform(final ItemStack item) {
        final Function<Item, ItemStack> transform = STACK_TRANSFORM.get(item.getItem());
        if(transform == null) {
            return item;
        }
        return transform.apply(item.getItem());
    }

    public enum VisibilityType {
        INVISIBLE, SEARCH_ONLY;

        public boolean has(final Item item) {
            return this == ITEM_VISIBILITY.get(item);
        }

        public <B extends Block, R> NonNullUnaryOperator<BlockBuilder<B, R>> applyBlock() {
            return builder -> builder.onRegisterAfter(Registries.ITEM, b -> ITEM_VISIBILITY.put(b.asItem(), this));
        }

        public <B extends Block, R> NonNullUnaryOperator<BlockBuilder<B, R>> conditionalApplyBlock(final Supplier<Boolean> visibiltySup) {
            return builder -> builder.onRegisterAfter(Registries.ITEM, b -> {
                if (visibiltySup.get())
                    ITEM_VISIBILITY.put(b.asItem(), this);
            });
        }

        public <B extends Item, R> NonNullUnaryOperator<ItemBuilder<B, R>> applyItem() {
            return builder -> builder.onRegisterAfter(Registries.ITEM, b -> ITEM_VISIBILITY.put(b.asItem(), this));
        }
    }

    public static <B extends Item, R> NonNullUnaryOperator<ItemBuilder<B, R>> transformItem(final Function<Item, ItemStack> func) {
        return builder -> builder.onRegisterAfter(Registries.ITEM, i -> STACK_TRANSFORM.put(i.asItem(), func));
    }
}
