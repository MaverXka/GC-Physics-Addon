package dev.galacticraft.gcphysics.compat.sable;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.server.level.ServerLevel;

public class GcClampSubLevelObserver implements SubLevelObserver {
    private final ServerLevel level;

    public GcClampSubLevelObserver(ServerLevel level) {
        this.level = level;
    }

    @Override
    public void tick(final SubLevelContainer subLevels) {
    }

    @Override
    public void onSubLevelAdded(final SubLevel subLevel) {
        this.getClampLockData().applyLockIfNeeded(subLevel);
    }

    @Override
    public void onSubLevelRemoved(final SubLevel subLevel, final SubLevelRemovalReason reason) {
        if (reason == SubLevelRemovalReason.REMOVED) {
            this.getClampLockData().removeLock(subLevel);
        }
    }

    private GcClampLockData getClampLockData() {
        return GcClampLockData.get(this.level);
    }
}