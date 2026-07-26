package dev.simulated_team.simulated.content.blocks.torsion_spring;

import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimMathUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class TorsionSpringVisual extends OrientedRotatingVisual<TorsionSpringBlockEntity> implements SimpleDynamicVisual {

    private final Axis rotationAxis;
    private final Quaternionf blockOrientation;

    private final RotatingInstance topInstance;
    private final OrientedInstance springInstance;

    private boolean wasSpringStatic;

    public TorsionSpringVisual(final VisualizationContext context, final TorsionSpringBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite(), Models.partial(AllPartialModels.SHAFT_HALF));
        final Direction facing = this.blockState.getValue(BlockStateProperties.FACING);
        this.rotationAxis = Axis.of(Direction.get(Direction.AxisDirection.POSITIVE, this.rotationAxis()).step());
        this.blockOrientation = SimMathUtils.getBlockStateOrientation(facing);

        this.topInstance = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF)).createInstance()
                .rotateToFace(Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING))
                .setup(blockEntity.getExtraKinetics())
                .setPosition(this.getVisualPosition());
        this.springInstance = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(SimPartialModels.TORSION_SPRING))
                .createInstance()
                .rotation(this.blockOrientation)
                .position(this.getVisualPosition());
        this.topInstance.setChanged();
        this.springInstance.setChanged();
        this.wasSpringStatic = false;
    }

    @Override
    public void update(final float pt) {
        super.update(pt);
        this.topInstance.setup(this.blockEntity.getExtraKinetics()).setChanged();
    }

    @Override
    public void beginFrame(final Context ctx) {
        if (this.wasSpringStatic && this.blockEntity.isSpringStatic()) {
            return;
        }

        this.wasSpringStatic = this.blockEntity.isSpringStatic();
        final float interpolatedAngle = this.blockEntity.interpolatedSpring(ctx.partialTick());
        final Quaternionf rot = this.rotationAxis.rotationDegrees(interpolatedAngle);
        rot.mul(this.blockOrientation);
        this.springInstance.rotation(rot).setChanged();
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);
        this.relight(this.topInstance);
        this.relight(this.springInstance);
    }

    @Override
    protected void _delete() {
        super._delete();
        this.topInstance.delete();
        this.springInstance.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.topInstance);
        consumer.accept(this.springInstance);
    }
}
