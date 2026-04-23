package dev.galacticraft.gcphysics.multiblock;

import dev.galacticraft.gcphysics.block.EngineeringBayBlock;
import dev.galacticraft.gcphysics.block.GcBlocks;
import dev.galacticraft.gcphysics.block.entity.EngineeringBayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class EngineeringBayMultiblock {
    private static boolean suppressUpdates = false;

    private EngineeringBayMultiblock() {
    }

    public static boolean isSuppressingUpdates() {
        return suppressUpdates;
    }

    public static void refreshAround(ServerLevel level, BlockPos origin, @Nullable UUID formingPlayerUuid) {
        Set<BlockPos> mastersToUnform = new HashSet<>();

        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos checkPos = origin.offset(x, y, z);
                    if (!(level.getBlockEntity(checkPos) instanceof EngineeringBayBlockEntity be)) {
                        continue;
                    }
                    if (be.isFormed()) {
                        mastersToUnform.add(be.getMasterPos());
                    }
                }
            }
        }

        for (BlockPos masterPos : mastersToUnform) {
            if (level.getBlockEntity(masterPos) instanceof EngineeringBayBlockEntity masterBe) {
                unform(level, masterPos, masterBe.getSizeX(), masterBe.getSizeY(), masterBe.getSizeZ());
            }
        }

        tryFormAt(level, origin, formingPlayerUuid);
    }

    private static void tryFormAt(ServerLevel level, BlockPos origin, @Nullable UUID formingPlayerUuid) {
        for (int ox = 0; ox < 3; ox++) {
            for (int oy = 0; oy < 2; oy++) {
                for (int oz = 0; oz < 2; oz++) {
                    BlockPos min = origin.offset(-ox, -oy, -oz);
                    if (isValidBox(level, min, 3, 2, 2)) {
                        form(level, min, 3, 2, 2);
                        return;
                    }
                }
            }
        }

        for (int ox = 0; ox < 2; ox++) {
            for (int oy = 0; oy < 2; oy++) {
                for (int oz = 0; oz < 3; oz++) {
                    BlockPos min = origin.offset(-ox, -oy, -oz);
                    if (isValidBox(level, min, 2, 2, 3)) {
                        form(level, min, 2, 2, 3);
                        return;
                    }
                }
            }
        }
    }

    private static boolean isValidBox(ServerLevel level, BlockPos min, int sizeX, int sizeY, int sizeZ) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockPos pos = min.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(GcBlocks.ENGINEERING_BAY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void applyOwnerToPart(EngineeringBayBlockEntity be, @Nullable UUID ownerUuid) {
        if (ownerUuid != null) {
            be.getSecurity().tryUpdate(ownerUuid);
            be.setChanged();
        }
    }

    private static void form(ServerLevel level, BlockPos masterPos, int sizeX, int sizeY, int sizeZ) {
        suppressUpdates = true;
        try {
            EngineeringBayBlockEntity securitySource = null;

            for (int x = 0; x < sizeX && securitySource == null; x++) {
                for (int y = 0; y < sizeY && securitySource == null; y++) {
                    for (int z = 0; z < sizeZ; z++) {
                        BlockPos pos = masterPos.offset(x, y, z);
                        if (level.getBlockEntity(pos) instanceof EngineeringBayBlockEntity be) {
                            if (be.getSecurity().getOwner() != null) {
                                securitySource = be;
                                break;
                            }
                        }
                    }
                }
            }

            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    for (int z = 0; z < sizeZ; z++) {
                        BlockPos pos = masterPos.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);

                        if (!state.getValue(EngineeringBayBlock.FORMED)) {
                            level.setBlock(pos, state.setValue(EngineeringBayBlock.FORMED, true), Block.UPDATE_ALL);
                        }

                        if (level.getBlockEntity(pos) instanceof EngineeringBayBlockEntity be) {
                            be.setStructureData(true, masterPos, sizeX, sizeY, sizeZ);

                            if (securitySource != null && be != securitySource) {
                                securitySource.getSecurity().copyInto(be.getSecurity());
                                be.setChanged();
                            }

                        }
                    }
                }
            }
        } finally {
            suppressUpdates = false;
        }
    }

    private static void unform(ServerLevel level, BlockPos masterPos, int sizeX, int sizeY, int sizeZ) {
        suppressUpdates = true;
        try {
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    for (int z = 0; z < sizeZ; z++) {
                        BlockPos pos = masterPos.offset(x, y, z);
                        BlockState state = level.getBlockState(pos);

                        if (!state.is(GcBlocks.ENGINEERING_BAY)) {
                            continue;
                        }

                        if (state.getValue(EngineeringBayBlock.FORMED)) {
                            level.setBlock(pos, state.setValue(EngineeringBayBlock.FORMED, false), Block.UPDATE_ALL);
                        }

                        if (level.getBlockEntity(pos) instanceof EngineeringBayBlockEntity be) {
                            be.resetStructureData();
                        }
                    }
                }
            }
        } finally {
            suppressUpdates = false;
        }
    }
}