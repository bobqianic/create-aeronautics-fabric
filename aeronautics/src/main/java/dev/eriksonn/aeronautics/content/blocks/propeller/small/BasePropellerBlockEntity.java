package dev.eriksonn.aeronautics.content.blocks.propeller.small;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.eriksonn.aeronautics.content.blocks.propeller.behaviour.PropellerActorBehaviour;
import dev.eriksonn.aeronautics.index.AeroAdvancements;
import dev.simulated_team.simulated.compat.create.IHaveGoggleInformation;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

public abstract class BasePropellerBlockEntity extends KineticBlockEntity implements BlockEntitySubLevelPropellerActor, BlockEntityPropeller, IHaveGoggleInformation {
    private final Quaternionf rot = new Quaternionf();
    public float rotationSpeed = 0;
    public PropellerActorBehaviour prop;
    private float previousAngle;
    private float angle;

    public BasePropellerBlockEntity(final BlockEntityType<?> typeIn, final BlockPos pos, final BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);

        this.prop = this.createBehavior();
        behaviours.add(this.prop);
    }

    /**
     * Creates and configures a prop behaviour for this propeller
     *
     * @return a new prop behaviour with the BE specific configurations
     */
    public PropellerActorBehaviour createBehavior() {
        final PropellerActorBehaviour prop = new PropellerActorBehaviour(this, this);
        prop.setThrustDirection(JOMLConversion.toJOML(Vec3.atLowerCornerOf(this.getBlockDirection().getUnitVec3i())));

        prop.setParticleAmountUpdater(() -> 0.12 * Math.abs(this.rotationSpeed));
        prop.setParticleCountProperties(5, 2);
        prop.addSimpleLayer(this.getOffset(), this.getRadius());
        prop.setParticlePositionUpdater((v, random) -> {
            final PropellerActorBehaviour.PropellerLayer layer = prop.getLayers().get(random.nextInt(prop.getLayers().size()));
            final double R = Math.sqrt(Mth.lerp(random.nextFloat(), layer.innerRadiusSquared(), layer.outerRadiusSquared()));
            final double angle = Math.PI * 2.0 * random.nextFloat();
            v.set(Math.cos(angle) * R, layer.offset(), Math.sin(angle) * R);
            this.rot.transform(v);
        });

        return prop;
    }

    public BlockEntityPropeller getPropeller() {
        return this;
    }

    /**
     * @return The configured thrust. usually based off of server configuration.
     */
    public abstract double getConfigThrust();

    /**
     * @return The configured airflow multiplier. usually based off of server configuration.
     */
    public abstract double getConfigAirflow();

    /**
     * @return The radius of this propeller. usually used for particle spawning and propeller pushing
     */
    public abstract float getRadius();

    /**
     * @return The offset of the propeller layer along the main axis. usually used for particle spawning and propeller pushing
     */
    public float getOffset() {
        return 0;
    }

    @Override
    public void tick() {
        this.updateRotationSpeed();
        this.setPreviousAngle(this.getAngle());
        this.setAngle(this.getAngle() + this.rotationSpeed);

        // TODO: figure out why this is offset on sub-levels
        this.rot.set(this.getBlockDirection().getRotation());

        super.tick();

        if (this.isActive() && !this.isVirtual()) {
            this.onActiveTick();
        }
    }

    /**
     * Called every tick this propeller is active
     */
    public void onActiveTick() {
        this.prop.pushEntities();
        this.prop.spawnParticles();
    }

    protected float getDirectionIndependentSpeed() {
        return this.getBlockDirection().getAxisDirection().getStep() * this.rotationSpeed * (10f / 3) * (this.getBlockState().getValue(BasePropellerBlock.REVERSED) ? -1 : 1);
    }

    private void updateRotationSpeed() {
        float nextSpeed = convertToAngular(this.getSpeed());

        if (this.getSpeed() == 0)
            nextSpeed = 0;

        final float lerpAmount = 0.15f;
        this.rotationSpeed = Mth.lerp(lerpAmount, this.rotationSpeed, nextSpeed);
    }

    @Override
    public void onSpeedChanged(final float previousSpeed) {
        super.onSpeedChanged(previousSpeed);

        if (Math.abs(this.getSpeed()) > 0) {
            AeroAdvancements.FOR_EVERY_ACTION.awardToNearby(this.getBlockPos(), this.getLevel());
        }
    }

    @Override
    protected void write(final ValueOutput compound, final boolean clientPacket) {
        super.write(compound, clientPacket);

        compound.putFloat("RotationSpeed", this.rotationSpeed);
    }

    @Override
    protected void read(final ValueInput compound, final boolean clientPacket) {
        super.read(compound, clientPacket);

        this.rotationSpeed = compound.getFloatOr("RotationSpeed", 0);
    }

    /**
     * @return The previous angle for client rendering
     */
    public float getPreviousAngle() {
        return this.previousAngle;
    }

    public void setPreviousAngle(final float previousAngle) {
        this.previousAngle = previousAngle;
    }

    /**
     * @return The current lerped angle for client rendering
     */
    public float getAngle() {
        return this.angle;
    }

    public void setAngle(final float angle) {
        this.angle = angle;
    }

    /**
     * @return The direction this propeller block is currently facing. Usually used for direction conditions
     */
    @Override
    public Direction getBlockDirection() {
        return this.getBlockState().getValue(BlockStateProperties.FACING);
    }

    /**
     * @return The airflow INDEPENDENT of direction
     */
    @Override
    public double getAirflow() {
        return this.getConfigAirflow() * this.getDirectionIndependentSpeed();
    }

    /**
     * @return The thrust INDEPENDENT of direction
     */
    @Override
    public double getThrust() {
        return this.getConfigThrust() * this.getDirectionIndependentSpeed();
    }

    /**
     * @return Whether this propeller is active.
     */
    @Override
    public boolean isActive() {
        return Math.abs(this.rotationSpeed) > 0.01f;
    }

    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return this.prop.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}
