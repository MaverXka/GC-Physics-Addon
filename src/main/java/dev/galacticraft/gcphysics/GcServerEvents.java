package dev.galacticraft.gcphysics;

import dev.galacticraft.gcphysics.compat.sable.GcClampSubLevelObserver;
import dev.galacticraft.gcphysics.rocket.RocketPhysicsController;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class GcServerEvents {
    private GcServerEvents() {
    }

    public static void init() {
        SableEventPlatform.INSTANCE.onSubLevelContainerReady((Level level, SubLevelContainer subLevelContainer) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            if (!(subLevelContainer instanceof ServerSubLevelContainer serverContainer)) {
                return;
            }

            serverContainer.addObserver(new GcClampSubLevelObserver(serverLevel));

            GcPhysicsMod.LOGGER.info(
                    "Registered GC hook sublevel observer for {}",
                    serverLevel.dimension().location()
            );
        });

        SableEventPlatform.INSTANCE.onPhysicsTick(RocketPhysicsController::tickPhysics);
    }
}