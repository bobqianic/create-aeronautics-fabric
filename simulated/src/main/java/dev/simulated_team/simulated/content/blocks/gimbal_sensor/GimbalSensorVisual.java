package dev.simulated_team.simulated.content.blocks.gimbal_sensor;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.AbstractInstance;
import com.zurrtum.create.client.flywheel.lib.instance.ColoredLitInstance;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import dev.simulated_team.simulated.util.SimDirectionUtil;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GimbalSensorVisual extends AbstractBlockEntityVisual<GimbalSensorBlockEntity> implements SimpleDynamicVisual {

    private final List<OrientedInstance> indicators = new ArrayList<>();
    private final OrientedInstance gimbal;
    private final OrientedInstance compass;
    private final OrientedInstance needle;

    private final List<ColoredLitInstance> allInstances = new ArrayList<>();


    public GimbalSensorVisual(final VisualizationContext ctx, final GimbalSensorBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);

        for (final Direction dir : SimDirectionUtil.Y_AXIS_PLANE) {
            final OrientedInstance inst = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(SimPartialModels.GIMBAL_SENSOR_INDICATOR))
                    .createInstance();

            inst.position(this.getVisualPosition())
                    .translatePivot(-0.5f, 0, 0)
                    .translatePosition(0.5f, 0, 0)
                    .rotateToFace(dir);

            this.indicators.add(inst);
        }

        this.gimbal = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(SimPartialModels.GIMBAL_SENSOR_GIMBAL))
                .createInstance().position(this.getVisualPosition())
                .translatePosition(0.5f, 0.5f, 0.5f)
                .translatePivot(-0.5f, -0.5f, -0.5f);


        this.compass = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(SimPartialModels.GIMBAL_SENSOR_COMPASS))
                .createInstance().position(this.getVisualPosition())
                .translatePosition(0.5f, 0.5f, 0.5f)
                .translatePivot(-0.5f, -0.5f, -0.5f);


        this.needle = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(SimPartialModels.GIMBAL_SENSOR_NEEDLE))
                .createInstance().position(this.getVisualPosition())
                .translatePosition(0.5f, 0.5f, 0.5f)
                .translatePivot(-0.5f, -0.5f, -0.5f);

        this.allInstances.addAll(this.indicators);
        this.allInstances.add(this.gimbal);
        this.allInstances.add(this.compass);
        this.allInstances.add(this.needle);
    }

    @Override
    public void beginFrame(final Context context) {
        for (int i = 0; i < SimDirectionUtil.Y_AXIS_PLANE.length; i++) {
            final Direction dir = SimDirectionUtil.Y_AXIS_PLANE[i];
            final OrientedInstance associatedInst = this.indicators.get(i);

            associatedInst.colorArgb(SimColors.redstone(Math.max(this.blockEntity.getPower(dir), 0) / 15.0F))
                    .setChanged();
        }

        this.handleRotations(context.partialTick());
    }

    private void handleRotations(final float partialTicks) {
        this.gimbal.identityRotation();
        this.compass.identityRotation();
        this.needle.identityRotation();

        final Quaternionf base = this.blockEntity.getBaseQuaternion();

        this.blockEntity.applyPrimaryQuaternion(base, partialTicks);
        this.gimbal.rotation(base);
        this.gimbal.setChanged();


        this.blockEntity.applySecondaryQuaternion(base, partialTicks);
        this.compass.rotation(base);
        this.compass.setChanged();

        this.blockEntity.applyCompassQuaternion(base, partialTicks);
        this.needle.rotation(base);
        this.needle.setChanged();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        for (final AbstractInstance inst : this.allInstances) {
            consumer.accept(inst);
        }
    }

    @Override
    public void updateLight(final float v) {
        for (final ColoredLitInstance inst : this.allInstances) {
            this.relight(inst);
        }
    }

    @Override
    protected void _delete() {
        for (final ColoredLitInstance inst : this.allInstances) {
            inst.delete();
        }
    }
}
