package com.cheatmod.cheatmod.events;

import com.cheatmod.cheatmod.CheatMod;
import com.cheatmod.cheatmod.config.CheatConfig;
import com.cheatmod.cheatmod.client.gui.CheatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CheatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CheatEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Handle keybind to open menu
        while (CheatConfig.OPEN_MENU_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new CheatScreen());
            }
        }

        // Apply fast mining: set the player's mining speed attribute each tick
        if (CheatConfig.fastMineEnabled) {
            mc.player.getAttributes().getInstance(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)
                    .setBaseValue(1024.0);
        } else {
            mc.player.getAttributes().getInstance(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)
                    .setBaseValue(4.0); // default
        }
    }

    @SubscribeEvent
    public static void onPlayerBlockBreakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        if (!CheatConfig.fastMineEnabled) return;
        if (!(event.getEntity() instanceof Player)) return;

        // Multiply the original break speed
        event.setNewSpeed(event.getOriginalSpeed() * CheatConfig.mineSpeedMultiplier);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!CheatConfig.godModeEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Cancel all damage to the local player
        if (event.getEntity() == mc.player) {
            event.setCanceled(true);
        }
    }
}
