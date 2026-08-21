package dev.eriksonn.aeronautics.gametest;

import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.LivingEntityMovementExtension;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlock;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffServerHandler;
import dev.simulated_team.simulated.index.SimBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Rendered-client coverage for collision behavior that cannot be represented by
 * the dedicated-server GameTest player substitute.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ClientPhysicsCollisionGameTest implements FabricClientGameTest {

    private static final int PLATE_LENGTH = 7;
    private static final int PLATE_WIDTH = 3;
    private static final double THIN_FACE_ANGLE = Math.PI / 2.0;
    private static final double SLOPE_ANGLE = Math.toRadians(40.0);

    @Override
    public void runTest(final ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            final TestServerContext serverContext = singleplayer.getServer();
            final CollisionScene scene = serverContext.computeOnServer(server -> {
                final ServerLevel level = server.overworld();
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.teleportTo(12.0, 104.0, 8.0);

                final PlateSetup thinFace = assembleAndRotatePlate(
                        level,
                        new BlockPos(0, 96, 0),
                        THIN_FACE_ANGLE
                );
                final PlateSetup slope = assembleAndRotatePlate(
                        level,
                        new BlockPos(24, 96, 0),
                        SLOPE_ANGLE
                );
                return new CollisionScene(thinFace, slope);
            });

            context.waitFor(client -> isPlateSynced(client, scene.thinFace())
                    && isPlateSynced(client, scene.slope()));

            verifyThinFaceEdgeFall(context, serverContext, scene.thinFace());
            verifyStationaryPlayerDoesNotCreepUphill(context, serverContext, scene.slope());
        }
    }

    private static void verifyThinFaceEdgeFall(
            final ClientGameTestContext context,
            final TestServerContext serverContext,
            final PlateSetup setup
    ) {
        serverContext.runOnServer(server -> {
            final ServerLevel level = server.overworld();
            final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
            final Vec3 localThinFaceCenter = Vec3.atLowerCornerOf(setup.plotStart())
                    .add(PLATE_LENGTH, 0.5, PLATE_WIDTH / 2.0);
            final Vec3 globalThinFaceCenter =
                    subLevel.logicalPose().transformPosition(localThinFaceCenter);

            clearTracking(player);
            player.setNoGravity(false);
            player.getAbilities().flying = false;
            player.setDeltaMovement(Vec3.ZERO);
            player.setOnGround(false);
            player.teleportTo(
                    globalThinFaceCenter.x,
                    globalThinFaceCenter.y + 2.0,
                    globalThinFaceCenter.z
            );
        });

        context.runOnClient(client -> {
            requirePlayer(client);
            clearTracking(client.player);
            client.player.setYRot(-90.0F);
            client.player.setXRot(45.0F);
        });
        context.waitFor(client -> {
            if (client.player == null || client.level == null) {
                return false;
            }
            final SubLevel subLevel = Sable.HELPER.getContaining(client.level, setup.plotStart());
            return subLevel != null
                    && Sable.HELPER.getTrackingSubLevel(client.player) == subLevel
                    && client.player.onGround();
        });
        context.waitTicks(3);
        context.takeScreenshot("physics-thin-face-before-walking-off-edge");

        final List<MotionStep> samples = new ArrayList<>();
        int fallingTicks = 0;
        context.getInput().holdKey(options -> options.keyUp);
        try {
            for (int tick = 0; tick < 40; tick++) {
                final ClientMotionState before =
                        context.computeOnClient(client -> observeClientMotion(client, setup));
                context.waitTick();
                final ClientMotionState after =
                        context.computeOnClient(client -> observeClientMotion(client, setup));
                final MotionStep sample = new MotionStep(tick, before, after);
                samples.add(sample);

                if (!after.tracking() && sample.movement().y < -1.0E-4) {
                    fallingTicks++;
                    if (fallingTicks >= 8) {
                        break;
                    }
                }
            }
        } finally {
            context.getInput().releaseKey(options -> options.keyUp);
        }
        context.waitTick();
        context.takeScreenshot("physics-thin-face-after-walking-off-edge");

        final List<MotionStep> fallingSamples = samples.stream()
                .filter(sample -> !sample.after().tracking() && sample.movement().y < -1.0E-4)
                .toList();
        final double minimumVerticalStep = fallingSamples.stream()
                .mapToDouble(sample -> sample.movement().y)
                .min()
                .orElse(0.0);
        final double minimumUnexpectedVerticalStep = fallingSamples.stream()
                .mapToDouble(MotionStep::unexpectedVerticalMovement)
                .min()
                .orElse(0.0);
        final double minimumSupportedVerticalVelocity = samples.stream()
                .map(MotionStep::before)
                .filter(state -> state.tracking() && state.onGround())
                .mapToDouble(state -> state.deltaMovement().y)
                .min()
                .orElse(0.0);

        System.out.println(
                "AERONAUTICS_RENDERED_CLIENT_THIN_EDGE sampleCount=" + samples.size()
                        + ", minimumSupportedVerticalVelocity=" + minimumSupportedVerticalVelocity
                        + ", minimumVerticalStep=" + minimumVerticalStep
                        + ", minimumUnexpectedVerticalStep=" + minimumUnexpectedVerticalStep
        );

        if (fallingSamples.isEmpty()) {
            throw new AssertionError(
                    "The rendered local player did not walk off and fall from the rotated thin face; samples="
                            + samples
            );
        }
        if (minimumSupportedVerticalVelocity < -0.12) {
            throw new AssertionError(
                    "The rendered local player accumulated downward velocity while supported by the "
                            + "rotated thin face; minimumSupportedVerticalVelocity="
                            + minimumSupportedVerticalVelocity + ", samples=" + samples
            );
        }
        if (minimumVerticalStep < -0.75) {
            throw new AssertionError(
                    "The rendered local player accelerated abnormally after leaving the rotated thin face; "
                            + "minimumVerticalStep=" + minimumVerticalStep + ", samples=" + samples
            );
        }
        if (minimumUnexpectedVerticalStep < -0.15) {
            throw new AssertionError(
                    "The rendered local player's downward displacement exceeded its client velocity after "
                            + "leaving the rotated thin face; unexpectedVerticalStep="
                            + minimumUnexpectedVerticalStep + ", samples=" + samples
            );
        }
    }

    private static void verifyStationaryPlayerDoesNotCreepUphill(
            final ClientGameTestContext context,
            final TestServerContext serverContext,
            final PlateSetup setup
    ) {
        serverContext.runOnServer(server -> {
            final ServerLevel level = server.overworld();
            final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
            final Vec3 localSurface = Vec3.atLowerCornerOf(setup.plotStart())
                    .add(PLATE_LENGTH / 2.0, 1.0, PLATE_WIDTH / 2.0);
            final Vec3 globalSurface = subLevel.logicalPose().transformPosition(localSurface);

            clearTracking(player);
            player.setNoGravity(false);
            player.getAbilities().flying = false;
            player.setDeltaMovement(Vec3.ZERO);
            player.setOnGround(false);
            player.teleportTo(globalSurface.x, globalSurface.y + 2.0, globalSurface.z);
        });

        context.runOnClient(client -> {
            requirePlayer(client);
            clearTracking(client.player);
            client.player.setYRot(90.0F);
            client.player.setXRot(45.0F);
        });
        context.waitFor(client -> {
            if (client.player == null || client.level == null) {
                return false;
            }
            final SubLevel subLevel = Sable.HELPER.getContaining(client.level, setup.plotStart());
            return subLevel != null
                    && Sable.HELPER.getTrackingSubLevel(client.player) == subLevel
                    && client.player.onGround();
        });
        context.waitTicks(5);

        final ClientMotionState before =
                context.computeOnClient(client -> observeClientMotion(client, setup));
        context.takeScreenshot("physics-slope-stationary-before-natural-client-ticks");
        final List<MotionStep> samples = new ArrayList<>();
        for (int tick = 0; tick < 20; tick++) {
            final ClientMotionState tickBefore =
                    context.computeOnClient(client -> observeClientMotion(client, setup));
            context.waitTick();
            final ClientMotionState tickAfter =
                    context.computeOnClient(client -> observeClientMotion(client, setup));
            samples.add(new MotionStep(tick, tickBefore, tickAfter));
        }
        context.takeScreenshot("physics-slope-stationary-after-natural-client-ticks");
        final ClientMotionState after =
                context.computeOnClient(client -> observeClientMotion(client, setup));
        final Vec3 localMovement = after.localPosition().subtract(before.localPosition());
        final double maximumUphillStep = samples.stream()
                .mapToDouble(sample ->
                        sample.after().localPosition().x - sample.before().localPosition().x)
                .max()
                .orElse(0.0);

        System.out.println(
                "AERONAUTICS_RENDERED_CLIENT_SLOPE before=" + before
                        + ", after=" + after
                        + ", localMovement=" + localMovement
                        + ", maximumUphillStep=" + maximumUphillStep
        );

        if (!after.tracking() || !after.onGround()) {
            throw new AssertionError(
                    "The rendered local player did not remain supported on the locked slope; samples="
                            + samples
            );
        }
        if (localMovement.x > 0.02 || maximumUphillStep > 0.01) {
            throw new AssertionError(
                    "The rendered local player crept uphill while standing still; localMovement="
                            + localMovement + ", maximumUphillStep=" + maximumUphillStep
                            + ", samples=" + samples
            );
        }
    }

    private static PlateSetup assembleAndRotatePlate(
            final ServerLevel level,
            final BlockPos worldStart,
            final double angle
    ) {
        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            throw new AssertionError("The rendered-client test world has no Sable sub-level container");
        }
        final Set<UUID> existingSubLevels = new HashSet<>();
        for (final ServerSubLevel subLevel : container.getAllSubLevels()) {
            existingSubLevels.add(subLevel.getUniqueId());
        }

        for (int x = 0; x < PLATE_LENGTH; x++) {
            for (int z = 0; z < PLATE_WIDTH; z++) {
                level.setBlockAndUpdate(
                        worldStart.offset(x, 0, z),
                        Blocks.OAK_PLANKS.defaultBlockState()
                );
            }
        }
        level.addFreshEntity(new HoneyGlueEntity(
                level,
                SuperGlueEntity.span(
                        worldStart,
                        worldStart.offset(PLATE_LENGTH - 1, 0, PLATE_WIDTH - 1)
                )
        ));

        final BlockPos assemblerPos = worldStart.west();
        final BlockState assemblerState = SimBlocks.PHYSICS_ASSEMBLER.get().defaultBlockState()
                .setValue(PhysicsAssemblerBlock.FACE, AttachFace.WALL)
                .setValue(PhysicsAssemblerBlock.FACING, Direction.WEST);
        level.setBlockAndUpdate(assemblerPos, assemblerState);
        if (!(level.getBlockEntity(assemblerPos) instanceof PhysicsAssemblerBlockEntity assembler)) {
            throw new AssertionError("Physics Assembler block entity was not created for the client collision test");
        }
        assembler.assembleOrDisassemble();

        final List<ServerSubLevel> createdSubLevels = container.getAllSubLevels().stream()
                .filter(subLevel -> !existingSubLevels.contains(subLevel.getUniqueId()))
                .toList();
        if (createdSubLevels.size() != 1) {
            throw new AssertionError(
                    "Physics Assembler created " + createdSubLevels.size()
                            + " substructures instead of one plate"
            );
        }

        final ServerSubLevel subLevel = createdSubLevels.getFirst();
        final List<BlockPos> plateBlocks = findPlateBlocks(level, subLevel);
        if (plateBlocks.size() != PLATE_LENGTH * PLATE_WIDTH) {
            throw new AssertionError(
                    "Physics Assembler moved " + plateBlocks.size() + " of "
                            + (PLATE_LENGTH * PLATE_WIDTH) + " plate blocks"
            );
        }
        final int minX = plateBlocks.stream().mapToInt(BlockPos::getX).min().orElseThrow();
        final int minY = plateBlocks.stream().mapToInt(BlockPos::getY).min().orElseThrow();
        final int minZ = plateBlocks.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        final BlockPos plotStart = new BlockPos(minX, minY, minZ);

        final Pose3d pose = subLevel.logicalPose();
        pose.orientation().set(new Quaterniond().rotationZ(angle));
        final SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(level);
        if (physicsSystem == null) {
            throw new AssertionError("The rendered-client test world has no Sable physics system");
        }
        physicsSystem.getPipeline().teleport(subLevel, pose.position(), pose.orientation());
        subLevel.updateLastPose();
        subLevel.updateBoundingBox();

        final PhysicsStaffServerHandler staffHandler = PhysicsStaffServerHandler.get(level);
        if (!staffHandler.isLocked(subLevel)) {
            staffHandler.toggleLock(subLevel.getUniqueId());
        }
        return new PlateSetup(subLevel.getUniqueId(), plotStart, angle);
    }

    private static List<BlockPos> findPlateBlocks(
            final ServerLevel level,
            final ServerSubLevel subLevel
    ) {
        final BlockPos center = subLevel.getPlot().getCenterBlock();
        final List<BlockPos> positions = new ArrayList<>();
        for (final BlockPos pos : BlockPos.betweenClosed(
                center.offset(-16, -8, -16),
                center.offset(16, 8, 16)
        )) {
            if (level.getBlockState(pos).is(Blocks.OAK_PLANKS)) {
                positions.add(pos.immutable());
            }
        }
        return positions;
    }

    private static boolean isPlateSynced(final Minecraft client, final PlateSetup setup) {
        if (client.level == null) {
            return false;
        }
        final SubLevel subLevel = Sable.HELPER.getContaining(client.level, setup.plotStart());
        if (subLevel == null) {
            return false;
        }
        final Vec3 transformedLocalX =
                subLevel.logicalPose().transformNormal(new Vec3(1.0, 0.0, 0.0));
        return Math.abs(transformedLocalX.x - Math.cos(setup.angle())) < 0.01
                && Math.abs(transformedLocalX.y - Math.sin(setup.angle())) < 0.01;
    }

    private static ClientMotionState observeClientMotion(
            final Minecraft client,
            final PlateSetup setup
    ) {
        final Player player = requirePlayer(client);
        if (client.level == null) {
            throw new AssertionError("Client world is unavailable while recording physics motion");
        }
        final SubLevel subLevel = requireSubLevel(client.level, setup.plotStart());
        final Vec3 localPosition =
                subLevel.logicalPose().transformPositionInverse(player.position());
        final Vector3d inherited =
                ((LivingEntityMovementExtension) (Object) player).sable$getInheritedVelocity();
        final SubLevelEntityCollision.CollisionInfo collisionInfo =
                ((EntityMovementExtension) (Object) player).sable$getCollisionInfo();
        final String collision = collisionInfo == null
                ? "none"
                : "vertical=" + collisionInfo.verticalCollision
                + ", below=" + collisionInfo.verticalCollisionBelow
                + ", horizontal=" + collisionInfo.subLevelHorizontalCollision
                + ", result=" + collisionInfo.motion;
        return new ClientMotionState(
                player.position(),
                localPosition,
                player.getDeltaMovement(),
                new Vec3(inherited.x, inherited.y, inherited.z),
                player.onGround(),
                Sable.HELPER.getTrackingSubLevel(player) == subLevel,
                collision
        );
    }

    private static Player requirePlayer(final Minecraft client) {
        if (client.player == null) {
            throw new AssertionError("Rendered client player is unavailable");
        }
        return client.player;
    }

    private static SubLevel requireSubLevel(final net.minecraft.world.level.Level level, final BlockPos plotPosition) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, plotPosition);
        if (subLevel == null) {
            throw new AssertionError("No real assembled substructure contains " + plotPosition);
        }
        return subLevel;
    }

    private static void clearTracking(final Player player) {
        ((EntityMovementExtension) (Object) player).sable$setTrackingSubLevel(null);
    }

    private record CollisionScene(PlateSetup thinFace, PlateSetup slope) {
    }

    private record PlateSetup(UUID subLevelId, BlockPos plotStart, double angle) {
    }

    private record ClientMotionState(
            Vec3 position,
            Vec3 localPosition,
            Vec3 deltaMovement,
            Vec3 inheritedVelocity,
            boolean onGround,
            boolean tracking,
            String collision
    ) {
    }

    private record MotionStep(int tick, ClientMotionState before, ClientMotionState after) {

        private Vec3 movement() {
            return this.after.position().subtract(this.before.position());
        }

        private double unexpectedVerticalMovement() {
            final double inheritedVerticalMovement =
                    this.before.tracking() ? 0.0 : this.before.inheritedVelocity().y;
            return this.movement().y
                    - this.before.deltaMovement().y
                    - inheritedVerticalMovement;
        }
    }
}
