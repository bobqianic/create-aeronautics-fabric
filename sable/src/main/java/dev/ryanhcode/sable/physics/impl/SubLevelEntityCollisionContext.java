package dev.ryanhcode.sable.physics.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SubLevelEntityCollisionContext extends EntityCollisionContext {
    public SubLevelEntityCollisionContext(final Entity entity) {
        // PORT-NOTE(mc26.1): the 1-arg ctor was replaced; (entity, alwaysCollideWithFluid=false,
        // placement=false) matches the old default behavior.
        super(entity, false, false);
    }

    /**
     * For scaffolding logic
     */
    @Override
    public boolean isAbove(final VoxelShape voxelShape, final BlockPos blockPos, final boolean bl) {
        return false;
    }
}
