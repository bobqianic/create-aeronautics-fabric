package dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.FilteringRenderer;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

public class MountedPotatoCannonRenderer extends SafeBlockEntityRenderer<MountedPotatoCannonBlockEntity> {

	public MountedPotatoCannonRenderer(final BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(final MountedPotatoCannonBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
		FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
		this.renderComponents(be, partialTicks, ms, buffer, light, overlay);
	}

	private void renderComponents(final MountedPotatoCannonBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
		final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

		final boolean drawParts = isRenderingInSubLevel() || !VisualizationManager.supportsVisualization(be.getLevel());
		if (drawParts) {
			KineticBlockEntityRenderer.renderRotatingKineticBlock(be, this.getRenderedBlockState(be), ms, vb, light);
		}

		final BlockState blockState = be.getBlockState();

		//TODO Don't make this jank
		final float barrelOffset = !be.isBlocked() ? be.getBarrelDistance(partialTicks) : (float) -(be.getBlockedLength() / 2);
		final float bellowOffset = -be.getBellowDistance(partialTicks);

		final SuperByteBuffer barrel = CachedBuffers.partial(AeroPartialModels.CANNON_BARREL, blockState);
		transform(barrel, blockState, true)
				.translate(0, 0, barrelOffset)
				.light(light)
				.renderInto(ms.last(), vb);

		final SuperByteBuffer bellow = CachedBuffers.partial(AeroPartialModels.CANNON_BELLOW, blockState);
		transform(bellow, blockState, true)
				.translate(0, bellowOffset, 0)
				.light(light)
				.renderInto(ms.last(), vb);

		transform(bellow, blockState, true)
				.rotateCentered((float) (Math.PI), Direction.SOUTH)
				.light(light)
				.translate(0, bellowOffset, 0).renderInto(ms.last(), vb);

		if (drawParts) {
			final SuperByteBuffer cogwheel = CachedBuffers.partial(AeroPartialModels.CANNON_COG, blockState);
			final float angle = be.getCogwheelAngle(partialTicks);
			transform(cogwheel, blockState, true)
					.rotateCentered(Mth.DEG_TO_RAD * (angle % 360), Direction.SOUTH)
					.light(light)
					.renderInto(ms.last(), vb);
		}
	}

	private static SuperByteBuffer transform(final SuperByteBuffer buffer, final BlockState state, final boolean axisDirectionMatters) {
		final Direction facing = state.getValue(BlockStateProperties.FACING);

		final float zRotLast = axisDirectionMatters && (state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90 : 0;
		final float yRot = AngleHelper.horizontalAngle(facing);
		final float zRot = facing == Direction.UP ? (float) 90 : facing == Direction.DOWN ? 90 : 0;
		final float zRotSecondLast = facing == Direction.UP ? (float) 180 : 0;

		buffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
		buffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.DOWN);
		buffer.rotateCentered((float) ((yRot) / 180 * Math.PI), Direction.UP);
		buffer.rotateCentered((float) ((zRotLast) / 180 * Math.PI), Direction.SOUTH);
		buffer.rotateCentered((float) ((zRotSecondLast) / 180 * Math.PI), Direction.UP);

		return buffer;
	}

	private BlockState getRenderedBlockState(final MountedPotatoCannonBlockEntity te) {
		return KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(te));
	}

}
