package dev.simulated_team.simulated.content.entities.honey_glue;

import com.zurrtum.create.api.schematic.requirement.SpecialEntityItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimEntityTypes;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.network.packets.honey_glue.HoneyGlueSyncBoundsPacket;
import foundry.veil.api.network.VeilPacketManager;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HoneyGlueEntity extends Entity implements SpecialEntityItemRequirement {
    private static final EntityDataAccessor<Optional<BlockPos>> BLOCK_MIN = SynchedEntityData.defineId(HoneyGlueEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Optional<BlockPos>> BLOCK_MAX = SynchedEntityData.defineId(HoneyGlueEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public BlockPos blockMin = null;
    public BlockPos blockMax = null;

    public static HoneyGlueEntity create(final EntityType<?> entityType, final Level world) {
        return new HoneyGlueEntity(entityType, world);
    }

    public HoneyGlueEntity(final EntityType<?> type, final Level world) {
        super(type, world);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        builder.define(BLOCK_MIN, Optional.empty());
        builder.define(BLOCK_MAX, Optional.empty());
    }

    @Override
    public void onSyncedDataUpdated(final EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);

        if (!this.level().isClientSide() || accessor != BLOCK_MIN && accessor != BLOCK_MAX) {
            return;
        }

        final Optional<BlockPos> syncedMin = this.entityData.get(BLOCK_MIN);
        final Optional<BlockPos> syncedMax = this.entityData.get(BLOCK_MAX);
        if (syncedMin.isEmpty() || syncedMax.isEmpty()) {
            return;
        }

        final BlockPos min = syncedMin.get();
        final BlockPos max = syncedMax.get();
        if (max.getX() <= min.getX() || max.getY() <= min.getY() || max.getZ() <= min.getZ()) {
            return;
        }

        this.applyBounds(new AABB(Vec3.atLowerCornerOf(min), Vec3.atLowerCornerOf(max)));
    }

    public HoneyGlueEntity(final Level world, final AABB boundingBox) {
        this(SimEntityTypes.HONEY_GLUE.get(), world);
        this.setBoundingBox(boundingBox);
        this.resetPositionToBounds();
        this.setBoundsAndSync(boundingBox);
    }

    @Override
    public void tick() {
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();

        if (this.level().isClientSide()) {
            this.updateClientBounds();
        } else {
            if (this.getBoundingBox().getXsize() <0.9f || this.getBoundingBox().getYsize() <0.9f || this.getBoundingBox().getZsize() < 0.9f) {
                Simulated.LOGGER.warn("Removing {} ({}) due to invalid bounds!", this.getUUID(), SimLang.builder().add(this.getName()).string());
                this.discard();
            }
        }
    }

    public void updateClientBounds() {
        if (this.blockMin != null && this.blockMax != null) {
            this.setBoundingBox(new AABB(Vec3.atLowerCornerOf(this.blockMin), Vec3.atLowerCornerOf(this.blockMax)));
        }
    }

    public void resetPositionToBounds() {
        final AABB bb = this.getBoundingBox();
        this.setPosRaw(bb.getCenter().x, bb.minY, bb.getCenter().z);
    }

    public void spawnParticles() {
        final AABB bb = this.getBoundingBox();
        final Vec3 origin = new Vec3(bb.minX, bb.minY, bb.minZ);
        final Vec3 extents = new Vec3(bb.getXsize(), bb.getYsize(), bb.getZsize());

        if (!(this.level() instanceof final ServerLevel serverLevel)) {
            return;
        }

        for (final Direction.Axis axis : Iterate.axes) {
            final Direction.AxisDirection positive = Direction.AxisDirection.POSITIVE;
            final double max = axis.choose(extents.x, extents.y, extents.z);
            final Vec3 normal = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis, positive)
                    .getUnitVec3i());
            for (final Direction.Axis axis2 : Iterate.axes) {
                if (axis2 == axis) {
                    continue;
                }
                final double max2 = axis2.choose(extents.x, extents.y, extents.z);
                final Vec3 normal2 = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis2, positive)
                        .getUnitVec3i());
                for (final Direction.Axis axis3 : Iterate.axes) {
                    if (axis3 == axis2 || axis3 == axis) {
                        continue;
                    }
                    final double max3 = axis3.choose(extents.x, extents.y, extents.z);
                    final Vec3 normal3 = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis3, positive)
                            .getUnitVec3i());

                    for (int i = 0; i <= max * 2; i++) {
                        for (final int o1 : Iterate.zeroAndOne) {
                            for (final int o2 : Iterate.zeroAndOne) {
                                final Vec3 v = origin.add(normal.scale(i / 2f))
                                        .add(normal2.scale(max2 * o1))
                                        .add(normal3.scale(max3 * o2));

                                serverLevel.sendParticles(
                                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Blocks.HONEY_BLOCK)), v.x,
                                        v.y, v.z, 1, 0, 0, 0, 0);

                            }
                        }
                    }
                    break;
                }
                break;
            }
        }
    }

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public float rotate(final Rotation transformRotation) {
        final AABB bb = this.getBoundingBox().move(this.position().scale(-1));
        if (transformRotation == Rotation.CLOCKWISE_90 || transformRotation == Rotation.COUNTERCLOCKWISE_90) {
            this.setBoundsAndSync(new AABB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX).move(this.position()));
        }

        return super.rotate(transformRotation);
    }

    @Override
    public void setPos(final double x, final double y, final double z) {
        final AABB bb = this.getBoundingBox();
        this.setPosRaw(x, y, z);
        final Vec3 center = bb.getCenter();
        this.setBoundingBox(bb.move(-center.x, -bb.minY, -center.z)
                .move(x, y, z));
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput output) {
        final Vec3 position = this.position();
        final AABB savedBounds = this.getBoundingBox().move(position.scale(-1));

        output.store("From", Vec3.CODEC, new Vec3(savedBounds.minX, savedBounds.minY, savedBounds.minZ));
        output.store("To", Vec3.CODEC, new Vec3(savedBounds.maxX, savedBounds.maxY, savedBounds.maxZ));
        output.store("Pos", Vec3.CODEC, position);
    }

    @Override
    protected void readAdditionalSaveData(final ValueInput input) {
        final Vec3 pos = input.read("Pos", Vec3.CODEC).orElse(this.position());
        final Vec3 from = input.read("From", Vec3.CODEC).orElse(Vec3.ZERO);
        final Vec3 to = input.read("To", Vec3.CODEC).orElse(Vec3.ZERO);
        final AABB bb = new AABB(from, to).move(pos);

        final Level level = this.level();

        if (level.isClientSide()) {
            this.setBounds(bb);
        } else {
            this.setBoundsAndSync(bb);
        }
    }

    @Override
    public void thunderHit(final ServerLevel serverLevel, final LightningBolt lightningBolt) {
    }

    @Override
    public double getEyeY() {
        return 0.0f;
    }

    public void setBoundsAndSync(final AABB bounds) {
        final Level level = this.level();

        if (level.isClientSide()) {
            return;
        }

        this.setBounds(bounds);
        this.syncBounds(null);
    }

    public void setBoundsAndSync(final AABB bounds, @Nullable final Player player) {
        final Level level = this.level();

        if (level.isClientSide()) {
            return;
        }

        this.setBounds(bounds);
        this.syncBounds(player);
    }

    public void syncBounds(@Nullable final Player player) {
        VeilPacketManager.tracking(this).sendPacket(new HoneyGlueSyncBoundsPacket(this.getBoundingBox(), this.getId(), player != null ? player.getUUID() : null));
    }

    public void syncBoundsTo(final ServerPlayer player) {
        VeilPacketManager.player(player).sendPacket(new HoneyGlueSyncBoundsPacket(this.getBoundingBox(), this.getId(), null));
    }

    public void setBounds(final AABB bounds) {
        this.applyBounds(bounds);

        if (!this.level().isClientSide()) {
            this.entityData.set(BLOCK_MIN, Optional.of(this.blockMin));
            this.entityData.set(BLOCK_MAX, Optional.of(this.blockMax));
        }
    }

    private void applyBounds(final AABB bounds) {
        this.setBoundingBox(bounds);
        this.resetPositionToBounds();

        this.blockMin = BlockPos.containing(bounds.getMinPosition());
        this.blockMax = BlockPos.containing(bounds.getMaxPosition());
    }

    @Override
    public void move(final MoverType typeIn, final Vec3 pos) {
        if (!this.level().isClientSide() && this.isAlive() && pos.lengthSqr() > 0.0D) {
            this.discard();
        }
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void refreshDimensions() {
    }

    @Override
    public boolean hurtClient(final DamageSource source) {
        return false;
    }

    @Override
    public boolean hurtServer(final ServerLevel level, final DamageSource source, final float amount) {
        return false;
    }

    @Override
    public ItemRequirement getRequiredItems() {
        return new ItemRequirement(ItemRequirement.ItemUseType.DAMAGE, SimItems.HONEY_GLUE.get());
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public boolean contains(final BlockPos pos) {
        return this.getBoundingBox().contains(Vec3.atCenterOf(pos));
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull final Pose pose) {
        return super.getDimensions(pose).withEyeHeight(0.0F);
    }
}
