package dev.simulated_team.simulated.content.entities.diagram;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import dev.simulated_team.simulated.compat.minecraft.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class DiagramEntityRenderer extends EntityRenderer<DiagramEntity> {

	public DiagramEntityRenderer(final EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(final DiagramEntity entity, final float yaw, final float pt, final PoseStack ms, final MultiBufferSource buffer,
                       final int light) {
		final PartialModel partialModel = entity.size == 3 ? SimPartialModels.CONTRAPTION_DIAGRAM_3x3
			: entity.size == 2 ? SimPartialModels.CONTRAPTION_DIAGRAM_2x2 : SimPartialModels.CONTRAPTION_DIAGRAM_1x1;
		final SuperByteBuffer sbb = CachedBuffers.partial(partialModel, Blocks.AIR.defaultBlockState());
		sbb.rotateYDegrees(-yaw)
			.rotateXDegrees(90.0F + entity.getXRot())
			.translate(-.5, -1 / 32f, -.5);
        if (entity.size == 2) {
            sbb.translate(.5, 0, -.5);
        }

		sbb.disableDiffuse()
			.light(light)
			.renderInto(ms.last(), buffer.getBuffer(Sheets.solidBlockSheet()));
		super.render(entity, yaw, pt, ms, buffer, light);
	}

	@Override
	public ResourceLocation getTextureLocation(final DiagramEntity entity) {
		return null;
	}

}
