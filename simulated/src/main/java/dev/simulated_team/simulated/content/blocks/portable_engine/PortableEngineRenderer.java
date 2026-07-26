package dev.simulated_team.simulated.content.blocks.portable_engine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.FilteringRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3f;

public class PortableEngineRenderer extends KineticBlockEntityRenderer<PortableEngineBlockEntity> {

    public PortableEngineRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected static float getHatchOpenProgress(final PortableEngineBlockEntity engine, final float partialTicks) {
        return Mth.sin(engine.getHatchOpenTime(partialTicks) / 10 * Mth.HALF_PI);
    }

    @Override
    protected void renderSafe(final PortableEngineBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        final BlockState state = this.getRenderedBlockState(be);
        final RenderType type = RenderType.solid();
        renderRotatingBuffer(be, this.getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
        final VertexConsumer cutout = buffer.getBuffer(RenderType.cutout());

        final Direction direction = be.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING);
        final BlockState blockState = be.getBlockState();

        SimPartialModels.EngineParts engineParts = SimPartialModels.ENGINE_PARTS;

        final float visualStrength = be.visualStrength.getValue(partialTicks);

        final boolean lit = blockState.getValue(RedstoneTorchBlock.LIT);
        this.renderHatch(be, partialTicks, ms, light, blockState, direction, cutout, 255, engineParts, !lit, false);
        this.renderPipes(be, partialTicks, ms, light, blockState, direction, cutout, 255, engineParts, false);

        final float hatchOpenProgress = 1.0f - getHatchOpenProgress(be, partialTicks);
        if (visualStrength > 0) {
            final VertexConsumer translucent = buffer.getBuffer(RenderType.solid());
            engineParts = be.isSuperHeated() ? SimPartialModels.ENGINE_PARTS_SUPERHEATED : SimPartialModels.ENGINE_PARTS_HEATED;

            this.renderPipes(be, partialTicks, ms, LightTexture.FULL_BRIGHT, blockState, direction, translucent, (int) (visualStrength * 255), engineParts, true);
        }

        if (lit) {
            final VertexConsumer translucent = buffer.getBuffer(RenderType.solid());
            this.renderHatch(be, partialTicks, ms, LightTexture.FULL_BRIGHT, blockState, direction, translucent, (int) (hatchOpenProgress * 255), engineParts, lit, true);
        }
    }

    private void renderHatch(final PortableEngineBlockEntity be, final float partialTicks, final PoseStack ms, final int light, final BlockState blockState, final Direction direction, final VertexConsumer consumer, final int alpha, final SimPartialModels.EngineParts parts, final boolean renderInner, boolean lit) {
        if (be.isVirtual()) lit = false;

        final double hatchPivotY = 4.9f / 16.0f;
        final double hatchPivotZ = 3.7f / 16.0f;

        final float hatchOpenAmount = getHatchOpenProgress(be, partialTicks) * 0.65f;

        final SuperByteBuffer hatchBottom = this.rotateToFacing(CachedBuffers.partial(parts.hatchBottom, blockState), direction);
        if (lit) hatchBottom.disableDiffuse();
        hatchBottom
                .translate(0.0f, hatchPivotY, hatchPivotZ)
                .rotate(-hatchOpenAmount, Direction.EAST)
                .translate(-0.0f, -hatchPivotY, -hatchPivotZ)
                .light(light)
                .color(255, 255, 255, alpha)
                .renderInto(ms.last(), consumer);

        final SuperByteBuffer hatchTop = this.rotateToFacing(CachedBuffers.partial(parts.hatchTop, blockState), direction);
        if (lit) hatchTop.disableDiffuse();
        hatchTop
                .light(light)
                .color(255, 255, 255, alpha)
                .renderInto(ms.last(), consumer);

        if (renderInner) {
            final SuperByteBuffer mouth = this.rotateToFacing(CachedBuffers.partial(parts.mouth, blockState), direction.getOpposite());
            if (lit) mouth.disableDiffuse();
            mouth
                    .light(light)
                    .renderInto(ms.last(), consumer);
        }
    }

