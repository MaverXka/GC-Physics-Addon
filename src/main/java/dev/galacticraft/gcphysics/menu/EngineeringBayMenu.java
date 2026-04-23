package dev.galacticraft.gcphysics.menu;

import dev.galacticraft.gcphysics.block.EngineeringBayBlock;
import dev.galacticraft.gcphysics.block.GcBlocks;
import dev.galacticraft.gcphysics.block.entity.EngineeringBayBlockEntity;
import dev.galacticraft.gcphysics.multiblock.EngineeringBayConstruction;
import dev.galacticraft.gcphysics.multiblock.LaunchPadDetector;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.MenuData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EngineeringBayMenu extends MachineMenu<EngineeringBayBlockEntity> {
    public static final int BUTTON_DETECT_LAUNCH_SITE = 0;
    public static final int BUTTON_BEGIN_CONSTRUCTION = 1;
    public static final int BUTTON_FINISH_CONSTRUCTION = 2;

    private final @Nullable EngineeringBayBlockEntity machine;
    private final BlockPos accessPos;

    private boolean formed;
    private int masterX;
    private int masterY;
    private int masterZ;

    private boolean hasLaunchPad;
    private int launchPadMinX;
    private int launchPadMinY;
    private int launchPadMinZ;
    private int launchPadMaxX;
    private int launchPadMaxY;
    private int launchPadMaxZ;
    private int launchPadWidth;
    private int launchPadLength;

    private boolean hasLaunchTower;
    private int launchTowerMinX;
    private int launchTowerMinY;
    private int launchTowerMinZ;
    private int launchTowerMaxX;
    private int launchTowerMaxY;
    private int launchTowerMaxZ;
    private int launchTowerHeight;

    private boolean hasConstructionClamp;

    public EngineeringBayMenu(MenuType<? extends MachineMenu<EngineeringBayBlockEntity>> type, int syncId, Inventory inventory, BlockPos pos) {
        super(type, syncId, inventory, pos, 8, 84);
        this.machine = null;
        this.accessPos = pos;
    }

    public EngineeringBayMenu(MenuType<? extends MachineMenu<EngineeringBayBlockEntity>> type, int syncId, Player player, @Nullable EngineeringBayBlockEntity machine) {
        super(type, syncId, player, Objects.requireNonNull(machine));
        this.machine = machine;
        this.accessPos = machine.getBlockPos();
        this.registerData(this.getData());
    }

    @Override
    public void registerData(@NotNull MenuData data) {
        super.registerData(data);

        data.registerBoolean(this::formedSupplier, value -> this.formed = value);
        data.registerInt(this::masterXSupplier, value -> this.masterX = value);
        data.registerInt(this::masterYSupplier, value -> this.masterY = value);
        data.registerInt(this::masterZSupplier, value -> this.masterZ = value);

        data.registerBoolean(this::hasLaunchPadSupplier, value -> this.hasLaunchPad = value);
        data.registerInt(this::launchPadMinXSupplier, value -> this.launchPadMinX = value);
        data.registerInt(this::launchPadMinYSupplier, value -> this.launchPadMinY = value);
        data.registerInt(this::launchPadMinZSupplier, value -> this.launchPadMinZ = value);
        data.registerInt(this::launchPadMaxXSupplier, value -> this.launchPadMaxX = value);
        data.registerInt(this::launchPadMaxYSupplier, value -> this.launchPadMaxY = value);
        data.registerInt(this::launchPadMaxZSupplier, value -> this.launchPadMaxZ = value);
        data.registerInt(this::launchPadWidthSupplier, value -> this.launchPadWidth = value);
        data.registerInt(this::launchPadLengthSupplier, value -> this.launchPadLength = value);

        data.registerBoolean(this::hasLaunchTowerSupplier, value -> this.hasLaunchTower = value);
        data.registerInt(this::launchTowerMinXSupplier, value -> this.launchTowerMinX = value);
        data.registerInt(this::launchTowerMinYSupplier, value -> this.launchTowerMinY = value);
        data.registerInt(this::launchTowerMinZSupplier, value -> this.launchTowerMinZ = value);
        data.registerInt(this::launchTowerMaxXSupplier, value -> this.launchTowerMaxX = value);
        data.registerInt(this::launchTowerMaxYSupplier, value -> this.launchTowerMaxY = value);
        data.registerInt(this::launchTowerMaxZSupplier, value -> this.launchTowerMaxZ = value);
        data.registerInt(this::launchTowerHeightSupplier, value -> this.launchTowerHeight = value);

        data.registerBoolean(this::hasConstructionClampSupplier, value -> this.hasConstructionClamp = value);
    }

    private boolean formedSupplier() {
        return machine != null ? machine.isFormed() : formed;
    }

    private int masterXSupplier() {
        return machine != null ? machine.getMasterPos().getX() : masterX;
    }

    private int masterYSupplier() {
        return machine != null ? machine.getMasterPos().getY() : masterY;
    }

    private int masterZSupplier() {
        return machine != null ? machine.getMasterPos().getZ() : masterZ;
    }

    private boolean hasLaunchPadSupplier() {
        return machine != null ? machine.hasLaunchPad() : hasLaunchPad;
    }

    private int launchPadMinXSupplier() {
        return machine != null ? machine.getLaunchPadMin().getX() : launchPadMinX;
    }

    private int launchPadMinYSupplier() {
        return machine != null ? machine.getLaunchPadMin().getY() : launchPadMinY;
    }

    private int launchPadMinZSupplier() {
        return machine != null ? machine.getLaunchPadMin().getZ() : launchPadMinZ;
    }

    private int launchPadMaxXSupplier() {
        return machine != null ? machine.getLaunchPadMax().getX() : launchPadMaxX;
    }

    private int launchPadMaxYSupplier() {
        return machine != null ? machine.getLaunchPadMax().getY() : launchPadMaxY;
    }

    private int launchPadMaxZSupplier() {
        return machine != null ? machine.getLaunchPadMax().getZ() : launchPadMaxZ;
    }

    private int launchPadWidthSupplier() {
        return machine != null ? machine.getLaunchPadWidth() : launchPadWidth;
    }

    private int launchPadLengthSupplier() {
        return machine != null ? machine.getLaunchPadLength() : launchPadLength;
    }

    private boolean hasLaunchTowerSupplier() {
        return machine != null ? machine.hasLaunchTower() : hasLaunchTower;
    }

    private int launchTowerMinXSupplier() {
        return machine != null ? machine.getLaunchTowerMin().getX() : launchTowerMinX;
    }

    private int launchTowerMinYSupplier() {
        return machine != null ? machine.getLaunchTowerMin().getY() : launchTowerMinY;
    }

    private int launchTowerMinZSupplier() {
        return machine != null ? machine.getLaunchTowerMin().getZ() : launchTowerMinZ;
    }

    private int launchTowerMaxXSupplier() {
        return machine != null ? machine.getLaunchTowerMax().getX() : launchTowerMaxX;
    }

    private int launchTowerMaxYSupplier() {
        return machine != null ? machine.getLaunchTowerMax().getY() : launchTowerMaxY;
    }

    private int launchTowerMaxZSupplier() {
        return machine != null ? machine.getLaunchTowerMax().getZ() : launchTowerMaxZ;
    }

    private int launchTowerHeightSupplier() {
        return machine != null ? machine.getLaunchTowerHeight() : launchTowerHeight;
    }

    private boolean hasConstructionClampSupplier() {
        return machine != null && machine.getConstructionClampId() != null;
    }

    public boolean hasLaunchPad() {
        return hasLaunchPad;
    }

    public int getLaunchPadMinX() {
        return launchPadMinX;
    }

    public int getLaunchPadMinY() {
        return launchPadMinY;
    }

    public int getLaunchPadMinZ() {
        return launchPadMinZ;
    }

    public int getLaunchPadMaxX() {
        return launchPadMaxX;
    }

    public int getLaunchPadMaxY() {
        return launchPadMaxY;
    }

    public int getLaunchPadMaxZ() {
        return launchPadMaxZ;
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

    public int getLaunchTowerMinX() {
        return launchTowerMinX;
    }

    public int getLaunchTowerMinY() {
        return launchTowerMinY;
    }

    public int getLaunchTowerMinZ() {
        return launchTowerMinZ;
    }

    public int getLaunchTowerMaxX() {
        return launchTowerMaxX;
    }

    public int getLaunchTowerMaxY() {
        return launchTowerMaxY;
    }

    public int getLaunchTowerMaxZ() {
        return launchTowerMaxZ;
    }

    public int getLaunchTowerHeight() {
        return launchTowerHeight;
    }

    public boolean canBeginConstruction() {
        return hasLaunchPad
                && launchPadWidth >= 5
                && launchPadLength >= 5
                && hasLaunchTower
                && launchTowerHeight >= 5
                && !hasConstructionClamp;
    }

    public boolean hasConstructionClamp() {
        return hasConstructionClamp;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        BlockEntity be = serverLevel.getBlockEntity(accessPos);
        if (!(be instanceof EngineeringBayBlockEntity bay)) {
            return false;
        }

        BlockEntity masterBe = serverLevel.getBlockEntity(bay.getMasterPos());
        if (!(masterBe instanceof EngineeringBayBlockEntity masterBay)) {
            return false;
        }

        if (id == BUTTON_DETECT_LAUNCH_SITE) {
            LaunchPadDetector.detect(serverLevel, masterBay);
            masterBay.setChanged();
            return true;
        }

        if (id == BUTTON_BEGIN_CONSTRUCTION) {
            return EngineeringBayConstruction.beginConstruction(serverLevel, masterBay);
        }

        if (id == BUTTON_FINISH_CONSTRUCTION) {
            return EngineeringBayConstruction.finishConstruction(serverLevel, masterBay);
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockState state = player.level().getBlockState(accessPos);
        if (!state.is(GcBlocks.ENGINEERING_BAY)) {
            return false;
        }

        if (!state.hasProperty(EngineeringBayBlock.FORMED) || !state.getValue(EngineeringBayBlock.FORMED)) {
            return false;
        }

        BlockEntity be = player.level().getBlockEntity(accessPos);
        if (!(be instanceof EngineeringBayBlockEntity bay) || !bay.isFormed()) {
            return false;
        }

        BlockEntity masterBe = player.level().getBlockEntity(bay.getMasterPos());
        if (!(masterBe instanceof EngineeringBayBlockEntity masterBay) || !masterBay.isFormed()) {
            return false;
        }

        return player.canInteractWithBlock(accessPos, 8.0);
    }
}