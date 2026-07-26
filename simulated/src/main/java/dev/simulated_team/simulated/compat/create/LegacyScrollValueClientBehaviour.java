package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LegacyScrollValueClientBehaviour<T extends SmartBlockEntity>
        extends com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour<T, ScrollValueBehaviour> {
    private final ScrollValueBehaviour legacyBehaviour;

    public LegacyScrollValueClientBehaviour(final T blockEntity) {
        this(blockEntity, (ScrollValueBehaviour) blockEntity.getBehaviour(ServerScrollValueBehaviour.TYPE));
    }

    private LegacyScrollValueClientBehaviour(final T blockEntity, final ScrollValueBehaviour legacyBehaviour) {
        super(legacyBehaviour.label, blockEntity,
                LegacyValueBoxTransform.of(blockEntity, legacyBehaviour.getSlotPositioning()));
        this.legacyBehaviour = legacyBehaviour;
        this.behaviour = legacyBehaviour;
        withFormatter(ignored -> legacyBehaviour.formatValue());
    }

    @Override
    public boolean isActive() {
        return legacyBehaviour.isActive();
    }

    @Override
    public boolean testHit(final Vec3 hit) {
        return legacyBehaviour.testHit(hit);
    }

    @Override
    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        return legacyBehaviour.createBoard(player, hitResult);
    }

    @Override
    public ValueSettings getValueSettings() {
        return legacyBehaviour.getValueSettings();
    }

    @Override
    public void setValueSettings(final Player player, final ValueSettings valueSetting, final boolean ctrlDown) {
        legacyBehaviour.setValueSettings(player, valueSetting, ctrlDown);
    }

    @Override
    public boolean mayInteract(final Player player) {
        return legacyBehaviour.mayInteract(player);
    }

    @Override
    public void onShortInteract(final Player player, final InteractionHand hand, final Direction side,
                                final BlockHitResult hitResult) {
        legacyBehaviour.onShortInteract(player, hand, side, hitResult);
    }

    @Override
    public boolean onlyVisibleWithWrench() {
        return legacyBehaviour.onlyVisibleWithWrench();
    }

    @Override
    public boolean acceptsValueSettings() {
        return legacyBehaviour.acceptsValueSettings();
    }

    @Override
    public int netId() {
        return legacyBehaviour.netId();
    }
}
