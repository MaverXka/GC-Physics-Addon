package dev.galacticraft.gcphysics.block.entity;

import dev.galacticraft.gcphysics.GcPhysicsMod;
import dev.galacticraft.gcphysics.block.GcBlocks;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class GcBlockEntities {
    public static final BlockEntityType<EngineeringBayBlockEntity> ENGINEERING_BAY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(GcPhysicsMod.MOD_ID, "engineering_bay"),
            BlockEntityType.Builder.of(EngineeringBayBlockEntity::new, GcBlocks.ENGINEERING_BAY).build(null)
    );

    private GcBlockEntities() {
    }

    public static void init() {
        MachineBlockEntity.registerProviders(ENGINEERING_BAY);
    }
}