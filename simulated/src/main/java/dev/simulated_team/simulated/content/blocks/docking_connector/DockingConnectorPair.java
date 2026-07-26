package dev.simulated_team.simulated.content.blocks.docking_connector;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.config.server.physics.SimPhysics;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetPair;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.SimMagnet;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.SimMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class DockingConnectorPair extends MagnetPair<DockingConnectorBlockEntity> {

    private final Vector3d v = new Vector3d();

    private final Quaterniond orientation1 = new Quaterniond();
    private final Quaterniond orientation2 = new Quaterniond();
    private final Quaterniond relativeBlockOrientation = new Quaterniond();
    private final Quaterniond target = new Quaterniond();

    public DockingConnectorPair(final Level level, final BlockPos pos1, final BlockPos pos2) {
        super(level, pos1, pos2);
    }

    public static Vector3d getRelativeTipPosition(final DockingConnectorBlockEntity magnet1, final DockingConnectorBlockEntity magnet2, final Vector3d dest) {
        final Vec3 plotPos1 = magnet1.getTipPosition();
        final Vec3 plotPos2 = magnet2.getTipPosition();

        return getRelativePosition(magnet1, magnet2, plotPos1, plotPos2, dest);
    }

    public static Vector3d getAverageTipPosition(final DockingConnectorBlockEntity magnet1, final DockingConnectorBlockEntity magnet2, final Vector3d dest) {
        final Vec3 plotPos1 = magnet1.getTipPosition();
        final Vec3 plotPos2 = magnet2.getTipPosition();
        final SubLevel shell1 = magnet1.getLatestSubLevel();
        final SubLevel shell2 = magnet2.getLatestSubLevel();
        Vec3 pos1 = plotPos1;
        Vec3 pos2 = plotPos2;

        if (shell1 != null) {
            pos1 = shell1.logicalPose().transformPosition(pos1);
        }
        if (shell2 != null) {
            pos2 = shell2.logicalPose().transformPosition(pos2);
        }

        return dest.set(pos1.x + pos2.x, pos1.y + pos2.y, pos1.z + pos2.z).div(2);
    }

    @Override
    public void tick() {
        super.tick();
        this.dock(false);
    }

    private void toggleLock(final boolean newLock, final DockingConnectorBlockEntity dock1, final DockingConnectorBlockEntity dock2) {
        final Vector3d pos = getAverageTipPosition(dock1, dock2, new Vector3d());
        if (newLock) {
            this.level.playSound(null, pos.x, pos.y, pos.z, SimSoundEvents.DOCKING_CONNECTOR_DOCKS.event(), SoundSource.BLOCKS, 1.0f, 0.9f + this.level.getRandom().nextFloat() * 0.3f);

            SimAdvancements.A_CALCULATED_CONNECTION.awardToNearby(dock1.getBlockPos(), this.level, 16f);
            SimAdvancements.A_CALCULATED_CONNECTION.awardToNearby(dock2.getBlockPos(), this.level, 16f);
        } else {
            this.level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.NETHERITE_BLOCK_FALL, SoundSource.BLOCKS, 0.25f, 0.75f);
        }
    }

    public void dock(final boolean force) {
        if (this.level.getBlockEntity(this.blockPos1) instanceof final DockingConnectorBlockEntity dock1 &&
                this.level.getBlockEntity(this.blockPos2) instanceof final DockingConnectorBlockEntity dock2 &&
                dock1.isExtended() &&
                dock2.isExtended()) {

            boolean locked = dock1.isFeetExtended() && dock2.isFeetExtended();

            final Vector3d t = this.getRelativeOrientationDifference(dock1, dock2, new Vector3d());
            final Vector3d r = getRelativeTipPosition(dock1, dock2, new Vector3d());

            final double minDist = Math.min(dock1.closestPairDistance,dock2.closestPairDistance);
            if(!force && r.lengthSquared() > minDist * minDist * 2)
            {
                return; //avoid attempting dock if this pair is not the closest one
            }

            final SimPhysics config = SimConfigService.INSTANCE.server().physics;
            final double angleTolerance = Math.toRadians(config.dockingConnectorAngleTolerance.get());
            final double distanceTolerance = config.dockingConnectorDistanceTolerance.get();
            if (force || (t.lengthSquared() < angleTolerance * angleTolerance && r.lengthSquared() < distanceTolerance * distanceTolerance)) {
                if (force) {
                    locked = true;
                    dock1.state = DockingConnectorBlockEntity.DockingConnectorState.LOCKING;
                    dock2.state = DockingConnectorBlockEntity.DockingConnectorState.LOCKING;
                } else {
                    final boolean previousLockingState = dock1.state == DockingConnectorBlockEntity.DockingConnectorState.LOCKED && dock2.state == DockingConnectorBlockEntity.DockingConnectorState.LOCKED;
                    if (previousLockingState != locked) {
                        this.toggleLock(locked, dock1, dock2);
                    }
                }

                if (!this.level.isClientSide()) {
                    final ServerSubLevel firstSubLevel = (ServerSubLevel) Sable.HELPER.getContaining(dock1);
                    final ServerSubLevel secondSubLevel = (ServerSubLevel) Sable.HELPER.getContaining(dock2);

                    if (firstSubLevel != secondSubLevel) {
                        final boolean first;
                        if (firstSubLevel != null ^ secondSubLevel != null) {
                            first = firstSubLevel != null;
                        } else {
                            first = true;
                        }

                        final Quaterniond blockOrientation1 = new Quaterniond(dock1.getOrientation());
                        final Quaterniond blockOrientation2 = new Quaterniond(dock2.getOrientation());

                        final Quaterniond orientation = this.target.mul(blockOrientation1, new Quaterniond()).mul(blockOrientation2.conjugate());

                        this.orientation1.set(firstSubLevel == null ? this.orientation1.identity() : firstSubLevel.logicalPose().orientation());
                        this.orientation2.set(secondSubLevel == null ? this.orientation2.identity() : secondSubLevel.logicalPose().orientation());
                        this.orientation2.transformInverse(r);
                        this.orientation2.premul(this.orientation1.conjugate());

                        //orientation.conjugate();
                        dock1.setDock(dock2, locked, first ? orientation : null,r, this.orientation2);
                        dock2.setDock(dock1, locked, first ? null : orientation.conjugate(),r.negate(), this.orientation2.conjugate());
                        return;
                    }

                    dock1.setDock(dock2, locked, null,null,null);
                    dock2.setDock(dock1, locked, null,null,null);
                }
            } else {
                if (dock1.state == DockingConnectorBlockEntity.DockingConnectorState.LOCKING && dock2.state == DockingConnectorBlockEntity.DockingConnectorState.LOCKING) {
                    dock1.unDock();
                    dock2.unDock();
                }
            }
        }
    }

    public void unDock() {
        final BlockEntity blockEntity1 = this.level.getBlockEntity(this.blockPos1);
        final BlockEntity blockEntity2 = this.level.getBlockEntity(this.blockPos2);
        if (blockEntity1 instanceof final DockingConnectorBlockEntity dock1 && blockEntity2 instanceof final DockingConnectorBlockEntity dock2) {
            dock1.unDock();
            dock2.unDock();
            this.toggleLock(false, dock1, dock2);
        }
    }

    @Override
    protected double forceDistanceScale(double distance, final DockingConnectorBlockEntity magnet1, final DockingConnectorBlockEntity magnet2) {
        final double scale = this.preventPairFightingScale(distance, magnet1, magnet2);

        distance = 1+Math.max(0,distance-0.25);
        distance *= distance;
        distance *= distance;

        return 4 * scale / distance;
    }

    @Override
    protected double torqueDistanceScale(double distance, final DockingConnectorBlockEntity magnet1, final DockingConnectorBlockEntity magnet2) {
        final double scale = this.preventPairFightingScale(distance, magnet1, magnet2);

        distance = 1+Math.max(0,distance-0.5);
        distance *= distance;

        return scale / distance;
    }

    protected double preventPairFightingScale(final double distance, final DockingConnectorBlockEntity magnet1, final DockingConnectorBlockEntity magnet2) {
        final double d = Math.min(magnet1.closestPairDistance, magnet2.closestPairDistance);
        if (d < distance * 0.9) {
            final double k = d / (distance + 1E-5);
            return k * k;
        }
        return 1;
    }

    @Override
    protected boolean canConnect(final Vector3d relativePosition, final Vector3d moment1, final Vector3d moment2) {
        return relativePosition.lengthSquared() < 16 && moment1.dot(moment2) < 0;
    }

    @Override
    protected Vector3d getForce(final PairData data, final Vector3d f) {
        //magnet-like force
        f.set(data.moment2()).mul(-0.5 * data.moment1().dot(data.relativePosition()));
        f.fma(-0.5 * data.moment2().dot(data.relativePosition()), data.moment1());
        f.fma(data.moment1().dot(data.moment2()), data.relativePosition());
        f.fma(3 * (data.moment1().dot(data.relativePosition()) * data.moment2().dot(data.relativePosition())) / (data.relativePosition().lengthSquared() + 1E-12), data.relativePosition());
        return f.mul(data.forceScale() / 3);
    }

    @Override
    protected Vector3d getTorque(final PairData data, final Vector3d t) {
        data.moment2().cross(data.moment1(), t);
        return t.mul(data.torqueScale());
    }

    @Override
    public double getDampingRatio() {
        return 0.8;
    }

    public double getAccelerationLimit() {
        return SimConfigService.INSTANCE.server().physics.dockingConnectorLinearAccelerationClamping.get();
    }

    public double getAngularAccelerationLimit() {
        return SimConfigService.INSTANCE.server().physics.dockingConnectorAngularAccelerationClamping.get();
    }

    @Override
    protected Vector3d getSymmetricTorque(final PairData data, final Vector3d t) {
        this.getRelativeOrientationDifference(data.magnet1(), data.magnet2(), t);

        this.v.set(data.moment1()).sub(data.moment2()).div(2);
        this.v.mul(this.v.dot(t), t);

        return t.mul(data.torqueScale());
    }

    private Vector3d getRelativeOrientationDifference(final SimMagnet magnet1, final SimMagnet magnet2, final Vector3d t) {
        final SubLevel subLevel1 = magnet1.getLatestSubLevel();
        final SubLevel subLevel2 = magnet2.getLatestSubLevel();

        this.orientation1.set(subLevel1 == null ? this.orientation1.identity() : subLevel1.logicalPose().orientation());
        this.orientation2.set(subLevel2 == null ? this.orientation2.identity() : subLevel2.logicalPose().orientation());

        final Quaterniond blockOrientation1 = new Quaterniond(magnet1.getOrientation());
        final Quaterniond blockOrientation2 = new Quaterniond(magnet2.getOrientation());

        blockOrientation2.premul(this.orientation2).premul(this.orientation1.conjugate(new Quaterniond()));

        this.relativeBlockOrientation.set(blockOrientation1).div(blockOrientation2);

        SimMathUtils.clampQuaternionToGrid(this.relativeBlockOrientation, SimMathUtils.GridQuats.REAL.opposite(), this.target);
        this.relativeBlockOrientation.div(this.target);

        t.set(this.relativeBlockOrientation.x, this.relativeBlockOrientation.y, this.relativeBlockOrientation.z).mul(2);
        this.orientation1.transform(t);

        return t;
    }
}
