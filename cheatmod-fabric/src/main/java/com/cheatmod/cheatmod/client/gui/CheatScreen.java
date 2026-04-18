package com.cheatmod.cheatmod.client.gui;

import com.cheatmod.cheatmod.config.CheatConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheatScreen extends Screen {

    private int currentTab = 0;
    private static final String[] TAB_NAMES = {"General", "World", "Teleport", "Give", "Effects"};

    private TextFieldWidget mineSpeedBox;
    private TextFieldWidget timeBox;
    private TextFieldWidget tpXBox, tpYBox, tpZBox;
    private TextFieldWidget itemSearchBox;

    private List<Item> searchResults = new ArrayList<>();
    private int searchScroll = 0;
    private int effectScroll = 0;
    private List<StatusEffect> allEffects;

    private String feedbackMsg = "";
    private int feedbackTimer = 0;

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 360;
    private static final int TAB_H   = 20;
    private static final int BTN_W   = 155;
    private static final int BTN_H   = 20;
    private static final int GAP     = 6;

    public CheatScreen() {
        super(Text.literal("Cheat Menu"));
        allEffects = new ArrayList<>(Registries.STATUS_EFFECT.stream().collect(Collectors.toList()));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();
        int cx = width / 2, cy = height / 2;
        int pl = cx - PANEL_W / 2, pt = cy - PANEL_H / 2;
        int contentTop = pt + 14 + TAB_H + 6;

        int tabW = PANEL_W / TAB_NAMES.length;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(
                Text.literal((currentTab == i ? "§e" : "§7") + TAB_NAMES[i]),
                btn -> { currentTab = idx; rebuildWidgets(); }
            ).dimensions(pl + i * tabW, pt + 14, tabW, TAB_H).build());
        }

        switch (currentTab) {
            case 0 -> buildGeneralTab(pl, contentTop, cx);
            case 1 -> buildWorldTab(pl, contentTop, cx);
            case 2 -> buildTeleportTab(pl, contentTop, cx);
            case 3 -> buildGiveTab(pl, contentTop, cx);
            case 4 -> buildEffectsTab(pl, contentTop, cx);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("§cClose [/]"), btn -> close())
                .dimensions(cx - 40, pt + PANEL_H - 24, 80, 18).build());
    }

    // ── GENERAL ──────────────────────────────────────────────────────────────
    private void buildGeneralTab(int pl, int top, int cx) {
        int col1 = pl + 8, col2 = pl + 8 + BTN_W + GAP, y = top;

        addToggle("Fast Mine",       col1, y, () -> CheatConfig.fastMineEnabled,  v -> CheatConfig.fastMineEnabled  = v);
        addToggle("God Mode",        col2, y, () -> CheatConfig.godModeEnabled,   v -> CheatConfig.godModeEnabled   = v); y += BTN_H + GAP;
        addToggle("No Fall Damage",  col1, y, () -> CheatConfig.noFallDamage,     v -> CheatConfig.noFallDamage     = v);
        addToggle("Speed Boost",     col2, y, () -> CheatConfig.speedBoost,       v -> CheatConfig.speedBoost       = v); y += BTN_H + GAP;
        addToggle("Night Vision",    col1, y, () -> CheatConfig.nightVision,      v -> CheatConfig.nightVision      = v);
        addToggle("Auto-Heal",       col2, y, () -> CheatConfig.autoHeal,         v -> CheatConfig.autoHeal         = v); y += BTN_H + GAP;
        addToggle("Inf. Hunger",     col1, y, () -> CheatConfig.infiniteHunger,   v -> CheatConfig.infiniteHunger   = v);
        addToggle("Creative Flight", col2, y, () -> CheatConfig.creativeFlight,   v -> CheatConfig.creativeFlight   = v); y += BTN_H + GAP;
        addToggle("X-Ray",           col1, y, () -> CheatConfig.xrayEnabled,      v -> { CheatConfig.xrayEnabled = v; MinecraftClient.getInstance().worldRenderer.reload(); });
        addToggle("No-Clip",         col2, y, () -> CheatConfig.noClipEnabled,    v -> CheatConfig.noClipEnabled    = v); y += BTN_H + GAP + 4;

        // Creative inventory
        addDrawableChild(ButtonWidget.builder(Text.literal("§bOpen Creative Inventory"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                close();
                mc.setScreen(new CreativeInventoryScreen(mc.player, mc.player.networkHandler.getEnabledFeatures(), mc.options.operatorItemsTab().getValue()));
            }
        }).dimensions(col1, y, BTN_W * 2 + GAP, BTN_H).build()); y += BTN_H + GAP;

        // Mine speed
        mineSpeedBox = new TextFieldWidget(textRenderer, col2 + 5, y, 90, 16, Text.literal("speed"));
        mineSpeedBox.setText(String.valueOf(CheatConfig.mineSpeedMultiplier));
        mineSpeedBox.setMaxLength(6);
        addDrawableChild(mineSpeedBox);
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Mine Speed"), btn -> {
            try {
                float v = Float.parseFloat(mineSpeedBox.getText());
                if (v > 0 && v <= 1000) { CheatConfig.mineSpeedMultiplier = v; feedback("Mine speed: " + v + "x"); }
            } catch (NumberFormatException ignored) {}
        }).dimensions(col1, y - 2, BTN_W - 10, BTN_H).build());
    }

    // ── WORLD ────────────────────────────────────────────────────────────────
    private void buildWorldTab(int pl, int top, int cx) {
        int col1 = pl + 8, col2 = pl + 8 + BTN_W + GAP, y = top;

        addDrawableChild(ButtonWidget.builder(Text.literal("Set Day"),      btn -> setTime(1000)).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Night"),    btn -> setTime(13000)).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Noon"),     btn -> setTime(6000)).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Midnight"), btn -> setTime(18000)).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        timeBox = new TextFieldWidget(textRenderer, col2 + 5, y, 90, 16, Text.literal("ticks"));
        timeBox.setText("6000");
        timeBox.setMaxLength(7);
        addDrawableChild(timeBox);
        addDrawableChild(ButtonWidget.builder(Text.literal("Custom Time"), btn -> {
            try { setTime(Long.parseLong(timeBox.getText())); } catch (NumberFormatException ignored) {}
        }).dimensions(col1, y - 2, BTN_W - 10, BTN_H).build()); y += BTN_H + GAP + 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Clear Weather"), btn -> setWeather(false, false)).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Rain"),          btn -> setWeather(true, false)).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;
        addDrawableChild(ButtonWidget.builder(Text.literal("Thunder"),       btn -> setWeather(true, true)).dimensions(col1, y, BTN_W, BTN_H).build()); y += BTN_H + GAP + 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Add 100 Levels"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) { mc.player.addExperienceLevels(100); feedback("+100 XP levels"); }
        }).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Add 9999 Levels"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) { mc.player.addExperienceLevels(9999); feedback("+9999 XP levels"); }
        }).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        addDrawableChild(ButtonWidget.builder(Text.literal("Clear XP"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) { mc.player.experienceLevel = 0; mc.player.experienceProgress = 0; feedback("XP cleared"); }
        }).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Instant Full Heal"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) { mc.player.heal(mc.player.getMaxHealth()); feedback("Healed!"); }
        }).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        addDrawableChild(ButtonWidget.builder(Text.literal("Kill Nearby Mobs"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.world != null) {
                mc.world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
                        mc.player.getBoundingBox().expand(32),
                        e -> !(e instanceof net.minecraft.entity.player.PlayerEntity))
                        .forEach(e -> e.kill());
                feedback("Killed nearby mobs");
            }
        }).dimensions(col1, y, BTN_W * 2 + GAP, BTN_H).build());
    }

    // ── TELEPORT ─────────────────────────────────────────────────────────────
    private void buildTeleportTab(int pl, int top, int cx) {
        int col1 = pl + 8, col2 = pl + 8 + BTN_W + GAP, y = top;

        addDrawableChild(ButtonWidget.builder(Text.literal("To Spawn (0,64,0)"), btn -> teleportTo(0, 64, 0)).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("To World 0,0"),      btn -> teleportTo(0, 100, 0)).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP + 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("§aSave Logoff Spot"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.world != null) {
                CheatConfig.logoffX = mc.player.getX();
                CheatConfig.logoffY = mc.player.getY();
                CheatConfig.logoffZ = mc.player.getZ();
                CheatConfig.logoffDimension = mc.world.getRegistryKey().getValue().toString();
                CheatConfig.hasLogoffSpot = true;
                feedback(String.format("Saved: %.1f %.1f %.1f", CheatConfig.logoffX, CheatConfig.logoffY, CheatConfig.logoffZ));
            }
        }).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§eTeleport to Logoff"), btn -> {
            if (CheatConfig.hasLogoffSpot) teleportTo(CheatConfig.logoffX, CheatConfig.logoffY, CheatConfig.logoffZ);
        }).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP + 8;

        int bw = (PANEL_W - 16 - GAP * 2) / 3;
        tpXBox = makeField(col1,                y, bw, "X", "0");
        tpYBox = makeField(col1 + bw + GAP,     y, bw, "Y", "64");
        tpZBox = makeField(col1 + (bw+GAP)*2,   y, bw, "Z", "0");
        addDrawableChild(tpXBox); addDrawableChild(tpYBox); addDrawableChild(tpZBox); y += 24;

        addDrawableChild(ButtonWidget.builder(Text.literal("Teleport to Coords"), btn -> {
            try {
                teleportTo(Double.parseDouble(tpXBox.getText()), Double.parseDouble(tpYBox.getText()), Double.parseDouble(tpZBox.getText()));
            } catch (NumberFormatException ignored) { feedback("Invalid coordinates!"); }
        }).dimensions(cx - 70, y, 140, BTN_H).build());
    }

    // ── GIVE ─────────────────────────────────────────────────────────────────
    private void buildGiveTab(int pl, int top, int cx) {
        int col1 = pl + 8, y = top;
        int btnW = (PANEL_W - 16 - GAP) / 2;
        int col2 = col1 + btnW + GAP;

        itemSearchBox = new TextFieldWidget(textRenderer, col1, y, PANEL_W - 16, 16, Text.literal("Search items..."));
        itemSearchBox.setMaxLength(50);
        itemSearchBox.setChangedListener(text -> {
            searchResults = Registries.ITEM.stream()
                    .filter(item -> item != Items.AIR)
                    .filter(item -> {
                        String id = Registries.ITEM.getId(item).getPath();
                        String name = item.getName().getString().toLowerCase();
                        return id.contains(text.toLowerCase()) || name.contains(text.toLowerCase());
                    })
                    .limit(100)
                    .collect(Collectors.toList());
            searchScroll = 0;
        });
        addDrawableChild(itemSearchBox); y += 20;

        for (int i = 0; i < Math.min(8, searchResults.size() - searchScroll); i++) {
            Item item = searchResults.get(searchScroll + i);
            String name = item.getName().getString();
            int bx = col1 + (i % 2) * (btnW + GAP);
            int by = y + (i / 2) * (BTN_H + GAP);
            addDrawableChild(ButtonWidget.builder(Text.literal("§f" + name), btn -> giveItem(item, 64))
                    .dimensions(bx, by, btnW, BTN_H).build());
        }
        y += 4 * (BTN_H + GAP) + GAP;

        if (searchResults.size() > 8) {
            addDrawableChild(ButtonWidget.builder(Text.literal("▲"), btn -> { if (searchScroll > 0) { searchScroll -= 2; rebuildWidgets(); } }).dimensions(cx - 25, y, 22, 16).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("▼"), btn -> { if (searchScroll + 8 < searchResults.size()) { searchScroll += 2; rebuildWidgets(); } }).dimensions(cx + 3, y, 22, 16).build());
            y += 20;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("64x Diamond"),       btn -> giveItem(Items.DIAMOND, 64)).dimensions(col1, y, btnW, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("64x Netherite Ingot"),btn -> giveItem(Items.NETHERITE_INGOT, 64)).dimensions(col2, y, btnW, BTN_H).build()); y += BTN_H + GAP;
        addDrawableChild(ButtonWidget.builder(Text.literal("64x Gold Ingot"),    btn -> giveItem(Items.GOLD_INGOT, 64)).dimensions(col1, y, btnW, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("64x Iron Ingot"),    btn -> giveItem(Items.IRON_INGOT, 64)).dimensions(col2, y, btnW, BTN_H).build());
    }

    // ── EFFECTS ──────────────────────────────────────────────────────────────
    private void buildEffectsTab(int pl, int top, int cx) {
        int col1 = pl + 8;
        int btnW = (PANEL_W - 16 - GAP) / 2;
        int col2 = col1 + btnW + GAP;
        int y = top;

        for (int i = 0; i < Math.min(10, allEffects.size() - effectScroll); i++) {
            StatusEffect eff = allEffects.get(effectScroll + i);
            String name = eff.getName().getString();
            int bx = col1 + (i % 2) * (btnW + GAP);
            int by = y + (i / 2) * (BTN_H + GAP);
            addDrawableChild(ButtonWidget.builder(Text.literal("§d" + name), btn -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) { mc.player.addStatusEffect(new StatusEffectInstance(eff, 999999, 10, false, false)); feedback("Applied: " + name); }
            }).dimensions(bx, by, btnW, BTN_H).build());
        }
        y += 5 * (BTN_H + GAP) + GAP;

        addDrawableChild(ButtonWidget.builder(Text.literal("▲ Prev"), btn -> { if (effectScroll > 0) { effectScroll -= 2; rebuildWidgets(); } }).dimensions(col1, y, BTN_W, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next ▼"), btn -> { if (effectScroll + 10 < allEffects.size()) { effectScroll += 2; rebuildWidgets(); } }).dimensions(col2, y, BTN_W, BTN_H).build()); y += BTN_H + GAP;

        addDrawableChild(ButtonWidget.builder(Text.literal("§cClear All Effects"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) { mc.player.clearStatusEffects(); feedback("Effects cleared"); }
        }).dimensions(cx - 60, y, 120, BTN_H).build());
    }

    // ── CLIENT HELPERS ───────────────────────────────────────────────────────
    private void teleportTo(double x, double y, double z) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.updatePosition(x, y, z);
        feedback(String.format("Teleported to %.1f %.1f %.1f", x, y, z));
    }

    private void setTime(long time) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        mc.world.setTimeOfDay(time);
        feedback("Time set to " + time);
    }

    private void setWeather(boolean rain, boolean thunder) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        mc.world.getLevelProperties().setRaining(rain);
        mc.world.getLevelProperties().setRainTime(rain ? 6000 : 0);
        mc.world.getLevelProperties().setThunderTime(thunder ? 6000 : 0);
        feedback(thunder ? "Thunder!" : rain ? "Raining" : "Clear skies");
    }

    private void giveItem(Item item, int count) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.getInventory().insertStack(new ItemStack(item, count));
        feedback("Given " + count + "x " + item.getName().getString());
    }

    private void feedback(String msg) { feedbackMsg = msg; feedbackTimer = 80; }

    private TextFieldWidget makeField(int x, int y, int w, String hint, String def) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 16, Text.literal(hint));
        f.setPlaceholder(Text.literal(hint));
        f.setText(def);
        f.setMaxLength(12);
        return f;
    }

    private void addToggle(String label, int x, int y,
                           java.util.function.BooleanSupplier getter,
                           java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(ButtonWidget.builder(makeLabel(label, getter.getAsBoolean()), btn -> {
            boolean v = !getter.getAsBoolean();
            setter.accept(v);
            btn.setMessage(makeLabel(label, v));
        }).dimensions(x, y, BTN_W, BTN_H).build());
    }

    // ── RENDER ───────────────────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width / 2, cy = height / 2;
        int pl = cx - PANEL_W / 2, pt = cy - PANEL_H / 2;
        int pr = pl + PANEL_W, pb = pt + PANEL_H;

        ctx.fill(pl, pt, pr, pb, 0xD0101010);
        ctx.fill(pl,     pt,     pr,     pt + 2, 0xFF44FF44);
        ctx.fill(pl,     pb - 2, pr,     pb,     0xFF44FF44);
        ctx.fill(pl,     pt,     pl + 2, pb,     0xFF44FF44);
        ctx.fill(pr - 2, pt,     pr,     pb,     0xFF44FF44);

        ctx.drawCenteredTextWithShadow(textRenderer, "§a§lCheat Menu  §7[/]", cx, pt + 4, 0xFFFFFF);
        ctx.fill(pl + 8, pt + 14 + TAB_H, pr - 8, pt + 14 + TAB_H + 1, 0xFF44FF44);

        if (feedbackTimer > 0) {
            feedbackTimer--;
            int alpha = Math.min(255, feedbackTimer * 6);
            ctx.drawCenteredTextWithShadow(textRenderer, "§a" + feedbackMsg, cx, pb - 36, (alpha << 24) | 0xFFFFFF);
        }

        if (currentTab == 2 && CheatConfig.hasLogoffSpot) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                String.format("§7Saved: §fX%.1f Y%.1f Z%.1f §7(%s)",
                    CheatConfig.logoffX, CheatConfig.logoffY, CheatConfig.logoffZ,
                    CheatConfig.logoffDimension.replace("minecraft:", "")),
                cx, pb - 48, 0xFFFFFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean editFocused = (mineSpeedBox != null && mineSpeedBox.isFocused())
                || (timeBox != null && timeBox.isFocused())
                || (tpXBox != null && tpXBox.isFocused())
                || (tpYBox != null && tpYBox.isFocused())
                || (tpZBox != null && tpZBox.isFocused())
                || (itemSearchBox != null && itemSearchBox.isFocused());
        if (keyCode == GLFW.GLFW_KEY_SLASH && !editFocused) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean shouldPause() { return false; }

    private static Text makeLabel(String name, boolean on) {
        return Text.literal((on ? "§a[ON]  " : "§c[OFF] ") + "§f" + name);
    }
}
