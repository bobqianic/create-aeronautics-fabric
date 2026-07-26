package dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.function.Consumer;

public class ModulatingLinkVisual extends AbstractBlockEntityVisual<ModulatingLinkedReceiverBlockEntity> implements SimpleDynamicVisual {

    public static final float MAX_DISTANCE = 256;
    public static final float SMOOTHING = 20;

    private final Vector3d tempNormal = new Vector3d();

    private final TransformedInstance topPlate;
    private final TransformedInstance bottomPlate;

    private final Direction facing;

    public ModulatingLinkVisual(final VisualizationContext ctx, final ModulatingLinkedReceiverBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);

        this.topPlate = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.MODULATING_RECEIVER_PLATE))
                .createInstance();

        this.bottomPlate = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.MODULATING_RECEIVER_PLATE))
                .createInstance();

        this.facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);

        this.handleTransform();
    }

    @Override
    public void beginFrame(final Context context) {
        this.handleTransform();
    }

    private void handleTransform() {
        this.topPlate.setIdentityTransform();
        this.bottomPlate.setIdentityTransform();

        this.tempNormal.set(this.facing.step()).mul(1 / 16.0);
        final float max = this.getMax();
        this.topPlate.translate(this.getVisualPosition())
                .translate(this.tempNormal.x() * (0.5 + max), this.tempNormal.y() * (0.5 + max), this.tempNormal.z() * (0.5 + max));

        final float min = this.getMin();
        this.bottomPlate.translate(this.getVisualPosition())
                .translate(this.tempNormal.x() * min, this.tempNormal.y() * min, this.tempNormal.z() * min);

        if (this.facing.getAxis().isHorizontal()) {
            this.rotateInstanceHorizontally(this.topPlate, this.facing);
            this.rotateInstanceHorizontally(this.bottomPlate, this.facing);
        }

        this.rotateInstanceVertically(this.topPlate, this.facing);
        this.rotateInstanceVertically(this.bottomPlate, this.facing);

        this.topPlate.setChanged();
        this.bottomPlate.setChanged();
    }

    private void rotateInstanceHorizontally(final TransformedInstance inst, final Direction facing) {
        inst.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
    }

    private void rotateInstanceVertically(final TransformedInstance inst, final Direction facing) {
        inst.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)), Direction.EAST);
    }

    private float getMin() {
        return 5.5f * ((this.blockEntity.minRange - 1) * (SMOOTHING + MAX_DISTANCE - 1)) / ((MAX_DISTANCE - 1) * (SMOOTHING + this.blockEntity.minRange - 1));
    }

    private float getMax() {
        return 5.5f * ((this.blockEntity.maxRange - 1) * (SMOOTHING + MAX_DISTANCE - 1)) / ((MAX_DISTANCE - 1) * (SMOOTHING + this.blockEntity.maxRange - 1));
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        consumer.accept(this.topPlate);
        consumer.accept(this.bottomPlate);
    }

    @Override
    public void updateLight(final float v) {
        this.relight(this.topPlate);
        this.relight(this.bottomPlate);
    }

    @Override
    protected void _delete() {
        this.topPlate.delete();
        this.bottomPlate.delete();
    }
}
