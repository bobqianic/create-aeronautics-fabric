package dev.simulated_team.simulated.content.physics_staff;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.server.level.ServerLevel;

public class PhysicsStaffSubLevelObserver implements SubLevelObserver {
    private final ServerLevel level;

    public PhysicsStaffSubLevelObserver(final ServerLevel level) {
        this.level = level;
    }

    @Override
    public void tick(final SubLevelContainer subLevels) {
        this.getPhysicsHandler().tick();
    }

    @Override
    public void onSubLevelAdded(final SubLevel subLevel) {
        this.getPhysicsHandler().applyLockIfNeeded(subLevel);
    }

    @Override
    public void onSubLevelRemoved(final SubLevel subLevel, final SubLevelRemovalReason reason) {
        if (reason == SubLevelRemovalReason.REMOVED) {
            this.getPhysicsHandler().removeLock(subLevel);
        }
    }

    public PhysicsStaffServerHandler getPhysicsHandler() {
        return PhysicsStaffServerHandler.get(this.level);
    }
}
