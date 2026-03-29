package com.cheatmod.cheatmod.events;

import com.cheatmod.cheatmod.CheatMod;
import com.cheatmod.cheatmod.config.CheatConfig;
import com.cheatmod.cheatmod.client.gui.CheatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CheatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CheatEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // --- Keybind: open menu ---
        while (CheatConfig.OPEN_MENU_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new CheatScreen());
            }
        }

        // --- Speed Boost ---
        AttributeInstance speedAttr = mc.player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            if (CheatConfig.speedBoost) {
                // Only add modifier if not already present
                if (speedAttr.getModifier(CheatConfig.SPEED_MODIFIER_UUID) == null) {
                    speedAttr.addTransientModifier(new AttributeModifier(
                            CheatConfig.SPEED_MODIFIER_UUID,
                            "cheatmod_speed",
                            CheatConfig.SPEED_BOOST_AMOUNT,
                            AttributeModifier.Operation.MULTIPLY_BASE
                    ));
                }
            } else {
                // Remove modifier if present
                if (speedAttr.getModifier(CheatConfig.SPEED_MODIFIER_UUID) != null) {
                    speedAttr.removeModifier(CheatConfig.SPEED_MODIFIER_UUID);
                }
            }
        }

        // --- Night Vision ---
        if (CheatConfig.nightVision) {
            // Refresh every 5 seconds so it never runs out; invisible to HUD
            if (mc.player.tickCount % 100 == 0 || !mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            }
        } else {
            if (mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
                mc.player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }

        // --- Auto-Heal (1 HP every second) ---
        if (CheatConfig.autoHeal && mc.player.tickCount % 20 == 0) {
            if (mc.player.getHealth() < mc.player.getMaxHealth()) {
                mc.player.heal(1.0f);
            }
        }

        // --- Infinite Hunger ---
        if (CheatConfig.infiniteHunger) {
            mc.player.getFoodData().setFoodLevel(20);
            mc.player.getFoodData().setSaturation(20.0f);
        }

        // --- Creative Flight ---
        if (CheatConfig.creativeFlight) {
            if (!mc.player.getAbilities().mayfly) {
                mc.player.getAbilities().mayfly = true;
                mc.player.onUpdateAbilities();
            }
        } else {
            // Only revoke if player is not already in creative/spectator
            if (mc.player.getAbilities().mayfly
                    && !mc.player.isCreative()
                    && !mc.player.isSpectator()) {
                mc.player.getAbilities().mayfly = false;
                mc.player.getAbilities().flying = false;
                mc.player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!CheatConfig.fastMineEnabled) return;
        event.setNewSpeed(event.getOriginalSpeed() * CheatConfig.mineSpeedMultiplier);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (event.getEntity() != mc.player) return;

        // God Mode: cancel ALL damage
        if (CheatConfig.godModeEnabled) {
            event.setCanceled(true);
            return;
        }

        // No Fall Damage: cancel only fall-type damage
        if (CheatConfig.noFallDamage && event.getSource().is(DamageTypeTags.IS_FALL)) {
            event.setCanceled(true);
        }
    }
}
