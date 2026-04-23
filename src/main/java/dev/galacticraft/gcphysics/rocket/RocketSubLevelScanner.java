package dev.galacticraft.gcphysics.rocket;

import dev.galacticraft.gcphysics.block.rocket.RocketEngineBlock;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class RocketSubLevelScanner {
    private RocketSubLevelScanner() {
    }

    public static EngineScanResult scanPoweredEngines(ServerSubLevel subLevel) {
        ServerLevelPlot plot = subLevel.getPlot();

        int totalEngines = 0;
        int poweredEngines = 0;
        List<EngineForceSample> samples = new ArrayList<>();

        for (PlotChunkHolder holder : plot.getLoadedChunks()) {
            LevelChunk chunk = holder.getChunk();
            LevelChunkSection[] sections = chunk.getSections();

            for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                LevelChunkSection section = sections[sectionIndex];
                if (section == null || section.hasOnlyAir()) {
                    continue;
                }

                int baseY = chunk.getSectionYFromSectionIndex(sectionIndex);

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

                            int blockX = chunk.getPos().getMinBlockX() + localX;
                            int blockY = baseY + localY;
                            int blockZ = chunk.getPos().getMinBlockZ() + localZ;

                            Vec3 localCenter = new Vec3(
                                    blockX + 0.5,
                                    blockY + 0.5,
                                    blockZ + 0.5
                            );

                            Vec3 localThrustPerSecond = RocketEngineBlock.getLocalThrustPerSecond(state);

                            samples.add(new EngineForceSample(
                                    localCenter,
                                    localThrustPerSecond
                            ));
                        }
                    }
                }
            }
        }

        return new EngineScanResult(
                totalEngines,
                poweredEngines,
                samples
        );
    }

    public record EngineForceSample(
            Vec3 localPosition,
            Vec3 localThrustPerSecond
    ) {
    }

    public record EngineScanResult(
            int totalEngineCount,
            int poweredEngineCount,
            List<EngineForceSample> samples
    ) {
    }
}