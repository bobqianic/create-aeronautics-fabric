package dev.simulated_team.simulated.mixin.player;

import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntity;
import dev.simulated_team.simulated.mixin_interface.PlayerLaunchedPlungerExtension;
import dev.simulated_team.simulated.mixin_interface.PlayerTypewriterExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerMixin implements PlayerTypewriterExtension, PlayerLaunchedPlungerExtension {

    @Unique
    BlockPos simulated$currentTypeWriter = null;

    @Unique
    LaunchedPlungerEntity simulated$launchedPlunger = null;

    @Override
    public BlockPos simulated$getCurrentTypewriter() {
        return this.simulated$currentTypeWriter;
    }

    @Override
    public void simulated$setCurrentTypewriter(final BlockPos pos) {
        this.simulated$currentTypeWriter = pos;
    }

    @Override
    public void simulated$setLaunchedPlunger(final LaunchedPlungerEntity plunger) {
        this.simulated$launchedPlunger = plunger;
    }

    @Override
    public LaunchedPlungerEntity simulated$getLaunchedPlunger() {
        return this.simulated$launchedPlunger;
    }
}
