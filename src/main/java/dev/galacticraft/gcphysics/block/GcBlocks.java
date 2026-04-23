package dev.galacticraft.gcphysics.block;

import dev.galacticraft.gcphysics.GcPhysicsMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public final class GcBlocks {
    public static final Block LAUNCH_PAD = register(
            "launch_pad",
            new LaunchPadBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f))
    );

    public static final Block LAUNCH_TOWER = register(
            "launch_tower",
            new LaunchTowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f))
    );

    public static final Block ENGINEERING_BAY = register(
            "engineering_bay",
            new EngineeringBayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f).noOcclusion())
    );

    public static final Block CLAMP = register(
            "clamp",
            new ClampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1.0f, 3600000.0f).noOcclusion())
    );

    private GcBlocks() {
    }

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(GcPhysicsMod.MOD_ID, name), block);
    }

    public static void init() {
    }
}