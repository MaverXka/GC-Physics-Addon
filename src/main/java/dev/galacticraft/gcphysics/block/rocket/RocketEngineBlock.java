package dev.galacticraft.gcphysics.block.rocket;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RocketEngineBlock extends Block {
    public static final MapCodec<RocketEngineBlock> CODEC = simpleCodec(RocketEngineBlock::new);

    public static final double THRUST_PER_SECOND = 200.0; // this is super strong

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<EnginePart> PART = EnumProperty.create("part", EnginePart.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RocketEngineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, EnginePart.MAIN)
                .setValue(POWERED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos extensionPos = pos.relative(facing);
        BlockState extensionState = level.getBlockState(extensionPos);

        if (!level.getWorldBorder().isWithinBounds(extensionPos)) {
            if (level.isClientSide() && context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.literal("Cannot place rocket engine: extension would be outside the world border."),
                        true
                );
            }
            return null;
        }

        if (!extensionState.canBeReplaced(context)) {
            if (level.isClientSide() && context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                        Component.literal("Cannot place rocket engine: extension space is occupied by "
                                + extensionState.getBlock().getName().getString()),
                        true
                );
            }
            return null;
        }

        boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(extensionPos);

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, EnginePart.MAIN)
                .setValue(POWERED, powered);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, POWERED);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.isClientSide() || state.is(oldState.getBlock())) {
            return;
        }

        if (state.getValue(PART) == EnginePart.MAIN) {
            BlockPos extensionPos = getOtherPartPos(state, pos);
            BlockState extensionState = this.defaultBlockState()
                    .setValue(FACING, state.getValue(FACING))
                    .setValue(PART, EnginePart.EXTENSION)
                    .setValue(POWERED, state.getValue(POWERED));

            if (!level.getBlockState(extensionPos).is(this)) {
                level.setBlock(extensionPos, extensionState, Block.UPDATE_ALL);
            }
        }

        updatePowered(level, pos, state);
    }


    public static boolean isMainEngine(BlockState state) {
        return state.getBlock() instanceof RocketEngineBlock
                && state.getValue(PART) == EnginePart.MAIN;
    }

    public static boolean isEngineActive(BlockState state) {
        return isMainEngine(state) && state.getValue(POWERED);
    }

    //TODO this currently only makes the sublevel move directly upwards relative to the sublevel. so the thrust is always pushing the sublevel in its y direction.
    public static Vec3 getLocalThrustPerSecond(BlockState state) {
        if (!isEngineActive(state)) {
            return Vec3.ZERO;
        }

        return new Vec3(0.0, THRUST_PER_SECOND, 0.0);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide()) {
            return;
        }

        BlockPos otherPos = getOtherPartPos(state, pos);
        BlockState otherState = level.getBlockState(otherPos);

        if (!isMatchingCounterpart(state, otherState)) {
            level.removeBlock(pos, false);
            return;
        }

        updatePowered(level, pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean changedBlock = !state.is(newState.getBlock());

        if (changedBlock && !level.isClientSide()) {
            BlockPos otherPos = getOtherPartPos(state, pos);
            BlockState otherState = level.getBlockState(otherPos);

            if (isMatchingCounterpart(state, otherState)) {
                level.removeBlock(otherPos, false);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        EnginePart part = state.getValue(PART);

        if (part == EnginePart.MAIN) {
            return getMainShape(facing);
        }

        return getExtensionShape(facing);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos otherPos = getOtherPartPos(state, pos);
        BlockState otherState = level.getBlockState(otherPos);

        if (state.getValue(PART) == EnginePart.MAIN) {
            if (isMatchingCounterpart(state, otherState)) {
                return true;
            }

            return otherState.canBeReplaced();
        }

        return isMatchingCounterpart(state, otherState);
    }

    private static void updatePowered(Level level, BlockPos pos, BlockState state) {
        BlockPos mainPos = getMainPos(state, pos);
        BlockState mainState = level.getBlockState(mainPos);

        if (!mainState.is(state.getBlock())) {
            return;
        }

        BlockPos extensionPos = getOtherPartPos(mainState, mainPos);
        BlockState extensionState = level.getBlockState(extensionPos);

        boolean powered = level.hasNeighborSignal(mainPos) || level.hasNeighborSignal(extensionPos);

        if (mainState.getValue(POWERED) != powered) {
            level.setBlock(mainPos, mainState.setValue(POWERED, powered), Block.UPDATE_ALL);
        }

        if (extensionState.is(state.getBlock()) && extensionState.getValue(POWERED) != powered) {
            level.setBlock(extensionPos, extensionState.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    private static boolean isMatchingCounterpart(BlockState state, BlockState otherState) {
        return otherState.is(state.getBlock())
                && otherState.getValue(FACING) == state.getValue(FACING)
                && otherState.getValue(PART) != state.getValue(PART);
    }

    private static BlockPos getMainPos(BlockState state, BlockPos pos) {
        if (state.getValue(PART) == EnginePart.MAIN) {
            return pos;
        }
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    private static BlockPos getOtherPartPos(BlockState state, BlockPos pos) {
        if (state.getValue(PART) == EnginePart.MAIN) {
            return pos.relative(state.getValue(FACING));
        }
        return pos.relative(state.getValue(FACING).getOpposite());
    }

    private static VoxelShape getMainShape(Direction facing) {
        return switch (facing) {
            case NORTH -> Shapes.or(
                    box(3, 3, 6, 13, 13, 16),
                    box(5, 5, 0, 11, 11, 6)
            );
            case SOUTH -> Shapes.or(
                    box(3, 3, 0, 13, 13, 10),
                    box(5, 5, 10, 11, 11, 16)
            );
            case WEST -> Shapes.or(
                    box(6, 3, 3, 16, 13, 13),
                    box(0, 5, 5, 6, 11, 11)
            );
            case EAST -> Shapes.or(
                    box(0, 3, 3, 10, 13, 13),
                    box(10, 5, 5, 16, 11, 11)
            );
            default -> Shapes.block();
        };
    }

    private static VoxelShape getExtensionShape(Direction facing) {
        return switch (facing) {
            case NORTH -> Shapes.or(
                    box(2, 2, 0, 14, 14, 12),
                    box(4, 4, 12, 12, 12, 16)
            );
            case SOUTH -> Shapes.or(
                    box(2, 2, 4, 14, 14, 16),
                    box(4, 4, 0, 12, 12, 4)
            );
            case WEST -> Shapes.or(
                    box(0, 2, 2, 12, 14, 14),
                    box(12, 4, 4, 16, 12, 12)
            );
            case EAST -> Shapes.or(
                    box(4, 2, 2, 16, 14, 14),
                    box(0, 4, 4, 4, 12, 12)
            );
            default -> Shapes.block();
        };
    }

    public enum EnginePart implements StringRepresentable {
        MAIN("main"),
        EXTENSION("extension");

        private final String name;

        EnginePart(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}