package dev.ryanhcode.sable.mixin.assembly;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BaseContainerBlockEntity implements Clearable {

    // mc26.1: recipesUsed keys on ResourceKey<Recipe<?>> now
    @Shadow @Final private Reference2IntOpenHashMap<net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>>> recipesUsed;

    protected AbstractFurnaceBlockEntityMixin(final BlockEntityType<?> blockEntityType, final BlockPos blockPos, final BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.recipesUsed.clear();
    }

}
