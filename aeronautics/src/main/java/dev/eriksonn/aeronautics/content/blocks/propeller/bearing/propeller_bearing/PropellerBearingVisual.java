package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing;

import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import dev.simulated_team.simulated.util.SimMathUtils;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class PropellerBearingVisual extends OrientedRotatingVisual<PropellerBearingBlockEntity> implements SimpleDynamicVisual {

    private final OrientedInstance topInstance;
    private final Axis rotationAxis;
    private final Quaternionf blockOrientation;

    public PropellerBearingVisual(final VisualizationContext context, final PropellerBearingBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, (blockEntity.getBlockState().getValue(BlockStateProperties.FACING)).getOpposite(), Models.partial(AllPartialModels.SHAFT_HALF));
        final Direction facing = this.blockState.getValue(BlockStateProperties.FACING);
        this.rotationAxis = Axis.of(Direction.get(Direction.AxisDirection.POSITIVE, this.rotationAxis()).step());
        this.blockOrientation = SimMathUtils.getBlockStateOrientation(facing);
        final PartialModel top = AeroPartialModels.BEARING_PLATE;
        this.topInstance = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(top)).createInstance();
        this.topInstance.position(this.getVisualPosition()).rotation(this.blockOrientation).setChanged();
    }

    @Override
    public void beginFrame(final Context ctx) {
        final float interpolatedAngle = this.blockEntity.getInterpolatedAngle(ctx.partialTick() - 1.0F);
        final Quaternionf rot = this.rotationAxis.rotationDegrees(interpolatedAngle);
        rot.mul(this.blockOrientation);
        this.topInstance.rotation(rot).setChanged();
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);
        this.relight(this.topInstance);
    }

    @Override
    protected void _delete() {
        super._delete();
        this.topInstance.delete();
    }

    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.topInstance);
    }
}
