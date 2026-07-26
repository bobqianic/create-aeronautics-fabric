package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import dev.simulated_team.simulated.util.SimMathUtils;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.function.Consumer;

public class GyroscopicPropellerBearingVisual extends OrientedRotatingVisual<GyroscopicPropellerBearingBlockEntity> implements SimpleDynamicVisual {

    private final TransformedInstance topInstance;
    private final Axis rotationAxis;
    private final Quaternionf blockOrientation;
    protected TransformedInstance[] pistonHeads = new TransformedInstance[4];
    protected TransformedInstance[] pistonPoles = new TransformedInstance[4];

    public GyroscopicPropellerBearingVisual(VisualizationContext context, GyroscopicPropellerBearingBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Direction.SOUTH, (blockEntity.getBlockState().getValue(BlockStateProperties.FACING)).getOpposite(), Models.partial(AllPartialModels.SHAFT_HALF));
        final Direction facing = this.blockState.getValue(BlockStateProperties.FACING);
        this.rotationAxis = Axis.of(Direction.get(Direction.AxisDirection.POSITIVE, this.rotationAxis()).step());
        this.blockOrientation = SimMathUtils.getBlockStateOrientation(facing);
        final PartialModel top = AeroPartialModels.BEARING_PLATE_METAL;
        this.topInstance = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AeroPartialModels.BEARING_PLATE_METAL))
                .createInstance();
        //this.topInstance.position(this.getVisualPosition()).rotation(this.blockOrientation).setChanged();
        //this.topInstance.translatePivot((float)this.blockEntity.blockNormal.x*0.25f,(float)this.blockEntity.blockNormal.y*0.25f,(float)this.blockEntity.blockNormal.z*0.25f);
        //this.topInstance.translatePivot(0,0.25f,0);
        Instancer<TransformedInstance> headProvider = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AeroPartialModels.GYRO_BEARING_PISTON_HEAD));
        Instancer<TransformedInstance> poleProvider = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AeroPartialModels.GYRO_BEARING_PISTON_POLE));
        for (int i = 0; i < 4; i++) {
            //pistonHeads[i] = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(AeroPartialModels.GYRO_BEARING_PISTON_HEAD)).createInstance();
            //pistonPoles[i] = this.instancerProvider().instancer(InstanceTypes.ORIENTED, Models.partial(AeroPartialModels.GYRO_BEARING_PISTON_POLE)).createInstance();
            //pistonPoles[i].rotation(this.blockOrientation);
            pistonPoles[i] = poleProvider.createInstance();
            pistonHeads[i] = headProvider.createInstance();
        }
    }

    @Override
    public void beginFrame(final Context ctx) {
        final float interpolatedAngle = this.blockEntity.getInterpolatedAngle(ctx.partialTick() - 1.0F);
        this.topInstance.setIdentityTransform();
        this.topInstance.translate(getVisualPosition());

        Quaternionf tilt = new Quaternionf(this.blockEntity.previousTiltQuat).slerp(new Quaternionf(this.blockEntity.tiltQuat),ctx.partialTick());

        this.topInstance.translate(JOMLConversion.toMojang(this.blockEntity.blockNormal).scale(0.25))
                .rotateCentered(tilt)
                .rotateCentered(this.rotationAxis.rotationDegrees(interpolatedAngle))
                .translate(JOMLConversion.toMojang(this.blockEntity.blockNormal).scale(-0.25));
        this.topInstance.rotateCentered(this.blockOrientation);
        this.topInstance.setChanged();

        PoseStack ms = new PoseStack();
        TransformStack<PoseTransformStack> msr = TransformStack.of(ms);
        msr.translate(getVisualPosition());
        msr.center();
        msr.rotate(blockOrientation);

        for (int i = 0; i < 4; i++) {
            Vector3d originalPos = JOMLConversion.toJOML(VecHelper.rotate(new Vec3(6 / 16.0, 0, 0), -90 * i, Direction.Axis.Y));

            Vector3d translatedPos = new Vector3d(originalPos);

            blockOrientation.transform(translatedPos);
            double translateDistance = -translatedPos.dot( this.blockEntity.tiltVector) / this.blockEntity.blockNormal.dot(this.blockEntity.tiltVector);

            translatedPos = originalPos.add(0,translateDistance + 3 / 16.0,0);

            msr.pushPose();
            msr.translate(translatedPos.x,translatedPos.y,translatedPos.z);

            msr.pushPose();
            msr.rotate((float)Math.toRadians(-90 * i), Direction.Axis.Y);
            msr.translate(-0.2 / 16, 1 / 32.0, 0);
            pistonPoles[i].setTransform(ms);
            msr.popPose();
            Quaternionf Q = new Quaternionf(blockOrientation);
            Q.conjugate();
            Q.mul(tilt);
            Q.mul(blockOrientation);
            msr.rotate(Q);
            msr.rotate((float)Math.toRadians(-90 * i), Direction.Axis.Y);
            pistonHeads[i].setTransform(ms);
            msr.popPose();
            pistonHeads[i].setChanged();
            pistonPoles[i].setChanged();

        }

        //double interpolatedAngle = Math.toRadians(this.blockEntity.getInterpolatedAngle(ctx.partialTick() - 1));
        /*Quaternionf rot = new Quaternionf();

        //Vector3f rotVector = new Vector3f((float) rotationAxis.x, (float) rotationAxis.y, (float) rotationAxis.z);

        Quaternionf tilt = new Quaternionf(this.blockEntity.previousTiltQuat).slerp(new Quaternionf(this.blockEntity.tiltQuat),ctx.partialTick());

        rot.mul(tilt);

        rot.mul(this.rotationAxis.rotationDegrees(interpolatedAngle));

        rot.mul(blockOrientation);*/

        //PoseStack ms = new PoseStack();
        /*TransformStack<PoseTransformStack> msr = TransformStack.of(ms);

        msr.translate(this.getVisualPosition());
        msr.center();
        msr.pushPose();
        msr.translate(JOMLConversion.toMojang(this.blockEntity.blockNormal).scale(0.25));
        msr.rotateCentered(tilt);
        msr.translate(JOMLConversion.toMojang(this.blockEntity.blockNormal).scale(-0.25));
        msr.rotateCentered(new Quaternionf(new AxisAngle4d(interpolatedAngle, rotVector.x, rotVector.y, rotVector.z)));
        msr.rotateCentered(blockOrientation);


        msr.uncenter();
        topInstance.;
        msr.popPose();
        for (int i = 0; i < 4; i++) {
            msr.pushPose();
            msr.multiply(blockOrientation);
            Vector3d originalPos = JOMLConversion.toJOML(VecHelper.rotate(new Vec3(6 / 16.0, 0, 0), -90 * i, Direction.Axis.Y));

            Vector3d translatedPos = new Vector3d(originalPos);

            blockOrientation.transform(translatedPos);
            double translateDistance = -translatedPos.dot( this.blockEntity.tiltVector) / this.blockEntity.blockNormal.dot(this.blockEntity.tiltVector);
            //translateDistance=0;
            translatedPos = originalPos.add(new Vector3d(0., 1, 0).mul(translateDistance + 3 / 16.0));


            msr.translate(translatedPos.x,translatedPos.y,translatedPos.z);
            msr.pushPose();
            msr.rotate(-90 * i, Direction.Axis.Y);
            msr.translate(-0.2 / 16, 1 / 32.0, 0);
            pistonPoles[i].setTransform(ms);
            msr.popPose();
            Quaternionf Q = new Quaternionf(blockOrientation);
            Q.conjugate();
            Q.mul(tilt);
            Q.mul(blockOrientation);
            msr.multiply(Q);
            msr.rotate(-90 * i, Direction.Axis.Y);
            pistonHeads[i].setTransform(ms);
            msr.popPose();
        }*/
    }

    @Override
    public void updateLight(final float partialTick) {
        super.updateLight(partialTick);
        this.relight(this.topInstance);
        relight(pistonHeads);
        relight(pistonPoles);
    }

    @Override
    protected void _delete() {
        super._delete();
        this.topInstance.delete();
        for (int i = 0; i < 4; i++) {
            pistonHeads[i].delete();
            pistonPoles[i].delete();

        }
    }

    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.topInstance);

    }
}
