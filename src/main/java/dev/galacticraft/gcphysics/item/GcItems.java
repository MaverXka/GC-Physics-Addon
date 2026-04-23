package dev.galacticraft.gcphysics.item;

import dev.galacticraft.gcphysics.GcPhysicsMod;
import dev.galacticraft.gcphysics.block.GcBlocks;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public final class GcItems {
    private GcItems() {
    }

    public static void init() {
        registerBlockItem("launch_pad", GcBlocks.LAUNCH_PAD);
        registerBlockItem("launch_tower", GcBlocks.LAUNCH_TOWER);
        registerBlockItem("engineering_bay", GcBlocks.ENGINEERING_BAY);
        registerBlockItem("clamp", GcBlocks.CLAMP);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(GcBlocks.LAUNCH_PAD);
            entries.accept(GcBlocks.LAUNCH_TOWER);
            entries.accept(GcBlocks.ENGINEERING_BAY);
            entries.accept(GcBlocks.CLAMP);
        });
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(GcPhysicsMod.MOD_ID, name),
                new BlockItem(block, new Item.Properties())
        );
    }
}