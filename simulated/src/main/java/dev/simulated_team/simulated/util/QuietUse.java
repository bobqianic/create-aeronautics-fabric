package dev.simulated_team.simulated.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A block implementing this interface will, when right-clicked on the client side, conditionally run code there without sending any packet to the server
 */
public interface QuietUse {
    /**
     * @return non-null value to not send use packet to server
     */
    @Nullable InteractionResult quietUse(Player player, InteractionHand hand, BlockPos pos, BlockState state);
}
