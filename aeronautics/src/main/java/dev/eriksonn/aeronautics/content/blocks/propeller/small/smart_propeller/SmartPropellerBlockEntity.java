package dev.eriksonn.aeronautics.content.blocks.propeller.small.smart_propeller;

import dev.ryanhcode.sable.Sable;
import dev.simulated_team.simulated.util.SimMathUtils;
import dev.eriksonn.aeronautics.config.AeroConfig;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import dev.eriksonn.aeronautics.content.particle.PropellerAirParticleData;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4d;
import org.joml.Matrix3f;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class SmartPropellerBlockEntity extends BasePropellerBlockEntity {

    public final Vector3d thrustDir;
    public LerpedFloat hingeAngle;

    public SmartPropellerBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
        super(typeIn, pos, state);

        this.hingeAngle = LerpedFloat.linear()
                .startWithValue(state.getValue(SmartPropellerBlock.CEILING) ? 180 : 0)
                .chase(0, 0.1f, LerpedFloat.Chaser.LINEAR);

        this.thrustDir = new Vector3d();
        this.prop.setThrustDirection(this.thrustDir);
    }

    @Override
    public double getConfigThrust() {
        return AeroConfig.server().physics.smartPropellerThrust.get();
    }

    @Override
    public double getConfigAirflow() {
        return AeroConfig.server().physics.smartPropellerAirflow.get();
    }

    @Override
    public float getRadius() {
        return 1;
    }

    @Override
    public float getOffset() {
        return 10.0f / 16.0f;
    }

    @Override
    public Direction getBlockDirection() {
        return Direction.UP;
    }

    @Override
    public void tick() {
        super.tick();

        this.hingeAngle.tickChaser();
        this.hingeAngle.setValue(this.getHingeAngle(this.getBlockState().getValue(SmartPropellerBlock.HORIZONTAL_AXIS), this.hingeAngle.getValue()));

        this.setThrustDirection();
    }

    @Override
    public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
        this.setThrustDirection();

        if (this.isActive()) {
            super.applyForces(subLevel, JOMLConversion.toMojang(this.thrustDir), timeStep);
        }
    }

    private void setThrustDirection() {
        final Vector3d thrustDirection = new Vector3d(0, 1, 0);
        final Direction dir = Direction.get(Direction.AxisDirection.POSITIVE, this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_AXIS)).getClockWise();

        thrustDirection.rotate(new Quaterniond(new AxisAngle4d(-Math.toRadians(this.hingeAngle.getValue()), dir.getStepX(), dir.getStepY(), dir.getStepZ())));
        this.thrustDir.set(thrustDirection);
    }

    @Override
    public void onActiveTick() {
        this.prop.pushEntities();
        this.spawnParticles();
    }

    // A special implementation of spawn particles, used instead of base spawn particles
    public void spawnParticles() {
        final Direction dir = Direction.get(Direction.AxisDirection.POSITIVE, this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_AXIS)).getClockWise();
        final Quaterniond rot = new Quaterniond(new AxisAngle4d(-Math.toRadians(this.hingeAngle.getValue()), dir.getStepX(), dir.getStepY(), dir.getStepZ()));

        double particleCount = this.level.random.nextFloat();
        particleCount = Math.min(particleCount, 10);

        for (int i = 0; i < particleCount; i++) {
            final double R = this.getRadius() * Math.sqrt(this.level.random.nextFloat());
            final double angle = Math.PI * 2.0 * this.level.random.nextFloat();

            Vec3 particlePos = new Vec3(Math.cos(angle) * R, this.getOffset(), Math.sin(angle) * R);
            Vec3 speedVector = new Vec3(0, this.getAirflow() / 40f, 0);

            particlePos = SimMathUtils.rotateQuatReverse(particlePos, rot);
            speedVector = SimMathUtils.rotateQuatReverse(speedVector, rot);

            particlePos = particlePos.add(VecHelper.getCenterOf(this.getBlockPos()));
            this.level.addParticle(new PropellerAirParticleData(true, false),
                    particlePos.x, particlePos.y, particlePos.z,
                    speedVector.x(), speedVector.y(), speedVector.z());
        }
    }

    public float getLerpedHingeAngle(final float partialTick) {
        return this.hingeAngle.getValue(partialTick);
    }

    public float getHingeAngle(final Direction.Axis horizontal, float hingeAngle) {
        final SubLevel subLevel = Sable.HELPER.getContaining(this);
        if (subLevel != null) {
            final Quaterniond Q = new Quaterniond(subLevel.logicalPose().orientation());

            // Create a quaternion representing the orientation of the pendulum
            final Quaterniond pendulumOrientation = new Quaterniond();
            pendulumOrientation.set(Q);

            // Rotate the pendulum orientation by 90 degrees around the axis of rotation
            if (horizontal == Direction.Axis.Z)
                pendulumOrientation.mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90), 0, 1d, 0)));

            // Convert the pendulum orientation to a rotation matrix
            final Matrix3f rotMatrix = new Matrix3f();
            rotMatrix.set(pendulumOrientation);
            final float pitch = (float) Math.atan2(rotMatrix.m01, rotMatrix.m11);
            hingeAngle = -(float) Math.toDegrees(pitch);

            if (horizontal == Direction.Axis.X) {
                hingeAngle *= -1;
            }

            // clamp
            hingeAngle = Mth.clamp(hingeAngle, -45, 45);

            if (this.getBlockState().getValue(SmartPropellerBlock.CEILING)) {
                hingeAngle = 180 - hingeAngle;
            }
        }

        if (subLevel == null) {
            hingeAngle = this.getBlockState().getValue(SmartPropellerBlock.CEILING) ? 180.0f : 0.0f;
        }

        return hingeAngle;
    }
}
