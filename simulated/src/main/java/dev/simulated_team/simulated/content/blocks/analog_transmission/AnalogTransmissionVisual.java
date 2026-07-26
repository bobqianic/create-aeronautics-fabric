package dev.simulated_team.simulated.content.blocks.analog_transmission;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.core.Direction;

import java.util.function.Consumer;

public class AnalogTransmissionVisual extends SingleAxisRotatingVisual<AnalogTransmissionBlockEntity> {

    private final RotatingInstance cogInstance;

    public AnalogTransmissionVisual(final VisualizationContext context, final AnalogTransmissionBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick, Direction.UP, Models.partial(AllPartialModels.SHAFT));
        this.cogInstance = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.ANALOG_TRANSMISSION_COG)).createInstance()
                .rotateToFace(Direction.UP, this.rotationAxis())
                .setup(blockEntity.getExtraKinetics())
                .setPosition(this.getVisualPosition());
        this.cogInstance.setChanged();
    }

    @Override
    public void update(final float pt) {
        super.update(pt);
        this.cogInstance.setup(this.blockEntity.getExtraKinetics()).setChanged();
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);
        this.relight(this.cogInstance);
    }

    @Override
    protected void _delete() {
        super._delete();
        this.cogInstance.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.cogInstance);
    }
}
