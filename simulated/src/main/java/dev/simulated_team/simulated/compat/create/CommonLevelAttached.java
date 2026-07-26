package dev.simulated_team.simulated.compat.create;

import net.minecraft.world.level.LevelAccessor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class CommonLevelAttached<T> {
    private final Function<LevelAccessor, T> factory;
    private final Map<LevelAccessor, T> values = Collections.synchronizedMap(new WeakHashMap<>());

    public CommonLevelAttached(final Function<LevelAccessor, T> factory) {
        this.factory = factory;
    }

    public T get(final LevelAccessor level) {
        synchronized (this.values) {
            final T existing = this.values.get(level);
            if (existing != null || this.values.containsKey(level)) {
                return existing;
            }
            final T created = this.factory.apply(level);
            if (created != null) {
                this.values.put(level, created);
            }
            return created;
        }
    }
}
