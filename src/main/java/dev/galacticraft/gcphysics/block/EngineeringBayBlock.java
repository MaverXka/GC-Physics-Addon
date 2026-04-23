package dev.galacticraft.gcphysics.block;

import com.mojang.serialization.MapCodec;
import dev.galacticraft.gcphysics.block.entity.EngineeringBayBlockEntity;
import dev.galacticraft.gcphysics.multiblock.EngineeringBayMultiblock;
import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EngineeringBayBlock extends MachineBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final MapCodec<EngineeringBayBlock> CODEC = simpleCodec(EngineeringBayBlock::new);

    public EngineeringBayBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ACTIVE, false)
                .setValue(FORMED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable MachineBlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EngineeringBayBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && !EngineeringBayMultiblock.isSuppressingUpdates()) {
            UUID placerUuid = placer instanceof ServerPlayer serverPlayer ? serverPlayer.getUUID() : null;
            EngineeringBayMultiblock.refreshAround((ServerLevel) level, pos, placerUuid);
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(FORMED)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EngineeringBayBlockEntity bay)) {
            return InteractionResult.PASS;
        }

        BlockPos masterPos = bay.getMasterPos();
        BlockEntity masterBe = level.getBlockEntity(masterPos);
        if (!(masterBe instanceof EngineeringBayBlockEntity masterBay)) {
            return InteractionResult.PASS;
        }

        var security = masterBay.getSecurity();
        var beforeOwner = security.getOwner();

        security.tryUpdate(player.getUUID());

        var afterOwner = security.getOwner();
        masterBay.setChanged();

        if (!security.hasAccess(player)) {
            return InteractionResult.SUCCESS;
        }

        if (masterBay instanceof MenuProvider provider) {
            player.openMenu(provider);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        boolean changedBlock = !state.is(newState.getBlock());
        super.onRemove(state, level, pos, newState, moved);

        if (changedBlock && !level.isClientSide() && !EngineeringBayMultiblock.isSuppressingUpdates()) {
            EngineeringBayMultiblock.refreshAround((ServerLevel) level, pos, null);
        }
    }
}