package dev.ryanhcode.offroad.index;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OffroadPartialModels {

	public static final PartialModel
			DIODE_LEFT = block("wheel_mount/diode_left"),
			DIODE_RIGHT = block("wheel_mount/diode_right"),
			TELE_OUTER = block("wheel_mount/tele_outer"),
			TELE_INNER = block("wheel_mount/tele_inner"),
			TELE_MOUNT = block("wheel_mount/mount"),
			SPRING_UPPER = block("wheel_mount/spring_upper"),
			SPRING_MIDDLE = block("wheel_mount/spring_middle"),
			SPRING_LOWER = block("wheel_mount/spring_lower"),
			SMALL_TIRE_WHEEL = item("small_tire/item"),
			TIRE_WHEEL = item("tire/item"),
			LARGE_TIRE_WHEEL = item("large_tire/item"),
			MONSTROUS_TIRE_WHEEL = item("monstrous_tire/item"),
			ROCK_CUTTING_WHEEL_WHEEL = block("rockcutting_wheel/wheel");

	private static final Map<ResourceLocation, MountedTireModel> TIRE_MODELS = new HashMap<>();

	static {
		// Keep the legacy TireLike model IDs as lookup keys for saved stacks.
		// Their 1.21.10 "block" models are empty placeholders, so render the
		// neighboring OBJ-backed item partials instead. The item mesh includes
		// the axle shaft omitted by block.obj. These models are centered on
		// Y=0.5, while the mount's legacy model origin is on Y=0.
		registerTireModel(TireLike.SMALL_TIRE, SMALL_TIRE_WHEEL, -0.5f);
		registerTireModel(TireLike.TIRE, TIRE_WHEEL, -0.5f);
		registerTireModel(TireLike.LARGE_TIRE, LARGE_TIRE_WHEEL, -0.5f);
		registerTireModel(TireLike.MONSTROUS_TIRE, MONSTROUS_TIRE_WHEEL, -0.5f);
		registerTireModel(TireLike.CRUSHING_WHEEL);
		registerTireModel(TireLike.WATER_WHEEL);
		registerTireModel(TireLike.FLYWHEEL);
		registerTireModel(TireLike.LARGE_WATER_WHEEL);
		registerTireModel(TireLike.ROCKCUTTING_WHEEL, ROCK_CUTTING_WHEEL_WHEEL);
		registerTireModel(TireLike.MECHANICAL_ROLLER);
	}

	private static void registerTireModel(final TireLike tireLike) {
		tireLike.model().ifPresent(model ->
				TIRE_MODELS.put(model, new MountedTireModel(PartialModel.of(model), 0.0f)));
	}

	private static void registerTireModel(final TireLike tireLike, final PartialModel partialModel) {
		registerTireModel(tireLike, partialModel, 0.0f);
	}

	private static void registerTireModel(final TireLike tireLike, final PartialModel partialModel,
										 final float modelOffsetY) {
		tireLike.model().ifPresent(model ->
				TIRE_MODELS.put(model, new MountedTireModel(partialModel, modelOffsetY)));
	}

	@Nullable
	public static MountedTireModel getTireModel(final ResourceLocation model) {
		return TIRE_MODELS.get(model);
	}

	public record MountedTireModel(PartialModel partialModel, float modelOffsetY) {
	}

	private static PartialModel block(final String path) {
		return PartialModel.of(Offroad.path("block/" + path));
	}
	private static PartialModel entity(final String path) {
		return PartialModel.of(Offroad.path("entity/" + path));
	}
	private static PartialModel item(final String path) {
		return PartialModel.of(Offroad.path("item/" + path));
	}

	public static void init() {
	}
}
