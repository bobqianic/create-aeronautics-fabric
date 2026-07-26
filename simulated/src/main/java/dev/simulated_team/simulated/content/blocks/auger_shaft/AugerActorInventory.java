package dev.simulated_team.simulated.content.blocks.auger_shaft;


import dev.simulated_team.simulated.multiloader.inventory.MultiSlotContainer;

public class AugerActorInventory extends MultiSlotContainer {

    private final AugerShaftBlockEntity be;

    public AugerActorInventory(final AugerShaftBlockEntity be, final int size) {
        super(size);
        this.be = be;
    }

    @Override
    public void setChanged() {
        this.be.notifyUpdate();
    }
}
