package dev.galacticraft.gcphysics.multiblock;

import dev.galacticraft.gcphysics.block.GcBlocks;
import dev.galacticraft.gcphysics.block.entity.EngineeringBayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class LaunchPadDetector {
    private static final int MIN_PAD_WIDTH = 5;
    private static final int MIN_PAD_LENGTH = 5;
    private static final int MIN_TOWER_HEIGHT = 5;

    private LaunchPadDetector() {
    }

    public static void detect(ServerLevel level, EngineeringBayBlockEntity bay) {
        bay.clearLaunchPadData();
        bay.clearLaunchTowerData();

        if (!bay.isFormed()) {
            return;
        }

        BlockPos master = bay.getMasterPos();
        int sizeX = bay.getSizeX();
        int sizeZ = bay.getSizeZ();

        LaunchPadResult best = null;

        if (sizeX == 3 && sizeZ == 2) {
            best = chooseBetter(best, detectFromEdge(level, master.offset(0, 0, -1), 1, 0, 0, -1));
            best = chooseBetter(best, detectFromEdge(level, master.offset(0, 0, 2), 1, 0, 0, 1));
        } else if (sizeX == 2 && sizeZ == 3) {
            best = chooseBetter(best, detectFromEdge(level, master.offset(-1, 0, 0), 0, 1, -1, 0));
            best = chooseBetter(best, detectFromEdge(level, master.offset(2, 0, 0), 0, 1, 1, 0));
        }

        if (best != null) {
            bay.setLaunchPadData(best.min, best.max, best.width, best.length);

            if (best.hasTower()) {
                bay.setLaunchTowerData(best.towerMin, best.towerMax, best.towerHeight);
            }
        }
    }

    private static LaunchPadResult detectFromEdge(ServerLevel level, BlockPos start, int alongX, int alongZ, int outwardX, int outwardZ) {
        for (int i = 0; i < 3; i++) {
            BlockPos pos = start.offset(alongX * i, 0, alongZ * i);
            if (!level.getBlockState(pos).is(GcBlocks.LAUNCH_PAD)) {
                return null;
            }
        }

        Set<BlockPos> component = collectConnectedPads(level, start, alongX, alongZ);
        if (component.isEmpty()) {
            return null;
        }

        Set<LocalCell> cells = new HashSet<>();
        int minU = Integer.MAX_VALUE;
        int maxU = Integer.MIN_VALUE;
        int maxV = Integer.MIN_VALUE;

        for (BlockPos pos : component) {
            int dx = pos.getX() - start.getX();
            int dz = pos.getZ() - start.getZ();

            int u = dx * alongX + dz * alongZ;
            int v = dx * outwardX + dz * outwardZ;

            if (v < 0) {
                continue;
            }

            LocalCell cell = new LocalCell(u, v);
            cells.add(cell);

            minU = Math.min(minU, u);
            maxU = Math.max(maxU, u);
            maxV = Math.max(maxV, v);
        }

        if (cells.isEmpty()) {
            return null;
        }

        LaunchPadResult best = null;

        for (int rectMinU = minU; rectMinU <= 0; rectMinU++) {
            for (int rectMaxU = 2; rectMaxU <= maxU; rectMaxU++) {
                int width = rectMaxU - rectMinU + 1;

                for (int rectMaxV = 0; rectMaxV <= maxV; rectMaxV++) {
                    int length = rectMaxV + 1;

                    if (width < MIN_PAD_WIDTH || length < MIN_PAD_LENGTH) {
                        continue;
                    }

                    if (!isFilledRectangle(cells, rectMinU, rectMaxU, rectMaxV)) {
                        continue;
                    }

                    BlockPos cornerA = start.offset(
                            alongX * rectMinU,
                            0,
                            alongZ * rectMinU
                    );

                    BlockPos cornerB = start.offset(
                            alongX * rectMaxU + outwardX * rectMaxV,
                            0,
                            alongZ * rectMaxU + outwardZ * rectMaxV
                    );

                    BlockPos min = minPos(cornerA, cornerB);
                    BlockPos max = maxPos(cornerA, cornerB);

                    TowerResult tower = detectTower(level, start, alongX, alongZ);

                    LaunchPadResult candidate = new LaunchPadResult(
                            min,
                            max,
                            width,
                            length,
                            tower.min,
                            tower.max,
                            tower.height
                    );

                    best = chooseBetter(best, candidate);
                }
            }
        }

        return best;
    }

    private static TowerResult detectTower(ServerLevel level, BlockPos start, int alongX, int alongZ) {
        BlockPos towerPad = start.offset(alongX, 0, alongZ);
        BlockPos firstTowerBlock = towerPad.above();

        if (!level.getBlockState(firstTowerBlock).is(GcBlocks.LAUNCH_TOWER)) {
            return TowerResult.none();
        }

        int height = 0;
        BlockPos current = firstTowerBlock;

        while (level.getBlockState(current).is(GcBlocks.LAUNCH_TOWER)) {
            height++;
            current = current.above();
        }

        if (height < MIN_TOWER_HEIGHT) {
            return TowerResult.none();
        }

        BlockPos min = firstTowerBlock;
        BlockPos max = firstTowerBlock.above(height - 1);
        return new TowerResult(min, max, height);
    }

    private static Set<BlockPos> collectConnectedPads(ServerLevel level, BlockPos start, int alongX, int alongZ) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        for (int i = 0; i < 3; i++) {
            BlockPos pos = start.offset(alongX * i, 0, alongZ * i);
            if (level.getBlockState(pos).is(GcBlocks.LAUNCH_PAD)) {
                visited.add(pos);
                queue.add(pos);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();

            for (BlockPos next : new BlockPos[]{
                    current.north(),
                    current.south(),
                    current.east(),
                    current.west()
            }) {
                if (visited.contains(next)) {
                    continue;
                }

                if (next.getY() != start.getY()) {
                    continue;
                }

                if (!level.getBlockState(next).is(GcBlocks.LAUNCH_PAD)) {
                    continue;
                }

                visited.add(next);
                queue.add(next);
            }
        }

        return visited;
    }

    private static boolean isFilledRectangle(Set<LocalCell> cells, int minU, int maxU, int maxV) {
        for (int u = minU; u <= maxU; u++) {
            for (int v = 0; v <= maxV; v++) {
                if (!cells.contains(new LocalCell(u, v))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static LaunchPadResult chooseBetter(LaunchPadResult a, LaunchPadResult b) {
        if (a == null) return b;
        if (b == null) return a;

        int areaA = a.area();
        int areaB = b.area();

        if (areaB > areaA) {
            return b;
        }

        if (areaA > areaB) {
            return a;
        }

        if (b.width > a.width) {
            return b;
        }

        return a;
    }

    private static BlockPos minPos(BlockPos a, BlockPos b) {
        return new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
    }

    private static BlockPos maxPos(BlockPos a, BlockPos b) {
        return new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );
    }

    private record LocalCell(int u, int v) {
    }

    private record TowerResult(BlockPos min, BlockPos max, int height) {
        static TowerResult none() {
            return new TowerResult(BlockPos.ZERO, BlockPos.ZERO, 0);
        }
    }

    private record LaunchPadResult(
            BlockPos min,
            BlockPos max,
            int width,
            int length,
            BlockPos towerMin,
            BlockPos towerMax,
            int towerHeight
    ) {
        int area() {
            return width * length;
        }

        boolean hasTower() {
            return towerHeight > 0;
        }
    }
}