package dev.galacticraft.gcphysics;

import dev.galacticraft.gcphysics.client.screen.EngineeringBayScreen;
import dev.galacticraft.gcphysics.menu.GcMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class GcPhysicsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(GcMenuTypes.ENGINEERING_BAY, EngineeringBayScreen::new);
    }
}