package dev.galacticraft.gcphysics.menu;

import dev.galacticraft.gcphysics.GcPhysicsMod;
import dev.galacticraft.machinelib.api.menu.SynchronizedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public final class GcMenuTypes {
    public static final MenuType<EngineeringBayMenu> ENGINEERING_BAY = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(GcPhysicsMod.MOD_ID, "engineering_bay"),
            SynchronizedMenuType.create(EngineeringBayMenu::new)
    );

    private GcMenuTypes() {
    }

    public static void init() {
    }
}