package dev.simulated_team.simulated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zurrtum.create.content.contraptions.glue.SuperGlueEntity;
import dev.ryanhcode.sable.api.command.SubLevelArgumentType;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueEntity;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffServerHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;

public class SimCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext buildContext) {
        final LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("simulated");

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            cmd.then(Commands.literal("debugthing")
                    .requires(command -> command.hasPermission(2))
                    .then(Commands.literal("start")
                            .then(Commands.argument("steps", IntegerArgumentType.integer()).executes(SimDebugThingCommands::start)))
                    .then(Commands.literal("stop").executes(SimDebugThingCommands::stop))
                    .then(Commands.literal("abort").executes(SimDebugThingCommands::abort))
                    .then(Commands.literal("stop_sublevels").executes(SimDebugThingCommands::stopSublevels)));
        }

        cmd.then(Commands.literal("lock")
                .requires(command -> command.hasPermission(2))
                .then(Commands.argument("sub_levels", SubLevelArgumentType.subLevels())
                        .executes(ctx -> lockSubLevels(ctx, true))
                        .then(Commands.argument("locked", BoolArgumentType.bool())
                                .executes(ctx -> lockSubLevels(ctx, false)))));
        cmd.then(Commands.literal("glue")
                .requires(command -> command.hasPermission(2))
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                .executes(SimCommand::glueArea))));

        dispatcher.register(cmd);
    }

    private static int glueArea(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");

        ServerLevel world = ctx.getSource()
                .getLevel();

        HoneyGlueEntity entity = new HoneyGlueEntity(world, SuperGlueEntity.span(from, to));
        world.addFreshEntity(entity);
        return 1;
    }

    private static int lockSubLevels(CommandContext<CommandSourceStack> ctx, boolean toggle) throws CommandSyntaxException {
        Collection<ServerSubLevel> subLevels = SubLevelArgumentType.getSubLevels(ctx, "sub_levels");
        int updated = 0;

        PhysicsStaffServerHandler handler = PhysicsStaffServerHandler.get(ctx.getSource().getLevel());
        for (ServerSubLevel subLevel : subLevels) {
            if(toggle) {
                handler.toggleLock(subLevel.getUniqueId());
                updated++;
            } else {
                boolean isLocked = handler.isLocked(subLevel);
                boolean shouldLock = BoolArgumentType.getBool(ctx, "locked");
                if(shouldLock != isLocked) {
                    handler.toggleLock(subLevel.getUniqueId());
                    updated++;
                }
            }
        }

        Component message = Component.translatable("commands.simulated.lock.success", updated, updated == 1 ? "" : "s");
        ctx.getSource().sendSuccess(() -> message, true);

        return updated;
    }
}
