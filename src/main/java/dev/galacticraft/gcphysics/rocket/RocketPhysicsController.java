package dev.galacticraft.gcphysics.rocket;

import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
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

            RigidBodyHandle handle = physicsSystem.getPhysicsHandle(subLevel);
            if (handle == null || !handle.isValid()) {
                continue;
            }

            for (RocketSubLevelScanner.EngineForceSample sample : scan.samples()) {
                Vec3 impulseThisTick = sample.localThrustPerSecond().scale(timeStep);
                Vec3 enginePlotPosition = sample.localPosition();

                handle.applyImpulseAtPoint(
                        new Vector3d(
                                enginePlotPosition.x,
                                enginePlotPosition.y,
                                enginePlotPosition.z
                        ),
                        new Vector3d(
                                impulseThisTick.x,
                                impulseThisTick.y,
                                impulseThisTick.z
                        )
                );
            }
        }
    }
}