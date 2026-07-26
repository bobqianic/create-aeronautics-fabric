package dev.ryanhcode.offroad.content.blocks.borehead_bearing;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.api.CustomStressImpactTooltipProvider;
import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import dev.ryanhcode.offroad.data.OffroadLang;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BoreheadBearingBlock extends DirectionalAxisKineticBlock implements IBE<BoreheadBearingBlockEntity>, CustomStressImpactTooltipProvider {

    private static final ObjectList<BlockPos> TEMP_POSITIONS = new ObjectArrayList<>();

    static {
        
        SimBlockMovementChecks.registerAdditionalBlocks( (blockState, level, blockPos, set) -> {
            TEMP_POSITIONS.clear();

            if (blockState.getBlock() instanceof BoreheadBearingBlock) {
                TEMP_POSITIONS.addFirst(blockPos.relative(blockState.getValue(FACING)));
            }

            return TEMP_POSITIONS;
        });

    }


    public BoreheadBearingBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (!player.mayBuild())
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return InteractionResult.FAIL;
        if (stack.isEmpty()) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            this.withBlockEntityDo(level, pos, be -> {
                if (be.isRunning()) {
                    be.startDisassemblySlowdown();
                    return;
                }

                be.setAssembleNextTick(true);
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hasShaftTowards(final LevelReader world, final BlockPos pos, final BlockState state, final Direction face) {
        return super.hasShaftTowards(world, pos, state, face);
    }

    @Override
    public Class<BoreheadBearingBlockEntity> getBlockEntityClass() {
        return BoreheadBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BoreheadBearingBlockEntity> getBlockEntityType() {
        return OffroadBlockEntityTypes.BOREHEAD_BEARING.get();
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final InteractionResult resultType = super.onWrenched(state, context);
        if (!context.getLevel().isClientSide() && resultType.consumesAction()) {
            final BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());

            if (be instanceof BoreheadBearingBlockEntity) {
                if (context.getLevel().getBlockState(context.getClickedPos()).getValue(FACING) != state.getValue(FACING)) {
                    ((BoreheadBearingBlockEntity) be).disassemble();
                }
            }
        }

        return resultType;
    }

    @Override
    public MutableComponent getCustomImpactLang() {
        return OffroadLang.translate("tooltip.borehead_bearing_stress").component();
    }

    @Override
    public int getBarLength() {
        return 4;
    }

    @Override
    public int getFilledBarLength() {
        return 4;
    }
}
