package dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RockCuttingWheelActorVisual extends ActorVisual {

    private final TransformedInstance wheel;
    private final Direction facing;
    private final boolean axisFirst;

    public RockCuttingWheelActorVisual(final VisualizationContext visualizationContext, final BlockAndTintGetter world, final MovementContext context) {
        super(visualizationContext, world, context);

        this.facing = context.state.getValue(BlockStateProperties.FACING);
        this.axisFirst = context.state.getValue(RockCuttingWheelBlock.AXIS_ALONG_FIRST_COORDINATE);

        this.wheel = visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(OffroadPartialModels.ROCK_CUTTING_WHEEL_WHEEL))
                .createInstance();
        this.wheel.light(this.localBlockLight(), 0);
        this.wheel.setChanged();
    }

    @Override
    public void beginFrame() {
        this.wheel.setIdentityTransform()
                .translate(this.context.localPos);

        if ((this.facing.getAxis() == Direction.Axis.Z || this.facing.getAxis() == Direction.Axis.Y) ^ this.axisFirst) {
            this.wheel.rotateCentered(this.facing.getRotation())
                    .rotateZCenteredDegrees(90)
                    .rotateXCenteredDegrees(0)
                    .translate(0.625, 0.5, 0);
        } else {
            this.wheel.rotateCentered(this.facing.getRotation())
                    .rotateZCenteredDegrees(0)
                    .rotateXCenteredDegrees(90)
                    .translate(0, 0.5, -0.625);
        }

        this.wheel.rotateYCenteredDegrees(((LerpedFloat) this.context.temporaryData).getValue(AnimationTickHolder.getPartialTicks()));
        this.wheel.setChanged();
    }

    @Override
    protected void _delete() {
        this.wheel.delete();
    }
}
