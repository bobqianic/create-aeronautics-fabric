package dev.simulated_team.simulated.index;

import com.zurrtum.create.api.registry.CreateBuiltInRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SimArmInteractions {
    private static <T extends ArmInteractionPointType> void register(final String name, final T type) {
        Registry.register(CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE, Simulated.path(name), type);
    }

    static {
        register("portable_engine", new PortableEngineType());
        register("navigation_table", new NavTableType());
    }

    public static class PortableEngineType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(final Level level, final BlockPos pos, final BlockState state) {
            return SimBlocks.PORTABLE_ENGINES.contains(state.getBlock());
        }

        @Override
        public @Nullable ArmInteractionPoint createPoint(final Level level, final BlockPos pos, final BlockState state) {
            return new PortableEngineInteractionPoint(this, level, pos, state);
        }
    }

    public static class PortableEngineInteractionPoint extends AllArmInteractionPointTypes.DepotPoint {
        public PortableEngineInteractionPoint(final ArmInteractionPointType type, final Level level, final BlockPos pos, final BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        public ItemStack insert(final ArmBlockEntity armBlockEntity, final ItemStack stack, final boolean simulate) {
            if (this.cachedState.hasBlockEntity()) {
                final BlockEntity be = this.level.getBlockEntity(this.pos);
                if (be instanceof final PortableEngineBlockEntity sbe) {
                    return sbe.inventory.insertSlot(stack, 0, simulate);
                }
            }

            return super.insert(armBlockEntity, stack, simulate);
        }
    }

    public static class NavTableType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(final Level level, final BlockPos pos, final BlockState state) {
            return SimBlocks.NAVIGATION_TABLE.has(state);
        }

        @Override
        public @Nullable ArmInteractionPoint createPoint(final Level level, final BlockPos pos, final BlockState state) {
            return new NavTablePoint(this, level, pos, state);
        }
    }

    public static class NavTablePoint extends AllArmInteractionPointTypes.DepotPoint {
        public NavTablePoint(final ArmInteractionPointType type, final Level level, final BlockPos pos, final BlockState state) {
            super(type, level, pos, state);
        }
    }

    public static void init() {}
}
