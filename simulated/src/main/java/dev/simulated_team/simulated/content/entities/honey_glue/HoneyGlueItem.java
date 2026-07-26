package dev.simulated_team.simulated.content.entities.honey_glue;

import dev.simulated_team.simulated.index.SimClickInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glue entity spawning is done via {@link HoneyGlueClientHandler}
 */
public class HoneyGlueItem extends Item {

    public HoneyGlueItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        if (context.getLevel().isClientSide() && player != null
                && SimClickInteractions.HONEY_GLUE_MANAGER.tryUse(player)) {
            return InteractionResult.SUCCESS;
        }
        return context.getLevel().isClientSide() ? InteractionResult.FAIL : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (level.isClientSide() && SimClickInteractions.HONEY_GLUE_MANAGER.tryUse(player)) {
            return InteractionResult.SUCCESS;
        }
        return level.isClientSide() ? super.use(level, player, hand) : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean canDestroyBlock(final ItemStack stack, final BlockState pState, final Level pLevel, final BlockPos pPos, final LivingEntity entity) {
        return false;
    }
}
