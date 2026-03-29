package com.cheatmod.cheatmod.client.gui;

import com.cheatmod.cheatmod.config.CheatConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CheatScreen extends Screen {

    private Button fastMineButton;
    private Button godModeButton;
    private EditBox mineSpeedBox;

    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 220;

    public CheatScreen() {
        super(Component.literal("Cheat Menu"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelLeft = cx - PANEL_WIDTH / 2;
        int panelTop = cy - PANEL_HEIGHT / 2;

        // Fast Mine Toggle Button
        fastMineButton = Button.builder(
                getToggleLabel("Fast Mine", CheatConfig.fastMineEnabled),
                btn -> {
                    CheatConfig.fastMineEnabled = !CheatConfig.fastMineEnabled;
                    btn.setMessage(getToggleLabel("Fast Mine", CheatConfig.fastMineEnabled));
                }
        ).bounds(panelLeft + 20, panelTop + 50, 220, 24).build();
        this.addRenderableWidget(fastMineButton);

        // God Mode Toggle Button
        godModeButton = Button.builder(
                getToggleLabel("God Mode (No Damage)", CheatConfig.godModeEnabled),
                btn -> {
                    CheatConfig.godModeEnabled = !CheatConfig.godModeEnabled;
                    btn.setMessage(getToggleLabel("God Mode (No Damage)", CheatConfig.godModeEnabled));
                }
        ).bounds(panelLeft + 20, panelTop + 84, 220, 24).build();
        this.addRenderableWidget(godModeButton);

        // Mining speed text field label + box
        mineSpeedBox = new EditBox(
                this.font,
                panelLeft + 130,
                panelTop + 130,
                80, 18,
                Component.literal("Speed")
        );
        mineSpeedBox.setValue(String.valueOf(CheatConfig.mineSpeedMultiplier));
        mineSpeedBox.setMaxLength(6);
        mineSpeedBox.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
        this.addRenderableWidget(mineSpeedBox);

        // Apply button for speed
        Button applySpeed = Button.builder(
                Component.literal("Apply"),
                btn -> {
                    try {
                        float val = Float.parseFloat(mineSpeedBox.getValue());
                        if (val > 0 && val <= 1000) {
                            CheatConfig.mineSpeedMultiplier = val;
                        }
                    } catch (NumberFormatException ignored) {}
                }
        ).bounds(panelLeft + 20, panelTop + 165, 100, 22).build();
        this.addRenderableWidget(applySpeed);

        // Close button
        Button closeBtn = Button.builder(
                Component.literal("Close [/]"),
                btn -> this.onClose()
        ).bounds(panelLeft + 130, panelTop + 165, 110, 22).build();
        this.addRenderableWidget(closeBtn);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dim background
        this.renderBackground(graphics);

        int cx = this.width / 2;
        int cy = this.height / 2;
        int panelLeft = cx - PANEL_WIDTH / 2;
        int panelTop = cy - PANEL_HEIGHT / 2;

        // Panel background
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xCC111111);

        // Panel border
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 2, 0xFF44FF44);
        graphics.fill(panelLeft, panelTop + PANEL_HEIGHT - 2, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF44FF44);
        graphics.fill(panelLeft, panelTop, panelLeft + 2, panelTop + PANEL_HEIGHT, 0xFF44FF44);
        graphics.fill(panelLeft + PANEL_WIDTH - 2, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF44FF44);

        // Title
        graphics.drawCenteredString(this.font, "§a§lCheat Menu", cx, panelTop + 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Press §f/§7 to close", cx, panelTop + 26, 0xAAAAAA);

        // Mining speed label
        graphics.drawString(this.font, "§7Mine Speed Multiplier:", panelLeft + 20, panelTop + 134, 0xFFFFFF);

        // Active status display
        String fastStatus = CheatConfig.fastMineEnabled ? "§a✔ Active" : "§c✘ Off";
        String godStatus = CheatConfig.godModeEnabled ? "§a✔ Active" : "§c✘ Off";

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Close when / is pressed again
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Game keeps running while menu is open
    }

    private Component getToggleLabel(String name, boolean enabled) {
        return Component.literal((enabled ? "§a[ON]  " : "§c[OFF] ") + "§f" + name);
    }
}
