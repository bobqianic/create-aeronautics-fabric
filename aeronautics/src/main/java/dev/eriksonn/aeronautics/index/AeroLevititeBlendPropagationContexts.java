package dev.eriksonn.aeronautics.index;

import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.CrystalPropagationContext;
import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeCrystalPropagationContext;
import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeSoulCrystalPropagationContext;
import foundry.veil.platform.registry.RegistryObject;

import java.util.function.Supplier;

public class AeroLevititeBlendPropagationContexts {
	public static RegistryObject<CrystalPropagationContext>
			STANDARD_CONTEXT = create("standard", LevititeCrystalPropagationContext::new),
			SOUL_CONTEXT = create("soul", LevititeSoulCrystalPropagationContext::new);

	public static RegistryObject<CrystalPropagationContext> create(String id, final Supplier<CrystalPropagationContext> context) {
		return AeroRegistries.LEVITITE_CRYSTAL_PROPAGATION_CONTEXT.register(id, context);
	}

	public static void init() {

	}
}
