package dev.eriksonn.aeronautics.gametest;

import com.mojang.authlib.GameProfile;
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
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.LevelExtension;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @GameTest(maxTicks = 20, skyAccess = true)
    public void sandLayerRemainsAssembledAboveUnassembledDirt(final GameTestHelper helper) {
        final BlockPos relativeDirtStart = new BlockPos(3, 2, 3);
        final BlockPos relativeDirtEnd = relativeDirtStart.east();
        final BlockPos relativeSandStart = relativeDirtStart.above();
        final BlockPos relativeSandEnd = relativeDirtEnd.above();
        final BlockPos worldDirtStart = helper.absolutePos(relativeDirtStart);
        final BlockPos worldDirtEnd = helper.absolutePos(relativeDirtEnd);
        final BlockPos worldSandStart = helper.absolutePos(relativeSandStart);
        final BlockPos worldSandEnd = helper.absolutePos(relativeSandEnd);

        helper.setBlock(relativeDirtStart, Blocks.DIRT.defaultBlockState());
        helper.setBlock(relativeDirtEnd, Blocks.DIRT.defaultBlockState());
        helper.setBlock(relativeSandStart, Blocks.SAND.defaultBlockState());
        helper.setBlock(relativeSandEnd, Blocks.SAND.defaultBlockState());

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldSandStart,
                List.of(worldSandStart, worldSandEnd),
                new BoundingBox3i(worldSandStart, worldSandEnd)
        );
        final BlockPos plotSandStart = subLevel.getPlot().getCenterBlock();
        final BlockPos plotSandEnd = plotSandStart.east();

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(
                    helper.getLevel().getBlockState(plotSandStart).is(Blocks.SAND)
                            && helper.getLevel().getBlockState(plotSandEnd).is(Blocks.SAND),
                    Component.literal("The assembled sand layer detached from its sub-level")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(worldDirtStart).is(Blocks.DIRT)
                            && helper.getLevel().getBlockState(worldDirtEnd).is(Blocks.DIRT),
                    Component.literal("The unassembled dirt layer was unexpectedly moved")
            );

            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 20, skyAccess = true)
    public void sugarCaneRemainsAssembledWhenWaterStaysInWorld(final GameTestHelper helper) {
        final BlockPos relativeSandPos = new BlockPos(3, 2, 3);
        final BlockPos relativeCanePos = relativeSandPos.above();
        final BlockPos relativeWaterPos = relativeSandPos.east();
        final BlockPos worldSandPos = helper.absolutePos(relativeSandPos);
        final BlockPos worldCanePos = helper.absolutePos(relativeCanePos);
        final BlockPos worldWaterPos = helper.absolutePos(relativeWaterPos);

        helper.setBlock(relativeSandPos, Blocks.SAND.defaultBlockState());
        helper.setBlock(relativeWaterPos, Blocks.WATER.defaultBlockState());
        helper.setBlock(relativeCanePos, Blocks.SUGAR_CANE.defaultBlockState());

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldSandPos,
                List.of(worldSandPos, worldCanePos),
                new BoundingBox3i(worldSandPos, worldCanePos)
        );
        final BlockPos plotSandPos = subLevel.getPlot().getCenterBlock();
        final BlockPos plotCanePos = plotSandPos.above();

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(
                    helper.getLevel().getBlockState(plotSandPos).is(Blocks.SAND)
                            && helper.getLevel().getBlockState(plotCanePos).is(Blocks.SUGAR_CANE),
                    Component.literal("The assembled sugar cane broke when its water stayed in the world")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(worldWaterPos).is(Blocks.WATER),
                    Component.literal("The unassembled water was unexpectedly moved")
            );

            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 60, skyAccess = true)
    public void unsupportedSandPillarFallsWithoutSplittingSubLevel(final GameTestHelper helper) {
        final BlockPos relativeDirtPos = new BlockPos(3, 2, 3);
        final BlockPos relativeBottomSandPos = relativeDirtPos.above();
        final BlockPos relativeMiddleSandPos = relativeBottomSandPos.above();
        final BlockPos relativeTopSandPos = relativeMiddleSandPos.above();
        final BlockPos worldDirtPos = helper.absolutePos(relativeDirtPos);
        final BlockPos worldBottomSandPos = helper.absolutePos(relativeBottomSandPos);
        final BlockPos worldMiddleSandPos = helper.absolutePos(relativeMiddleSandPos);
        final BlockPos worldTopSandPos = helper.absolutePos(relativeTopSandPos);

        helper.setBlock(relativeDirtPos, Blocks.DIRT.defaultBlockState());
        helper.setBlock(relativeBottomSandPos, Blocks.SAND.defaultBlockState());
        helper.setBlock(relativeMiddleSandPos, Blocks.SAND.defaultBlockState());
        helper.setBlock(relativeTopSandPos, Blocks.SAND.defaultBlockState());

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldDirtPos,
                List.of(worldDirtPos, worldBottomSandPos, worldMiddleSandPos, worldTopSandPos),
                new BoundingBox3i(worldDirtPos, worldTopSandPos)
        );
        final BlockPos plotDirtPos = subLevel.getPlot().getCenterBlock();
        final BlockPos plotBottomSandPos = plotDirtPos.above();
        final ServerSubLevelContainer container = SubLevelContainer.getContainer(helper.getLevel());
        if (container == null) {
            throw helper.assertionException("The test level has no Sable sub-level container");
        }
        final int subLevelsBeforeBreak = container.getAllSubLevels().size();

        helper.runAfterDelay(3, () -> helper.getLevel().destroyBlock(plotBottomSandPos, false));
        helper.succeedWhen(() -> {
            final boolean splitCreated =
                    container.getAllSubLevels().size() > subLevelsBeforeBreak
                            || container.getAllSubLevels().stream().anyMatch(candidate ->
                            !candidate.isRemoved()
                                    && subLevel.getUniqueId().equals(candidate.getSplitFromSubLevel())
                    );
            helper.assertTrue(
                    !splitCreated,
                    Component.literal("The unsupported sand pillar split into another sub-level")
            );
            helper.assertTrue(
                    !helper.getEntities(EntityType.FALLING_BLOCK).isEmpty(),
                    Component.literal("The unsupported sand pillar did not create falling-block entities")
            );

            helper.killAllEntitiesOfClass(FallingBlockEntity.class);
            subLevel.markRemoved();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void removedSubLevelDoesNotProcessPendingSplitAfterBlockTick(final GameTestHelper helper) {
        final BlockPos relativeSandPos = new BlockPos(2, 3, 3);
        final BlockPos relativeMasslessPos = relativeSandPos.east(3);
        final BlockPos worldSandPos = helper.absolutePos(relativeSandPos);
        final BlockPos worldMasslessPos = helper.absolutePos(relativeMasslessPos);

        helper.setBlock(relativeSandPos, Blocks.SAND.defaultBlockState());
        helper.setBlock(relativeMasslessPos, Blocks.STRUCTURE_VOID.defaultBlockState());

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldSandPos,
                List.of(worldSandPos, worldMasslessPos),
                new BoundingBox3i(worldSandPos, worldMasslessPos)
        );
        final BlockPos plotSandPos = subLevel.getPlot().getCenterBlock();

        // The scheduled falling-block tick runs from ServerSubLevel.super.tick(). Removing
        // the only massive block retires the sub-level while a disconnected heatmap region
        // is still queued. The retired heatmap must not try to split that region afterward.
        helper.getLevel().scheduleTick(plotSandPos, Blocks.SAND, 1);

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    subLevel.isRemoved(),
                    Component.literal("The massless sub-level was not retired after its sand fell")
            );
            helper.assertTrue(
                    !helper.getEntities(EntityType.FALLING_BLOCK).isEmpty(),
                    Component.literal("The scheduled sand tick did not create a falling-block entity")
            );
            helper.killAllEntitiesOfClass(FallingBlockEntity.class);
        });
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
            final Player player = helper.makeMockPlayer(GameType.CREATIVE);
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
    public void entitiesDoNotCreepUphillOnTiltedSubLevels(final GameTestHelper helper) {
        final BlockPos relativeBlockPos = new BlockPos(3, 3, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeBlockPos,
                Blocks.STONE.defaultBlockState()
        );
        final BlockPos plotBlockPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            rotateSubLevel(helper, subLevel, new Quaterniond().rotationZ(Math.toRadians(40.0)));

            final Vec3 localTopCenter = Vec3.atLowerCornerOf(plotBlockPos).add(0.5, 1.0, 0.5);
            final Vec3 globalTopCenter = subLevel.logicalPose().transformPosition(localTopCenter);
            final var zombie = helper.spawn(EntityType.ZOMBIE, relativeBlockPos.above(3));
            zombie.setNoAi(true);
            zombie.setNoGravity(true);
            zombie.setDeltaMovement(Vec3.ZERO);
            zombie.setPos(globalTopCenter.add(0.0, 1.5, 0.0));

            for (int attempt = 0; attempt < 80 && !zombie.onGround(); attempt++) {
                zombie.move(MoverType.SELF, new Vec3(0.0, -0.05, 0.0));
            }
            helper.assertTrue(
                    zombie.onGround(),
                    Component.literal("The test entity did not land on the tilted sub-level")
            );

            final Vec3 supportedPosition = zombie.position();
            for (int tick = 0; tick < 12; tick++) {
                zombie.move(MoverType.SELF, new Vec3(0.0, -0.08, 0.0));
            }

            final Vec3 supportDrift = zombie.position().subtract(supportedPosition);
            helper.assertTrue(
                    supportDrift.horizontalDistanceSqr() < 1.0E-4,
                    Component.literal(
                            "Gravity moved the stationary entity across the tilted sub-level; drift="
                                    + supportDrift
                    )
            );
            helper.assertTrue(
                    zombie.onGround(),
                    Component.literal("The stationary entity was pushed off the tilted sub-level")
            );

            zombie.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void playersDoNotStepUphillOnSteepRotatedFaces(final GameTestHelper helper) {
        final BlockPos relativeBlockPos = new BlockPos(3, 3, 3);
        final ServerSubLevel subLevel = assembleSingleBlock(
                helper,
                relativeBlockPos,
                Blocks.STONE.defaultBlockState()
        );
        final BlockPos plotBlockPos = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            final double angle = Math.toRadians(58.0);
            rotateSubLevel(helper, subLevel, new Quaterniond().rotationZ(angle));

            // At this offset, the player's upright box contacts the same steep
            // block face and lower edge highlighted in the reported setup.
            final double localOffset = 0.4;
            final Vec3 localSurface = Vec3.atLowerCornerOf(plotBlockPos)
                    .add(localOffset, 1.0, 0.5);
            final Vec3 globalSurface = subLevel.logicalPose().transformPosition(localSurface);
            final TestLocalPlayer player = spawnTestLocalPlayer(
                    helper,
                    globalSurface.add(0.0, Math.tan(angle) * 0.3 + 1.0E-4, 0.0)
            );
            player.setNoGravity(true);
            player.setDeltaMovement(Vec3.ZERO);
            player.setOnGround(true);
            final Vec3 positionBeforeMove = player.position();
            player.move(MoverType.SELF, new Vec3(0.0, -0.08, 0.0));
            final Vec3 actualMovement = player.position().subtract(positionBeforeMove);

            helper.assertTrue(
                    Math.abs(actualMovement.x) > 1.0E-6
                            || Math.abs(actualMovement.y + 0.08) > 1.0E-6,
                    Component.literal("The test entity never contacted the steep rotated face")
            );
            helper.assertTrue(
                    actualMovement.x < 1.0E-4,
                    Component.literal(
                            "Gravity was converted into an uphill step on a steep rotated face; movement="
                                    + actualMovement
                    )
            );
            helper.assertTrue(
                    actualMovement.y >= -0.081,
                    Component.literal(
                            "A rotated edge amplified the requested fall; movement="
                                    + actualMovement
                    )
            );

            player.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void playersDoNotStoreGravityWhileSupportedByRotatedThinFaces(final GameTestHelper helper) {
        final BlockPos relativeStart = new BlockPos(2, 3, 2);
        final BlockPos worldStart = helper.absolutePos(relativeStart);
        final BlockPos worldEnd = worldStart.offset(6, 0, 2);
        final List<BlockPos> blocks = new ArrayList<>();
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 2; z++) {
                final BlockPos relativePos = relativeStart.offset(x, 0, z);
                helper.setBlock(relativePos, Blocks.OAK_PLANKS.defaultBlockState());
                blocks.add(helper.absolutePos(relativePos));
            }
        }

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldStart,
                blocks,
                new BoundingBox3i(worldStart, worldEnd)
        );
        final BlockPos plotStart = subLevel.getPlot().getCenterBlock();

        helper.runAfterDelay(2, () -> {
            rotateSubLevel(helper, subLevel, new Quaterniond().rotationZ(Math.PI / 2.0));

            // Rotating the plate by 90 degrees makes its one-block-thick east
            // end the horizontal top face. This is the narrow "t" face from
            // the player reproduction, not the plate's broad face.
            final Vec3 localThinFaceCenter = Vec3.atLowerCornerOf(plotStart).add(7.0, 0.5, 1.5);
            final Vec3 globalThinFaceCenter =
                    subLevel.logicalPose().transformPosition(localThinFaceCenter);
            final TestLocalPlayer player = spawnTestLocalPlayer(
                    helper,
                    globalThinFaceCenter.add(0.0, 1.0, 0.0)
            );
            player.setNoGravity(true);
            player.setDeltaMovement(Vec3.ZERO);

            for (int attempt = 0; attempt < 40 && !player.onGround(); attempt++) {
                player.move(MoverType.SELF, new Vec3(0.0, -0.05, 0.0));
            }
            helper.assertTrue(
                    player.onGround(),
                    Component.literal("The test player did not land on the rotated thin face")
            );

            player.setNoGravity(false);
            player.setDeltaMovement(Vec3.ZERO);
            final Vec3 supportedPosition = player.position();
            double minimumVerticalVelocity = 0.0;
            for (int step = 0; step < 20; step++) {
                player.travel(Vec3.ZERO);
                minimumVerticalVelocity =
                        Math.min(minimumVerticalVelocity, player.getDeltaMovement().y);
            }

            helper.assertTrue(
                    player.onGround(),
                    Component.literal("The supported player stopped colliding with the rotated thin face")
            );
            helper.assertTrue(
                    player.position().distanceToSqr(supportedPosition) < 1.0E-8,
                    Component.literal(
                            "Gravity moved the stationary player on the rotated thin face; movement="
                                    + player.position().subtract(supportedPosition)
                    )
            );
            helper.assertTrue(
                    minimumVerticalVelocity >= -0.12,
                    Component.literal(
                            "The rotated thin face stored gravity while supporting the player; "
                                    + "minimumVerticalVelocity=" + minimumVerticalVelocity
                    )
            );

            player.discard();
            subLevel.markRemoved();
            helper.succeed();
        });
    }

    @GameTest(maxTicks = 40, skyAccess = true)
    public void entitiesLeavingRotatedThinEdgesUseTheCurrentCollisionPose(final GameTestHelper helper) {
        final BlockPos relativeStart = new BlockPos(2, 3, 3);
        final BlockPos worldStart = helper.absolutePos(relativeStart);
        final BlockPos worldEnd = worldStart.east(4);
        for (int offset = 0; offset <= 4; offset++) {
            helper.setBlock(relativeStart.east(offset), Blocks.STONE.defaultBlockState());
        }

        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                helper.getLevel(),
                worldStart,
                BlockPos.betweenClosed(worldStart, worldEnd),
                new BoundingBox3i(worldStart, worldEnd)
        );
        final BlockPos plotEnd = subLevel.getPlot().getCenterBlock().east(4);

        helper.runAfterDelay(2, () -> {
            rotateSubLevel(helper, subLevel, new Quaterniond().rotationZ(Math.PI / 2.0));

            // The long X axis is now vertical. Place the entity just outside the
            // narrow top face, partly below it, as it would be immediately after
            // walking off the edge.
            final Vec3 localTopFaceCenter = Vec3.atLowerCornerOf(plotEnd).add(1.0, 0.5, 0.5);
            final Vec3 globalTopFaceCenter = subLevel.logicalPose().transformPosition(localTopFaceCenter);
            final var zombie = helper.spawn(EntityType.ZOMBIE, relativeStart.above(6));
            zombie.setNoAi(true);
            zombie.setNoGravity(true);
            zombie.setDeltaMovement(Vec3.ZERO);
            zombie.setPos(globalTopFaceCenter.add(0.85, -0.2, 0.0));

            // LevelReusedVectors is intentionally shared. A collision elsewhere can
            // leave this unrelated yaw behind before this entity starts its substep.
            ((LevelExtension) helper.getLevel()).sable$getJOMLSink()
                    .subLevelPose
                    .orientation()
                    .rotationY(Math.PI / 4.0);

            final Vec3 beforeFall = zombie.position();
            final Vec3 requestedFall = new Vec3(0.0, -0.08, 0.0);
            zombie.move(MoverType.SELF, requestedFall);
            final Vec3 actualFall = zombie.position().subtract(beforeFall);

            helper.assertTrue(
                    Math.abs(actualFall.x) < 1.0E-6 && Math.abs(actualFall.z) < 1.0E-6,
                    Component.literal(
                            "A stale sub-level pose pushed the falling entity sideways at the thin edge; movement="
                                    + actualFall
                    )
            );
            helper.assertTrue(
                    Math.abs(actualFall.y - requestedFall.y) < 1.0E-6,
                    Component.literal(
                            "The falling entity did not retain its requested edge motion; movement="
                                    + actualFall
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

            final ServerPlayer player = makePacketTestPlayer(helper);
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

    private static TestLocalPlayer spawnTestLocalPlayer(final GameTestHelper helper, final Vec3 position) {
        final TestLocalPlayer player = new TestLocalPlayer(helper.getLevel());
        player.setPos(position);
        helper.getLevel().addFreshEntity(player);
        return player;
    }

    private static ServerPlayer makePacketTestPlayer(final GameTestHelper helper) {
        final GameProfile profile = new GameProfile(UUID.randomUUID(), "aeronautics_packet_test");
        final CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        // Packet handlers require a server connection, but the vanilla helper that joins a
        // fake player to the server is deprecated for removal. Keep this fixture unregistered.
        final ServerPlayer player = new ServerPlayer(
                helper.getLevel().getServer(),
                helper.getLevel(),
                profile,
                cookie.clientInformation()
        ) {
            @Override
            public GameType gameMode() {
                return GameType.CREATIVE;
            }
        };
        player.connection = new ServerGamePacketListenerImpl(
                helper.getLevel().getServer(),
                new Connection(PacketFlow.SERVERBOUND),
                player,
                cookie
        );
        return player;
    }

    private static void rotateSubLevelAroundY(
            final GameTestHelper helper, final ServerSubLevel subLevel, final double angle
    ) {
        rotateSubLevel(helper, subLevel, new Quaterniond().rotationY(angle));
    }

    private static void rotateSubLevel(
            final GameTestHelper helper, final ServerSubLevel subLevel, final Quaterniond orientation
    ) {
        final Pose3d pose = subLevel.logicalPose();
        pose.orientation().set(orientation);
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

    private static final class TestLocalPlayer extends Player {

        private TestLocalPlayer(final Level level) {
            super(level, new GameProfile(UUID.randomUUID(), "sable_collision_test"));
        }

        @Override
        public GameType gameMode() {
            return GameType.SURVIVAL;
        }

        @Override
        public boolean isLocalPlayer() {
            return true;
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
