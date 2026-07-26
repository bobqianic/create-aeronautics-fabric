package dev.simulated_team.simulated.content.blocks.velocity_sensor;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VelocitySensorVisual extends AbstractBlockEntityVisual<VelocitySensorBlockEntity> implements SimpleDynamicVisual {

    public VelocitySensorVisual(final VisualizationContext ctx, final VelocitySensorBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);
    }

    @Override
    public void beginFrame(final Context context) {

    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {

    }

    @Override
    public void updateLight(final float v) {

    }

    @Override
    protected void _delete() {

    }
}
