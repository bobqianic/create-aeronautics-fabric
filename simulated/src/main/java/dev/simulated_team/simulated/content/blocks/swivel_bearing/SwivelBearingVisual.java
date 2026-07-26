package dev.simulated_team.simulated.content.blocks.swivel_bearing;

import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Consumer;

public class SwivelBearingVisual extends OrientedRotatingVisual<SwivelBearingBlockEntity> {

    private final RotatingInstance topShaft;
    private final RotatingInstance cogInstance;

    public SwivelBearingVisual(final VisualizationContext context, final SwivelBearingBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite(), Models.partial(SimPartialModels.SHAFT_SIXTEENTH));
        this.topShaft = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SHAFT_SIXTEENTH))
                .createInstance()
                .rotateToFace(Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING))
                .setup(blockEntity)
                .setPosition(this.getVisualPosition());
        this.cogInstance = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SWIVEL_BEARING_COG))
                .createInstance()
                .rotateToFace(Direction.UP, blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite())
                .setup(blockEntity.getExtraKinetics())
                .setPosition(this.getVisualPosition());
        this.topShaft.setVisible(!this.blockEntity.isAssembled());
        this.topShaft.setChanged();
        this.cogInstance.setChanged();
    }

    @Override
    public void update(final float pt) {
        super.update(pt);
        this.topShaft.setVisible(!this.blockEntity.isAssembled());
        this.topShaft.setup(this.blockEntity).setChanged();
        this.cogInstance.setup(this.blockEntity.getExtraKinetics()).setChanged();
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);
        this.relight(this.topShaft);
        this.relight(this.cogInstance);
    }

    @Override
    protected void _delete() {
        super._delete();
        this.topShaft.delete();
        this.cogInstance.delete();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.topShaft);
        consumer.accept(this.cogInstance);
    }
}
