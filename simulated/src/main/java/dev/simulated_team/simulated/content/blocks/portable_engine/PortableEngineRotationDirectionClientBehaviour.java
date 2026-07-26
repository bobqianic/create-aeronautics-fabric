package dev.simulated_team.simulated.content.blocks.portable_engine;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter.ScrollOptionSettingsFormatter;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import dev.simulated_team.simulated.compat.create.LegacyValueBoxTransform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public final class PortableEngineRotationDirectionClientBehaviour extends ScrollOptionBehaviour<RotationDirection> {
    private final dev.simulated_team.simulated.compat.create.ScrollOptionBehaviour<?> legacyBehaviour;

    public PortableEngineRotationDirectionClientBehaviour(final PortableEngineBlockEntity blockEntity) {
        this(blockEntity, (dev.simulated_team.simulated.compat.create.ScrollOptionBehaviour<?>)
                blockEntity.getBehaviour(ServerScrollValueBehaviour.TYPE));
    }

    private PortableEngineRotationDirectionClientBehaviour(
            final PortableEngineBlockEntity blockEntity,
            final dev.simulated_team.simulated.compat.create.ScrollOptionBehaviour<?> legacyBehaviour) {
        super(DirectionIcon.class, DirectionIcon::from, legacyBehaviour.label, blockEntity,
                LegacyValueBoxTransform.of(blockEntity, legacyBehaviour.getSlotPositioning()));
        this.legacyBehaviour = legacyBehaviour;
    }

    @Override
    public INamedIconOptions getIconForSelected() {
        return legacyBehaviour.getValue() == 0 ? DirectionIcon.CLOCKWISE : DirectionIcon.COUNTER_CLOCKWISE;
    }

    @Override
    public ValueSettingsBoard createBoard(final Player player, final BlockHitResult hitResult) {
        return new ValueSettingsBoard(
                label,
                legacyBehaviour.getMax(),
                1,
                List.of(Component.literal("Select")),
                new ScrollOptionSettingsFormatter(DirectionIcon.values())
        );
    }

    private enum DirectionIcon implements INamedIconOptions {
        CLOCKWISE(AllIcons.I_REFRESH, "create.generic.clockwise"),
        COUNTER_CLOCKWISE(AllIcons.I_ROTATE_CCW, "create.generic.counter_clockwise");

        private final AllIcons icon;
        private final String translationKey;

        DirectionIcon(final AllIcons icon, final String translationKey) {
            this.icon = icon;
            this.translationKey = translationKey;
        }

        private static DirectionIcon from(final RotationDirection direction) {
            return switch (direction) {
                case CLOCKWISE -> CLOCKWISE;
                case COUNTER_CLOCKWISE -> COUNTER_CLOCKWISE;
            };
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }
    }
}
