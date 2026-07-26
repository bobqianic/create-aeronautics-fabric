package dev.simulated_team.simulated.content.blocks.auger_shaft;

import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class AugerCogBlock extends AugerShaftBlock implements ICogWheel {

    public AugerCogBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean isSmallCog() {
        return true;
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final Level level = context.getLevel();
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        return this.transformAuger(state, SimBlocks.AUGER_SHAFT.getDefaultState(), context, level);
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return super.canSurvive(state, level, pos);
    }
}