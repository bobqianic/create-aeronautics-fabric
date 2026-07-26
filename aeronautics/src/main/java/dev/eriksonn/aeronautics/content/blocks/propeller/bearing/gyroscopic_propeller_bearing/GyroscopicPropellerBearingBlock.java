package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing;

import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.foundation.block.IBE;
import dev.simulated_team.simulated.api.CustomStressImpactTooltipProvider;
import dev.eriksonn.aeronautics.data.AeroLang;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import dev.eriksonn.aeronautics.index.AeroBlockShapes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GyroscopicPropellerBearingBlock extends BearingBlock implements IBE<GyroscopicPropellerBearingBlockEntity>, CustomStressImpactTooltipProvider {
    public GyroscopicPropellerBearingBlock(final Properties properties) {
        super(properties);
    }

    public MutableComponent getCustomImpactLang() {
        return AeroLang.translate("propeller.sails").component();
    }

    @Override
    public int getBarLength() {
        return 3;
    }

    @Override
    public int getFilledBarLength() {
        return 3;
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        if (!player.mayBuild())
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return InteractionResult.FAIL;
        if (stack.isEmpty()) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            this.withBlockEntityDo(level, pos, te -> {
                if (te.isRunning()) {

                    te.startDisassemblySlowdown();
                    return;
                }
                te.setAssembleNextTick(true);

            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final InteractionResult result = super.onWrenched(state, context);

        final Level level = context.getLevel();
        if (level.isClientSide() && result.consumesAction()) {
            final BlockState newState = this.getRotatedBlockState(state, context.getClickedFace());
            level.setBlock(context.getClickedPos(), newState, 2);
            this.withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> be.forceTilt(newState));
        }

        return result;
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext ctx) {
        return AeroBlockShapes.PROPELLER_BEARING.get(pState.getValue(FACING));
    }


    @Override
    public Class<GyroscopicPropellerBearingBlockEntity> getBlockEntityClass() {
        return GyroscopicPropellerBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GyroscopicPropellerBearingBlockEntity> getBlockEntityType() {
        return AeroBlockEntityTypes.GYROSCOPIC_PROPELLER_BEARING.get();
    }
}
