package dev.galacticraft.gcphysics.multiblock;

import dev.galacticraft.gcphysics.GcPhysicsMod;
import dev.galacticraft.gcphysics.block.entity.EngineeringBayBlockEntity;
import dev.galacticraft.gcphysics.compat.sable.GcSableBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class EngineeringBayConstruction {
    private EngineeringBayConstruction() {
    }

    public static boolean canBeginConstruction(EngineeringBayBlockEntity bay) {
        return bay.isFormed()
                && bay.hasLaunchPad()
                && bay.getLaunchPadWidth() >= 5
                && bay.getLaunchPadLength() >= 5
                && bay.hasLaunchTower()
                && bay.getLaunchTowerHeight() >= 5;
    }

    public static boolean beginConstruction(ServerLevel level, EngineeringBayBlockEntity bay) {
        if (!canBeginConstruction(bay)) {
            return false;
        }

        BlockPos towerBase = bay.getLaunchTowerMin();
        BlockPos towerTop = bay.getLaunchTowerMax();
        Vec3 clampPos = getClampPosition(bay, towerBase, towerTop);

        if (clampPos == null) {
            GcPhysicsMod.LOGGER.warn("Could not determine clamp position for bay at {}", bay.getBlockPos());
            return false;
        }

        UUID clampId = GcSableBridge.spawnLockedClamp(level, clampPos);
        if (clampId == null) {
            return false;
        }

            bay.setConstructionClampId(clampId);
        bay.setChanged();
        return true;
    }

    public static boolean finishConstruction(ServerLevel level, EngineeringBayBlockEntity bay) {
        UUID clampId = bay.getConstructionClampId();
        if (clampId == null) {
            bay.clearConstructionClampId();
            bay.setChanged();
            return true;
        }

        boolean finished = GcSableBridge.finishClampConstruction(level, clampId);

        bay.clearConstructionClampId();
        bay.setChanged();

        return finished;
    }

    private static @Nullable Vec3 getClampPosition(EngineeringBayBlockEntity bay, BlockPos towerBase, BlockPos towerTop) {
        BlockPos padMin = bay.getLaunchPadMin();
        BlockPos padMax = bay.getLaunchPadMax();

        int x = towerTop.getX();
        int y = towerTop.getY();
        int z = towerTop.getZ();

        if (towerBase.getZ() == padMin.getZ()) {
            return new Vec3(x + 0.5, y + 0.5, z + 1.5);
        }

        if (towerBase.getZ() == padMax.getZ()) {
            return new Vec3(x + 0.5, y + 0.5, z - 0.5);
        }

        if (towerBase.getX() == padMin.getX()) {
            return new Vec3(x + 1.5, y + 0.5, z + 0.5);
        }

        if (towerBase.getX() == padMax.getX()) {
            return new Vec3(x - 0.5, y + 0.5, z + 0.5);
        }

        return null;
    }
}