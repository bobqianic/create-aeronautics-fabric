package dev.simulated_team.simulated.config.server.blocks;

import com.zurrtum.create.catnip.config.ConfigBase;

public class SimAssembly extends ConfigBase {

    public final ConfigInt maxBlocksMoved = this.i(128_000, 1, "maxBlocksMoved", Comments.maxBlocksMoved);
    public final ConfigInt honeyGlueRange = this.i(48, 1, Integer.MAX_VALUE,"honeyGlueRange",Comments.honeyGlueRange);
    public final ConfigFloat mergingGlueRange = this.f(4.0f, 0.0f, Float.MAX_VALUE,"mergingGlueRange",Comments.mergingGlueRange);
    public final ConfigInt maxDisassemblyTicks = this.i(20, 5, "maxDisassemblyTicks", Comments.maxDisassemblyTicks);
    public final ConfigFloat disassemblyDegreeTolerance = this.f(4, 0, "disassemblyDegreeTolerance", Comments.disassemblyDegreeTolerance);
    public final ConfigFloat disassemblyMaxVelocity = this.f(5, 0, "disassemblyMaxVelocity", Comments.disassemblyMaxVelocity);
    public final ConfigFloat disassemblyMaxAngularVelocity = this.f((float) (Math.PI / 2.0), 0, "disassemblyMaxAngularVelocity", Comments.disassemblyMaxAngularVelocity);
    public final ConfigBool disallowMidAirDisassembly = this.b(true, "disallowMidAirDisassembly", Comments.disallowMidAirDisassembly);

    public final ConfigBool primaryDisassembly = this.b(false, "Primary Disassembly", "Whether only the original Physics Assembler can disassemble the Sub-Level it assembled", "Disabling allows *ALL* Physics Assemblers to disassemble any Sub-Level");

    @Override
    public String getName() {
        return "assembly";
    }
    private static class Comments{
        static String honeyGlueRange = "Maximum range in blocks which honey glue may initially be placed";
        static String mergingGlueRange = "Maximum range in blocks which merging glue may be placed by items such as slime balls";
        static String maxBlocksMoved =
                "Maximum amount of blocks in a structure assemble-able by Physics Assemblers, Swivel Bearings, or other means.";
        static String maxDisassemblyTicks = "The amount of ticks that disassembly alignment is allowed to take before failing.";
        static String disassemblyDegreeTolerance = "The maximum amount of degrees a Simulated Contraption is allowed to be tilted to fully disassemble";
        static String disassemblyMaxVelocity = "The maximum velocity a Simulated Contraption is allowed to disassemble at in m/s";
        static String disassemblyMaxAngularVelocity = "The maximum angular velocity a Simulated Contraption is allowed to disassemble at in rad/s";
        static String disallowMidAirDisassembly = "Disallow disassembly of Simulated Contraptions in mid-air, requiring them to be within a few chunk sections of terrain";
    }
}
