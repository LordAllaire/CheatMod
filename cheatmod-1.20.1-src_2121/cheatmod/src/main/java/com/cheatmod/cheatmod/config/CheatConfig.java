package com.cheatmod.cheatmod.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class CheatConfig {

    // --- Toggle States ---
    public static boolean fastMineEnabled   = false;
    public static boolean godModeEnabled    = false;
    public static boolean noFallDamage      = false;
    public static boolean speedBoost        = false;
    public static boolean nightVision       = false;
    public static boolean autoHeal          = false;
    public static boolean infiniteHunger    = false;
    public static boolean creativeFlight    = false;

    // --- Values ---
    public static float mineSpeedMultiplier = 5.0f;
    // Speed multiplier: 1.0 = normal. 2.0 = double speed (MULTIPLY_BASE operation)
    public static final double SPEED_BOOST_AMOUNT = 1.5;

    // Stable UUID for the speed AttributeModifier — must never change between ticks
    public static final UUID SPEED_MODIFIER_UUID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    // --- Keybind ---
    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.cheatmod.open_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SLASH,
            "key.categories.cheatmod"
    );
}
