package dev.simulated_team.simulated.content.blocks.auger_shaft;


import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;

public class AugerInventory extends SingleSlotContainer {

    private final AugerShaftBlockEntity be;

    public AugerInventory(final AugerShaftBlockEntity be) {
        super(64);
        this.be = be;
    }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper info) {
        return true;
    }

    @Override
    public void setChanged() {
        this.be.notifyUpdate();
    }
}
