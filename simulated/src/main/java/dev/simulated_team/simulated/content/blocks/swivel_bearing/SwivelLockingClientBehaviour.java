package dev.simulated_team.simulated.content.blocks.swivel_bearing;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import dev.simulated_team.simulated.compat.create.LegacyValueBoxTransform;

public final class SwivelLockingClientBehaviour extends ScrollOptionBehaviour<SwivelBearingBlockEntity.LockingSetting> {
    public SwivelLockingClientBehaviour(final SwivelBearingBlockEntity blockEntity) {
        this(blockEntity, blockEntity.getLockingBehaviour());
    }

    private SwivelLockingClientBehaviour(final SwivelBearingBlockEntity blockEntity,
                                         final SwivelBearingBlockEntity.LockingBehaviour lockingBehaviour) {
        super(LockingIcon.class, LockingIcon::from, lockingBehaviour.getLabel(), blockEntity,
                LegacyValueBoxTransform.of(blockEntity, lockingBehaviour.getSlotPositioning()));
    }

    private enum LockingIcon implements INamedIconOptions {
        LOCKED_ALWAYS(AllIcons.I_CONFIG_LOCKED, SwivelBearingBlockEntity.LockingSetting.LOCKED_ALWAYS),
        LOCKED_DEFAULT(AllIcons.I_CONFIG_LOCKED, SwivelBearingBlockEntity.LockingSetting.LOCKED_DEFAULT),
        UNLOCKED_DEFAULT(AllIcons.I_CONFIG_UNLOCKED, SwivelBearingBlockEntity.LockingSetting.UNLOCKED_DEFAULT),
        UNLOCKED_ALWAYS(AllIcons.I_CONFIG_UNLOCKED, SwivelBearingBlockEntity.LockingSetting.UNLOCKED_ALWAYS);

        private final AllIcons icon;
        private final SwivelBearingBlockEntity.LockingSetting setting;

        LockingIcon(final AllIcons icon, final SwivelBearingBlockEntity.LockingSetting setting) {
            this.icon = icon;
            this.setting = setting;
        }

        private static LockingIcon from(final SwivelBearingBlockEntity.LockingSetting setting) {
            return values()[setting.ordinal()];
        }

        @Override
        public AllIcons getIcon() {
            return this.icon;
        }

        @Override
        public String getTranslationKey() {
            return this.setting.getTranslationKey();
        }
    }
}
