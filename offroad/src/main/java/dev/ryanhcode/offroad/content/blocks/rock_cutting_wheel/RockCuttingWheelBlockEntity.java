package dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RockCuttingWheelBlockEntity extends SmartBlockEntity {

    private final LerpedFloat angle = LerpedFloat.angular();
    private int maxDuration;
    private int duration;
    private float manuallyAnimatedSpeed;

    public RockCuttingWheelBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isVirtual()) {
            if (this.duration + 2 > this.maxDuration) {
                this.manuallyAnimatedSpeed = 0;
            } else {
                this.duration++;
            }

            this.angle.chase(this.angle.getValue() + this.manuallyAnimatedSpeed, 1, LerpedFloat.Chaser.EXP);
            this.angle.tickChaser();
        }
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return AABB.encapsulatingFullBlocks(this.worldPosition, this.worldPosition.offset(this.getBlockState().getValue(BlockStateProperties.FACING).getUnitVec3i()));
    }

    public float getAnimatedSpeed(final float partialTicks) {
        return this.angle.getValue(partialTicks);
    }

    public void setAnimatedSpeed(final float speed) {
        this.manuallyAnimatedSpeed = speed;
    }

    public void setMaxDuration(final int duration) {
        this.maxDuration = duration;
        this.duration = 0;
    }
}
