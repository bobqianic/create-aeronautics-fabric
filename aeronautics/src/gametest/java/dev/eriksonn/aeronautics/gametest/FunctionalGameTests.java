package dev.eriksonn.aeronautics.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllHandle;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.fluids.drain.ItemDrainBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler;
import com.zurrtum.create.content.kinetics.drill.DrillBlockEntity;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.zurrtum.create.infrastructure.packet.c2s.ValueSettingsPacket;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.HotAirBurnerBlockEntity;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class FunctionalGameTests {

    @GameTest(maxTicks = 20)
    public void liftingGasProviderAccessorsSurviveRuntimeRemapping(final GameTestHelper helper) {
        final BlockPos relativePos = new BlockPos(1, 1, 1);
        final BlockPos worldPos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos, AeroBlocks.HOT_AIR_BURNER.getDefaultState());

        if (!(helper.getLevel().getBlockEntity(worldPos) instanceof HotAirBurnerBlockEntity burner)) {
            throw helper.assertionException("Hot air burner block entity was not created");
        }

        // Dispatch through the custom interface: this is where vanilla-named bridge methods
        // produced AbstractMethodError after Fabric remapped BlockEntity in production.
        final BlockEntityLiftingGasProvider provider = burner;
        if (provider.getProviderLevel() != helper.getLevel()) {
            throw helper.assertionException("Lifting gas provider returned the wrong level");
        }
        if (!provider.getProviderBlockPos().equals(worldPos)) {
            throw helper.assertionException("Lifting gas provider returned the wrong block position");
        }

        helper.succeed();
    }

    @GameTest(maxTicks = 80, skyAccess = true)
    public void itemDrainAcceptsThrownBucketOnSubLevel(final GameTestHelper helper) {
        final BlockPos relativeDrainPos = new BlockPos(3, 2, 3);
        final BlockPos worldDrainPos = helper.absolutePos(relativeDrainPos);

        helper.setBlock(relativeDrainPos, AllBlocks.ITEM_DRAIN.defaultBlockState());

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldDrainPos,
                List.of(worldDrainPos),
                new BoundingBox3i(worldDrainPos, worldDrainPos)
        );
        final BlockPos plotDrainPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            final Vec3 spawnPos = subLevel.logicalPose()
                    .transformPosition(Vec3.atCenterOf(plotDrainPos).add(0.0, 1.5, 0.0));
            final ItemEntity bucket = new ItemEntity(
                    helper.getLevel(),
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    new ItemStack(Items.WATER_BUCKET)
            );
            bucket.setDeltaMovement(0.0, -0.25, 0.0);
            helper.getLevel().addFreshEntity(bucket);
        });

        helper.succeedWhen(() -> {
            if (!(helper.getLevel().getBlockEntity(plotDrainPos) instanceof ItemDrainBlockEntity drain)) {
                throw helper.assertionException("Item drain block entity was not retained in the sub-level plot");
            }
            if (drain.internalTank == null
                    || drain.internalTank.getPrimaryHandler().getFluid().getAmount() < BucketFluidInventory.CAPACITY) {
                throw helper.assertionException(Component.literal("The sub-level item drain did not process the water bucket"));
            }

            subLevel.markRemoved();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void createEntityInsideCallbacksUseSubLevelCoordinates(final GameTestHelper helper) {
        final BlockPos relativeDrillPos = new BlockPos(3, 2, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeDrillPos,
                AllBlocks.MECHANICAL_DRILL.defaultBlockState()
        );
        final BlockPos plotDrillPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            if (!(helper.getLevel().getBlockEntity(plotDrillPos) instanceof DrillBlockEntity drill)) {
                throw helper.assertionException("Mechanical drill block entity was not retained in the sub-level plot");
            }
            drill.setSpeed(256);

            final Vec3 globalDrillCenter = subLevel.logicalPose().transformPosition(Vec3.atCenterOf(plotDrillPos));
            final var zombie = helper.spawn(EntityType.ZOMBIE, relativeDrillPos.above(2));
            zombie.setPos(globalDrillCenter);
            final float healthBefore = zombie.getHealth();

            zombie.applyEffectsFromBlocks(globalDrillCenter, globalDrillCenter);
            helper.assertTrue(
                    zombie.getHealth() < healthBefore,
                    Component.literal("The sub-level drill did not receive the entity in plot-local coordinates")
            );

            zombie.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void fanPushRotatesFromSubLevelToWorldCoordinates(final GameTestHelper helper) {
        final BlockPos relativeSourcePos = new BlockPos(3, 2, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeSourcePos,
                AllBlocks.ENCASED_FAN.defaultBlockState()
        );
        final BlockPos plotSourcePos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            rotateSubLevelAroundY(helper, subLevel, Math.PI / 2.0);

            final Vec3 localEntityPos = Vec3.atCenterOf(plotSourcePos).add(1.5, 0.0, 0.0);
            final Vec3 globalEntityPos = subLevel.logicalPose().transformPosition(localEntityPos);
            final ItemEntity item = new ItemEntity(
                    helper.getLevel(),
                    globalEntityPos.x,
                    globalEntityPos.y,
                    globalEntityPos.z,
                    new ItemStack(Items.ANDESITE)
            );
            item.setDeltaMovement(Vec3.ZERO);
            helper.getLevel().addFreshEntity(item);

            final FixedAirCurrentSource source = new FixedAirCurrentSource(helper.getLevel(), plotSourcePos);
            final TestAirCurrent airCurrent = new TestAirCurrent(source);
            source.airCurrent = airCurrent;
            airCurrent.direction = Direction.EAST;
            airCurrent.pushing = true;
            airCurrent.maxDistance = 4.0F;
            airCurrent.bounds = new AABB(plotSourcePos).inflate(4.0);
            airCurrent.setCaught(item);
            airCurrent.pushEntities(helper.getLevel());

            final Vec3 expectedDirection = subLevel.logicalPose()
                    .transformNormal(new Vec3(1.0, 0.0, 0.0))
                    .normalize();
            final Vec3 actualMotion = item.getDeltaMovement();
            helper.assertTrue(
                    actualMotion.lengthSqr() > 1.0E-8
                            && actualMotion.normalize().dot(expectedDirection) > 0.999,
                    Component.literal(
                            "The sub-level fan push was not rotated into world coordinates; expected "
                                    + expectedDirection + ", got " + actualMotion
                    )
            );

            item.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void toolboxRangeUsesPhysicalSubLevelPosition(final GameTestHelper helper) {
        final BlockPos relativeToolboxPos = new BlockPos(3, 2, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeToolboxPos,
                AllBlocks.RED_TOOLBOX.defaultBlockState()
        );
        final BlockPos plotToolboxPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            if (!(helper.getLevel().getBlockEntity(plotToolboxPos) instanceof ToolboxBlockEntity toolbox)) {
                throw helper.assertionException("Toolbox block entity was not retained in the sub-level plot");
            }

            final Vec3 globalPlayerPos = subLevel.logicalPose()
                    .transformPosition(Vec3.atCenterOf(plotToolboxPos).add(1.0, 0.0, 0.0));
            final ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setPos(globalPlayerPos);

            helper.assertTrue(
                    ToolboxHandler.withinRange(player, toolbox),
                    Component.literal("A physically nearby player was considered out of range of the sub-level toolbox")
            );

            player.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void createStepOnCallbacksUseSubLevelCoordinates(final GameTestHelper helper) {
        final BlockPos relativeStickerPos = new BlockPos(3, 2, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeStickerPos,
                AllBlocks.STICKER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP)
        );
        final BlockPos plotStickerPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            final Vec3 localStart = Vec3.atCenterOf(plotStickerPos).add(0.0, 1.0, 0.0);
            final Vec3 globalStart = subLevel.logicalPose().transformPosition(localStart);
            final var zombie = helper.spawn(EntityType.ZOMBIE, relativeStickerPos.above(2));
            zombie.setPos(globalStart);
            zombie.setDeltaMovement(Vec3.ZERO);
            zombie.move(MoverType.SELF, new Vec3(0.0, -1.0, 0.0));

            helper.assertTrue(
                    zombie.onGround(),
                    Component.literal("The test entity did not collide with the sub-level sticker")
            );

            zombie.setDeltaMovement(1.0, 0.0, 0.0);
            zombie.applyEffectsFromBlocks(zombie.position(), zombie.position());
            helper.assertTrue(
                    zombie.getDeltaMovement().x > 0.35 && zombie.getDeltaMovement().x < 0.45,
                    Component.literal(
                            "The sub-level sticker did not apply its step slowdown; got "
                                    + zombie.getDeltaMovement()
                    )
            );

            zombie.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void createValueSettingsPacketsUsePhysicalSubLevelDistance(final GameTestHelper helper) {
        final BlockPos relativeMotorPos = new BlockPos(3, 2, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeMotorPos,
                AllBlocks.CREATIVE_MOTOR.defaultBlockState()
        );
        final BlockPos plotMotorPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            if (!(helper.getLevel().getBlockEntity(plotMotorPos) instanceof CreativeMotorBlockEntity motor)) {
                throw helper.assertionException("Creative motor block entity was not retained in the sub-level plot");
            }

            final ServerPlayer player = helper.makeMockServerPlayerInLevel();
            final Vec3 globalPlayerPos = subLevel.logicalPose()
                    .transformPosition(Vec3.atCenterOf(plotMotorPos).add(1.0, 0.0, 0.0));
            player.setPos(globalPlayerPos);

            final int updatedSpeed = 96;
            AllHandle.onValueSettings(
                    player.connection,
                    new ValueSettingsPacket(
                            plotMotorPos,
                            0,
                            updatedSpeed,
                            null,
                            null,
                            Direction.UP,
                            false,
                            motor.generatedSpeed.netId()
                    )
            );

            helper.assertTrue(
                    Math.abs(motor.generatedSpeed.getValue()) == updatedSpeed,
                    Component.literal(
                            "Create rejected a value-setting packet for a physically nearby sub-level block; got "
                                    + motor.generatedSpeed.getValue()
                    )
            );

            player.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void mechanicalBeltMovesEntitiesInSubLevelCoordinates(final GameTestHelper helper) {
        final BlockPos relativeStartPos = new BlockPos(3, 2, 3);
        final BlockState startState = AllBlocks.BELT.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL)
                .setValue(BeltBlock.PART, BeltPart.START);
        final BlockState endState = startState.setValue(BeltBlock.PART, BeltPart.END);
        final BlockPos worldStartPos = helper.absolutePos(relativeStartPos);
        final BlockPos worldEndPos = worldStartPos.east();

        helper.setBlock(relativeStartPos, startState);
        helper.setBlock(relativeStartPos.east(), endState);
        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldStartPos,
                List.of(worldStartPos, worldEndPos),
                new BoundingBox3i(worldStartPos, worldEndPos)
        );
        final BlockPos plotStartPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            if (!(helper.getLevel().getBlockEntity(plotStartPos) instanceof BeltBlockEntity belt)) {
                throw helper.assertionException("Mechanical belt block entity was not retained in the sub-level plot");
            }

            belt.setController(plotStartPos);
            belt.beltLength = 2;
            belt.index = 0;
            belt.setSpeed(240);
            rotateSubLevelAroundY(helper, subLevel, Math.PI / 2.0);

            final Vec3 localStart = Vec3.atLowerCornerOf(plotStartPos).add(0.5, 13.0 / 16.0, 0.5);
            final Vec3 globalStart = subLevel.logicalPose().transformPosition(localStart);
            final var zombie = helper.spawn(EntityType.ZOMBIE, relativeStartPos.above(2));
            zombie.setPos(globalStart);
            zombie.setNoGravity(true);
            zombie.setDeltaMovement(Vec3.ZERO);

            BeltMovementHandler.transportEntity(
                    belt,
                    zombie,
                    new BeltMovementHandler.TransportedEntityInfo(plotStartPos, startState)
            );

            final Vec3 localEnd = subLevel.logicalPose().transformPositionInverse(zombie.position());
            final Vec3 localMovement = localEnd.subtract(localStart);
            helper.assertTrue(
                    Math.abs(localMovement.x) > 0.1
                            && Math.abs(localMovement.x) > Math.abs(localMovement.z)
                            && Math.abs(localMovement.y) < 0.05,
                    Component.literal(
                            "The mechanical belt did not move its passenger along its local belt axis; got "
                                    + localMovement
                    )
            );

            zombie.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 100, skyAccess = true)
    public void mechanicalBeltAcceptsThrownItemsOnSubLevel(final GameTestHelper helper) {
        final BlockPos relativeStartPos = new BlockPos(3, 2, 3);
        final BlockState startState = AllBlocks.BELT.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL)
                .setValue(BeltBlock.PART, BeltPart.START);
        final BlockState endState = startState.setValue(BeltBlock.PART, BeltPart.END);
        final BlockPos worldStartPos = helper.absolutePos(relativeStartPos);
        final BlockPos worldEndPos = worldStartPos.east();

        helper.setBlock(relativeStartPos, startState);
        helper.setBlock(relativeStartPos.east(), endState);
        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldStartPos,
                List.of(worldStartPos, worldEndPos),
                new BoundingBox3i(worldStartPos, worldEndPos)
        );
        final BlockPos plotStartPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            if (!(helper.getLevel().getBlockEntity(plotStartPos) instanceof BeltBlockEntity belt)) {
                throw helper.assertionException("Mechanical belt block entity was not retained in the sub-level plot");
            }

            belt.setController(plotStartPos);
            belt.beltLength = 2;
            belt.index = 0;
            belt.setSpeed(240);
            rotateSubLevelAroundY(helper, subLevel, Math.PI / 2.0);

            final Vec3 spawnPos = subLevel.logicalPose().transformPosition(
                    Vec3.atLowerCornerOf(plotStartPos).add(0.5, 1.6, 0.5)
            );
            final ItemEntity item = new ItemEntity(
                    helper.getLevel(),
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    new ItemStack(Items.ANDESITE)
            );
            item.setDeltaMovement(0.0, -0.25, 0.0);
            helper.getLevel().addFreshEntity(item);
        });

        helper.succeedWhen(() -> {
            if (!(helper.getLevel().getBlockEntity(plotStartPos) instanceof BeltBlockEntity belt)) {
                throw helper.assertionException("Mechanical belt block entity was not retained in the sub-level plot");
            }
            final var inventory = belt.getInventory();
            if (inventory == null || inventory.getTransportedItems().isEmpty()) {
                throw helper.assertionException("The sub-level mechanical belt did not accept the thrown item");
            }

            final float positionBeforeTick = inventory.getTransportedItems().getFirst().beltPosition;
            belt.setSpeed(240);
            inventory.tick();
            if (!inventory.getTransportedItems().isEmpty()) {
                final float positionAfterTick = inventory.getTransportedItems().getFirst().beltPosition;
                helper.assertTrue(
                        Math.abs(positionAfterTick - positionBeforeTick) > 0.01F,
                        Component.literal("The sub-level mechanical belt accepted the item but did not transport it")
                );
            }

            subLevel.markRemoved();
        });
    }

    private static ServerSubLevel assembleSingleBlock(
            final GameTestHelper helper, final BlockPos relativePos, final BlockState state
    ) {
        final BlockPos worldPos = helper.absolutePos(relativePos);
        helper.setBlock(relativePos, state);
        return SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldPos,
                List.of(worldPos),
                new BoundingBox3i(worldPos, worldPos)
        );
    }

    private static void rotateSubLevelAroundY(
            final GameTestHelper helper, final ServerSubLevel subLevel, final double angle
    ) {
        final Pose3d pose = subLevel.logicalPose();
        pose.orientation().rotationY(angle);
        final SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(helper.getLevel());
        if (physicsSystem == null) {
            throw helper.assertionException("The test level has no Sable physics system");
        }
        physicsSystem.getPipeline().teleport(subLevel, pose.position(), pose.orientation());
        subLevel.updateLastPose();
        subLevel.updateBoundingBox();
    }

    private static final class TestAirCurrent extends AirCurrent {

        private TestAirCurrent(final IAirCurrentSource source) {
            super(source);
        }

        private void setCaught(final Entity entity) {
            this.caughtEntities = new ArrayList<>(List.of(entity));
        }

        private void pushEntities(final Level level) {
            this.tickAffectedEntities(level);
        }
    }

    private static final class FixedAirCurrentSource implements IAirCurrentSource {

        private final Level level;
        private final BlockPos pos;
        @Nullable
        private AirCurrent airCurrent;

        private FixedAirCurrentSource(final Level level, final BlockPos pos) {
            this.level = level;
            this.pos = pos;
        }

        @Override
        public @Nullable AirCurrent getAirCurrent() {
            return this.airCurrent;
        }

        @Override
        public Level getAirCurrentWorld() {
            return this.level;
        }

        @Override
        public BlockPos getAirCurrentPos() {
            return this.pos;
        }

        @Override
        public float getSpeed() {
            return 64.0F;
        }

        @Override
        public Direction getAirflowOriginSide() {
            return Direction.EAST;
        }

        @Override
        public Direction getAirFlowDirection() {
            return Direction.EAST;
        }

        @Override
        public boolean isSourceRemoved() {
            return false;
        }
    }
}
