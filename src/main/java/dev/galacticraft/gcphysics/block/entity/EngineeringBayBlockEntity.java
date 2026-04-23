package dev.galacticraft.gcphysics.block.entity;

import dev.galacticraft.gcphysics.menu.EngineeringBayMenu;
import dev.galacticraft.gcphysics.menu.GcMenuTypes;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EngineeringBayBlockEntity extends MachineBlockEntity {
    private static final StorageSpec SPEC = StorageSpec.empty();

    @Nullable
    private UUID constructionClampId = null;

    private boolean formed = false;
    private BlockPos masterPos = BlockPos.ZERO;
    private int sizeX = 1;
    private int sizeY = 1;
    private int sizeZ = 1;

    private boolean hasLaunchPad = false;
    private BlockPos launchPadMin = BlockPos.ZERO;
    private BlockPos launchPadMax = BlockPos.ZERO;
    private int launchPadWidth = 0;
    private int launchPadLength = 0;

    private boolean hasLaunchTower = false;
    private BlockPos launchTowerMin = BlockPos.ZERO;
    private BlockPos launchTowerMax = BlockPos.ZERO;
    private int launchTowerHeight = 0;

    public EngineeringBayBlockEntity(BlockPos pos, BlockState state) {
        super(GcBlockEntities.ENGINEERING_BAY, pos, state, SPEC);
        this.masterPos = pos;
    }

    @Override
    protected void tickConstant(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        super.tickConstant(level, pos, state, profiler);
    }

    @Override
    public @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        return MachineStatuses.ACTIVE;
    }

    @Nullable
    @Override
    public MachineMenu<? extends MachineBlockEntity> createMenu(int syncId, Inventory playerInventory, Player player) {
        return new EngineeringBayMenu(
                GcMenuTypes.ENGINEERING_BAY,
                syncId,
                player,
                this
        );
    }

    public boolean isFormed() {
        return formed;
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public boolean hasLaunchPad() {
        return hasLaunchPad;
    }

    public BlockPos getLaunchPadMin() {
        return launchPadMin;
    }

    public BlockPos getLaunchPadMax() {
        return launchPadMax;
    }

    public int getLaunchPadWidth() {
        return launchPadWidth;
    }

    public int getLaunchPadLength() {
        return launchPadLength;
    }

    public boolean hasLaunchTower() {
        return hasLaunchTower;
    }

    public BlockPos getLaunchTowerMin() {
        return launchTowerMin;
    }

    public BlockPos getLaunchTowerMax() {
        return launchTowerMax;
    }

    public int getLaunchTowerHeight() {
        return launchTowerHeight;
    }

    public void setStructureData(boolean formed, BlockPos masterPos, int sizeX, int sizeY, int sizeZ) {
        this.formed = formed;
        this.masterPos = masterPos;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        setChanged();
    }

    public void resetStructureData() {
        this.formed = false;
        this.masterPos = this.worldPosition;
        this.sizeX = 1;
        this.sizeY = 1;
        this.sizeZ = 1;
        clearLaunchPadData();
        clearLaunchTowerData();
        setChanged();
    }

    public void setLaunchPadData(BlockPos min, BlockPos max, int width, int length) {
        this.hasLaunchPad = true;
        this.launchPadMin = min;
        this.launchPadMax = max;
        this.launchPadWidth = width;
        this.launchPadLength = length;
        setChanged();
    }

    public void clearLaunchPadData() {
        this.hasLaunchPad = false;
        this.launchPadMin = BlockPos.ZERO;
        this.launchPadMax = BlockPos.ZERO;
        this.launchPadWidth = 0;
        this.launchPadLength = 0;
        setChanged();
    }

    public void setLaunchTowerData(BlockPos min, BlockPos max, int height) {
        this.hasLaunchTower = true;
        this.launchTowerMin = min;
        this.launchTowerMax = max;
        this.launchTowerHeight = height;
        setChanged();
    }

    public void clearLaunchTowerData() {
        this.hasLaunchTower = false;
        this.launchTowerMin = BlockPos.ZERO;
        this.launchTowerMax = BlockPos.ZERO;
        this.launchTowerHeight = 0;
        setChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Engineering Bay");
    }

    @Nullable
    public UUID getConstructionClampId() {
        return constructionClampId;
    }

    public void setConstructionClampId(@Nullable UUID constructionClampId) {
        this.constructionClampId = constructionClampId;
    }

    public void clearConstructionClampId() {
        this.constructionClampId = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("Formed", formed);
        tag.putInt("MasterX", masterPos.getX());
        tag.putInt("MasterY", masterPos.getY());
        tag.putInt("MasterZ", masterPos.getZ());
        tag.putInt("SizeX", sizeX);
        tag.putInt("SizeY", sizeY);
        tag.putInt("SizeZ", sizeZ);

        tag.putBoolean("HasLaunchPad", hasLaunchPad);
        tag.putInt("LaunchPadMinX", launchPadMin.getX());
        tag.putInt("LaunchPadMinY", launchPadMin.getY());
        tag.putInt("LaunchPadMinZ", launchPadMin.getZ());
        tag.putInt("LaunchPadMaxX", launchPadMax.getX());
        tag.putInt("LaunchPadMaxY", launchPadMax.getY());
        tag.putInt("LaunchPadMaxZ", launchPadMax.getZ());
        tag.putInt("LaunchPadWidth", launchPadWidth);
        tag.putInt("LaunchPadLength", launchPadLength);

        tag.putBoolean("HasLaunchTower", hasLaunchTower);
        tag.putInt("LaunchTowerMinX", launchTowerMin.getX());
        tag.putInt("LaunchTowerMinY", launchTowerMin.getY());
        tag.putInt("LaunchTowerMinZ", launchTowerMin.getZ());
        tag.putInt("LaunchTowerMaxX", launchTowerMax.getX());
        tag.putInt("LaunchTowerMaxY", launchTowerMax.getY());
        tag.putInt("LaunchTowerMaxZ", launchTowerMax.getZ());
        tag.putInt("LaunchTowerHeight", launchTowerHeight);

        if (constructionClampId != null) {
            tag.putUUID("ConstructionClampId", constructionClampId);
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.formed = tag.getBoolean("Formed");
        this.masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        this.sizeX = tag.getInt("SizeX");
        this.sizeY = tag.getInt("SizeY");
        this.sizeZ = tag.getInt("SizeZ");

        this.hasLaunchPad = tag.getBoolean("HasLaunchPad");
        this.launchPadMin = new BlockPos(
                tag.getInt("LaunchPadMinX"),
                tag.getInt("LaunchPadMinY"),
                tag.getInt("LaunchPadMinZ")
        );
        this.launchPadMax = new BlockPos(
                tag.getInt("LaunchPadMaxX"),
                tag.getInt("LaunchPadMaxY"),
                tag.getInt("LaunchPadMaxZ")
        );
        this.launchPadWidth = tag.getInt("LaunchPadWidth");
        this.launchPadLength = tag.getInt("LaunchPadLength");

        this.hasLaunchTower = tag.getBoolean("HasLaunchTower");
        this.launchTowerMin = new BlockPos(
                tag.getInt("LaunchTowerMinX"),
                tag.getInt("LaunchTowerMinY"),
                tag.getInt("LaunchTowerMinZ")
        );
        this.launchTowerMax = new BlockPos(
                tag.getInt("LaunchTowerMaxX"),
                tag.getInt("LaunchTowerMaxY"),
                tag.getInt("LaunchTowerMaxZ")
        );
        this.launchTowerHeight = tag.getInt("LaunchTowerHeight");

        if (tag.hasUUID("ConstructionClampId")) {
            this.constructionClampId = tag.getUUID("ConstructionClampId");
        } else {
            this.constructionClampId = null;
        }
    }
}