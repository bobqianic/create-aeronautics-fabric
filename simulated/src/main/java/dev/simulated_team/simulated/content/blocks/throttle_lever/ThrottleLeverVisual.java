package dev.simulated_team.simulated.content.blocks.throttle_lever;

import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ThrottleLeverVisual extends AbstractBlockEntityVisual<ThrottleLeverBlockEntity> implements SimpleDynamicVisual {

    private final TransformedInstance diode;
    private final TransformedInstance handle;
    private final TransformedInstance button;

    private final AttachFace attached;
    private final Direction facing;

    public ThrottleLeverVisual(final VisualizationContext ctx, final ThrottleLeverBlockEntity blockEntity, final float partialTick) {
        super(ctx, blockEntity, partialTick);

        this.diode = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.THROTTLE_LEVER_DIODE))
                .createInstance();

        this.handle = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.THROTTLE_LEVER_HANDLE))
                .createInstance();

        this.button = this.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(SimPartialModels.THROTTLE_LEVER_BUTTON))
                .createInstance();

        this.attached = blockEntity.getBlockState().getValue(AnalogLeverBlock.FACE);
        this.facing = blockEntity.getBlockState().getValue(AnalogLeverBlock.FACING);

        this.diode.colorArgb(SimColors.redstone(Math.max(0, blockEntity.state / 15F)));

        this.transformAll(partialTick);
    }

    @Override
    public void beginFrame(final Context context) {
        this.diode.colorArgb(SimColors.redstone(Math.max(0, this.blockEntity.state / 15F)));
        this.transformAll(context.partialTick());
    }

    private void transformAll(final float partialTicks) {
        this.diode.setIdentityTransform();
        this.handle.setIdentityTransform();
        this.button.setIdentityTransform();

        this.initialTransform(this.handle);
        this.initialTransform(this.button);
        this.initialTransform(this.diode);

        final double buttonAngle = this.blockEntity.clientPressedLerp.getValue(partialTicks) * -7f;
        float angle = (float) (((this.blockEntity.clientAngle.getValue(partialTicks) / 15) * (ThrottleLeverRenderer.ANGLE_LIMIT * 2) - ThrottleLeverRenderer.ANGLE_LIMIT) / 180 * Math.PI);

        if (this.attached == AttachFace.WALL) {
            angle = -angle;
        }

        this.transformHandle(this.handle, angle, this.attached);

        this.transformHandle(this.button, angle, this.attached);
        this.button
                .translate(0, 14 / 16f, 8 / 16f)
                .rotateXDegrees((float) buttonAngle)
                .translateBack(0, 14 / 16f, 8 / 16f);

        this.diode.setChanged();
        this.handle.setChanged();
        this.button.setChanged();
    }

    private void initialTransform(final TransformedInstance instance) {
        instance.translate(this.getVisualPosition());

        final float rX;
        switch (this.attached) {
            case FLOOR -> rX = 0;
            case WALL -> rX = 90;
            default -> rX = 180;
        }

        final float rY = AngleHelper.horizontalAngle(this.facing);
        instance.rotateCentered((float) (rY / 180 * Math.PI), Direction.UP);
        instance.rotateCentered((float) (rX / 180 * Math.PI), Direction.EAST);
        instance.rotateCentered(this.attached == AttachFace.CEILING ? (float) Math.PI : 0.0f, Direction.UP);
    }

    private void transformHandle(final TransformedInstance instance, final float angle, final AttachFace face) {
        instance.translate(1 / 2f, 3 / 16f, 1 / 2f)
                .rotateX(angle)
                .translateBack(1 / 2f, 3 / 16f, 1 / 2f)
                .rotateCentered(face == AttachFace.WALL ? (float) Math.PI : 0.0f, Direction.UP);
    }

    @Override
    public void collectCrumblingInstances(final Consumer<@Nullable Instance> consumer) {
        consumer.accept(this.handle);
        consumer.accept(this.button);
        consumer.accept(this.diode);
    }

    @Override
    public void updateLight(final float v) {
        this.relight(this.handle);
        this.relight(this.button);
        this.relight(this.diode);
    }

    @Override
    protected void _delete() {
        this.handle.delete();
        this.button.delete();
        this.diode.delete();
    }
}
