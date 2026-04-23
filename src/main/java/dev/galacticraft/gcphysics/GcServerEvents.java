package dev.galacticraft.gcphysics;

import dev.galacticraft.gcphysics.compat.sable.GcClampSubLevelObserver;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import net.minecraft.world.level.Level;

public final class GcServerEvents {
    private GcServerEvents() {
    }

    public static void init() {
        SableEventPlatform.INSTANCE.onSubLevelContainerReady((Level level, SubLevelContainer subLevelContainer) -> {
            if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
                return;
            }

            if (!(subLevelContainer instanceof ServerSubLevelContainer serverContainer)) {
                return;
            }

            serverContainer.addObserver(new GcClampSubLevelObserver(serverLevel));
        });
    }
}