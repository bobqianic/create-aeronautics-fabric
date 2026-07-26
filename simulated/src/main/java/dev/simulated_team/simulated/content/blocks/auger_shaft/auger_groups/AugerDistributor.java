package dev.simulated_team.simulated.content.blocks.auger_shaft.auger_groups;

import dev.ryanhcode.sable.util.LevelAccelerator;
import dev.simulated_team.simulated.content.blocks.auger_shaft.BlockHarvester;
import dev.simulated_team.simulated.content.blocks.auger_shaft.ItemReciever;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

//TODO: make this an actual system instead of something slapped on top of augers
public record AugerDistributor(List<ItemReciever> receivers, List<BlockHarvester> harvesters, MutableInt index) {

	public AugerDistributor() {
		this(new ArrayList<>(), new ArrayList<>(), new MutableInt());
	}

	/**
     * Attempts to distribute the given item stack between receivers
     */
    public ItemStack distributeItem(final ItemStack stack, final BlockPos fromPos) {
        if (this.receivers.isEmpty()) {
            return stack;
        }

        if (this.checkAndCleanReceivers()) {
            return stack;
        }

        ItemStack modifiedStack = stack;
        final int startIndex = this.index.getValue();
        do {
            final ItemReciever nextReceiver = this.receivers.get(this.index.getValue());
            if (nextReceiver.isActive()) {
                modifiedStack = nextReceiver.onRecieveItem(stack, fromPos);
            }

            //increment round robin distribution
            this.index.increment();
            this.index.setValue(this.index.getValue() % this.receivers.size());
        } while (startIndex != this.index.getValue() && modifiedStack.equals(stack));

        return modifiedStack;
    }

    private boolean checkAndCleanReceivers() {
        if (this.receivers.removeIf(ItemReciever::removed)) {
            if (this.receivers.isEmpty()) {
                return true;
            }

            this.index.setValue(this.index.getValue() % this.receivers.size());
        }

        return false;
    }

    /**
     * Flood-fills a given *plane*, gathering and associating all harvesters in the area with this distributor
     */
    public void gatherAndAssociateHarvesters(final Direction[] surrounding, final BlockPos startingPos, final Level level /*Need this for block entities*/, final LevelAccelerator accelerator) {
//	    if (this.receivers.isEmpty() || this.receivers.getFirst() != caller) { //make sure only the first receiver can refresh the list
//			return;
//	    }

		final Set<BlockPos> visited = new HashSet<>();
        final Queue<BlockPos> frontier = new ArrayDeque<>(16);

	    for (final BlockHarvester harvester : this.harvesters) {
			harvester.simulated$setDistributor(null); //clear distribution data for refresh
	    }
	    this.harvesters.clear();

        frontier.add(startingPos);
        while (!frontier.isEmpty()) {
            final BlockPos pos = frontier.poll();
            if (visited.contains(pos))
                continue;
            visited.add(pos);

            //check the position to make sure it's a block harvester, and if so, do some sick logic
            final BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof final BlockHarvester harvester) {
	            this.harvesters.add(harvester);
                harvester.simulated$setDistributor(this);
            }

            //iterate through the surrounding directions and add them to the frontier
            for (final Direction d : surrounding) {
                final BlockPos newp = pos.relative(d);
                if (accelerator.getBlockState(newp).hasBlockEntity()) {
                    //Technically this is causing iteration to happen multiple times when it is not needed
                    frontier.add(newp);
                }
            }
        }

        accelerator.clearCache();
        visited.clear();
        frontier.clear();
    }

    public void addReceiver(final ItemReciever receiver) {
        this.receivers.add(receiver);
    }

    public void removeReceiver(final ItemReciever receiver) {
        this.receivers.remove(receiver);
    }
}
