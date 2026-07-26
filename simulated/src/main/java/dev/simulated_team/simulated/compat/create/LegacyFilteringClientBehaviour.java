package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class LegacyFilteringClientBehaviour
        extends com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour<FilteringBehaviour> {
    public LegacyFilteringClientBehaviour(final SmartBlockEntity blockEntity) {
        super(blockEntity, LegacyValueBoxTransform.of(blockEntity,
                ((FilteringBehaviour) blockEntity.getBehaviour(
                        com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour.TYPE))
                        .getSlotPositioning()));
    }
}
