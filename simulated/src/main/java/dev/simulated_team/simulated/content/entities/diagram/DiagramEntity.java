package dev.simulated_team.simulated.content.entities.diagram;

import com.zurrtum.create.api.schematic.requirement.SpecialEntityItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.networking.ISyncPersistentData;
import com.zurrtum.create.foundation.utility.IInteractionChecker;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimEntityTypes;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.index.SimStats;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramOpenPacket;
import dev.simulated_team.simulated.util.SimColors;
import foundry.veil.api.network.VeilPacketManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class DiagramEntity extends HangingEntity implements ISyncPersistentData, IInteractionChecker, SpecialEntityItemRequirement {
    private static final Map<ResourceKey<Level>, Map<ServerSubLevel, DiagramRecordingTicket>> queuedDiagramRecordings = new WeakHashMap<>();

    protected int size;
    protected Direction verticalOrientation;
    protected DiagramConfig config;

    public static DiagramEntity create(final EntityType<? extends HangingEntity> entityType, final Level world) {
        return new DiagramEntity(entityType, world);
    }

    @SuppressWarnings("unchecked")
    public DiagramEntity(final EntityType<? extends HangingEntity> entityType, final Level level) {
        super(entityType, level);
        this.size = 1;
        this.config = DiagramConfig.makeDefault(this);
    }

    public DiagramEntity(final Level world, final BlockPos pos, final Direction facing, final Direction verticalOrientation) {
        super(SimEntityTypes.CONTRAPTION_DIAGRAM.get(), world, pos);

        for (int size = 3; size > 0; size--) {
            this.size = size;
            this.updateFacingWithBoundingBox(facing, verticalOrientation);
            if (this.survives()) {
                break;
            }
        }

        this.config = DiagramConfig.makeDefault(this);
    }

    public static void queueDiagramDataFor(final SubLevel subLevel, final ServerPlayer player) {
        if (!(subLevel instanceof final ServerSubLevel serverSubLevel)) return;
        serverSubLevel.enableIndividualQueuedForcesTracking(true);

        final Map<ServerSubLevel, DiagramRecordingTicket> map = queuedDiagramRecordings.get(serverSubLevel.getLevel().dimension());
        DiagramRecordingTicket ticket = map != null ? map.get(serverSubLevel) : null;

        if (ticket != null && !ticket.isValid()) {
            queuedDiagramRecordings.remove(serverSubLevel);
            ticket = null;
        }

        if (ticket == null) {
            final List<ServerPlayer> players = new ObjectArrayList<>();
            ticket = new DiagramRecordingTicket(serverSubLevel, players);
            queuedDiagramRecordings.computeIfAbsent(serverSubLevel.getLevel().dimension(), x -> new Object2ObjectOpenHashMap<>())
                    .put(serverSubLevel, ticket);
        }

        final List<ServerPlayer> players = ticket.players();
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public static void postPhysicsTick(final Level level) {
        final Map<ServerSubLevel, DiagramRecordingTicket> map = queuedDiagramRecordings.get(level.dimension());
        if (map == null) return;

        final var iter = map.entrySet().iterator();

        while (iter.hasNext()) {
            final var entry = iter.next();
            final ServerSubLevel subLevel = entry.getKey();
            final DiagramRecordingTicket ticket = entry.getValue();

            if (!ticket.isValid()) {
                iter.remove();

                subLevel.enableIndividualQueuedForcesTracking(false);
                continue;
            }

            final DiagramDataPacket dataPacket = makeDiagramDataPacket(ticket.subLevel());

            for (final var player : ticket.players()) {
                VeilPacketManager.player(player).sendPacket(dataPacket);
            }

            subLevel.enableIndividualQueuedForcesTracking(false);
            iter.remove();
        }
    }

    private static DiagramDataPacket makeDiagramDataPacket(final ServerSubLevel serverSubLevel) {
        final MassData massTracker = serverSubLevel.getMassTracker();
        final Object2ObjectMap<ForceGroup, List<QueuedForceGroup.PointForce>> sentForces = new Object2ObjectOpenHashMap<>();

        final Object2ObjectMap<ForceGroup, QueuedForceGroup> queuedForceGroups = serverSubLevel.getQueuedForceGroups();

        final ServerLevel level = serverSubLevel.getLevel();
        final SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(level);
        final double timeStep = 1.0 / 20.0 / physicsSystem.getConfig().substepsPerTick;

        if (queuedForceGroups != null) {
            for (final Map.Entry<ForceGroup, QueuedForceGroup> entry : queuedForceGroups.entrySet()) {
                final ForceGroup key = entry.getKey();
                final QueuedForceGroup value = entry.getValue();
                final List<QueuedForceGroup.PointForce> pointForces = new ObjectArrayList<>();

                for (final QueuedForceGroup.PointForce pointForce : value.getRecordedPointForces()) {
                    final Vector3dc force = new Vector3d(pointForce.force()).div(timeStep);
                    pointForces.add(new QueuedForceGroup.PointForce(pointForce.point(), force));
                }

                if (pointForces.isEmpty()) {
                    continue;
                }

                sentForces.put(key, pointForces);
            }
        }

        final Vector3dc centerOfMass = serverSubLevel.getMassTracker().getCenterOfMass();
        final Pose3d pose = serverSubLevel.logicalPose();
        final Vector3d localGravity = pose.transformNormalInverse(DimensionPhysicsData.getGravity(level)).mul(serverSubLevel.getMassTracker().getMass());

        sentForces.put(ForceGroups.GRAVITY.get(), List.of(new QueuedForceGroup.PointForce(new Vector3d(centerOfMass), localGravity)));

        return new DiagramDataPacket(sentForces, massTracker.getMass());
    }

    @Override
    public void remove(final RemovalReason reason) {
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput output) {
        output.putByte("Facing", (byte) this.getDirection().get3DDataValue());
        output.putByte("Orientation", (byte) this.verticalOrientation.get3DDataValue());
        output.putInt("Size", this.size);

        if (this.config != null) {
            output.store("Config", DiagramConfig.CODEC, this.config);
        }

        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        this.setDirectionRaw(Direction.from3DDataValue(input.getByteOr("Facing", (byte) Direction.SOUTH.get3DDataValue())));
        this.verticalOrientation = Direction.from3DDataValue(input.getByteOr("Orientation", (byte) Direction.DOWN.get3DDataValue()));
        this.size = input.getIntOr("Size", 1);

        this.config = input.read("Config", DiagramConfig.CODEC).orElseGet(() -> DiagramConfig.makeDefault(this));

        super.readAdditionalSaveData(input);
        this.updateFacingWithBoundingBox(this.getDirection(), this.verticalOrientation);
    }

    protected void updateFacingWithBoundingBox(final Direction facing, final Direction verticalOrientation) {
        Objects.requireNonNull(facing);
        this.setDirectionRaw(facing);
        this.verticalOrientation = verticalOrientation;
        if (facing.getAxis()
                .isHorizontal()) {
            this.setXRot(0.0F);
            this.setYRot(this.getDirection().get2DDataValue() * 90);
        } else {
            this.setXRot(-90 * facing.getAxisDirection()
                    .getStep());
            this.setYRot(verticalOrientation.getAxis()
                    .isHorizontal() ? 180 + verticalOrientation.toYRot() : 0);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    public EntityDimensions getDimensions(final Pose pose) {
        return super.getDimensions(pose).withEyeHeight(0);
    }

    @Override
    protected AABB calculateBoundingBox(final BlockPos blockPos, final Direction direction) {
        Vec3 pos = Vec3.atLowerCornerOf(this.getPos())
                .add(.5, .5, .5)
                .subtract(Vec3.atLowerCornerOf(direction.getUnitVec3i())
                        .scale(0.46875));
        double d1 = pos.x;
        double d2 = pos.y;
        double d3 = pos.z;
        this.setPosRaw(d1, d2, d3);

        final Axis axis = direction.getAxis();
        if (this.size == 2)
            pos = pos.add(Vec3.atLowerCornerOf(axis.isHorizontal() ? direction.getCounterClockWise()
                                    .getUnitVec3i()
                                    : this.verticalOrientation.getClockWise()
                                    .getUnitVec3i())
                            .scale(0.5))
                    .add(Vec3
                            .atLowerCornerOf(axis.isHorizontal() ? Direction.UP.getUnitVec3i()
                                    : direction == Direction.UP ? this.verticalOrientation.getUnitVec3i()
                                    : this.verticalOrientation.getOpposite()
                                    .getUnitVec3i())
                            .scale(0.5));

        d1 = pos.x;
        d2 = pos.y;
        d3 = pos.z;

        double d4 = this.getWidth();
        double d5 = this.getHeight();
        double d6 = this.getWidth();
        final Axis direction$axis = this.getDirection().getAxis();
        switch (direction$axis) {
            case X:
                d4 = 1.0D;
                break;
            case Y:
                d5 = 1.0D;
                break;
            case Z:
                d6 = 1.0D;
        }

        d4 = d4 / 32.0D;
        d5 = d5 / 32.0D;
        d6 = d6 / 32.0D;

        return new AABB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6);
    }

    @Override
    public void recalculateBoundingBox() {
        if (this.getDirection() != null && this.verticalOrientation != null) {
            this.setBoundingBox(this.calculateBoundingBox(this.pos, this.getDirection()));
        }
    }

    @Override
    public Vec3 getLightProbePosition(final float partialTicks) {
        return this.position();
    }

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        }
        final int i = Math.max(1, this.getWidth() / 16);
        final int j = Math.max(1, this.getHeight() / 16);
        final BlockPos blockpos = this.pos.relative(this.getDirection().getOpposite());
        final Direction upDirection = this.getDirection().getAxis()
                .isHorizontal() ? Direction.UP
                : this.getDirection() == Direction.UP ? this.verticalOrientation : this.verticalOrientation.getOpposite();
        final Direction newDirection = this.getDirection().getAxis()
                .isVertical() ? this.verticalOrientation.getClockWise() : this.getDirection().getCounterClockWise();
        final BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int k = 0; k < i; ++k) {
            for (int l = 0; l < j; ++l) {
                final int i1 = (i - 1) / -2;
                final int j1 = (j - 1) / -2;
                blockpos$mutable.set(blockpos)
                        .move(newDirection, k + i1)
                        .move(upDirection, l + j1);
                final BlockState blockstate = this.level().getBlockState(blockpos$mutable);
                if (Block.canSupportCenter(this.level(), blockpos$mutable, this.getDirection())) {
                    continue;
                }
                if (!blockstate.isSolid() && !DiodeBlock.isDiode(blockstate)) {
                    return false;
                }
            }
        }

        return this.level().getEntities(this, this.getBoundingBox(), entity -> entity instanceof HangingEntity)
                .isEmpty();
    }

    public int getWidth() {
        return 16 * this.size;
    }

    public int getHeight() {
        return 16 * this.size;
    }

    @Override
    public void dropItem(final ServerLevel level, @Nullable final Entity breaker) {
        if (!level.getGameRules()
                .getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }

        this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
        if (breaker instanceof final Player playerentity) {
            if (playerentity.getAbilities().instabuild) {
                return;
            }
        }

        this.spawnAtLocation(level, SimItems.CONTRAPTION_DIAGRAM.asStack(), 0.0F);
    }

    @Override
    public ItemStack getPickResult() {
        return SimItems.CONTRAPTION_DIAGRAM.asStack();
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void snapTo(final double x, final double y, final double z, final float yRot, final float xRot) {
        this.setPos(x, y, z);
    }

    @Override
    protected void lerpPositionAndRotationStep(final int steps, final double pX, final double pY, final double pZ,
                                               final double pYRot, final double pXRot) {
        final BlockPos blockpos =
                this.pos.offset(BlockPos.containing(pX - this.getX(), pY - this.getY(), pZ - this.getZ()));
        this.setPos(blockpos.getX(), blockpos.getY(), blockpos.getZ());
    }

    @Override
    public void setPos(final double pX, final double pY, final double pZ) {
        this.setPosRaw(pX, pY, pZ);
        super.setPos(pX, pY, pZ);
    }

    @Override
    public InteractionResult interactAt(final Player player, final Vec3 vec, final InteractionHand hand) {

        if (this.level().isClientSide()) {
            final SubLevel subLevel = Sable.HELPER.getContaining(this);

            if (subLevel == null) {
                player.displayClientMessage(SimLang.translate("contraption_diagram.cannot_use").color(SimColors.NUH_UH_RED).component(), true);
            }
        } else {
            final SubLevel subLevel = Sable.HELPER.getContaining(this);

            if (subLevel != null) {
                DiagramEntity.queueDiagramDataFor(subLevel, ((ServerPlayer) player));
                VeilPacketManager.player((ServerPlayer) player).sendPacket(new DiagramOpenPacket(this.getId(), this.config));
                SimStats.INTERACT_WITH_CONTRAPTION_DIAGRAM.awardTo(player);
                SimAdvancements.MEASURE_ONCE_BUILD_TWICE.awardTo(player);
            }
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public void onPersistentDataUpdated() {

    }

    @Override
    public boolean canPlayerUse(final Player player) {
        final AABB box = this.getBoundingBox();

        double dx = 0;
        if (box.minX > player.getX()) {
            dx = box.minX - player.getX();
        } else if (player.getX() > box.maxX) {
            dx = player.getX() - box.maxX;
        }

        double dy = 0;
        if (box.minY > player.getY()) {
            dy = box.minY - player.getY();
        } else if (player.getY() > box.maxY) {
            dy = player.getY() - box.maxY;
        }

        double dz = 0;
        if (box.minZ > player.getZ()) {
            dz = box.minZ - player.getZ();
        } else if (player.getZ() > box.maxZ) {
            dz = player.getZ() - box.maxZ;
        }

        return (dx * dx + dy * dy + dz * dz) <= 64.0D;
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, SimItems.CONTRAPTION_DIAGRAM.get());
    }

    public void setConfig(final DiagramConfig config) {
        this.config = config;
    }
}
