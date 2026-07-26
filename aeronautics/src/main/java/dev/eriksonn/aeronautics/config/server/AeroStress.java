package dev.eriksonn.aeronautics.config.server;

import com.zurrtum.create.infrastructure.config.CStress;
import com.zurrtum.create.catnip.config.Builder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.eriksonn.aeronautics.Aeronautics;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class AeroStress extends CStress {

	private static final Object2DoubleMap<ResourceLocation> DEFAULT_IMPACTS = new Object2DoubleOpenHashMap<>();
	private static final Object2DoubleMap<ResourceLocation> DEFAULT_CAPACITIES = new Object2DoubleOpenHashMap<>();

	@Override
	public void registerAll(Builder builder) {
		builder.comment(".", Comments.su, Comments.impact).push("impact");
		DEFAULT_IMPACTS.forEach((id, value) -> this.impacts.put(id, builder.define(id.getPath(), value)));
		builder.pop();

		builder.comment(".", Comments.su, Comments.capacity).push("capacity");
		DEFAULT_CAPACITIES.forEach((id, value) -> this.capacities.put(id, builder.define(id.getPath(), value)));
		builder.pop();
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setNoImpact() {
		return setImpact(0.0F);
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setImpact(final double value) {
		return (builder) -> {
			assertFromAeronautics(builder);
			DEFAULT_IMPACTS.put(Aeronautics.path(builder.getName()), value);
			return builder;
		};
	}

	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> setCapacity(final double value) {
		return (builder) -> {
			assertFromAeronautics(builder);
			DEFAULT_CAPACITIES.put(Aeronautics.path(builder.getName()), value);
			return builder;
		};
	}

	private static void assertFromAeronautics(final BlockBuilder<?, ?> builder) {
		if (!builder.getOwner().getModid().equals("aeronautics")) {
			throw new IllegalStateException("Non-Aeronautics blocks cannot be added to Aeronautics's config.");
		}
	}

	private static class Comments {
		static String su = "[in Stress Units]";
		static String impact = "Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
		static String capacity = "Configure how much stress a source can accommodate for.";
	}
}
