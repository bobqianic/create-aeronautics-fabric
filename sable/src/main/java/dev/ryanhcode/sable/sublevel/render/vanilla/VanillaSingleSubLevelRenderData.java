package dev.ryanhcode.sable.sublevel.render.vanilla;

import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import net.minecraft.client.Camera;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Render data for single-block sub-levels.
 *
 * <p>PORT-TODO(mc26.1): the dedicated single-block draw path (Tesselator +
 * ShaderInstance batch in the old dispatcher) was removed with the render
 * pipeline rework; single-block sub-levels are currently not drawn. This class
 * is kept as the state holder so the resize/dirty plumbing keeps working and
 * the path can be rebuilt on the new pipeline. (Dungeon Train plots are always
 * multi-block, so DT is unaffected.)
 */
public class VanillaSingleSubLevelRenderData implements SubLevelRenderData {

    /**
     * The sub-level this renderer is for
     */
    private final ClientSubLevel subLevel;

    /**
     * The cached block state for single block rendering
     */
    private BlockState singleBlockState = null;

    /**
     * The cached block position for single block rendering
     */
    private BlockPos singleBlockPos = null;

    /**
     * The cached block seed for single block rendering
     */
    private long singleBlockSeed = 42L;

    /**
     * The cached block entity position for single block rendering
     */
    private BlockEntity singleBlockEntity = null;

    /**
     * Creates a new renderer for the given sub-level
     *
     * @param subLevel the sub-level to render
     */
    public VanillaSingleSubLevelRenderData(final ClientSubLevel subLevel) {
        this.subLevel = subLevel;
        this.rebuild();
    }

    private void handleBlockEntity(@Nullable final BlockEntity blockEntity) {
        if (Objects.equals(this.singleBlockEntity, blockEntity)) {
            return;
        }

        this.singleBlockEntity = blockEntity;
    }

    public @Nullable BlockEntity getRenderBlockEntity() {
        if (this.singleBlockState.isAir()) {
            this.rebuild();
        }
        return this.singleBlockEntity;
    }

    @Override
    public void rebuild() {
        final BoundingBox3ic bounds = this.subLevel.getPlot().getBoundingBox();
        final BlockPos pos = new BlockPos(bounds.minX(), bounds.minY(), bounds.minZ());

        final BlockState blockState = this.subLevel.getLevel().getBlockState(pos);

        this.singleBlockState = blockState;
        this.singleBlockPos = pos;
        this.singleBlockSeed = blockState.getSeed(pos);

        this.handleBlockEntity(blockState.hasBlockEntity() ? this.subLevel.getLevel().getBlockEntity(pos) : null);
    }

    @Override
    public void compileSections(final PrioritizeChunkUpdates chunkUpdates, final RenderRegionCache renderRegionCache, final Camera camera) {
    }

    @Override
    public int getVisibleSectionCount() {
        return 1;
    }

    @Override
    public ClientSubLevel getSubLevel() {
        return this.subLevel;
    }

    @Override
    public void setDirty(final int x, final int y, final int z, final boolean playerChanged) {
        this.rebuild();
    }

    @Override
    public boolean isSectionCompiled(final int x, final int y, final int z) {
        return true;
    }

    @Override
    public void close() {
        this.singleBlockEntity = null;
    }
}
