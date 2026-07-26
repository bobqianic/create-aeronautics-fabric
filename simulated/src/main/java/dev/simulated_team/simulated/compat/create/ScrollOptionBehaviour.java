package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ScrollOptionBehaviour<T extends Enum<T>> extends ScrollValueBehaviour {
    private final Class<T> enumClass;

    public ScrollOptionBehaviour(final Class<T> enumClass, final Component label, final SmartBlockEntity blockEntity,
                                 final ValueBoxTransform slotPositioning) {
        super(label, blockEntity, slotPositioning);
        this.enumClass = enumClass;
        between(0, enumClass.getEnumConstants().length - 1);
    }

    public T get() {
        final T[] values = enumClass.getEnumConstants();
        return values[Mth.clamp(value, 0, values.length - 1)];
    }
}
