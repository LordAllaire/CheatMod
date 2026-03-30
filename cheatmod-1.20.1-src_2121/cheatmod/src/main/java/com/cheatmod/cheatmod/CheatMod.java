package com.cheatmod.cheatmod;

import com.cheatmod.cheatmod.config.CheatConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CheatMod.MOD_ID)
public class CheatMod {
    public static final String MOD_ID = "cheatmod";
    public static final Logger LOGGER = LogManager.getLogger();

    public CheatMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onRegisterKeyMappings);
        // CheatEvents is auto-registered via @Mod.EventBusSubscriber
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CheatMod common setup complete.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("CheatMod client setup complete.");
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(CheatConfig.OPEN_MENU_KEY);
    }
}
