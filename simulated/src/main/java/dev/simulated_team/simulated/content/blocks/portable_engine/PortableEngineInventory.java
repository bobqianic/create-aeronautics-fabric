package dev.simulated_team.simulated.content.blocks.portable_engine;

import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;
import dev.simulated_team.simulated.service.SimItemService;

public class PortableEngineInventory extends SingleSlotContainer {

    private final PortableEngineBlockEntity be;

    public PortableEngineInventory(final PortableEngineBlockEntity be) {
        super(64);
        this.be = be;
    }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper info) {
        return SimItemService.INSTANCE.getBurnTime(info.type().getDefaultInstance()) > 0;
    }


    @Override
    public void setChanged() {
        this.be.notifyUpdate();
    }
}
