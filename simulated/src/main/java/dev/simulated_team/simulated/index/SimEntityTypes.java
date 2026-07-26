package dev.simulated_team.simulated.index;

import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.entry.EntityEntry;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntity;
import dev.simulated_team.simulated.content.entities.diagram.DiagramEntityRenderer;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueRenderer;
import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntity;
import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntityRenderer;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimEntityService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;

public class SimEntityTypes {
    private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();

    public static final EntityEntry<HoneyGlueEntity> HONEY_GLUE =
            REGISTRATE.entity("honey_glue", HoneyGlueEntity::create, MobCategory.MISC)
                    .renderer(() -> HoneyGlueRenderer::new)
                    .transform((builder) -> applyLoaderSpecificTransform(builder,
                            new EntityLoaderData(10, Integer.MAX_VALUE, 0.1f, 0.1f, 0,false, true, false)))
                    .register();

    public static final EntityEntry<LaunchedPlungerEntity> PLUNGER =
            REGISTRATE.entity("launched_plunger", LaunchedPlungerEntity::create, MobCategory.MISC)
                    .renderer(() -> LaunchedPlungerEntityRenderer::new)
                    .transform((builder) -> applyLoaderSpecificTransform(builder,
                            new EntityLoaderData(10, 1, 0.5f, 0.5f, 0.25f,true, true, false)))
                    .register();

    public static final EntityEntry<DiagramEntity> CONTRAPTION_DIAGRAM =
            REGISTRATE.entity("contraption_diagram", DiagramEntity::create, MobCategory.MISC)
                    .renderer(() -> DiagramEntityRenderer::new)
                    .transform((builder) -> applyLoaderSpecificTransform(builder,
                            new EntityLoaderData(10, Integer.MAX_VALUE, 0.1f, 0.1f, 0.0f, false, true, true)))
                    .register();

    public static <T extends Entity, P> EntityBuilder<T, P> applyLoaderSpecificTransform(final EntityBuilder<T, P> builder, final EntityLoaderData data) {
        return SimEntityService.INSTANCE.loaderEntityTransform(builder, data);
    }

    public record EntityLoaderData(int clientTrackingRange, int updateFrequency, float width, float height, float eyeHeight, boolean sendVelocity, boolean immuneToFire, boolean fixed) {}

    public static void register() {
    }
}
