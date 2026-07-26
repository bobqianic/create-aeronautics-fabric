package dev.eriksonn.aeronautics.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlock;
import com.zurrtum.create.content.kinetics.motor.CreativeMotorBlockEntity;
import dev.eriksonn.aeronautics.compat.create.SubLevelBeltPlayerHandler;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlock;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffServerHandler;
import dev.simulated_team.simulated.index.SimBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class ClientBeltGameTest implements FabricClientGameTest {

    private static final int BELT_LENGTH = 8;
    private static final int PLAYER_BELT_INDEX = 4;
    private static final double PLAYER_LATERAL_POSITION = 0.35;
    private static final BlockPos ASSEMBLY_SHAFT_START = new BlockPos(11, -56, 8);

    @Override
    public void runTest(final ClientGameTestContext context) {
        final Properties dedicatedProperties = new Properties();
        dedicatedProperties.setProperty("online-mode", "false");
        dedicatedProperties.setProperty("server-port", Integer.toString(findAvailablePort()));
        try (TestDedicatedServerContext dedicated =
                     context.worldBuilder().createServer(dedicatedProperties);
             TestServerConnection connection = dedicated.connect()) {
            runScenario(context, dedicated, "multiplayer");
        }

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            runScenario(context, singleplayer.getServer(), "singleplayer");
        }
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new AssertionError("Could not reserve a port for the dedicated multiplayer belt test", exception);
        }
    }

    private static void runScenario(
            final ClientGameTestContext context,
            final TestServerContext serverContext,
            final String environment
    ) {
            final AssembledShaftSetup setup = serverContext.computeOnServer(server -> {
                final ServerLevel level = server.overworld();
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                // This exact placement deterministically allocates the formerly
                // failing odd-Z plot near 20 million, where Create's float belt
                // center used to round into the neighbouring block.
                final BlockPos shaftStart = ASSEMBLY_SHAFT_START;
                final BlockPos shaftEnd = shaftStart.east(BELT_LENGTH - 1);
                final BlockPos motorPos = shaftStart.north();
                final BlockPos assemblerPos = shaftStart.west();

                player.teleportTo(
                        shaftStart.getX() - 8.0,
                        shaftStart.getY() + 8.0,
                        shaftStart.getZ() + 8.0
                );

                for (int index = -1; index < BELT_LENGTH; index++) {
                    level.setBlockAndUpdate(shaftStart.east(index).below(), Blocks.SLIME_BLOCK.defaultBlockState());
                }
                level.setBlockAndUpdate(motorPos.below(), Blocks.SLIME_BLOCK.defaultBlockState());

                final BlockState shaftState = AllBlocks.SHAFT.defaultBlockState()
                        .setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
                level.setBlockAndUpdate(shaftStart, shaftState);
                level.setBlockAndUpdate(shaftEnd, shaftState);

                final BlockState motorState = AllBlocks.CREATIVE_MOTOR.defaultBlockState()
                        .setValue(CreativeMotorBlock.FACING, Direction.SOUTH);
                level.setBlockAndUpdate(motorPos, motorState);
                if (!(level.getBlockEntity(motorPos) instanceof CreativeMotorBlockEntity motor)) {
                    throw new AssertionError("Creative Motor was not created beside the substructure shafts");
                }
                motor.generatedSpeed.setValue(32);

                final BlockState assemblerState = SimBlocks.PHYSICS_ASSEMBLER.get().defaultBlockState()
                        .setValue(PhysicsAssemblerBlock.FACE, AttachFace.FLOOR)
                        .setValue(PhysicsAssemblerBlock.FACING, Direction.NORTH);
                level.setBlockAndUpdate(assemblerPos, assemblerState);
                if (!(level.getBlockEntity(assemblerPos) instanceof PhysicsAssemblerBlockEntity assembler)) {
                    throw new AssertionError("Physics Assembler block entity was not created");
                }

                final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
                if (container == null) {
                    throw new AssertionError("The " + environment + " world has no Sable sub-level container");
                }
                final Set<UUID> existingSubLevels = new HashSet<>();
                for (final ServerSubLevel subLevel : container.getAllSubLevels()) {
                    existingSubLevels.add(subLevel.getUniqueId());
                }

                assembler.assembleOrDisassemble();
                final List<ServerSubLevel> createdSubLevels = container.getAllSubLevels().stream()
                        .filter(subLevel -> !existingSubLevels.contains(subLevel.getUniqueId()))
                        .toList();
                if (createdSubLevels.size() != 1) {
                    throw new AssertionError(
                            "Physics Assembler created " + createdSubLevels.size() + " substructures instead of one"
                    );
                }

                final ServerSubLevel subLevel = createdSubLevels.getFirst();
                final List<BlockPos> shafts = findAssembledBlocks(level, subLevel, AllBlocks.SHAFT);
                if (shafts.size() != 2) {
                    throw new AssertionError(
                            "Real Physics Assembler moved " + shafts.size() + " of 2 shafts into the substructure"
                    );
                }
                shafts.sort(Comparator.comparingInt(BlockPos::getX));
                final BlockPos plotStart = shafts.getFirst();
                final BlockPos plotEnd = shafts.getLast();
                if (!plotEnd.equals(plotStart.east(BELT_LENGTH - 1))) {
                    throw new AssertionError(
                            "Assembled shafts no longer have the requested belt spacing: "
                                    + plotStart + " -> " + plotEnd
                    );
                }

                final PhysicsStaffServerHandler staffHandler = PhysicsStaffServerHandler.get(level);
                if (!staffHandler.isLocked(subLevel)) {
                    staffHandler.toggleLock(subLevel.getUniqueId());
                }

                return new AssembledShaftSetup(
                        plotStart.immutable(),
                        plotEnd.immutable()
                );
            });

            context.waitFor(client -> client.level != null
                    && Sable.HELPER.getContaining(client.level, setup.plotStart()) != null
                    && client.level.getBlockState(setup.plotStart()).is(AllBlocks.SHAFT)
                    && client.level.getBlockState(setup.plotEnd()).is(AllBlocks.SHAFT));

            serverContext.runOnServer(server -> {
                final ServerLevel level = server.overworld();
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
                player.setItemInHand(
                        InteractionHand.MAIN_HAND,
                        AllItems.BELT_CONNECTOR.getDefaultInstance()
                );
                player.setNoGravity(true);
                positionPlayerForTarget(player, subLevel, setup.plotStart());
            });
            context.waitTicks(3);
            context.waitFor(client -> client.player != null
                    && client.player.getMainHandItem().is(AllItems.BELT_CONNECTOR));
            aimClientAtSubLevelBlock(context, setup.plotStart());
            context.waitFor(client -> isAimingAt(client, setup.plotStart()));
            context.takeScreenshot(environment + "-belt-connector-first-shaft-inside-substructure");
            final InteractionResult firstUse = useHeldItemOnTarget(context, setup.plotStart());
            if (firstUse == InteractionResult.FAIL) {
                throw new AssertionError("Client rejected the first Belt Connector interaction");
            }
            context.waitTicks(2);

            serverContext.runOnServer(server -> {
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                final BlockPos selected = player.getMainHandItem()
                        .get(AllDataComponents.BELT_FIRST_SHAFT);
                if (!setup.plotStart().equals(selected)) {
                    throw new AssertionError(
                            "The first real client click did not select the substructure shaft; selected=" + selected
                    );
                }
            });

            context.waitTicks(5);
            serverContext.runOnServer(server -> {
                final ServerLevel level = server.overworld();
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                positionPlayerForTarget(player, requireSubLevel(level, setup.plotStart()), setup.plotEnd());
            });
            context.waitTicks(3);
            aimClientAtSubLevelBlock(context, setup.plotEnd());
            context.waitFor(client -> isAimingAt(client, setup.plotEnd()));
            context.takeScreenshot(environment + "-belt-connector-second-shaft-inside-substructure");
            final InteractionResult secondUse = useHeldItemOnTarget(context, setup.plotEnd());
            if (secondUse == InteractionResult.FAIL) {
                throw new AssertionError("Client rejected the second Belt Connector interaction");
            }

            context.waitFor(client -> {
                if (client.level == null) {
                    return false;
                }
                final BeltBlockEntity controller = findBeltController(client.level, setup.plotStart());
                return controller != null
                        && Math.abs(controller.getSpeed()) >= 1.0F
                        && countBeltSegments(client.level, setup.plotStart()) == BELT_LENGTH;
            });

            final BeltAssemblyState assemblyState = context.computeOnClient(client ->
                    observeAssembledBelt(client.level, setup.plotStart()));
            System.out.println(
                    "AERONAUTICS_BELT_CREATED_INSIDE_SUBSTRUCTURE environment="
                            + environment + ", state={" + assemblyState + "}"
            );
            serverContext.runOnServer(server -> {
                final ServerLevel level = server.overworld();
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                positionPlayerForOverview(
                        player,
                        requireSubLevel(level, setup.plotStart()),
                        setup.plotStart().east(PLAYER_BELT_INDEX)
                );
            });
            context.waitTicks(3);
            aimClientAtSubLevelBlock(context, setup.plotStart().east(PLAYER_BELT_INDEX));
            context.takeScreenshot(environment + "-create-belt-created-inside-real-substructure");

            serverContext.runOnServer(server -> {
                final ServerLevel level = server.overworld();
                final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
                player.setNoGravity(false);
                player.getAbilities().flying = false;
                final Vec3 localStart = Vec3.atLowerCornerOf(setup.plotStart().east(PLAYER_BELT_INDEX))
                        .add(0.5, 13.0 / 16.0, PLAYER_LATERAL_POSITION);
                final Vec3 position = subLevel.logicalPose().transformPosition(localStart);
                player.teleportTo(position.x, position.y + 2.0, position.z);
                player.setDeltaMovement(Vec3.ZERO);
            });

            context.waitFor(client -> {
                if (client.level == null || client.player == null) {
                    return false;
                }
                final SubLevel subLevel = Sable.HELPER.getContaining(client.level, setup.plotStart());
                if (subLevel == null) {
                    return false;
                }
                return Sable.HELPER.getTrackingSubLevel(client.player) == subLevel
                        && SubLevelBeltPlayerHandler.findContact(client.player, subLevel, null) != null;
            });

            final ClientBeltState before = context.computeOnClient(client ->
                    observeClientBelt(client, setup.plotStart()));
            final ServerBeltState serverBefore = serverContext.computeOnServer(server ->
                    observeServerBelt(server, setup.plotStart()));
            context.takeScreenshot(environment + "-in-substructure-belt-before-natural-ticks");
            context.waitTicks(20);
            context.takeScreenshot(environment + "-in-substructure-belt-after-natural-ticks");
            final ClientBeltState after = context.computeOnClient(client ->
                    observeClientBelt(client, setup.plotStart()));
            final ServerBeltState serverAfter = serverContext.computeOnServer(server ->
                    observeServerBelt(server, setup.plotStart()));
            final Vec3 localMovement = after.localPlayerPosition().subtract(before.localPlayerPosition());
            final Vec3 serverLocalMovement =
                    serverAfter.localPlayerPosition().subtract(serverBefore.localPlayerPosition());
            final double expectedLateralPosition =
                    setup.plotStart().getZ() + PLAYER_LATERAL_POSITION;
            System.out.println(
                    "AERONAUTICS_IN_SUBSTRUCTURE_CLIENT_BELT environment=" + environment
                            + ", before={" + before + "}, after={" + after
                            + "}, localMovement=" + localMovement
                            + ", serverBefore={" + serverBefore + "}, serverAfter={" + serverAfter
                            + "}, serverLocalMovement=" + serverLocalMovement
            );

            if (Math.abs(localMovement.x) <= 0.1
                    || Math.abs(localMovement.x) <= Math.abs(localMovement.z)) {
                throw new AssertionError(
                        "Natural " + environment
                                + " client ticks did not move the player along the belt created inside the substructure; "
                                + "movement=" + localMovement + ", before={" + before + "}, after={" + after + "}"
                );
            }
            if (Math.abs(serverLocalMovement.x) <= 0.1
                    || Math.abs(serverLocalMovement.x) <= Math.abs(serverLocalMovement.z)) {
                throw new AssertionError(
                        "Natural " + environment
                                + " server ticks did not authoritatively move the player along the belt; "
                                + "movement=" + serverLocalMovement
                                + ", before={" + serverBefore + "}, after={" + serverAfter + "}"
                );
            }
            if (Math.abs(before.localPlayerPosition().z - expectedLateralPosition) > 0.05
                    || Math.abs(after.localPlayerPosition().z - expectedLateralPosition) > 0.05) {
                throw new AssertionError(
                        "The " + environment + " substructure belt changed the player's cross-belt position; "
                                + "expectedZ=" + expectedLateralPosition
                                + ", before={" + before + "}, after={" + after + "}"
                );
            }
            if (Math.abs(serverBefore.localPlayerPosition().z - expectedLateralPosition) > 0.05
                    || Math.abs(serverAfter.localPlayerPosition().z - expectedLateralPosition) > 0.05) {
                throw new AssertionError(
                        "The authoritative " + environment
                                + " server changed the player's cross-belt position; "
                                + "expectedZ=" + expectedLateralPosition
                                + ", before={" + serverBefore + "}, after={" + serverAfter + "}"
                );
            }
            if (Math.abs(localMovement.x - serverLocalMovement.x) > 0.2
                    || Math.abs(localMovement.z - serverLocalMovement.z) > 0.05) {
                throw new AssertionError(
                        "The " + environment + " client and server disagreed on belt movement; "
                                + "client=" + localMovement + ", server=" + serverLocalMovement
                );
            }
            verifyPlayerLeavesBeltEnd(context, serverContext, environment, setup);
            verifyNonPlayerEntityPositionSync(context, serverContext, environment, setup);
    }

    private static void verifyNonPlayerEntityPositionSync(
            final ClientGameTestContext context,
            final TestServerContext serverContext,
            final String environment,
            final AssembledShaftSetup setup
    ) {
        final BlockPos landingBlock = setup.plotStart().west();
        final Vec3 spawnLocalPosition = Vec3.atBottomCenterOf(landingBlock);
        final int cowId = serverContext.computeOnServer(server -> {
            final ServerLevel level = server.overworld();
            final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
            final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            player.teleportTo(
                    ASSEMBLY_SHAFT_START.getX() - 8.0,
                    ASSEMBLY_SHAFT_START.getY() + 8.0,
                    ASSEMBLY_SHAFT_START.getZ() + 8.0
            );

            final var cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
            if (cow == null) {
                throw new AssertionError("Could not create a cow for the non-player entity sync test");
            }

            cow.setNoAi(true);
            cow.setNoGravity(true);
            cow.snapTo(subLevel.logicalPose().transformPosition(spawnLocalPosition.add(0.0, 2.0, 0.0)));
            if (!level.addFreshEntity(cow)) {
                throw new AssertionError("Could not spawn a cow for the non-player entity sync test");
            }
            return cow.getId();
        });

        // Ensure the initial spawn packet reached the client while the cow was
        // still in ordinary world space. The following landing then changes
        // onGround and forces ClientboundEntityPositionSyncPacket.
        context.waitFor(client -> {
            final Entity cow = client.level == null ? null : client.level.getEntity(cowId);
            return cow != null && !cow.onGround();
        });

        final Vec3 expectedLocalPosition = serverContext.computeOnServer(server -> {
            final ServerLevel level = server.overworld();
            final Entity cow = level.getEntity(cowId);
            if (cow == null) {
                throw new AssertionError("Server cow disappeared before the position-sync test");
            }

            final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
            cow.move(
                    MoverType.SELF,
                    subLevel.logicalPose().transformNormal(new Vec3(0.0, -3.0, 0.0))
            );
            cow.setDeltaMovement(Vec3.ZERO);
            cow.setOnGround(true);
            if (Sable.HELPER.getTrackingSubLevel(cow) != subLevel) {
                throw new AssertionError("Cow did not begin tracking the substructure after landing");
            }
            return subLevel.logicalPose().transformPositionInverse(cow.position());
        });

        context.waitFor(client -> {
            if (client.level == null) {
                return false;
            }
            final Entity cow = client.level.getEntity(cowId);
            final SubLevel subLevel = Sable.HELPER.getContaining(client.level, setup.plotStart());
            if (cow == null
                    || subLevel == null
                    || Sable.HELPER.getTrackingSubLevel(cow) != subLevel) {
                return false;
            }

            final Vec3 localPosition = subLevel.logicalPose().transformPositionInverse(cow.position());
            return localPosition.distanceToSqr(expectedLocalPosition) < 0.25;
        });
        final Vec3 clientLocalPosition = context.computeOnClient(client -> {
            final Entity cow = client.level.getEntity(cowId);
            final SubLevel subLevel = requireSubLevel(client.level, setup.plotStart());
            if (cow == null) {
                throw new AssertionError("Client cow disappeared after the position-sync test");
            }
            return subLevel.logicalPose().transformPositionInverse(cow.position());
        });
        final Vec3 serverLocalPosition = serverContext.computeOnServer(server -> {
            final Entity cow = server.overworld().getEntity(cowId);
            final SubLevel subLevel = requireSubLevel(server.overworld(), setup.plotStart());
            if (cow == null) {
                throw new AssertionError("Server cow disappeared after the position-sync test");
            }
            return subLevel.logicalPose().transformPositionInverse(cow.position());
        });
        System.out.println(
                "AERONAUTICS_NON_PLAYER_ENTITY_SYNC environment=" + environment
                        + ", expected=" + expectedLocalPosition
                        + ", client=" + clientLocalPosition
                        + ", server=" + serverLocalPosition
        );
        context.takeScreenshot(environment + "-non-player-entity-on-substructure");

        serverContext.runOnServer(server -> {
            final Entity cow = server.overworld().getEntity(cowId);
            if (cow != null) {
                cow.discard();
            }
        });
        context.waitFor(client -> client.level != null && client.level.getEntity(cowId) == null);
    }

    private static void verifyPlayerLeavesBeltEnd(
            final ClientGameTestContext context,
            final TestServerContext serverContext,
            final String environment,
            final AssembledShaftSetup setup
    ) {
        serverContext.runOnServer(server -> {
            final ServerLevel level = server.overworld();
            final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            final SubLevel subLevel = requireSubLevel(level, setup.plotStart());
            final Vec3 localStart = Vec3.atLowerCornerOf(setup.plotStart())
                    .add(0.5, 13.0 / 16.0, PLAYER_LATERAL_POSITION);
            final Vec3 position = subLevel.logicalPose().transformPosition(localStart);
            player.teleportTo(position.x, position.y, position.z);
            player.setDeltaMovement(Vec3.ZERO);
            player.setOnGround(true);
            final BeltBlockEntity controller = findBeltController(level, setup.plotStart());
            if (controller != null && controller.passengers != null) {
                controller.passengers.remove(player);
            }
        });

        context.waitFor(client -> {
            if (client.level == null || client.player == null) {
                return false;
            }
            final SubLevel subLevel = Sable.HELPER.getContaining(client.level, setup.plotStart());
            if (subLevel == null
                    || SubLevelBeltPlayerHandler.findContact(client.player, subLevel, null) == null) {
                return false;
            }
            final Vec3 localPosition =
                    subLevel.logicalPose().transformPositionInverse(client.player.position());
            return Math.abs(localPosition.x - (setup.plotStart().getX() + 0.5)) < 0.2;
        });

        final ClientBeltState before = context.computeOnClient(client ->
                observeClientBelt(client, setup.plotStart()));
        final ServerBeltState serverBefore = serverContext.computeOnServer(server ->
                observeServerBelt(server, setup.plotStart()));
        context.takeScreenshot(environment + "-substructure-belt-end-before-discharge");
        context.waitTicks(20);
        context.takeScreenshot(environment + "-substructure-belt-end-after-discharge");
        final ClientBeltState after = context.computeOnClient(client ->
                observeClientBelt(client, setup.plotStart()));
        final ServerBeltState serverAfter = serverContext.computeOnServer(server ->
                observeServerBelt(server, setup.plotStart()));
        final double playerHalfWidth = context.computeOnClient(client -> {
            if (client.player == null) {
                throw new AssertionError("Client player disappeared during belt-end verification");
            }
            return client.player.getBbWidth() / 2.0;
        });
        final double fullyClearCenterX = setup.plotStart().getX() - playerHalfWidth;
        final double expectedLateralPosition =
                setup.plotStart().getZ() + PLAYER_LATERAL_POSITION;

        System.out.println(
                "AERONAUTICS_SUBSTRUCTURE_BELT_END_DISCHARGE environment=" + environment
                        + ", fullyClearCenterX=" + fullyClearCenterX
                        + ", before={" + before + "}, after={" + after + "}"
                        + ", serverBefore={" + serverBefore + "}, serverAfter={" + serverAfter + "}"
        );

        if (after.localPlayerPosition().x > fullyClearCenterX + 0.02) {
            throw new AssertionError(
                    "The " + environment + " client player remained overlapping the discharge end of the belt; "
                            + "requiredCenterX<=" + fullyClearCenterX
                            + ", before={" + before + "}, after={" + after + "}"
            );
        }
        if (serverAfter.localPlayerPosition().x > fullyClearCenterX + 0.02) {
            throw new AssertionError(
                    "The authoritative " + environment
                            + " server left the player overlapping the discharge end of the belt; "
                            + "requiredCenterX<=" + fullyClearCenterX
                            + ", before={" + serverBefore + "}, after={" + serverAfter + "}"
            );
        }
        if (Math.abs(after.localPlayerPosition().x - serverAfter.localPlayerPosition().x) > 0.2) {
            throw new AssertionError(
                    "The " + environment + " client and server disagreed after belt discharge; "
                            + "client={" + after + "}, server={" + serverAfter + "}"
            );
        }
        if (Math.abs(after.localPlayerPosition().z - expectedLateralPosition) > 0.05
                || Math.abs(serverAfter.localPlayerPosition().z - expectedLateralPosition) > 0.05) {
            throw new AssertionError(
                    "The " + environment + " belt-end discharge changed the player's lateral position; "
                            + "expectedZ=" + expectedLateralPosition
                            + ", client={" + after + "}, server={" + serverAfter + "}"
            );
        }
    }

    private static void positionPlayerForTarget(
            final ServerPlayer player,
            final SubLevel subLevel,
            final BlockPos localTarget
    ) {
        final Vec3 target = subLevel.logicalPose().transformPosition(Vec3.atCenterOf(localTarget));
        final Vec3 feet = target.add(0.0, -0.5, 3.0);
        player.teleportTo(feet.x, feet.y, feet.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void positionPlayerForOverview(
            final ServerPlayer player,
            final SubLevel subLevel,
            final BlockPos localTarget
    ) {
        final Vec3 target = subLevel.logicalPose().transformPosition(Vec3.atCenterOf(localTarget));
        final Vec3 feet = subLevel.logicalPose().transformPosition(
                Vec3.atCenterOf(localTarget).add(0.0, 3.0, 5.0)
        );
        player.teleportTo(feet.x, feet.y, feet.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void aimClientAtSubLevelBlock(
            final ClientGameTestContext context,
            final BlockPos localTarget
    ) {
        context.runOnClient(client -> {
            if (client.level == null || client.player == null) {
                throw new AssertionError("Client world or player is unavailable while aiming");
            }
            final SubLevel subLevel = requireSubLevel(client.level, localTarget);
            final Vec3 target = subLevel.logicalPose().transformPosition(Vec3.atCenterOf(localTarget));
            client.player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
        });
        context.waitTick();
    }

    private static boolean isAimingAt(
            final net.minecraft.client.Minecraft client,
            final BlockPos localTarget
    ) {
        return client.hitResult instanceof BlockHitResult hit
                && hit.getBlockPos().equals(localTarget);
    }

    private static InteractionResult useHeldItemOnTarget(
            final ClientGameTestContext context,
            final BlockPos localTarget
    ) {
        for (int attempt = 0; attempt < 10; attempt++) {
            aimClientAtSubLevelBlock(context, localTarget);
            final InteractionResult result = context.computeOnClient(client -> {
                if (client.player == null || client.gameMode == null) {
                    throw new AssertionError("Client player or game mode is unavailable for Belt Connector use");
                }
                if (!(client.hitResult instanceof BlockHitResult hit)
                        || !hit.getBlockPos().equals(localTarget)) {
                    return null;
                }
                return client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
            });
            if (result != null) {
                return result;
            }
        }
        throw new AssertionError(
                "Client crosshair could not remain on the requested substructure shaft long enough to use it"
        );
    }

    private static SubLevel requireSubLevel(final Level level, final BlockPos plotPosition) {
        final SubLevel subLevel = Sable.HELPER.getContaining(level, plotPosition);
        if (subLevel == null) {
            throw new AssertionError("No real assembled substructure contains " + plotPosition);
        }
        return subLevel;
    }

    private static List<BlockPos> findAssembledBlocks(
            final ServerLevel level,
            final ServerSubLevel subLevel,
            final net.minecraft.world.level.block.Block block
    ) {
        final BlockPos center = subLevel.getPlot().getCenterBlock();
        final List<BlockPos> positions = new ArrayList<>();
        for (final BlockPos pos : BlockPos.betweenClosed(
                center.offset(-16, -8, -16),
                center.offset(16, 8, 16)
        )) {
            if (level.getBlockState(pos).is(block)) {
                positions.add(pos.immutable());
            }
        }
        return positions;
    }

    private static BeltBlockEntity findBeltController(final Level level, final BlockPos beltStart) {
        for (int index = 0; index < BELT_LENGTH; index++) {
            if (level.getBlockEntity(beltStart.east(index)) instanceof BeltBlockEntity belt
                    && belt.isController()) {
                return belt;
            }
        }
        return null;
    }

    private static int countBeltSegments(final Level level, final BlockPos beltStart) {
        int count = 0;
        for (int index = 0; index < BELT_LENGTH; index++) {
            if (level.getBlockState(beltStart.east(index)).is(AllBlocks.BELT)) {
                count++;
            }
        }
        return count;
    }

    private static BeltAssemblyState observeAssembledBelt(
            final Level level,
            final BlockPos plotStart
    ) {
        final BeltBlockEntity controller = findBeltController(level, plotStart);
        return new BeltAssemblyState(
                countBeltSegments(level, plotStart),
                controller != null,
                controller == null ? 0.0F : controller.getSpeed(),
                controller == null ? "<missing>" : controller.getBlockPos().toShortString()
        );
    }

    private static ClientBeltState observeClientBelt(
            final net.minecraft.client.Minecraft client,
            final BlockPos plotStart
    ) {
        if (client.level == null || client.player == null) {
            throw new AssertionError("Client world or local player is unavailable");
        }
        final SubLevel subLevel = requireSubLevel(client.level, plotStart);
        final BeltBlockEntity segment = BeltHelper.getSegmentBE(client.level, plotStart);
        if (segment == null) {
            throw new AssertionError("The Create belt made inside the substructure is unavailable");
        }

        final SubLevelBeltPlayerHandler.BeltContact contact =
                SubLevelBeltPlayerHandler.findContact(client.player, subLevel, null);
        final BeltBlockEntity controller = contact == null
                ? BeltHelper.getControllerBE(client.level, plotStart)
                : contact.controller();
        return new ClientBeltState(
                subLevel.logicalPose().transformPositionInverse(client.player.position()),
                controller == null ? segment.getSpeed() : controller.getSpeed(),
                controller != null && controller.isController(),
                controller != null
                        && controller.passengers != null
                        && controller.passengers.containsKey(client.player),
                contact != null,
                Sable.HELPER.getTrackingSubLevel(client.player) == subLevel
        );
    }

    private static ServerBeltState observeServerBelt(
            final net.minecraft.server.MinecraftServer server,
            final BlockPos plotStart
    ) {
        final ServerLevel level = server.overworld();
        final ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
        final SubLevel subLevel = requireSubLevel(level, plotStart);
        final BeltBlockEntity segment = BeltHelper.getSegmentBE(level, plotStart);
        if (segment == null) {
            throw new AssertionError("The server-side Create belt inside the substructure is unavailable");
        }

        final SubLevelBeltPlayerHandler.BeltContact contact =
                SubLevelBeltPlayerHandler.findContact(player, subLevel, null);
        final BeltBlockEntity controller = contact == null
                ? BeltHelper.getControllerBE(level, plotStart)
                : contact.controller();
        return new ServerBeltState(
                player.position(),
                subLevel.logicalPose().transformPositionInverse(player.position()),
                controller == null ? segment.getSpeed() : controller.getSpeed(),
                controller != null
                        && controller.passengers != null
                        && controller.passengers.containsKey(player),
                contact != null,
                Sable.HELPER.getTrackingSubLevel(player) == subLevel
        );
    }

    private record AssembledShaftSetup(BlockPos plotStart, BlockPos plotEnd) {
    }

    private record BeltAssemblyState(
            int segmentCount,
            boolean controllerPresent,
            float speed,
            String controllerPosition
    ) {
    }

    private record ClientBeltState(
            Vec3 localPlayerPosition,
            float speed,
            boolean controller,
            boolean passenger,
            boolean contact,
            boolean tracking
    ) {
    }

    private record ServerBeltState(
            Vec3 physicalPlayerPosition,
            Vec3 localPlayerPosition,
            float speed,
            boolean passenger,
            boolean contact,
            boolean tracking
    ) {
    }
}
