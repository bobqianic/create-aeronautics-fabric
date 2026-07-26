package dev.ryanhcode.offroad.index;

import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import dev.simulated_team.simulated.service.SimInventoryService;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlockEntity;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingRenderer;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingVisual;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelBlockEntity;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelRenderer;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;

public class OffroadBlockEntityTypes {
    private static final SimulatedRegistrate REGISTRATE = Offroad.getRegistrate();

    public static final BlockEntityEntry<WheelMountBlockEntity> WHEEL_MOUNT = REGISTRATE
            .blockEntity("wheel_mount", WheelMountBlockEntity::new)
            .onRegister(SimInventoryService.INSTANCE.registerInventory((be, dir) -> be.getInventory()))
            .validBlocks(OffroadBlocks.WHEEL_MOUNT)
            .renderer(() -> WheelMountRenderer::new)
            .register();

    public static final BlockEntityEntry<BoreheadBearingBlockEntity> BOREHEAD_BEARING = REGISTRATE
            .blockEntity("borehead_bearing", BoreheadBearingBlockEntity::new)
            .renderer(() -> BoreheadBearingRenderer::new)
            .validBlock(OffroadBlocks.BOREHEAD_BEARING_BLOCK)
            .onRegister(SimInventoryService.INSTANCE.<BoreheadBearingBlockEntity>registerInventory((be, dir) -> be.getContraptionWrappedInventory()))
            .register();

    public static final BlockEntityEntry<RockCuttingWheelBlockEntity> ROCKCUTTING_WHEEL_BLOCK_ENTITY = REGISTRATE
            .blockEntity("rockcutting_wheel", RockCuttingWheelBlockEntity::new)
            .renderer(() -> RockCuttingWheelRenderer::new)
            .validBlock(OffroadBlocks.ROCK_CUTTER_BLOCK)
            .register();

    public static void init() {
    }
}
