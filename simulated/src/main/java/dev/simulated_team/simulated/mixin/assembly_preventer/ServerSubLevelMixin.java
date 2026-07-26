package dev.simulated_team.simulated.mixin.assembly_preventer;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.simulated_team.simulated.mixin_interface.assembly_preventer.PrimaryAssemblerExtension;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerSubLevel.class)
public class ServerSubLevelMixin implements PrimaryAssemblerExtension {

    @Unique
    private BlockPos simulated$primaryAssembler = null;

    @Override
    public @Nullable BlockPos simulated$getPrimaryAssembler() {
        return this.simulated$primaryAssembler;
    }

    @Override
    public void simulated$setPrimaryAssembler(final BlockPos pos) {
        this.simulated$primaryAssembler = pos;
    }

}
