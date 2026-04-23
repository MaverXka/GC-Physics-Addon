package dev.galacticraft.gcphysics.client.screen;

import dev.galacticraft.gcphysics.block.entity.EngineeringBayBlockEntity;
import dev.galacticraft.gcphysics.menu.EngineeringBayMenu;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;

public class EngineeringBayScreen extends MachineScreen<EngineeringBayBlockEntity, EngineeringBayMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");

    private Button beginConstructionButton;
    private Button finishConstructionButton;

    public EngineeringBayScreen(EngineeringBayMenu menu, Inventory inv, Component title) {
        super(menu, title, TEXTURE);
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(
                Component.literal("Detect launch site"),
                button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                EngineeringBayMenu.BUTTON_DETECT_LAUNCH_SITE
                        );
                    }
                }
        ).bounds(this.leftPos + 8, this.topPos + 58, 120, 20).build());

        this.beginConstructionButton = this.addRenderableWidget(Button.builder(
                Component.literal("Begin construction"),
                button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                EngineeringBayMenu.BUTTON_BEGIN_CONSTRUCTION
                        );
                    }
                }
        ).bounds(this.leftPos + 132, this.topPos + 58, 120, 20).build());

        this.finishConstructionButton = this.addRenderableWidget(Button.builder(
                Component.literal("Finish construction"),
                button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                                this.menu.containerId,
                                EngineeringBayMenu.BUTTON_FINISH_CONSTRUCTION
                        );
                    }
                }
        ).bounds(this.leftPos + 132, this.topPos + 82, 120, 20).build());

        updateConstructionButtons();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateConstructionButtons();
    }

    private void updateConstructionButtons() {
        boolean canBegin = this.menu.canBeginConstruction();
        boolean canFinish = this.menu.hasConstructionClamp();

        if (this.beginConstructionButton != null) {
            this.beginConstructionButton.visible = canBegin && !canFinish;
            this.beginConstructionButton.active = canBegin && !canFinish;
        }

        if (this.finishConstructionButton != null) {
            this.finishConstructionButton.visible = canFinish;
            this.finishConstructionButton.active = canFinish;
        }
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderForeground(graphics, mouseX, mouseY, delta);

        int textX = this.leftPos + 8;
        int lineY = this.topPos + 18;

        UUID owner = this.menu.security.getOwner();
        graphics.drawString(
                this.font,
                Component.literal("Owner: " + (owner != null ? owner : "none")),
                textX,
                lineY,
                0x404040,
                false
        );
        lineY += 10;

        if (this.menu.hasLaunchPad()) {
            graphics.drawString(
                    this.font,
                    Component.literal("Launch pad size: " + this.menu.getLaunchPadWidth() + " x " + this.menu.getLaunchPadLength()),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;

            graphics.drawString(
                    this.font,
                    Component.literal("Launch pad min: " + this.menu.getLaunchPadMinX() + ", " + this.menu.getLaunchPadMinY() + ", " + this.menu.getLaunchPadMinZ()),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;

            graphics.drawString(
                    this.font,
                    Component.literal("Launch pad max: " + this.menu.getLaunchPadMaxX() + ", " + this.menu.getLaunchPadMaxY() + ", " + this.menu.getLaunchPadMaxZ()),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;
        } else {
            graphics.drawString(
                    this.font,
                    Component.literal("No valid launch pad"),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;
        }

        if (this.menu.hasLaunchTower()) {
            graphics.drawString(
                    this.font,
                    Component.literal("Tower height: " + this.menu.getLaunchTowerHeight()),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;

            graphics.drawString(
                    this.font,
                    Component.literal("Tower min: " + this.menu.getLaunchTowerMinX() + ", " + this.menu.getLaunchTowerMinY() + ", " + this.menu.getLaunchTowerMinZ()),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;

            graphics.drawString(
                    this.font,
                    Component.literal("Tower max: " + this.menu.getLaunchTowerMaxX() + ", " + this.menu.getLaunchTowerMaxY() + ", " + this.menu.getLaunchTowerMaxZ()),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;
        } else {
            graphics.drawString(
                    this.font,
                    Component.literal("No valid launch tower"),
                    textX,
                    lineY,
                    0x404040,
                    false
            );
            lineY += 10;
        }

        graphics.drawString(
                this.font,
                Component.literal("Construction active: " + (this.menu.hasConstructionClamp() ? "yes" : "no")),
                textX,
                lineY,
                0x404040,
                false
        );
    }
}