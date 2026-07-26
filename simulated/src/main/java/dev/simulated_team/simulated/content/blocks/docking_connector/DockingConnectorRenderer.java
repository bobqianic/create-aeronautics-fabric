package dev.simulated_team.simulated.content.blocks.docking_connector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class DockingConnectorRenderer extends SafeBlockEntityRenderer<DockingConnectorBlockEntity> {
    public DockingConnectorRenderer(final BlockEntityRendererProvider.Context context) {

        super(context);

    }

    @Override
    protected void renderSafe(final DockingConnectorBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay) {
        final VertexConsumer vb = bufferSource.getBuffer(RenderType.cutout());
        final Direction direction = be.getBlockState()
                .getValue(BlockStateProperties.FACING);
        final BlockState blockState = be.getBlockState();
        final float extension = be.getExtensionDistance(partialTicks);
        final float rotation = be.getFeetRotation(partialTicks) * 90;

        final SuperByteBuffer piston1 = CachedBuffers.partial(SimPartialModels.DOCKING_CONNECTOR_MAIN_PISTON_BOTTOM, blockState);
        final SuperByteBuffer piston2 = CachedBuffers.partial(SimPartialModels.DOCKING_CONNECTOR_MAIN_PISTON_TOP, blockState);
        final SuperByteBuffer sidePiston1 = CachedBuffers.partial(SimPartialModels.DOCKING_CONNECTOR_SIDE_PISTON_BOTTOM, blockState);
        final SuperByteBuffer sidePiston2 = CachedBuffers.partial(SimPartialModels.DOCKING_CONNECTOR_SIDE_PISTON_TOP, blockState);
        final SuperByteBuffer foot = CachedBuffers.partial(SimPartialModels.DOCKING_CONNECTOR_FOOT, blockState);
        ms.pushPose();
        rotateToFaceCentered(ms, direction);
        piston1.translate(0, extension * 0.5, 0);
        piston2.translate(0, extension, 0);
        piston1.light(light).renderInto(ms.last(), vb);
        piston2.light(light).renderInto(ms.last(), vb);

        final Vector2f footAnchor = new Vector2f();
        final Vector2f sidePistonTopAnchor = new Vector2f();
        final Vector2f sidePistonBottomAnchor = new Vector2f();
        final Vector2f relativeAnchor = new Vector2f();

        footAnchor.set(-7.5f, 15.5f).div(16).add(0, extension);
        this.rotateVector2f(sidePistonTopAnchor.set(1.5f, -2.5f).div(16), rotation).add(footAnchor);
        sidePistonBottomAnchor.set(-6, 2).div(16).add(0, extension / 2);

        relativeAnchor.set(sidePistonTopAnchor).sub(sidePistonBottomAnchor);
        relativeAnchor.normalize();

        final Matrix4f rotationMatrix = new Matrix4f(
                1, 0, 0, 0,
                0, relativeAnchor.y, relativeAnchor.x, 0,
                0, -relativeAnchor.x, relativeAnchor.y, 0,
                0, 0, 0, 1
        );


        for (int i = 0; i < 4; i++) {
            ms.pushPose();
            ms.translate(0.5, 0, 0.5);
            TransformStack.of(ms).rotateYDegrees(i * 90);

            sidePiston1.translate(0, sidePistonBottomAnchor.y, sidePistonBottomAnchor.x);
            sidePiston2.translate(0, sidePistonTopAnchor.y, sidePistonTopAnchor.x);
            foot.translate(0, footAnchor.y, footAnchor.x);
            foot.rotateXDegrees(rotation);

            sidePiston1.mulPose(rotationMatrix);
            sidePiston2.mulPose(rotationMatrix);

            sidePiston1.light(light).renderInto(ms.last(), vb);
            sidePiston2.light(light).renderInto(ms.last(), vb);
            foot.light(light).renderInto(ms.last(), vb);
            ms.popPose();
        }

        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public static void rotateToFaceCentered(final PoseStack ms, final Direction facing) {
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
                .uncenter();
    }

    private Vector2f rotateVector2f(final Vector2f v, float angle) {
        angle = (float) Math.toRadians(angle);
        final float s = Mth.sin(angle);
        final float c = Mth.cos(angle);
        v.set(v.x * c + v.y * s, v.y * c - v.x * s);
        return v;
    }

}
