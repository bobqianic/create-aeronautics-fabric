package dev.ryanhcode.sable.mixin.entity.entities_in_blocks;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract boolean isAlive();

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    private Level level;

    @Shadow
    protected abstract void onInsideBlock(BlockState blockState);

    // PORT-NOTE(mc26.1): entityInside gained (InsideBlockEffectApplier, boolean isPrecise) params; reuse the
    // entity's own StepBasedCollector, which vanilla flushes via applyAndClear right after checkInsideBlocks.
    @Shadow
    @Final
    private InsideBlockEffectApplier.StepBasedCollector insideEffectCollector;

    // PORT-NOTE(mc26.1): checkInsideBlocks() is now checkInsideBlocks(List<Entity.Movement>, StepBasedCollector).
    @Inject(method = "checkInsideBlocks(Ljava/util/List;Lnet/minecraft/world/entity/InsideBlockEffectApplier$StepBasedCollector;)V", at = @At("TAIL"))
    protected void checkInsideBlocks(final CallbackInfo ci) {
        final AABB bounds = this.getBoundingBox();
        final Entity entity = (Entity) (Object) this;

        final BoundingBox3d localBounds = new BoundingBox3d(bounds);
        for (final SubLevel intersecting : Sable.HELPER.getAllIntersecting(this.level, new BoundingBox3d(bounds))) {
            localBounds.set(bounds);
            localBounds.transformInverse(intersecting.logicalPose(), localBounds);
            final BlockPos minPos = BlockPos.containing(localBounds.minX + 1.0E-7, localBounds.minY + 1.0E-7, localBounds.minZ + 1.0E-7);
            final BlockPos maxPos = BlockPos.containing(localBounds.maxX - 1.0E-7, localBounds.maxY - 1.0E-7, localBounds.maxZ - 1.0E-7);

            final boolean projected = SubLevelHelper.pushEntityLocalIfNeeded(intersecting, entity);
            try {
                if (this.level.hasChunksAt(minPos, maxPos)) {
                    final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

                    for (int i = minPos.getX(); i <= maxPos.getX(); i++) {
                        for (int j = minPos.getY(); j <= maxPos.getY(); j++) {
                            for (int k = minPos.getZ(); k <= maxPos.getZ(); k++) {
                                if (!this.isAlive()) {
                                    return;
                                }

                                mutableBlockPos.set(i, j, k);
                                final BlockState blockState = this.level.getBlockState(mutableBlockPos);

                                try {
                                    blockState.entityInside(this.level, mutableBlockPos, entity, this.insideEffectCollector, true);
                                    this.onInsideBlock(blockState);
                                } catch (final Throwable var12) {
                                    final CrashReport crashReport = CrashReport.forThrowable(var12, "Colliding entity with block");
                                    final CrashReportCategory crashReportCategory = crashReport.addCategory("Block being collided with");
                                    CrashReportCategory.populateBlockDetails(crashReportCategory, this.level, mutableBlockPos, blockState);
                                    throw new ReportedException(crashReport);
                                }
                            }
                        }
                    }
                }
            } finally {
                SubLevelHelper.popEntityLocalIfNeeded(intersecting, entity, projected);
            }
        }
    }
}
