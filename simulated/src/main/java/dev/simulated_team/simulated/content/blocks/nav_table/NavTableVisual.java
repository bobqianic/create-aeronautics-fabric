package dev.simulated_team.simulated.content.blocks.nav_table;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import dev.simulated_team.simulated.util.SimDirectionUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NavTableVisual extends AbstractBlockEntityVisual<NavTableBlockEntity> implements SimpleDynamicVisual {

    private final Vector3f tempVec = new Vector3f();

    private final List<InstanceDirectionHolder> redstoneInstances = new ArrayList<>();
    private final TransformedInstance pointer;


    public NavTableVisual(final VisualizationContext ctx, final NavTableBlockEntity navBE, final float partialTick) {
        super(ctx, navBE, partialTick);
        final Direction facing = navBE.getBlockState().getValue(NavTableBlock.FACING);

        final Quaternionf facingRot = facing.getRotation();
        for (int i = 0; i < 4; i++) {
            final TransformedInstance inst = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.NAV_TABLE_INDICATOR))
                    .createInstance();
            final Direction dir = SimDirectionUtil.Y_AXIS_PLANE[i];

            inst.translate(this.getVisualPosition())
                    .center()
                    .rotate(facingRot);

            inst.translate(0, -0.5, 0);
            inst.rotateToFace(dir);
            inst.translate(0, 0, 0.5);

            facingRot.transform(dir.getStepX(), dir.getStepY(), dir.getStepZ(), this.tempVec);
            final Direction logicalDirection = Direction.getNearest(Math.round(this.tempVec.x), Math.round(this.tempVec.y), Math.round(this.tempVec.z), Direction.NORTH);

            inst.colorRgb(
                    SimColors.redstone(navBE.isPowering ? Math.max(navBE.getRedstoneStrength(logicalDirection), 0) / 15.0F : 0)
            );

            this.redstoneInstances.add(new InstanceDirectionHolder(inst, logicalDirection));
        }

        this.pointer = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.NAV_TABLE_POINTER))
                .createInstance();
        this.translatePointer(partialTick);
    }

    private void translatePointer(final float partialTick) {
        this.pointer.translate(this.getVisualPosition())
                .center()
                .rotate(this.blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getRotation())
                .translate(0, 0.3, 0)
                .rotateY((float) (this.blockEntity.getClientTargetAngle(partialTick) - Math.PI / 2));
    }

    @Override
    public void beginFrame(final Context context) {
        for (final InstanceDirectionHolder holder : this.redstoneInstances) {
            holder.instance().colorRgb(
                    SimColors.redstone(this.blockEntity.isPowering ? Math.max(this.blockEntity.getRedstoneStrength(holder.logicalDirection()), 0) / 15.0F : 0)
            ).setChanged();
        }

        this.pointer.setIdentityTransform();
        this.translatePointer(context.partialTick());
        this.pointer.setChanged();
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        for (final InstanceDirectionHolder holder : this.redstoneInstances) {
            consumer.accept(holder.instance());
        }

        consumer.accept(this.pointer);
    }

    @Override
    public void updateLight(final float v) {
        for (final InstanceDirectionHolder holder : this.redstoneInstances) {
            this.relight(holder.instance());
        }

        this.relight(this.pointer);
    }

    @Override
    protected void _delete() {
        for (final InstanceDirectionHolder holder : this.redstoneInstances) {
            holder.instance().delete();
        }

        this.pointer.delete();
        this.redstoneInstances.clear();
    }

    private record InstanceDirectionHolder(TransformedInstance instance, Direction logicalDirection) {

    }
}
