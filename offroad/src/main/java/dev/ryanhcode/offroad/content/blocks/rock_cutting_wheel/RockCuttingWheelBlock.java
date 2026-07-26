package dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.impl.contraption.BlockMovementChecksImpl;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RockCuttingWheelBlock extends AbstractDirectionalAxisBlock implements IBE<RockCuttingWheelBlockEntity> {
    public static final MapCodec<RockCuttingWheelBlock> CODEC = simpleCodec(RockCuttingWheelBlock::new);

    static {
        BlockMovementChecksImpl.registerAttachedCheck((state, world, pos, direction) -> {
            if (state.getBlock() instanceof RockCuttingWheelBlock) {
                if (direction != state.getValue(BlockStateProperties.FACING)) {
                    return BlockMovementChecks.CheckResult.SUCCESS;
                } else {
                    return BlockMovementChecks.CheckResult.FAIL;
                }
            }

            return BlockMovementChecks.CheckResult.PASS;
        });

        BlockMovementChecksImpl.registerNotSupportiveCheck(((state, direction) -> {
            if (state.getBlock() instanceof RockCuttingWheelBlock && direction.equals(state.getValue(BlockStateProperties.FACING))) {
                return BlockMovementChecks.CheckResult.FAIL;
            }

            return BlockMovementChecks.CheckResult.PASS;
        }));

    }

    public RockCuttingWheelBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<RockCuttingWheelBlockEntity> getBlockEntityClass() {
        return RockCuttingWheelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RockCuttingWheelBlockEntity> getBlockEntityType() {
        return OffroadBlockEntityTypes.ROCKCUTTING_WHEEL_BLOCK_ENTITY.get();
    }
}
