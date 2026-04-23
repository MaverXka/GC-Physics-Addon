package dev.galacticraft.gcphysics.compat.sable;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.physics.constraint.fixed.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.fixed.FixedConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GcClampLockData extends SavedData {
    public static final String ID = "gcphysics_clamp_locks";

    private final Map<UUID, Lock> locks = new HashMap<>();
    private ServerLevel level;

    public GcClampLockData() {
        this(null);
    }

    public GcClampLockData(@Nullable ServerLevel level) {
        this.level = level;
    }

    private static GcClampLockData create(ServerLevel level, CompoundTag tag, HolderLookup.Provider registries) {
        GcClampLockData data = new GcClampLockData(level);
        data.loadLocks(tag.getList(ID, Tag.TAG_INT_ARRAY));
        return data;
    }

    public static GcClampLockData get(ServerLevel level) {
        GcClampLockData data = level.getChunkSource().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GcClampLockData::new, (tag, lookup) -> create(level, tag, lookup), null),
                ID
        );
        data.level = level;
        return data;
    }

    private static FixedConstraintHandle addConstraint(ServerSubLevelContainer container, ServerSubLevel subLevel) {
        SubLevelPhysicsSystem physicsSystem = container.physicsSystem();
        PhysicsPipeline pipeline = physicsSystem.getPipeline();

        return pipeline.addConstraint(
                null,
                subLevel,
                new FixedConstraintConfiguration(
                        subLevel.logicalPose().position(),
                        subLevel.logicalPose().rotationPoint(),
                        subLevel.logicalPose().orientation()
                )
        );
    }

    public boolean hasLockEntry(UUID uuid) {
        return this.locks.containsKey(uuid);
    }

    public boolean isLocked(UUID uuid) {
        Lock lock = this.locks.get(uuid);
        return lock != null && lock.handle() != null && lock.handle().isValid();
    }

    public void toggleLock(final UUID uuid) {

        final ServerSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        if (container == null) {
            return;
        }

        final ServerSubLevel subLevel = (ServerSubLevel) container.getSubLevel(uuid);
        if (subLevel == null) {
            return;
        }

        final Lock existingLock = this.locks.get(uuid);
        if (existingLock != null) {
            this.locks.remove(uuid);
            existingLock.remove();
            this.setLocksDirty();
            return;
        }

        final FixedConstraintHandle handle = addConstraint(container, subLevel);
        this.locks.put(uuid, new Lock(uuid, handle));
        this.setLocksDirty();
    }

    public void applyLockIfNeeded(final SubLevel subLevel) {
        final Lock lock = this.locks.get(subLevel.getUniqueId());
        if (lock != null && (lock.handle() == null || !lock.handle().isValid())) {
            if (this.level == null) {
                return;
            }

            final ServerSubLevelContainer container = SubLevelContainer.getContainer(this.level);
            if (container == null) {
                return;
            }

            final FixedConstraintHandle handle = addConstraint(container, (ServerSubLevel) subLevel);
            this.locks.put(lock.subLevel(), new Lock(lock.subLevel(), handle));
            this.setLocksDirty();
        }
    }

    public boolean clearLockEntry(final UUID uuid) {
        final Lock removedLock = this.locks.remove(uuid);
        if (removedLock != null) {
            removedLock.remove();
            this.setLocksDirty();
            return true;
        }
        return false;
    }

    public void removeLock(final SubLevel subLevel) {
        clearLockEntry(subLevel.getUniqueId());
    }

    private void setLocksDirty() {
        this.setDirty(true);
    }

    private void loadLocks(final ListTag list) {
        for (final Tag tag : list) {
            final UUID uuid = NbtUtils.loadUUID(tag);
            this.locks.put(uuid, new Lock(uuid, null));
        }
    }

    private void saveLocks(final ListTag list) {
        list.addAll(this.locks.keySet().stream().map(NbtUtils::createUUID).toList());
    }

    @Override
    public @NotNull CompoundTag save(final CompoundTag tag, final HolderLookup.@NotNull Provider provider) {
        final ListTag tags = new ListTag();
        this.saveLocks(tags);
        tag.put(ID, tags);
        return tag;
    }

    private record Lock(@NotNull UUID subLevel, @Nullable PhysicsConstraintHandle handle) {
        private void remove() {
            if (this.handle != null) {
                this.handle.remove();
            }
        }
    }
}