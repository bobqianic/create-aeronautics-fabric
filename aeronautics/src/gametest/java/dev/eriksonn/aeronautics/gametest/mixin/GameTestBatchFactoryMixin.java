package dev.eriksonn.aeronautics.gametest.mixin;

import net.minecraft.gametest.framework.GameTestBatchFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Keeps physics-heavy Ponder fixtures from changing one another while they are being verified. */
@Mixin(GameTestBatchFactory.class)
abstract class GameTestBatchFactoryMixin {
    @ModifyConstant(method = "method_66926", constant = @Constant(intValue = 50))
    private static int aeronautics$runPonderFixturesIndividually(final int originalBatchSize) {
        return 1;
    }
}
