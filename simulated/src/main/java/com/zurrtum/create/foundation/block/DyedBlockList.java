package com.zurrtum.create.foundation.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DyedBlockList<T extends Block> implements Iterable<T> {
    private final Map<DyeColor, Supplier<? extends T>> entries = new EnumMap<>(DyeColor.class);

    public DyedBlockList(Function<DyeColor, ? extends Supplier<? extends T>> factory) {
        for (DyeColor color : DyeColor.values()) {
            Supplier<? extends T> supplier = factory.apply(color);
            if (supplier != null) entries.put(color, supplier);
        }
    }

    public T get(DyeColor color) {
        Supplier<? extends T> supplier = entries.get(color);
        return supplier == null ? null : supplier.get();
    }

    public boolean contains(Object value) {
        return entries.values().stream().map(Supplier::get).anyMatch(value::equals);
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[]) entries.values().stream().map(Supplier::get).toArray(Block[]::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return entries.values().stream().map(supplier -> (T) supplier.get()).iterator();
    }
}
