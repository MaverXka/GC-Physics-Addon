package dev.galacticraft.gcphysics.rocket;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public final class RocketPhysicsController {
    private RocketPhysicsController() {
    }

    public static void tickPhysics(SubLevelPhysicsSystem physicsSystem, double timeStep) {
        ServerLevel level = physicsSystem.getLevel();
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container == null) {
            return;
        }

        for (ServerSubLevel subLevel : container.getAllSubLevels()) {
            if (subLevel.isRemoved()) {
                continue;
            }

            RocketSubLevelScanner.EngineScanResult scan = RocketSubLevelScanner.scanPoweredEngines(subLevel);
            if (scan.poweredEngineCount() <= 0) {
                continue;
            }

            Vec3 thrustPerSecond = scan.totalLocalThrustPerSecond();
            Vec3 impulseThisTick = thrustPerSecond.scale(timeStep);

            physicsSystem.getPhysicsHandle(subLevel).applyLinearImpulse(
                    new Vector3d(
                            impulseThisTick.x,
                            impulseThisTick.y,
                            impulseThisTick.z
                    )
            );
        }
    }
}