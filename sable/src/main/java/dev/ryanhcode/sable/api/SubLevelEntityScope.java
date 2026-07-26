package dev.ryanhcode.sable.api;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Temporarily exposes entities in the local coordinate space used by a sub-level block or block entity.
 */
public final class SubLevelEntityScope implements AutoCloseable {

    @Nullable
    private final SubLevel subLevel;
    private final Set<Entity> included = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Entity> projected = new ArrayList<>();
    private boolean closed;

    private SubLevelEntityScope(@Nullable final SubLevel subLevel) {
        this.subLevel = subLevel;
    }

    public static SubLevelEntityScope at(final Level level, final BlockPos localPos) {
        return new SubLevelEntityScope(Sable.HELPER.getContaining(level, localPos));
    }

    public static SubLevelEntityScope forSubLevel(@Nullable final SubLevel subLevel) {
        return new SubLevelEntityScope(subLevel);
    }

    public <T extends Entity> T include(final T entity) {
        if (this.closed) {
            throw new IllegalStateException("Cannot add an entity to a closed sub-level scope");
        }
        if (this.subLevel == null || !this.included.add(entity)) {
            return entity;
        }
        if (SubLevelHelper.pushEntityLocalIfNeeded(this.subLevel, entity)) {
            this.projected.add(entity);
        }
        return entity;
    }

    public <T extends Entity> Iterable<T> includeAll(final Iterable<T> entities) {
        for (final T entity : entities) {
            this.include(entity);
        }
        return entities;
    }

    public boolean isActive() {
        return this.subLevel != null;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;

        if (this.subLevel != null) {
            for (int i = this.projected.size() - 1; i >= 0; i--) {
                SubLevelHelper.popEntityLocal(this.subLevel, this.projected.get(i));
            }
        }

        this.projected.clear();
        this.included.clear();
    }
}
