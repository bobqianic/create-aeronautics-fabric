package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import dev.simulated_team.simulated.compat.create.LegacyValueBoxTransform;

/** Client-only icon mapping for the propeller bearing's server-safe direction setting. */
public final class PropellerThrustDirectionClientBehaviour
        extends ScrollOptionBehaviour<PropellerBearingBlockEntity.ThrustDirection> {
    public PropellerThrustDirectionClientBehaviour(final PropellerBearingBlockEntity blockEntity) {
        this(blockEntity, blockEntity.getThrustDirectionOption());
    }

    private PropellerThrustDirectionClientBehaviour(
            final PropellerBearingBlockEntity blockEntity,
            final PropellerBearingBlockEntity.ThrustDirectionBehaviour directionBehaviour
    ) {
        super(
                ThrustDirectionIcon.class,
                ThrustDirectionIcon::from,
                directionBehaviour.getLabel(),
                blockEntity,
                LegacyValueBoxTransform.of(blockEntity, directionBehaviour.getSlotPositioning())
        );
    }

    private enum ThrustDirectionIcon implements INamedIconOptions {
        RIGHT_HANDED(AllIcons.I_REFRESH, PropellerBearingBlockEntity.ThrustDirection.RIGHT_HANDED),
        LEFT_HANDED(AllIcons.I_ROTATE_CCW, PropellerBearingBlockEntity.ThrustDirection.LEFT_HANDED);

        private final AllIcons icon;
        private final PropellerBearingBlockEntity.ThrustDirection direction;

        ThrustDirectionIcon(
                final AllIcons icon,
                final PropellerBearingBlockEntity.ThrustDirection direction
        ) {
            this.icon = icon;
            this.direction = direction;
        }

        private static ThrustDirectionIcon from(final PropellerBearingBlockEntity.ThrustDirection direction) {
            return values()[direction.ordinal()];
        }

        @Override
        public AllIcons getIcon() {
            return this.icon;
        }

        @Override
        public String getTranslationKey() {
            return this.direction.getTranslationKey();
        }
    }
}
