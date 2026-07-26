package dev.simulated_team.simulated.content.blocks.absorber;

import dev.simulated_team.simulated.compat.create.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionContainer;
import dev.ryanhcode.sable.sublevel.water_occlusion.WaterOcclusionRegion;
import dev.ryanhcode.sable.util.BoundedBitVolume3i;
import dev.ryanhcode.sable.util.LevelAccelerator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class AbsorberBlockEntity extends SmartBlockEntity {

    private static final Direction[] DIRECTION_PRIORITY = new Direction[] {
            Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN
    };
    
    @Nullable
    private WaterOcclusionRegion currentRegion;

    public LerpedFloat animationTimer = LerpedFloat.linear();

    public AbsorberBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        this.animationTimer.chase(0,0.04, LerpedFloat.Chaser.LINEAR);
    }

    /**
     * TODO: replace with non-recursive search
     * @param accelerator the level accelerator to use for block state lookups
     * @param pos the position to search at
     * @param visited a set of visited positions to avoid infinite loops
     * @param enclosed a set of positions that are enclosed by solid blocks
     * @return true if the position is safe (enclosed by solid blocks), false otherwise
     */
    private static boolean dfs(final LevelAccelerator accelerator, final BlockPos pos, final Set<BlockPos> visited, final Set<BlockPos> enclosed) {
        boolean safe = pos.getY() <= accelerator.getMinY() + accelerator.getHeight();
        visited.add(pos);

        final BlockState state = accelerator.getBlockState(pos);
        final boolean isEnclosed = !VoxelNeighborhoodState.isSolid(accelerator, pos, state);
        if (isEnclosed) {
            enclosed.add(pos);
        } else {
            return safe;
        }

        for (final Direction dir : DIRECTION_PRIORITY) {
            final BlockPos relative = pos.relative(dir);

            // inflate by 1 block
            enclosed.add(relative);

            if (!visited.contains(relative)) {
                safe = safe && dfs(accelerator, relative, visited, enclosed);
            }
        }

        return safe;
    }
    @Override
    public void tick() {
        super.tick();

        final boolean powered = this.getBlockState().getValue(AbsorberBlock.POWERED);

        if (this.currentRegion != null && this.currentRegion.isDirty()) {
            this.removeRegionIfExists();
        }

        this.animationTimer.tickChaser();
        if(this.animationTimer.settled()) {
            this.animationTimer.updateChaseTarget(powered ? 1 : 0);
        }
        if(this.animationTimer.settled() && !this.level.isClientSide())
        {
            if (powered) {
                if (this.currentRegion == null) {
                    this.buildRegion();
                }
                //temporary water removal
                boolean doWet = false;
                for (final Direction dir : Direction.values()) {
                    final BlockPos newPos = this.getBlockPos().relative(dir);
                    final BlockState blockstate = this.level.getBlockState(newPos);
                    final FluidState fluidstate = this.level.getFluidState(newPos);
                    if (fluidstate.is(FluidTags.WATER) && blockstate.getBlock() instanceof LiquidBlock)
                    {
                        this.level.setBlock(newPos, Blocks.AIR.defaultBlockState(), 3);
                        doWet =true;
                    }
                }
                if(doWet && !this.getBlockState().getValue(AbsorberBlock.WET))
                    this.level.setBlock(this.getBlockPos(), this.getBlockState().cycle(AbsorberBlock.WET), 2);

            } else {
                this.removeRegionIfExists();
                if(this.getBlockState().getValue(AbsorberBlock.WET))
                    this.level.setBlock(this.getBlockPos(), this.getBlockState().cycle(AbsorberBlock.WET), 2);
            }
        }
        if(this.level.isClientSide() && this.animationTimer.getChaseTarget() < this.animationTimer.getValue() && this.getBlockState().getValue(AbsorberBlock.WET))
        {
            final BlockPos pos = this.getBlockPos();
            final float t = this.animationTimer.getValue();
            final float offset = 0.5f+t*t*0.5f;
            for (int i = 0; i < 2; i++) {
                this.level.addParticle(ParticleTypes.SPLASH,pos.getX()+ this.level.random.nextFloat(),pos.getY()+offset,pos.getZ()+ this.level.random.nextFloat(),0,0,0);
            }

        }
    }

    private void buildRegion() {
        if (this.currentRegion != null) {
            throw new IllegalStateException("EvaporatorBlockEntity already has a region assigned.");
        }

        final WaterOcclusionContainer<?> container = WaterOcclusionContainer.getContainer(this.level);

        final ObjectOpenHashSet<BlockPos> visited = new ObjectOpenHashSet<>();
        final ObjectOpenHashSet<BlockPos> enclosed = new ObjectOpenHashSet<>();
        if (dfs(new LevelAccelerator(this.level), this.worldPosition.above(), visited, enclosed)) {
            if (!enclosed.isEmpty()) {
                this.currentRegion = container.addRegion(BoundedBitVolume3i.fromBlocks(enclosed));
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.removeRegionIfExists();
    }

    private void removeRegionIfExists() {
        if (this.currentRegion != null) {
            final WaterOcclusionContainer container = WaterOcclusionContainer.getContainer(this.level);

            if (container != null) {
                container.removeRegion(this.currentRegion);
            }

            this.currentRegion = null;
        }
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {

    }
}
