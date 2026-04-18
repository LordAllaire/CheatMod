package com.cheatmod.cheatmod.config;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.util.UUID;

public class CheatConfig {

    public static boolean fastMineEnabled  = false;
    public static boolean godModeEnabled   = false;
    public static boolean noFallDamage     = false;
    public static boolean speedBoost       = false;
    public static boolean nightVision      = false;
    public static boolean autoHeal         = false;
    public static boolean infiniteHunger   = false;
    public static boolean creativeFlight   = false;
    public static boolean xrayEnabled      = false;
    public static boolean noClipEnabled    = false;

    public static float  mineSpeedMultiplier = 20.0f;
    public static final double SPEED_BOOST_AMOUNT = 1.5;

    public static double  logoffX = 0, logoffY = 0, logoffZ = 0;
    public static String  logoffDimension = "";
    public static boolean hasLogoffSpot = false;

    public static final UUID SPEED_MODIFIER_UUID =
            UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public static KeyBinding OPEN_MENU_KEY;

    public static void registerKeybinds() {
        OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cheatmod.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SLASH,
                "key.categories.cheatmod"
        ));
    }
}