    private void renderPipes(final PortableEngineBlockEntity be, final float partialTicks, final PoseStack ms, final int light, final BlockState blockState, final Direction direction, final VertexConsumer consumer, final int alpha, final SimPartialModels.EngineParts parts, boolean lit) {
        final float renderTime = AnimationTickHolder.getRenderTime(be.getLevel()) / 20;

        final double pulseTime = renderTime * 7.0;
        final double clipHeight = 0.65;
        final float pulseStrength = 0.03f * be.visualStrength.getValue(partialTicks);
        final float pipePulseStrength = pulseStrength * 1.1f;

        final float pipeScale = (float) (Math.max(Math.sin(pulseTime) + clipHeight, 0.0) - clipHeight) * pipePulseStrength + 1.0f;
        final float outletScale = (float) (Math.max(Math.sin(pulseTime - 1.15) + clipHeight, 0.0) - clipHeight) * pulseStrength + 1.0f;

        final Vector3f outletRotationPointLeft = new Vector3f(2.2f, 10.2f, 11.0f).div(16.0f);
        final Vector3f outletRotationPointRight = new Vector3f(13.6f, 10.2f, 11.0f).div(16.0f);

        final float outletRotation = (float) Math.toRadians(7.5);

        final Vector3f pipeCenterRight = new Vector3f(14.0f, 10.0f, 8.0f).div(16.0f);
        final Vector3f pipeCenterLeft = new Vector3f(16.0f - 14.0f, 10.0f, 8.0f).div(16.0f);

        if (be.isVirtual()) lit = false;

        final SuperByteBuffer pipeRight = this.rotateToFacing(CachedBuffers.partial(parts.pipeRight, blockState), direction);
        if (lit) pipeRight.disableDiffuse();
        pipeRight
                .translate(pipeCenterRight)
                .scale(pipeScale)
                .translateBack(pipeCenterRight)
                .light(light)
                .color(255, 255, 255, alpha)
                .renderInto(ms.last(), consumer);

        final SuperByteBuffer outletRight = this.rotateToFacing(CachedBuffers.partial(parts.outletRight, blockState), direction);
        if (lit) outletRight.disableDiffuse();
        outletRight
                .translate(pipeCenterRight)
                .scale(outletScale)
                .translateBack(pipeCenterRight)
                .translate(outletRotationPointRight)
                .rotateY(-outletRotation)
                .translateBack(outletRotationPointRight)
                .light(light)
                .color(255, 255, 255, alpha)
                .renderInto(ms.last(), consumer);

        final SuperByteBuffer pipeLeft = this.rotateToFacing(CachedBuffers.partial(parts.pipeLeft, blockState), direction);
        if (lit) pipeLeft.disableDiffuse();
        pipeLeft
                .translate(pipeCenterLeft)
                .scale(pipeScale)
                .translateBack(pipeCenterLeft)
                .light(light)
                .color(255, 255, 255, alpha)
                .renderInto(ms.last(), consumer);

        final SuperByteBuffer outletLeft = this.rotateToFacing(CachedBuffers.partial(parts.outletLeft, blockState), direction);
        if (lit) outletLeft.disableDiffuse();
        outletLeft
                .translate(pipeCenterLeft)
                .scale(outletScale)
                .translateBack(pipeCenterLeft)
                .translate(outletRotationPointLeft)
                .rotateY(outletRotation)
                .translateBack(outletRotationPointLeft)
                .light(light)
                .color(255, 255, 255, alpha)
                .renderInto(ms.last(), consumer);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final PortableEngineBlockEntity te, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, te.getBlockState(), te.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    protected SuperByteBuffer rotateToFacing(final SuperByteBuffer buffer, final Direction facing) {
        buffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing)), Direction.UP);
        return buffer;
    }
}
