package com.cheatmod.cheatmod.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class CheatConfig {

    // Toggle states
    public static boolean fastMineEnabled = false;
    public static boolean godModeEnabled = false;

    // Mining speed multiplier (1.0 = normal, higher = faster)
    public static float mineSpeedMultiplier = 5.0f;

    public static final KeyMapping OPEN_MENU_KEY = new KeyMapping(
            "key.cheatmod.open_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SLASH,
            "key.categories.cheatmod"
    );
}
