package dev.simulated_team.simulated.content.blocks.redstone_magnet;

import dev.simulated_team.simulated.compat.create.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.particle.MagnetFieldParticleData;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.SimMovementContext;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RedstoneMagnetBlockEntity extends SmartBlockEntity implements SimMagnet {
    public static MagnetMap<RedstoneMagnetBlockEntity> GLOBAL_REDSTONE_MAGNET_MAP = new MagnetMap<>();
    private final HashSet<MagnetParticleEmitter> particleEmitters = new HashSet<>();
    public SubLevel latestSubLevel;
    public MagnetBehaviour magnet;
    public HashMap<Vector3d, Vector3d> nearbyMagnetPositions = new HashMap<>();
    protected boolean powered;
    protected int signalStrength;

    public RedstoneMagnetBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.updateSignal();
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour<?>> behaviours) {
        this.magnet = new MagnetBehaviour(this, GLOBAL_REDSTONE_MAGNET_MAP);
        behaviours.add(this.magnet);
    }

    public void updateSignal() {
        final boolean shouldPower = this.level.hasNeighborSignal(this.worldPosition);
        final int newSignalStrength = this.level.getBestNeighborSignal(this.worldPosition);
        if (newSignalStrength != this.signalStrength) {
            this.signalStrength = newSignalStrength;
            this.powered = shouldPower;
            this.sendData();
        }
    }

    @Override
    public void tick() {
        super.tick();
//        final Vector3i jomlPos = new Vector3i(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());

        this.latestSubLevel = Sable.HELPER.getContaining(this);
//        if (this.latestSubLevel != null && (this.latestSubLevel.isRemoved() || !this.latestSubLevel.getPlot().getBoundingBox().contains(jomlPos))) {
//            this.latestSubLevel = null;
//        }

        //todo: do this in the behaviour or using common method or something
        if (this.latestSubLevel != null) {
            final MagnetMap<RedstoneMagnetBlockEntity> map = RedstoneMagnetBlockEntity.GLOBAL_REDSTONE_MAGNET_MAP;
            final SimMovementContext context = SimMovementContext.getMovementContext(this.getLevel(), this.getBlockPos().getCenter());
            final List<SimMovementContext> contexts = map.findNearby(context);
            for (final SimMovementContext movementContext : contexts) {
                if (movementContext.subLevel() != this.latestSubLevel) {
                    map.tryAddPair(this.getLevel(), this.getBlockPos(), movementContext.localBlockPos(), MagnetPair::new);
                }
            }
        }

        if (this.level.isClientSide()) {
            return;
        }

        this.spawnParticles();
        for (final MagnetParticleEmitter emitter : this.particleEmitters) {
            emitter.update();
        }
        this.particleEmitters.removeIf(x -> x.time < 0);
    }

    @Override
    public void write(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        compound.putBoolean("IsPowered", this.powered);
        compound.putInt("SignalStrength", this.signalStrength);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.powered = compound.getBooleanOr("IsPowered", false);
        this.signalStrength = compound.getIntOr("SignalStrength", 0);
        super.read(compound, registries, clientPacket);
    }

    private void spawnParticles() {
        final float probability = 0.1f * (this.signalStrength / 15f);
        if (probability > 0 && this.level.random.nextFloat() < probability) {
            final boolean negative = this.level.random.nextBoolean();

            Vec3i dir = (this.getBlockState().getValue(RedstoneMagnetBlock.FACING)).getUnitVec3i();
            if (negative) {
                dir = dir.multiply(-1);
            }

            final BlockPos blockpos = this.getBlockPos().offset(dir);
            if (this.level.getBlockState(blockpos).isSolidRender()) {
                return;
            }
            final Vector3d offset = JOMLConversion.toJOML(VecHelper.offsetRandomly(new Vec3(0, 0, 0), this.level.random, 0.35f));
            final Vector3d pos = JOMLConversion.toJOML(Vec3.atLowerCornerOf(dir));
            offset.fma(-pos.dot(offset), pos);
            pos.mul(0.55);
            pos.add(offset);

            final SimMovementContext context = SimMovementContext.getMovementContext(this.level, Vec3.atCenterOf(this.getBlockPos()));
            context.orientation().transform(pos);
            pos.add(JOMLConversion.toJOML(context.globalPosition()));

            this.nearbyMagnetPositions.clear();
            final List<SimMovementContext> contexts = GLOBAL_REDSTONE_MAGNET_MAP.findNearby(context);
            contexts.add(context);

            final Level level = context.level();
            for (final SimMovementContext movementContext : contexts) {
                final RedstoneMagnetBlockEntity otherMagnet = (RedstoneMagnetBlockEntity) level.getBlockEntity(movementContext.localBlockPos());
                if (otherMagnet == null) {
                    continue;
                }
                final Vector3d otherMagneticMoment = otherMagnet.setMagneticMoment(new Vector3d());
                movementContext.orientation().transform(otherMagneticMoment);
                this.nearbyMagnetPositions.put(JOMLConversion.toJOML(movementContext.globalPosition()), otherMagneticMoment);
            }

            final int steps = 4 + (int) (20 * (this.signalStrength / 15f) * this.level.random.nextFloat());

            this.particleEmitters.add(new MagnetParticleEmitter(pos, this.nearbyMagnetPositions, steps, this.level, negative));
        }


    }

    @Override
    public Quaternionf getOrientation() {
        return this.getBlockState().getValue(BlockStateProperties.FACING).getRotation();
    }

    @Override
    public SubLevel getLatestSubLevel() {
        if (this.latestSubLevel != null && this.latestSubLevel.isRemoved()) {
            this.latestSubLevel = null;
        }
        return this.latestSubLevel;
    }

    @Override
    public Vector3d setMagneticMoment(final Vector3d v) {
        v.set(JOMLConversion.toJOML(Vec3.atLowerCornerOf(this.getBlockState().getValue(RedstoneMagnetBlock.FACING).getUnitVec3i())));
        v.mul((this.signalStrength / 15.0) * Math.sqrt(SimConfigService.INSTANCE.server().physics.redstoneMagnetStrength.get()));
        return v;
    }

    @Override
    public Vec3 getMagnetPosition() {
        return Vec3.atCenterOf(this.getBlockPos());
    }

    @Override
    public boolean magnetActive() {
        return this.signalStrength > 0;
    }

    private static class MagnetParticleEmitter {
        protected final HashMap<Vector3d, Vector3d> nearbyMagnets;
        protected final Vector3d pos;
        protected final Vector3d oldNudge = new Vector3d();
        protected final Vector3d newNudge = new Vector3d();
        protected int time;
        protected final int startTime;
        protected final Level level;
        protected final boolean negative;

        public MagnetParticleEmitter(final Vector3d startPos, final HashMap<Vector3d, Vector3d> nearbyMagnets, final int maxTime, final Level level, final boolean negative) {
            this.pos = new Vector3d(startPos);
            this.nearbyMagnets = nearbyMagnets;
            this.time = maxTime;
            this.startTime = maxTime;
            this.level = level;
            this.negative = negative;
            this.rk4(this.pos, this.newNudge);
        }


        public void update() {
            this.time--;

            if (this.time < 0) {
                return;
            }
            if (!this.level.getBlockState(BlockPos.containing(this.pos.x, this.pos.y, this.pos.z)).isAir()) {
                this.time = -1;
                return;
            }
            this.pos.add(this.newNudge);
            this.oldNudge.set(this.newNudge);
            this.rk4(this.pos, this.newNudge);
            ((ServerLevel) this.level).sendParticles(new MagnetFieldParticleData(this.negative), this.pos.x, this.pos.y, this.pos.z, 1, 0.01, 0.01, 0.01, 0);
//            ((ServerLevel) this.level).sendParticles(new MagnetFieldParticleData2(JOMLConversion.toMojang(oldNudge),JOMLConversion.toMojang(newNudge),this.negative,Math.min(this.time+1,this.startTime-time)), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
        static final Vector3d k1 = new Vector3d();
        static final Vector3d k2 = new Vector3d();
        static final Vector3d k3 = new Vector3d();
        static final Vector3d k4 = new Vector3d();
        static final Vector3d posTemp = new Vector3d();
        void rk4(final Vector3d pos, final Vector3d nudgeOut)
        {
            final double dt = 0.2;
            this.getField(pos,k1);
            this.getField(pos.fma(dt*0.5,k1,posTemp),k2);
            this.getField(pos.fma(dt*0.5,k2,posTemp),k3);
            this.getField(pos.fma(dt,k3,posTemp),k4);
            nudgeOut.set(k1).fma(2,k2).fma(2,k3).add(k4).mul(dt/6);
        }
        static final Vector3d currentField = new Vector3d();
        static final Vector3d relativePos = new Vector3d();
        static final Vector3d moment = new Vector3d();
        void getField(final Vector3d pos, final Vector3d field)
        {
            field.zero();
            for (final Map.Entry<Vector3d, Vector3d> entry : this.nearbyMagnets.entrySet()) {
                relativePos.set(pos).sub(entry.getKey());
                moment.set(entry.getValue()).mul(this.negative ? -1 : 1);
                if (moment.lengthSquared() == 0) {
                    continue;
                }

                final double distanceSq = relativePos.lengthSquared();
                if (distanceSq < 0.2) {
                    this.time = -1;
                    return;
                }

                final double d = moment.dot(relativePos) / distanceSq;
                currentField.set(relativePos).mul(3 * d);
                currentField.sub(moment);
                currentField.div(distanceSq);
                field.add(currentField);
            }
            field.normalize();
        }

        private Matrix3d generateOuterProduct(final Vector3d v1, final Vector3d v2) {
            return new Matrix3d(v1.x * v2.x, v1.x * v2.y, v1.x * v2.z, v1.y * v2.x, v1.y * v2.y, v1.y * v2.z, v1.z * v2.x, v1.z * v2.y, v1.z * v2.z);
        }
    }

}
