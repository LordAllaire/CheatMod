package com.cheatmod.cheatmod.events;

import com.cheatmod.cheatmod.CheatMod;
import com.cheatmod.cheatmod.config.CheatConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles damage cancellation on the SERVER side (where damage actually happens).
 * Must NOT have value = Dist.CLIENT or it won't fire in singleplayer.
 */
@Mod.EventBusSubscriber(modid = CheatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageEvents {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // Only care about ServerPlayer entities
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // God Mode: cancel ALL incoming damage
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
