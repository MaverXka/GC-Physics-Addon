package dev.galacticraft.gcphysics.rocket;

import dev.galacticraft.gcphysics.block.rocket.RocketEngineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;

public final class RocketSubLevelScanner {
    private RocketSubLevelScanner() {
    }

    public static EngineScanResult scanPoweredEngines(ServerSubLevel subLevel) {
        ServerLevelPlot plot = subLevel.getPlot();

        int totalEngines = 0;
        int poweredEngines = 0;
        double thrustX = 0.0;
        double thrustY = 0.0;
        double thrustZ = 0.0;

        for (PlotChunkHolder holder : plot.getLoadedChunks()) {
            LevelChunk chunk = holder.getChunk();
            LevelChunkSection[] sections = chunk.getSections();

            for (LevelChunkSection section : sections) {
                if (section == null || section.hasOnlyAir()) {
                    continue;
                }

                for (int localX = 0; localX < 16; localX++) {
                    for (int localY = 0; localY < 16; localY++) {
                        for (int localZ = 0; localZ < 16; localZ++) {
                            BlockState state = section.getBlockState(localX, localY, localZ);

                            if (!RocketEngineBlock.isMainEngine(state)) {
                                continue;
                            }

                            totalEngines++;

                            if (!RocketEngineBlock.isEngineActive(state)) {
                                continue;
                            }

                            poweredEngines++;

                            Vec3 thrust = RocketEngineBlock.getLocalThrustPerSecond(state);
                            thrustX += thrust.x;
                            thrustY += thrust.y;
                            thrustZ += thrust.z;
                        }
                    }
                }
            }
        }

        return new EngineScanResult(
                totalEngines,
                poweredEngines,
                new Vec3(thrustX, thrustY, thrustZ)
        );
    }

    public record EngineScanResult(
            int totalEngineCount,
            int poweredEngineCount,
            Vec3 totalLocalThrustPerSecond
    ) {
    }
}