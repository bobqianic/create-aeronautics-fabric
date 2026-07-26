package dev.simulated_team.simulated.content.blocks.lasers.laser_sensor;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.compat.create.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.simulated_team.simulated.compat.create.ValueBoxTransform;
import dev.simulated_team.simulated.compat.create.FilteringBehaviour;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LaserSensorBlockEntity extends SmartBlockEntity implements Clearable {
    public int currentPower = 0;
    public int nextPower = 0;
    public int trackedPower = 0;
    private int updateCooldown = 0;
    private static final int MAX_COOLDOWN = 3;

    private FilteringBehaviour filter;
    public double closestHitDistance = Double.MAX_VALUE;
    private double trackedHitDistance = Double.MAX_VALUE;

    public LaserSensorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
        this.nextPower = 0;
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(this.filter = new FilteringBehaviour(this, new FilterValueBoxTransform()).withPredicate(this::isItemValidFilter));
    }

    private boolean isItemValidFilter(final ItemStack itemStack) {
        return itemStack.getItem() instanceof DyeItem ||
                itemStack.is(SimTags.Items.LASER_POINTER_LENS) ||
                itemStack.is(SimTags.Items.LASER_POINTER_RAINBOW);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        // not toggling powered state, 99% of the time a pointer is updating its own power
        if ((this.currentPower != this.nextPower)) {
            if ((this.currentPower == 0) == (this.nextPower == 0)) {
                this.currentPower = this.nextPower;
                this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(LaserSensorBlock.POWERED, this.nextPower > 0));
                this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            } else if (this.updateCooldown <= 0) {
                this.currentPower = this.nextPower;
                this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(LaserSensorBlock.POWERED, this.nextPower > 0));
                this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());

                // Grant Advancement
                final boolean powered = this.getBlockState().getValue(LaserSensorBlock.POWERED);
                if (powered) {
                    SimAdvancements.MY_EYE.awardToNearby(this.getBlockPos(), this.getLevel());
                }

                this.updateCooldown = MAX_COOLDOWN;
            }
        }
        if (this.updateCooldown > 0) {
            this.updateCooldown--;
        }

        this.nextPower = this.trackedPower;
        this.trackedPower = 0;

        this.closestHitDistance = this.trackedHitDistance;
        this.trackedHitDistance = Double.MAX_VALUE;
    }

    public void updateFromPointer(final double distance, final int directPower) {
        this.trackedHitDistance = Math.min(distance, this.trackedHitDistance);

        if (directPower > this.trackedPower) {
            this.trackedPower = directPower;
        }
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        this.currentPower = tag.getIntOr("CurrentPower", 0);
        this.updateCooldown = Math.clamp(tag.getIntOr("UpdateCooldown", 0), 0, MAX_COOLDOWN);
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        tag.putInt("CurrentPower", this.currentPower);
        tag.putInt("UpdateCooldown", this.updateCooldown);
    }

    @Override
    public void clearContent() {
        this.filter.setFilter(ItemStack.EMPTY);
    }

    public boolean filterColor(final int testColor, final boolean rainbow) {
        final ItemStack stack = this.filter.getFilter();
        final boolean rainbowItem = stack.is(SimTags.Items.LASER_POINTER_RAINBOW);

        if (stack.isEmpty()) {
            return true;
        }

        if (rainbowItem && rainbow) {
            return true;
        }

        if (rainbowItem != rainbow) {
            return false;
        }

        final Item item = stack.getItem();

        int color = -1;

        if (stack.is(SimTags.Items.LASER_POINTER_LENS)) {
            color = SimColors.MEDIA_OURPLE;
        } else if (item instanceof final DyeItem dyeItem) {
            color = dyeItem.getDyeColor().getTextColor();
        }

        return testColor == color;
    }

    private static class FilterValueBoxTransform extends ValueBoxTransform.Sided {

        @Override
        protected boolean isSideActive(final BlockState state, final Direction direction) {
            return (switch (state.getValue(LaserSensorBlock.TARGET)) {
                case FLOOR, CEILING -> state.getValue(LaserSensorBlock.FACING);
                default -> Direction.UP;
            }).getAxis() == direction.getAxis();
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 15.5);
        }

        @Override
        public void rotate(final LevelAccessor level, final BlockPos pos, final BlockState state, final PoseStack ms) {
            super.rotate(level, pos, state, ms);
            final Direction facing = state.getValue(LaserSensorBlock.FACING);

            if (facing.getAxis() == Direction.Axis.Y)
                return;

            if (this.getSide() != Direction.UP)
                return;

            TransformStack.of(ms)
                    .rotateZDegrees(-AngleHelper.horizontalAngle(facing) + 180);
        }
    }
}
