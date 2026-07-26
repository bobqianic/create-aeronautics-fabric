package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionVisual;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.hot_air.gust.GustEntity;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.contraption.PropellerBearingContraptionEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.MobCategory;

import static dev.simulated_team.simulated.index.SimEntityTypes.applyLoaderSpecificTransform;
import static dev.simulated_team.simulated.index.SimEntityTypes.EntityLoaderData;

public class AeroEntityTypes {
	private static final SimulatedRegistrate REGISTRATE = Aeronautics.getRegistrate();

	public static final EntityEntry<PropellerBearingContraptionEntity> PROPELLER_CONTROLLED_CONTRAPTION =
			REGISTRATE.entity("propeller_bearing_contraption", PropellerBearingContraptionEntity::new, MobCategory.MISC)
					.renderer(() -> ContraptionEntityRenderer::new)
					.transform(builder -> applyLoaderSpecificTransform(builder,
							new EntityLoaderData(20, 40, 1, 1, 0, true, true, false)))
					.register();

    public static final EntityEntry<GustEntity> GUST =
            REGISTRATE.<GustEntity>entity("gust", GustEntity::new, MobCategory.MISC)
                    .renderer(() -> NoopRenderer::new)
                    .transform(builder -> applyLoaderSpecificTransform(builder,
                            new EntityLoaderData(20, 3, 1, 1, 0, true, true, false)))
                    .register();

	public static void init() {}
}
