package dev.simulated_team.simulated.content.blocks.behaviour;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import dev.simulated_team.simulated.compat.create.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HoldTipBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<HoldTipBehaviour> TYPE = new BehaviourType<>();
    private HoldTipGetter hoverTip;

    public HoldTipBehaviour(final SmartBlockEntity be, final MutableComponent hoverTip) {
        super(be);
        this.setHoverTip(hoverTip);
    }

    public HoldTipBehaviour(final SmartBlockEntity be, final HoldTipGetter hoverTip) {
        super(be);
        this.setHoverTip(hoverTip);
    }

    public void setHoverTip(final MutableComponent hoverTip) {
        this.hoverTip = (player, pos, state) -> hoverTip;
    }

    public void setHoverTip(final HoldTipGetter hoverTip) {
        this.hoverTip = hoverTip;
    }

    public MutableComponent getHoverTip(final Player player, final BlockPos pos, final BlockState state) {
        return this.hoverTip.getTip(player, pos, state);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @FunctionalInterface
    public interface HoldTipGetter {
        @Nullable MutableComponent getTip(Player player, BlockPos pos, BlockState state);
    }
}
