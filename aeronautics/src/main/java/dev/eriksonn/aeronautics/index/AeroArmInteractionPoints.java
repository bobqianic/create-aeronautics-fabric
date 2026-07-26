package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.api.registry.CreateBuiltInRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AeroArmInteractionPoints {
    private static <T extends ArmInteractionPointType> void register(final String name, final T type) {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, Aeronautics.path(name), type);
    }

    static {
        register("mounted_potato_cannon_point", new MountedPotatoCannonType());
    }

    public static class MountedPotatoCannonType extends ArmInteractionPointType {

        @Override
        public boolean canCreatePoint(final Level var1, final BlockPos var2, final BlockState var3) {
            return AeroBlocks.MOUNTED_POTATO_CANNON.has(var1.getBlockState(var2));
        }

        @Override
        public @Nullable ArmInteractionPoint createPoint(final Level var1, final BlockPos var2, final BlockState var3) {
            return new MountedPotatoCannonPoint(this, var1, var2, var3);
        }
    }

    public static class MountedPotatoCannonPoint extends AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint {
        public MountedPotatoCannonPoint(final ArmInteractionPointType type, final Level level, final BlockPos pos, final BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(final ArmBlockEntity armBlockEntity, final ItemStack stack, final boolean simulate) {
            if (this.cachedState.hasBlockEntity()) {
                final BlockEntity be = this.level.getBlockEntity(this.pos);
                if (be instanceof final MountedPotatoCannonBlockEntity sbe) {
                    return sbe.getInventory().insertSlot(stack, 0, simulate);
                }
            }

            return super.insert(armBlockEntity, stack, simulate);
        }
    }

    public static void init() {

    }
}