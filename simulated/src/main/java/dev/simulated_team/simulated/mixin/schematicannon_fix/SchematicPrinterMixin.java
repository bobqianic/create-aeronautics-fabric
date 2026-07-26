package dev.simulated_team.simulated.mixin.schematicannon_fix;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.schematics.SchematicPrinter;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// todo: delete this when updating create
@Mixin(SchematicPrinter.class)
public class SchematicPrinterMixin {
    @Shadow private SchematicLevel blockReader;

    @Redirect(method = "getCurrentRequirement", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/foundation/utility/BlockHelper;prepareBlockEntityData(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag fixBlockRequirements(final Level level, final BlockState block, final BlockEntity _blockEntity, @Local(name = "target") final BlockPos target) {
        return BlockHelper.prepareBlockEntityData(level, block, this.blockReader.getBlockEntity(target));
    }
}
