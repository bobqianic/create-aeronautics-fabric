package dev.ryanhcode.offroad.content.contraptions.borehead_contraption;

import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.bearing.BearingContraption;
import dev.simulated_team.simulated.multiloader.inventory.InventoryLoaderWrapper;
import dev.simulated_team.simulated.service.SimInventoryService;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelBlock;
import dev.ryanhcode.offroad.data.OffroadLang;
import dev.ryanhcode.offroad.index.OffroadContraptionTypes;
import dev.ryanhcode.offroad.mixin.MountedStorageAccessor;
import dev.ryanhcode.offroad.service.OffroadMountedStorageService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;

public class BoreheadBearingContraption extends BearingContraption {
    public int rockCuttingWheelAmount = 0;

    private InventoryLoaderWrapper multiLoaderWrappedInventory;

    public BoreheadBearingContraption() {
        this.storage = OffroadMountedStorageService.INSTANCE.getSidedBoreheadContraptionMountedStorage();
    }

    public BoreheadBearingContraption(final Direction direction) {
        super(false, direction);

        this.storage = OffroadMountedStorageService.INSTANCE.getSidedBoreheadContraptionMountedStorage();
    }

    @Override
    public boolean assemble(final Level world, final BlockPos pos) throws AssemblyException {
        final boolean assemble = super.assemble(world, pos);

        if (this.rockCuttingWheelAmount < 1) {
            throw new AssemblyException(OffroadLang.translate("exceptions.borehead_bearing.insuffecient_rockcutting_wheels").component());
        }

        if (((MountedStorageAccessor) this.storage).getItemsBuilder().isEmpty()) {
            throw new AssemblyException(OffroadLang.translate("exceptions.borehead_bearing.insuffecient_inventory_blocks").component());
        }

        return assemble;
    }

    @Override
    public ContraptionType getType() {
        return OffroadContraptionTypes.BOREHEAD_CONTRAPTION_TYPE;
    }

    @Override
    public void addBlock(final Level level, final BlockPos pos, final Pair<StructureTemplate.StructureBlockInfo, BlockEntity> capture) {
        super.addBlock(level, pos, capture);

        if (capture.getKey().state().getBlock() instanceof RockCuttingWheelBlock) {
            this.rockCuttingWheelAmount++;
        }
    }

    public InventoryLoaderWrapper getSimWrappedStorage() {
        if (this.multiLoaderWrappedInventory == null) {
            this.multiLoaderWrappedInventory = SimInventoryService.INSTANCE.getWrappedAllItemsFromContraption(this.getStorage());
        }

        return this.multiLoaderWrappedInventory;
    }
}
