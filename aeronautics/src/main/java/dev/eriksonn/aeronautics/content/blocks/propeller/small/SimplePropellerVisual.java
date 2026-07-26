package dev.eriksonn.aeronautics.content.blocks.propeller.small;

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
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public abstract class SimplePropellerVisual<T extends BasePropellerBlockEntity> extends OrientedRotatingVisual<T> implements SimpleDynamicVisual {

    protected final OrientedInstance propeller;
    protected final Vector3f rotationAxis;
    protected final Quaternionf blockOrientation;

    private float lastRotation;

    public SimplePropellerVisual(final VisualizationContext context, final T blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite(), Models.partial(AllPartialModels.SHAFT_HALF));
        final Direction facing = this.blockState.getValue(BlockStateProperties.FACING);

        final Vec3i normal = facing.getUnitVec3i();
        final Vec3 normalPos = new Vec3(normal.getX(), normal.getY(), normal.getZ());
        final Vector3f pos = Vec3.atLowerCornerOf(this.getVisualPosition()).add(normalPos.scale(3 / 16f)).toVector3f();

        this.rotationAxis = Direction.get(Direction.AxisDirection.POSITIVE, this.rotationAxis()).step();
        this.blockOrientation = SimMathUtils.getBlockStateOrientation(facing);

        this.propeller = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(this.getModel(blockEntity.getBlockState()))).createInstance();
        this.propeller.position(pos).rotation(this.blockOrientation).setChanged();
    }

    public abstract PartialModel getModel(BlockState state);

    @Override
    public void beginFrame(final Context context) {
        final float angle = this.getAngle(context.partialTick());
        if (this.lastRotation == angle) {
            return;
        }

        this.lastRotation = angle;
        this.propeller.identityRotation()
                .rotate(Mth.DEG_TO_RAD * angle, this.rotationAxis.x, this.rotationAxis.y, this.rotationAxis.z)
                .rotate(this.blockOrientation)
                .setChanged();
    }

    public float getAngle(final float partialTicks) {
        return 2.0F * (this.blockEntity.getPreviousAngle() * (1f - partialTicks) + this.blockEntity.getAngle() * partialTicks);
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);
        this.relight(this.pos, this.propeller);
    }

    @Override
    protected void _delete() {
        super._delete();
        this.propeller.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.propeller);
    }
}
