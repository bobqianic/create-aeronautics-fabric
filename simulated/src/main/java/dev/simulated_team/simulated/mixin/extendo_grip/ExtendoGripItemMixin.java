package dev.simulated_team.simulated.mixin.extendo_grip;

import com.zurrtum.create.content.equipment.extendoGrip.ExtendoGripItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ExtendoGripItem.class)
public abstract class ExtendoGripItemMixin extends Item {
    public ExtendoGripItemMixin(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean canDestroyBlock(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final LivingEntity entity) {
        return !(entity instanceof final Player player) || !player.isCreative();
    }
}
