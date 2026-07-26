package dev.simulated_team.simulated.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.plot.SubLevelContainerHolder;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.util.SimDebugThing;
import net.minecraft.commands.CommandSourceStack;
import org.joml.Vector3d;

public class SimDebugThingCommands {
    public static int start(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final int steps = context.getArgument("steps", Integer.class);
        SimDebugThing.start(steps, context.getSource().getLevel());
        return 1;
    }

    public static int stop(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SimDebugThing.stop();
        return 1;
    }

    public static int abort(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        SimDebugThing.abort();
        return 1;
    }

    public static int stopSublevels(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getLevel() instanceof final SubLevelContainerHolder holder) {
            final SubLevelContainer plotContainer = holder.sable$getPlotContainer();

            if (plotContainer instanceof final ServerSubLevelContainer serverContainer) {

                final SubLevelPhysicsSystem physicsSystem = serverContainer.physicsSystem();
                final Vector3d angularVelocity = new Vector3d();
                final Vector3d linearVelocity = new Vector3d();
                for (final SubLevel sublevel : holder.sable$getPlotContainer().getAllSubLevels()) {
                    if (sublevel instanceof final ServerSubLevel serverSubLevel) {
                        final RigidBodyHandle rigidBodyHandle = physicsSystem.getPhysicsHandle(serverSubLevel);
                        rigidBodyHandle.getAngularVelocity(angularVelocity).negate();
                        rigidBodyHandle.getLinearVelocity(linearVelocity).negate();
                        serverSubLevel.logicalPose().orientation().transformInverse(angularVelocity);
                        serverSubLevel.logicalPose().orientation().transformInverse(linearVelocity);
                        final ForceTotal forceTotal = new ForceTotal();
                        forceTotal.applyLinearImpulse(linearVelocity.mul(serverSubLevel.getMassTracker().getMass()));
                        forceTotal.applyTorqueImpulse(serverSubLevel.getMassTracker().getInertiaTensor().transform(angularVelocity));
                        rigidBodyHandle.applyForcesAndReset(forceTotal);
                    }
                }
            }
        }
        return 1;
    }
}
