package com.cheatmod.cheatmod.client.gui;

import com.cheatmod.cheatmod.config.CheatConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CheatScreen extends Screen {

    private EditBox mineSpeedBox;

    private static final int PANEL_WIDTH  = 300;
    private static final int PANEL_HEIGHT = 265;

    // Button grid: 2 columns, 4 rows
    // Each button: 135px wide, 22px tall, 8px vertical gap
    private static final int BTN_W = 135;
    private static final int BTN_H = 22;
    private static final int BTN_GAP_X = 10; // gap between columns
    private static final int BTN_GAP_Y = 8;  // gap between rows

    public CheatScreen() {
        super(Component.literal("Cheat Menu"));
    }

    @Override
    protected void init() {
        int cx       = this.width  / 2;
        int cy       = this.height / 2;
        int pl       = cx - PANEL_WIDTH  / 2; // panel left
        int pt       = cy - PANEL_HEIGHT / 2; // panel top

        int col1 = pl + 10;
        int col2 = col1 + BTN_W + BTN_GAP_X;
        int row1 = pt + 42;
        int row2 = row1 + BTN_H + BTN_GAP_Y;
        int row3 = row2 + BTN_H + BTN_GAP_Y;
        int row4 = row3 + BTN_H + BTN_GAP_Y;

        // Row 1
        addToggleButton("Fast Mine",       col1, row1, () -> CheatConfig.fastMineEnabled,   v -> CheatConfig.fastMineEnabled   = v);
        addToggleButton("God Mode",        col2, row1, () -> CheatConfig.godModeEnabled,    v -> CheatConfig.godModeEnabled    = v);
        // Row 2
        addToggleButton("No Fall Damage",  col1, row2, () -> CheatConfig.noFallDamage,      v -> CheatConfig.noFallDamage      = v);
        addToggleButton("Speed Boost",     col2, row2, () -> CheatConfig.speedBoost,        v -> CheatConfig.speedBoost        = v);
        // Row 3
        addToggleButton("Night Vision",    col1, row3, () -> CheatConfig.nightVision,       v -> CheatConfig.nightVision       = v);
        addToggleButton("Auto-Heal",       col2, row3, () -> CheatConfig.autoHeal,          v -> CheatConfig.autoHeal          = v);
        // Row 4
        addToggleButton("Inf. Hunger",     col1, row4, () -> CheatConfig.infiniteHunger,    v -> CheatConfig.infiniteHunger    = v);
        addToggleButton("Creative Flight", col2, row4, () -> CheatConfig.creativeFlight,    v -> CheatConfig.creativeFlight    = v);

        // --- Mine Speed Multiplier ---
        int speedRow = row4 + BTN_H + 18;
        mineSpeedBox = new EditBox(this.font, col2 + 10, speedRow - 1, 80, 16, Component.literal("Speed"));
        mineSpeedBox.setValue(String.valueOf(CheatConfig.mineSpeedMultiplier));
        mineSpeedBox.setMaxLength(6);
        // Allow digits and a single dot
        mineSpeedBox.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
        this.addRenderableWidget(mineSpeedBox);

        this.addRenderableWidget(Button.builder(Component.literal("Apply Speed"), btn -> {
            try {
                float val = Float.parseFloat(mineSpeedBox.getValue());
                if (val > 0 && val <= 1000) {
                    CheatConfig.mineSpeedMultiplier = val;
                }
            } catch (NumberFormatException ignored) {}
        }).bounds(col1, speedRow - 2, 100, 20).build());

        // --- Close ---
        int closeRow = speedRow + 28;
        this.addRenderableWidget(Button.builder(Component.literal("Close  [/]"), btn -> this.onClose())
                .bounds(cx - 60, closeRow, 120, 22).build());
    }

    /**
     * Helper to build a self-updating toggle button without duplicating boilerplate.
     */
    private void addToggleButton(String label,
                                  int x, int y,
                                  java.util.function.BooleanSupplier getter,
                                  java.util.function.Consumer<Boolean> setter) {
        final Button[] ref = new Button[1];
        ref[0] = Button.builder(makeLabel(label, getter.getAsBoolean()), btn -> {
            boolean newVal = !getter.getAsBoolean();
            setter.accept(newVal);
            btn.setMessage(makeLabel(label, newVal));
        }).bounds(x, y, BTN_W, BTN_H).build();
        this.addRenderableWidget(ref[0]);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int cx  = this.width  / 2;
        int cy  = this.height / 2;
        int pl  = cx - PANEL_WIDTH  / 2;
        int pt  = cy - PANEL_HEIGHT / 2;
        int pr  = pl + PANEL_WIDTH;
        int pb  = pt + PANEL_HEIGHT;

        // Background
        g.fill(pl, pt, pr, pb, 0xD0101010);

        // Border (green, 2px)
        g.fill(pl,     pt,     pr,     pt + 2, 0xFF44FF44);
        g.fill(pl,     pb - 2, pr,     pb,     0xFF44FF44);
        g.fill(pl,     pt,     pl + 2, pb,     0xFF44FF44);
        g.fill(pr - 2, pt,     pr,     pb,     0xFF44FF44);

        // Title
        g.drawCenteredString(this.font, "§a§lCheat Menu", cx, pt + 8,  0xFFFFFF);
        g.drawCenteredString(this.font, "§7Press §f/§7 to toggle", cx, pt + 20, 0xAAAAAA);

        // Divider under title
        g.fill(pl + 8, pt + 32, pr - 8, pt + 33, 0xFF2A2A2A);

        // Mine speed label
        int speedLabelY = pt + 42 + (BTN_H + BTN_GAP_Y) * 4 + 14;
        g.drawString(this.font, "§7Mine Speed ×:", pl + 10, speedLabelY, 0xCCCCCC);

        // Divider above close button
        int closeDivY = speedLabelY + 44;
        g.fill(pl + 8, closeDivY, pr - 8, closeDivY + 1, 0xFF2A2A2A);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Only close on / if the editbox is NOT focused (so you can type decimals freely)
        if (keyCode == GLFW.GLFW_KEY_SLASH && (mineSpeedBox == null || !mineSpeedBox.isFocused())) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Game keeps running while menu is open
    }

    private static Component makeLabel(String name, boolean enabled) {
        return Component.literal((enabled ? "§a[ON]  " : "§c[OFF] ") + "§f" + name);
    }
}
