package dev.eriksonn.aeronautics.gametest;

import dev.eriksonn.aeronautics.gametest.mixin.LevelRendererAccessor;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Verifies that every vanilla skull reaches the transformed block-entity
 * render queue after its blocks move into a substructure.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ClientSkullRenderGameTest implements FabricClientGameTest {

    private static final List<Block> HEADS = List.of(
            Blocks.SKELETON_SKULL,
            Blocks.WITHER_SKELETON_SKULL,
            Blocks.ZOMBIE_HEAD,
            Blocks.PLAYER_HEAD,
            Blocks.CREEPER_HEAD,
            Blocks.DRAGON_HEAD,
            Blocks.PIGLIN_HEAD
    );

    @Override
    public void runTest(final ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            final TestServerContext serverContext = singleplayer.getServer();
            final SkullScene scene = serverContext.computeOnServer(server -> createScene(server.overworld()));

            context.waitFor(client -> {
                if (client.level == null) {
                    return false;
                }

                final SubLevel subLevel = Sable.HELPER.getContaining(client.level, scene.plotHeadStart());
                if (subLevel == null || !subLevel.getUniqueId().equals(scene.subLevelId())) {
                    return false;
                }

                for (int index = 0; index < HEADS.size(); index++) {
                    final BlockPos headPos = scene.plotHeadStart().east(index);
                    if (!(client.level.getBlockEntity(headPos) instanceof SkullBlockEntity)
                            || !client.level.getBlockState(headPos).is(HEADS.get(index))) {
                        return false;
                    }
                }
                return true;
            });

            context.waitFor(client -> {
                final var levelRenderState =
                        ((LevelRendererAccessor) client.levelRenderer).aeronautics$getLevelRenderState();
                for (int index = 0; index < HEADS.size(); index++) {
                    final BlockPos expectedPos = scene.plotHeadStart().east(index);
                    final boolean extracted = levelRenderState.blockEntityRenderStates.stream()
                            .anyMatch(state -> state instanceof SkullBlockRenderState
                                    && state.blockPos.equals(expectedPos));
                    if (!extracted) {
                        return false;
                    }
                }
                return true;
            });

            context.waitTicks(5);
            context.takeScreenshot("vanilla-heads-inside-substructure");
        }
    }

    private static SkullScene createScene(final ServerLevel level) {
        final BlockPos supportStart = new BlockPos(0, 96, 0);
        final List<BlockPos> assembledBlocks = new ArrayList<>();
        for (int index = 0; index < HEADS.size(); index++) {
            final BlockPos supportPos = supportStart.east(index);
            final BlockPos headPos = supportPos.above();
            level.setBlockAndUpdate(supportPos, Blocks.QUARTZ_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(headPos, HEADS.get(index).defaultBlockState());
            assembledBlocks.add(supportPos);
            assembledBlocks.add(headPos);
        }

        final BlockPos anchor = supportStart.east(HEADS.size() / 2);
        final ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(
                level,
                anchor,
                assembledBlocks,
                new BoundingBox3i(supportStart, supportStart.east(HEADS.size() - 1).above())
        );
        final BlockPos plotHeadStart = subLevel.getPlot()
                .getCenterBlock()
                .west(HEADS.size() / 2)
                .above();
        final Vec3 physicalHeadCenter = subLevel.logicalPose()
                .transformPosition(Vec3.atCenterOf(plotHeadStart.east(HEADS.size() / 2)));

        final ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
        player.setGameMode(GameType.SPECTATOR);
        player.teleportTo(
                physicalHeadCenter.x,
                physicalHeadCenter.y + 1.0,
                physicalHeadCenter.z + 8.0
        );
        player.lookAt(EntityAnchorArgument.Anchor.EYES, physicalHeadCenter);

        return new SkullScene(subLevel.getUniqueId(), plotHeadStart);
    }

    private record SkullScene(UUID subLevelId, BlockPos plotHeadStart) {
    }
}
