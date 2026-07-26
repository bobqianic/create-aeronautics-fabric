package dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.SimplePropellerRenderer;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller.SmartPropellerBlock.REVERSED;

public class SmartPropellerRenderer extends SimplePropellerRenderer<SmartPropellerBlockEntity> {

    public SmartPropellerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderSafe(final SmartPropellerBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        final BlockState state = this.getRenderedBlockState(be);
        final RenderType type = this.getRenderType(be, state);
        renderRotatingBuffer(be, this.getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        final Direction.Axis horizontal = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);

        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        final SuperByteBuffer propeller = CachedBuffers.partialFacing(this.getCurrentModel(be), state, Direction.UP)
                .light(light);
        final SuperByteBuffer hinge = CachedBuffers.partialFacing(AeroPartialModels.SMART_PROPELLER_HINGE, state, Direction.UP)
                .light(light);

        final float hingeAngle = be.getLerpedHingeAngle(partialTicks);
        final float angle = this.getAngle(partialTicks, Direction.UP, be);

        final Direction d = Direction.get(Direction.AxisDirection.NEGATIVE, horizontal);

        hinge.rotateCentered(AngleHelper.rad(hingeAngle), d.getClockWise());
        propeller.rotateCentered(AngleHelper.rad(hingeAngle), d.getClockWise());

        final float factChecked = AngleHelper.rad(AngleHelper.horizontalAngle(d));
        propeller.rotateCentered(factChecked, Direction.UP);
        hinge.rotateCentered(factChecked, Direction.UP);

        kineticRotationTransform(propeller, be, Direction.UP.getAxis(), angle, light);

        propeller.translate(0, 10 / 16f, 0);
        propeller.rotateCentered(AngleHelper.rad(90), Direction.EAST);

        hinge.translate(0, -1 / 16f, 0);
        hinge.rotateCentered(AngleHelper.rad(90), Direction.EAST);

        propeller.renderInto(ms.last(), vb);
        hinge.renderInto(ms.last(), vb);
    }

    @Override
    public PartialModel getCurrentModel(final SmartPropellerBlockEntity be) {
        return be.getBlockState().getValue(REVERSED) ? AeroPartialModels.SMART_PROPELLER_REVERSED : AeroPartialModels.SMART_PROPELLER;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final SmartPropellerBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.DOWN);
    }
}