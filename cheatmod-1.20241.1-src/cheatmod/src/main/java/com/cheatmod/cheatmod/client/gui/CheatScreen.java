package com.cheatmod.cheatmod.client.gui;

import com.cheatmod.cheatmod.config.CheatConfig;
import com.cheatmod.cheatmod.client.XRayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheatScreen extends Screen {

    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"General", "World", "Teleport", "Give", "Effects"};

    private EditBox mineSpeedBox;
    private EditBox timeBox;
    private EditBox tpXBox, tpYBox, tpZBox;
    private EditBox itemSearchBox;

    private List<Item> searchResults = new ArrayList<>();
    private int searchScroll = 0;
    private int effectScroll = 0;
    private List<MobEffect> allEffects;

    // Feedback message shown at bottom
    private String feedbackMsg = "";
    private int feedbackTimer = 0;

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 360;
    private static final int TAB_H   = 20;
    private static final int BTN_W   = 155;
    private static final int BTN_H   = 20;
    private static final int GAP     = 6;

    public CheatScreen() {
        super(Component.literal("Cheat Menu"));
        allEffects = new ArrayList<>(BuiltInRegistries.MOB_EFFECT.stream().collect(Collectors.toList()));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        this.clearWidgets();
        int cx = width / 2, cy = height / 2;
        int pl = cx - PANEL_W / 2, pt = cy - PANEL_H / 2;
        int contentTop = pt + 14 + TAB_H + 6;

        // Tab buttons
        int tabW = PANEL_W / TAB_NAMES.length;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            this.addRenderableWidget(Button.builder(
                Component.literal((currentTab == i ? "§e" : "§7") + TAB_NAMES[i]),
                btn -> { currentTab = idx; rebuildWidgets(); }
            ).bounds(pl + i * tabW, pt + 14, tabW, TAB_H).build());
        }

        switch (currentTab) {
            case 0 -> buildGeneralTab(pl, contentTop, cx);
            case 1 -> buildWorldTab(pl, contentTop, cx);
            case 2 -> buildTeleportTab(pl, contentTop, cx);
            case 3 -> buildGiveTab(pl, contentTop, cx);
            case 4 -> buildEffectsTab(pl, contentTop, cx);
        }

        this.addRenderableWidget(Button.builder(Component.literal("§cClose [/]"), btn -> onClose())
                .bounds(cx - 40, pt + PANEL_H - 24, 80, 18).build());
    }

    // ── GENERAL ──────────────────────────────────────────────────────────────
    private void buildGeneralTab(int pl, int top, int cx) {
        int col1 = pl + 8, col2 = pl + 8 + BTN_W + GAP;
        int y = top;

        addToggle("Fast Mine",       col1, y, () -> CheatConfig.fastMineEnabled,  v -> CheatConfig.fastMineEnabled  = v);
        addToggle("God Mode",        col2, y, () -> CheatConfig.godModeEnabled,   v -> CheatConfig.godModeEnabled   = v); y += BTN_H + GAP;
        addToggle("No Fall Damage",  col1, y, () -> CheatConfig.noFallDamage,     v -> CheatConfig.noFallDamage     = v);
        addToggle("Speed Boost",     col2, y, () -> CheatConfig.speedBoost,       v -> CheatConfig.speedBoost       = v); y += BTN_H + GAP;
        addToggle("Night Vision",    col1, y, () -> CheatConfig.nightVision,      v -> CheatConfig.nightVision      = v);
        addToggle("Auto-Heal",       col2, y, () -> CheatConfig.autoHeal,         v -> CheatConfig.autoHeal         = v); y += BTN_H + GAP;
        addToggle("Inf. Hunger",     col1, y, () -> CheatConfig.infiniteHunger,   v -> CheatConfig.infiniteHunger   = v);
        addToggle("Creative Flight", col2, y, () -> CheatConfig.creativeFlight,   v -> CheatConfig.creativeFlight   = v); y += BTN_H + GAP;
        addToggle("X-Ray",           col1, y, () -> CheatConfig.xrayEnabled,      v -> { CheatConfig.xrayEnabled = v; Minecraft.getInstance().levelRenderer.allChanged(); });
        addToggle("No-Clip",         col2, y, () -> CheatConfig.noClipEnabled,    v -> CheatConfig.noClipEnabled    = v); y += BTN_H + GAP + 4;

        // Creative inventory
        this.addRenderableWidget(Button.builder(Component.literal("§bOpen Creative Inventory"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                onClose();
                mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get()));
            }
        }).bounds(col1, y, BTN_W * 2 + GAP, BTN_H).build()); y += BTN_H + GAP;

        // Mine speed
        mineSpeedBox = new EditBox(font, col2 + 5, y, 90, 16, Component.literal("speed"));
        mineSpeedBox.setValue(String.valueOf(CheatConfig.mineSpeedMultiplier));
        mineSpeedBox.setFilter(s -> s.matches("[0-9]*\\.?[0-9]*"));
        mineSpeedBox.setMaxLength(6);
        this.addRenderableWidget(mineSpeedBox);
        this.addRenderableWidget(Button.builder(Component.literal("Set Mine Speed"), btn -> {
            try {
                float v = Float.parseFloat(mineSpeedBox.getValue());
                if (v > 0 && v <= 1000) { CheatConfig.mineSpeedMultiplier = v; feedback("Mine speed set to " + v + "x"); }
            } catch (NumberFormatException ignored) {}
        }).bounds(col1, y - 2, BTN_W - 10, BTN_H).build());
    }

    // ── WORLD ────────────────────────────────────────────────────────────────
    private void buildWorldTab(int pl, int top, int cx) {
        int col1 = pl + 8, col2 = pl + 8 + BTN_W + GAP;
        int y = top;

        // Time — set directly on client level
        this.addRenderableWidget(Button.builder(Component.literal("Set Day"), btn -> setTime(1000)).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Set Night"), btn -> setTime(13000)).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;
        this.addRenderableWidget(Button.builder(Component.literal("Set Noon"), btn -> setTime(6000)).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Set Midnight"), btn -> setTime(18000)).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        timeBox = new EditBox(font, col2 + 5, y, 90, 16, Component.literal("ticks"));
        timeBox.setValue("6000");
        timeBox.setFilter(s -> s.matches("[0-9]*"));
        timeBox.setMaxLength(7);
        this.addRenderableWidget(timeBox);
        this.addRenderableWidget(Button.builder(Component.literal("Custom Time"), btn -> {
            try { setTime(Long.parseLong(timeBox.getValue())); } catch (NumberFormatException ignored) {}
        }).bounds(col1, y - 2, BTN_W - 10, BTN_H).build()); y += BTN_H + GAP + 4;

        // Weather — set via client level
        this.addRenderableWidget(Button.builder(Component.literal("Clear Weather"), btn -> setWeather(0, 0)).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Rain"), btn -> setWeather(6000, 0)).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;
        this.addRenderableWidget(Button.builder(Component.literal("Thunder"), btn -> setWeather(6000, 6000)).bounds(col1, y, BTN_W, BTN_H).build()); y += BTN_H + GAP + 4;

        // XP — manipulate directly
        this.addRenderableWidget(Button.builder(Component.literal("Add 100 Levels"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) { mc.player.giveExperienceLevels(100); feedback("Added 100 XP levels"); }
        }).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Add 9999 Levels"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) { mc.player.giveExperienceLevels(9999); feedback("Added 9999 XP levels"); }
        }).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        this.addRenderableWidget(Button.builder(Component.literal("Clear XP"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) { mc.player.experienceLevel = 0; mc.player.experienceProgress = 0; feedback("XP cleared"); }
        }).bounds(col1, y, BTN_W, BTN_H).build());

        // Full heal via effect
        this.addRenderableWidget(Button.builder(Component.literal("Instant Full Heal"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) { mc.player.heal(mc.player.getMaxHealth()); feedback("Healed to full"); }
        }).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        // Kill all nearby mobs client-prediction (marks them, server still handles actual kill)
        this.addRenderableWidget(Button.builder(Component.literal("Kill Nearby Mobs"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(32),
                        e -> !(e instanceof net.minecraft.world.entity.player.Player))
                        .forEach(e -> e.kill());
                feedback("Killed nearby mobs");
            }
        }).bounds(col1, y, BTN_W * 2 + GAP, BTN_H).build());
    }

    // ── TELEPORT ─────────────────────────────────────────────────────────────
    private void buildTeleportTab(int pl, int top, int cx) {
        int col1 = pl + 8, col2 = pl + 8 + BTN_W + GAP;
        int y = top;

        // Quick TPs
        this.addRenderableWidget(Button.builder(Component.literal("To Spawn (0,64,0)"), btn -> teleportTo(0, 64, 0)).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("To World 0,0"), btn -> teleportTo(0, 100, 0)).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP + 4;

        // Logoff spot
        this.addRenderableWidget(Button.builder(Component.literal("§aSave Logoff Spot"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                CheatConfig.logoffX = mc.player.getX();
                CheatConfig.logoffY = mc.player.getY();
                CheatConfig.logoffZ = mc.player.getZ();
                CheatConfig.logoffDimension = mc.level.dimension().location().toString();
                CheatConfig.hasLogoffSpot = true;
                feedback(String.format("Saved: %.1f %.1f %.1f", CheatConfig.logoffX, CheatConfig.logoffY, CheatConfig.logoffZ));
            }
        }).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("§eTeleport to Logoff"), btn -> {
            if (CheatConfig.hasLogoffSpot) teleportTo(CheatConfig.logoffX, CheatConfig.logoffY, CheatConfig.logoffZ);
        }).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP + 8;

        // Custom coords
        int bw = (PANEL_W - 16 - GAP * 2) / 3;
        tpXBox = makeCoordBox(col1,                y, bw, "X", "0");
        tpYBox = makeCoordBox(col1 + bw + GAP,     y, bw, "Y", "64");
        tpZBox = makeCoordBox(col1 + (bw+GAP)*2,   y, bw, "Z", "0");
        this.addRenderableWidget(tpXBox);
        this.addRenderableWidget(tpYBox);
        this.addRenderableWidget(tpZBox); y += 24;

        this.addRenderableWidget(Button.builder(Component.literal("Teleport to Coords"), btn -> {
            try {
                double tx = Double.parseDouble(tpXBox.getValue());
                double ty = Double.parseDouble(tpYBox.getValue());
                double tz = Double.parseDouble(tpZBox.getValue());
                teleportTo(tx, ty, tz);
            } catch (NumberFormatException ignored) { feedback("Invalid coordinates!"); }
        }).bounds(cx - 70, y, 140, BTN_H).build());
    }

    // ── GIVE ─────────────────────────────────────────────────────────────────
    private void buildGiveTab(int pl, int top, int cx) {
        int col1 = pl + 8;
        int y = top;
        int btnW = (PANEL_W - 16 - GAP) / 2;

        itemSearchBox = new EditBox(font, col1, y, PANEL_W - 16, 16, Component.literal("Search items..."));
        itemSearchBox.setHint(Component.literal("Search items..."));
        itemSearchBox.setMaxLength(50);
        itemSearchBox.setResponder(text -> {
            searchResults = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item != Items.AIR)
                    .filter(item -> {
                        String id = BuiltInRegistries.ITEM.getKey(item).getPath();
                        String name = item.getDescription().getString().toLowerCase();
                        return id.contains(text.toLowerCase()) || name.contains(text.toLowerCase());
                    })
                    .limit(100)
                    .collect(Collectors.toList());
            searchScroll = 0;
        });
        this.addRenderableWidget(itemSearchBox); y += 20;

        int col2 = col1 + btnW + GAP;
        for (int i = 0; i < Math.min(8, searchResults.size() - searchScroll); i++) {
            Item item = searchResults.get(searchScroll + i);
            String name = item.getDescription().getString();
            int bx = col1 + (i % 2) * (btnW + GAP);
            int by = y + (i / 2) * (BTN_H + GAP);
            this.addRenderableWidget(Button.builder(Component.literal("§f" + name), btn -> {
                giveItem(item, 64);
            }).bounds(bx, by, btnW, BTN_H).build());
        }
        y += 4 * (BTN_H + GAP) + GAP;

        if (searchResults.size() > 8) {
            this.addRenderableWidget(Button.builder(Component.literal("▲"), btn -> {
                if (searchScroll > 0) { searchScroll -= 2; rebuildWidgets(); }
            }).bounds(cx - 25, y, 22, 16).build());
            this.addRenderableWidget(Button.builder(Component.literal("▼"), btn -> {
                if (searchScroll + 8 < searchResults.size()) { searchScroll += 2; rebuildWidgets(); }
            }).bounds(cx + 3, y, 22, 16).build());
            y += 20;
        }

        // Quick give
        this.addRenderableWidget(Button.builder(Component.literal("64x Diamond"), btn -> giveItem(Items.DIAMOND, 64)).bounds(col1, y, btnW, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("64x Netherite Ingot"), btn -> giveItem(Items.NETHERITE_INGOT, 64)).bounds(col2, y, btnW, BTN_H).build()); y += BTN_H + GAP;
        this.addRenderableWidget(Button.builder(Component.literal("64x Gold Ingot"), btn -> giveItem(Items.GOLD_INGOT, 64)).bounds(col1, y, btnW, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("64x Iron Ingot"), btn -> giveItem(Items.IRON_INGOT, 64)).bounds(col2, y, btnW, BTN_H).build());
    }

    // ── EFFECTS ──────────────────────────────────────────────────────────────
    private void buildEffectsTab(int pl, int top, int cx) {
        int col1 = pl + 8;
        int btnW = (PANEL_W - 16 - GAP) / 2;
        int col2 = col1 + btnW + GAP;
        int y = top;

        for (int i = 0; i < Math.min(10, allEffects.size() - effectScroll); i++) {
            MobEffect eff = allEffects.get(effectScroll + i);
            String name = eff.getDisplayName().getString();
            int bx = col1 + (i % 2) * (btnW + GAP);
            int by = y + (i / 2) * (BTN_H + GAP);
            this.addRenderableWidget(Button.builder(Component.literal("§d" + name), btn -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.addEffect(new MobEffectInstance(eff, 999999, 10, false, false));
                    feedback("Applied: " + name);
                }
            }).bounds(bx, by, btnW, BTN_H).build());
        }
        y += 5 * (BTN_H + GAP) + GAP;

        this.addRenderableWidget(Button.builder(Component.literal("▲ Prev"), btn -> {
            if (effectScroll > 0) { effectScroll -= 2; rebuildWidgets(); }
        }).bounds(col1, y, BTN_W, BTN_H).build());
        this.addRenderableWidget(Button.builder(Component.literal("Next ▼"), btn -> {
            if (effectScroll + 10 < allEffects.size()) { effectScroll += 2; rebuildWidgets(); }
        }).bounds(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        this.addRenderableWidget(Button.builder(Component.literal("§cClear All Effects"), btn -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) { mc.player.removeAllEffects(); feedback("All effects cleared"); }
        }).bounds(cx - 60, y, 120, BTN_H).build());
    }

    // ── CLIENT-SIDE HELPERS ──────────────────────────────────────────────────

    private void teleportTo(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.moveTo(x, y, z, mc.player.getYRot(), mc.player.getXRot());
        feedback(String.format("Teleported to %.1f %.1f %.1f", x, y, z));
    }

    private void setTime(long time) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        mc.level.setDayTime(time);
        feedback("Time set to " + time);
    }

    private void setWeather(int rainTime, int thunderTime) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        mc.level.getLevelData().setRainTime(rainTime);
        mc.level.getLevelData().setThunderTime(thunderTime);
        mc.level.getLevelData().setRaining(rainTime > 0);
        mc.level.getLevelData().setThundering(thunderTime > 0);
        feedback(thunderTime > 0 ? "Thunder!" : rainTime > 0 ? "Raining" : "Clear skies");
    }

    private void giveItem(Item item, int count) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack stack = new ItemStack(item, count);
        mc.player.getInventory().add(stack);
        feedback("Given " + count + "x " + item.getDescription().getString());
    }

    private void feedback(String msg) {
        feedbackMsg = msg;
        feedbackTimer = 80;
    }

    private EditBox makeCoordBox(int x, int y, int w, String hint, String def) {
        EditBox box = new EditBox(font, x, y, w, 16, Component.literal(hint));
        box.setHint(Component.literal(hint));
        box.setValue(def);
        box.setFilter(s -> s.matches("-?[0-9]*\\.?[0-9]*"));
        return box;
    }

    // ── TOGGLE HELPER ────────────────────────────────────────────────────────
    private void addToggle(String label, int x, int y,
                           java.util.function.BooleanSupplier getter,
                           java.util.function.Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(makeLabel(label, getter.getAsBoolean()), btn -> {
            boolean v = !getter.getAsBoolean();
            setter.accept(v);
            btn.setMessage(makeLabel(label, v));
        }).bounds(x, y, BTN_W, BTN_H).build());
    }

    // ── RENDER ───────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int cx = width / 2, cy = height / 2;
        int pl = cx - PANEL_W / 2, pt = cy - PANEL_H / 2;
        int pr = pl + PANEL_W,     pb = pt + PANEL_H;

        g.fill(pl, pt, pr, pb, 0xD0101010);
        g.fill(pl,     pt,     pr,     pt + 2, 0xFF44FF44);
        g.fill(pl,     pb - 2, pr,     pb,     0xFF44FF44);
        g.fill(pl,     pt,     pl + 2, pb,     0xFF44FF44);
        g.fill(pr - 2, pt,     pr,     pb,     0xFF44FF44);

        g.drawCenteredString(font, "§a§lCheat Menu  §7[/]", cx, pt + 4, 0xFFFFFF);
        g.fill(pl + 8, pt + 14 + TAB_H, pr - 8, pt + 14 + TAB_H + 1, 0xFF44FF44);

        // Feedback
        if (feedbackTimer > 0) {
            feedbackTimer--;
            int alpha = Math.min(255, feedbackTimer * 6);
            g.drawCenteredString(font, "§a" + feedbackMsg, cx, pb - 36, (alpha << 24) | 0xFFFFFF);
        }

        // Logoff coords in teleport tab
        if (currentTab == 2 && CheatConfig.hasLogoffSpot) {
            g.drawCenteredString(font,
                String.format("§7Saved: §fX%.1f Y%.1f Z%.1f §7(%s)",
                    CheatConfig.logoffX, CheatConfig.logoffY, CheatConfig.logoffZ,
                    CheatConfig.logoffDimension.replace("minecraft:", "")),
                cx, pb - 48, 0xFFFFFF);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean editFocused = (mineSpeedBox != null && mineSpeedBox.isFocused())
                || (timeBox != null && timeBox.isFocused())
                || (tpXBox != null && tpXBox.isFocused())
                || (tpYBox != null && tpYBox.isFocused())
                || (tpZBox != null && tpZBox.isFocused())
                || (itemSearchBox != null && itemSearchBox.isFocused());
        if (keyCode == GLFW.GLFW_KEY_SLASH && !editFocused) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen() { return false; }

    private static Component makeLabel(String name, boolean on) {
        return Component.literal((on ? "§a[ON]  " : "§c[OFF] ") + "§f" + name);
    }
}
