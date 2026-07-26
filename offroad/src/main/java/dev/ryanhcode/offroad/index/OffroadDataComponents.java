package dev.ryanhcode.offroad.index;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import foundry.veil.platform.registry.RegistrationProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import java.util.function.UnaryOperator;

public class OffroadDataComponents {
	private static final RegistrationProvider<DataComponentType<?>> REGISTRY = RegistrationProvider.get(Registries.DATA_COMPONENT_TYPE, Offroad.MOD_ID);

	public static final DataComponentType<TireLike> TIRE = create("tire",
			builder -> builder.persistent(TireLike.CODEC));


	private static <T> DataComponentType<T> create(final String name, final UnaryOperator<DataComponentType.Builder<T>> builder) {
		final DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		REGISTRY.register(name, () -> type);
		return type;
	}

	public static void init() {
		// no-op
	}
}
