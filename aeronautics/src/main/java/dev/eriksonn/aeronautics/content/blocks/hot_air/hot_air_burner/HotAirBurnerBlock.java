package dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner;


import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import dev.eriksonn.aeronautics.index.AeroBlockShapes;
import dev.eriksonn.aeronautics.index.AeroTags;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.math.LevelReusedVectors;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.LevelExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.jetbrains.annotations.Nullable;

public class HotAirBurnerBlock extends Block implements IBE<HotAirBurnerBlockEntity>, IWrenchable {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public HotAirBurnerBlock(final Properties properties) {
        super(properties);
    }

    public static int getLightPower(final BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public void entityInside(final BlockState state, final Level level, final BlockPos pos, final Entity entity,
                             final InsideBlockEffectApplier effectApplier, final boolean entityCanPoseAsStanding) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final HotAirBurnerBlockEntity be) {
            if (!entity.fireImmune() && state.getValue(POWERED) && entity instanceof LivingEntity) {
                final LevelReusedVectors jomlSink = ((LevelExtension) level).sable$getJOMLSink();
                final SubLevel subLevel = Sable.HELPER.getContaining(level, pos);

                final Vector3d burnerCubePos = JOMLConversion.atCenterOf(pos).add(0.0, 0.25, 0.0);
                if (subLevel != null)
                    subLevel.logicalPose().transformPosition(burnerCubePos);

                final AABB entityAABB = entity.getBoundingBox();
                final Vector3d entityCenter = JOMLConversion.toJOML(entityAABB.getCenter());
                final Vector3d sideLengths = new Vector3d(entityAABB.getXsize(), entityAABB.getYsize(), entityAABB.getZsize());

                final OrientedBoundingBox3d burnerBounds = new OrientedBoundingBox3d(burnerCubePos, new Vector3d(10.0 / 16.0), subLevel != null ? subLevel.logicalPose().orientation() : JOMLConversion.QUAT_IDENTITY, jomlSink);
                final OrientedBoundingBox3d entityBounds = new OrientedBoundingBox3d(entityCenter, sideLengths, JOMLConversion.QUAT_IDENTITY, jomlSink);
                if (OrientedBoundingBox3d.sat(burnerBounds, entityBounds).lengthSquared() > 0.0) {
                    entity.hurt(level.damageSources().inFire(), (float) be.getSignalStrength() / 7.5F);
                }
            }

            super.entityInside(state, level, pos, entity, effectApplier, entityCanPoseAsStanding);
        }
    }

    @Override
    public Class<HotAirBurnerBlockEntity> getBlockEntityClass() {
        return HotAirBurnerBlockEntity.class;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, VARIANT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hitResult) {
        final Variant conversion = Variant.getConversionFromItem(stack.getItem());

        if (conversion != null) {
            final Variant current = state.getValue(VARIANT);
            if (conversion != current) {
                level.setBlockAndUpdate(pos, state.setValue(VARIANT, conversion));
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), conversion.sound, SoundSource.BLOCKS, 1, 1, false);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block blockIn, final @Nullable Orientation orientation,
                                final boolean isMoving) {
        if (level.isClientSide())
            return;
        this.withBlockEntityDo(level, pos, HotAirBurnerBlockEntity::updateSignal);
        final boolean previouslyPowered = state.getValue(POWERED);
        if (previouslyPowered != level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    @Override
    public BlockEntityType<? extends HotAirBurnerBlockEntity> getBlockEntityType() {
        return AeroBlockEntityTypes.HOT_AIR_BURNER.get();
    }

    @Override
    public VoxelShape getShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        if (pContext == CollisionContext.empty())
            return AeroBlockShapes.HOT_AIR_BURNER_SMOKE_CLIP;
        return AeroBlockShapes.HOT_AIR_BURNER;
    }

    @Override
    public RenderShape getRenderShape(final BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return AeroBlockShapes.HOT_AIR_BURNER_PLAYER_COLLISION;
    }

    @Override
    public VoxelShape getVisualShape(final BlockState pState, final BlockGetter pLevel, final BlockPos pPos, final CollisionContext pContext) {
        return AeroBlockShapes.HOT_AIR_BURNER;
    }

    public enum Variant implements StringRepresentable {
        FIRE("fire", SoundEvents.NETHERRACK_PLACE),
        SOUL_FIRE("soulful", SoundEvents.SOUL_SAND_PLACE);

        public final String name;
        public final SoundEvent sound;

        Variant(final String name, final SoundEvent sound) {
            this.name = name;
            this.sound = sound;
        }

        public static Variant getConversionFromItem(final Item item) {
            if (item.builtInRegistryHolder().is(AeroTags.ItemTags.BURNER_FIRE)) return FIRE;
            if (item.builtInRegistryHolder().is(ItemTags.SOUL_FIRE_BASE_BLOCKS)) return SOUL_FIRE;
            return null;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
