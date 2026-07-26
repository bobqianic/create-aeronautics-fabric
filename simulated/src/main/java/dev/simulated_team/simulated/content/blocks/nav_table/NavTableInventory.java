package dev.simulated_team.simulated.content.blocks.nav_table;

import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;

public class NavTableInventory extends SingleSlotContainer {

    NavTableBlockEntity be;

    public NavTableInventory(final NavTableBlockEntity be) {
        super(1);

        this.be = be;
    }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper item) {
        return NavigationTarget.ofStack(ItemInfoWrapper.generateFromInfo(item)) != null;
    }

    @Override
    public void setChanged() {
        this.be.notifyUpdate();
    }
}
