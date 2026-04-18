package com.cheatmod.cheatmod;

import com.cheatmod.cheatmod.config.CheatConfig;
import com.cheatmod.cheatmod.client.gui.CheatScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CheatModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CheatConfig.registerKeybinds();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Open menu keybind
            while (CheatConfig.OPEN_MENU_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CheatScreen());
                }
            }

            // Speed boost
            EntityAttributeInstance speedAttr = client.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (speedAttr != null) {
                if (CheatConfig.speedBoost) {
                    if (speedAttr.getModifier(CheatConfig.SPEED_MODIFIER_UUID) == null) {
                        speedAttr.addTemporaryModifier(new EntityAttributeModifier(
                                CheatConfig.SPEED_MODIFIER_UUID, "cheatmod_speed",
                                CheatConfig.SPEED_BOOST_AMOUNT, EntityAttributeModifier.Operation.MULTIPLY_BASE));
                    }
                } else {
                    speedAttr.removeModifier(CheatConfig.SPEED_MODIFIER_UUID);
                }
            }

            // Night vision
            if (CheatConfig.nightVision) {
                if (client.player.age % 100 == 0 || !client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false));
                }
            } else {
                if (client.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
                    client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }

            // God mode - heal every tick
            if (CheatConfig.godModeEnabled) client.player.heal(client.player.getMaxHealth());

            // Auto-heal
            if (CheatConfig.autoHeal && !CheatConfig.godModeEnabled && client.player.age % 20 == 0)
                if (client.player.getHealth() < client.player.getMaxHealth())
                    client.player.heal(1.0f);

            // Infinite hunger
            if (CheatConfig.infiniteHunger) {
                client.player.getHungerManager().setFoodLevel(20);
                client.player.getHungerManager().setSaturationLevel(20.0f);
            }

            // Creative flight
            if (CheatConfig.creativeFlight) {
                if (!client.player.getAbilities().allowFlying) {
                    client.player.getAbilities().allowFlying = true;
                    client.player.sendAbilitiesUpdate();
                }
            } else {
                if (client.player.getAbilities().allowFlying
                        && !client.player.isCreative()
                        && !client.player.isSpectator()) {
                    client.player.getAbilities().allowFlying = false;
                    client.player.getAbilities().flying = false;
                    client.player.sendAbilitiesUpdate();
                }
            }

            // No-clip
            client.player.noClip = CheatConfig.noClipEnabled;
        });
    }
}
