package dev.ryanhcode.offroad.content.blocks.wheel_mount;

import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import dev.simulated_team.simulated.multiloader.inventory.SingleSlotContainer;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;

public class WheelMountInventory extends SingleSlotContainer {
    private WheelMountBlockEntity be;
    public boolean suppressUpdate = false;

    public WheelMountInventory(final WheelMountBlockEntity be) {
        super(1);

        this.be = be;
    }

    @Override
    public boolean canInsertItem(final ItemInfoWrapper item) {
        final TireLike tireLike = ItemInfoWrapper.generateFromInfo(item).get(OffroadDataComponents.TIRE);
        return tireLike != null;
    }

    @Override
    public void setChanged() {
        if (!suppressUpdate) {
            this.be.notifyUpdate();
        }
    }
}
