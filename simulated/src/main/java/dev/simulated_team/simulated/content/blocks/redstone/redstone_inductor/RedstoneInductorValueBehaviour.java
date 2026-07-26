package dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor;

import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;

import dev.simulated_team.simulated.compat.create.ScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettingsHandleBehaviour;
import dev.simulated_team.simulated.compat.create.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RedstoneInductorValueBehaviour extends ScrollValueBehaviour implements ValueSettingsHandleBehaviour {

    public RedstoneInductorValueBehaviour(final Component label, final SmartBlockEntity be, final ValueBoxTransform slot) {
        super(label, be, slot);
    }

    @Override
    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        return super.createBoard(player, hitResult);
    }

    @Override
    public void onShortInteract(final Player player, final InteractionHand hand, final Direction side, final BlockHitResult hitResult) {
        final BlockState blockState = this.blockEntity.getBlockState();
        if (blockState.getBlock() instanceof final RedstoneInductorBlock bdb)
            bdb.toggle(this.getWorld(), this.getPos(), blockState, player, hand);
    }

    @Override
    public void setValueSettings(final Player player, final ValueSettings valueSetting, final boolean ctrlHeld) {
        final int value = valueSetting.value();
        final int multiplier = switch (valueSetting.row()) {
            case 0 -> 1;
            case 1 -> 20;
            default -> 60 * 20;
        };

        if (!valueSetting.equals(this.getValueSettings()))
            this.playFeedbackSound(this);

        final int clampingValue = valueSetting.row() == 0 ? 0 : 1;
        this.setValue(Math.max(clampingValue, Math.max(clampingValue, value) * multiplier));
    }

    @Override
    public ValueSettings getValueSettings() {
        int row = 0;
        int displayValue = this.value;
        if (displayValue > 60 * 20) {
            displayValue /= 60 * 20;
            row = 2;
        } else if (displayValue > 60) {
            displayValue /= 20;
            row = 1;
        }
        return new ValueSettings(row, displayValue);
    }

    public MutableComponent formatSettings(final ValueSettings settings) {
        final BlockState blockState = this.blockEntity.getBlockState();
        final Boolean inverted = blockState.getValue(RedstoneInductorBlock.INVERTED);

        final int row = settings.row();
        final int column = settings.value();
        if (row == 0 && column == 0) {
            return Component.translatable("block.simulated.redstone_inductor." + (inverted ? "invert" : "copy"));
        }

        return Component.literal(switch (settings.row()) {
            case 1 -> "0:" + (column < 10 ? "0" : "") + column;
            case 2 -> column + ":00";
            default -> column + "t";
        });
    }
}
