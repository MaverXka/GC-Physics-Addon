package dev.galacticraft.gcphysics.compat.sable;

import dev.galacticraft.gcphysics.GcPhysicsMod;
import dev.galacticraft.gcphysics.block.GcBlocks;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class GcSableBridge {
    private GcSableBridge() {
    }

    @Nullable
    public static UUID spawnLockedClamp(ServerLevel level, Vec3 pos) {
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            GcPhysicsMod.LOGGER.warn("Failed to spawn clamp at {}: no ServerSubLevelContainer", pos);
            return null;
        }

        Pose3d pose = new Pose3d();
        pose.position().set(pos.x(), pos.y(), pos.z());

        ServerSubLevel subLevel = (ServerSubLevel) container.allocateNewSubLevel(pose);
        subLevel.setName("gcphysics_clamp");

        ServerLevelPlot plot = subLevel.getPlot();
        ChunkPos center = plot.getCenterChunk();
        plot.newEmptyChunk(center);
        plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, GcBlocks.CLAMP.defaultBlockState(), 3);
        subLevel.updateLastPose();

        UUID uuid = subLevel.getUniqueId();
        GcClampLockData.get(level).toggleLock(uuid);

        return uuid;
    }

    public static boolean finishClampConstruction(ServerLevel level, UUID subLevelId) {
        GcClampLockData lockData = GcClampLockData.get(level);

        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            lockData.clearLockEntry(subLevelId);
            return true;
        }

        ServerSubLevel subLevel = (ServerSubLevel) container.getSubLevel(subLevelId);
        if (subLevel != null) {
            ServerLevelPlot plot = subLevel.getPlot();
            plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, Blocks.AIR.defaultBlockState(), 3);
            subLevel.updateLastPose();
        }

        lockData.clearLockEntry(subLevelId);
        return true;
    }
}