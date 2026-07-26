package dev.simulated_team.simulated.content.physics_staff;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PhysicsStaffItem extends Item {
    public static float RANGE = 128.0f;

    public PhysicsStaffItem(final Properties properties) {
        super(properties);
    }

    public static boolean isHolding(final Player player) {
        return player.getMainHandItem().getItem() instanceof PhysicsStaffItem ||
                player.getOffhandItem().getItem() instanceof PhysicsStaffItem;
    }

    @Override
    public boolean canDestroyBlock(final ItemStack stack, final BlockState state, final Level world, final BlockPos pos, final LivingEntity entity) {
        return false;
    }
}
