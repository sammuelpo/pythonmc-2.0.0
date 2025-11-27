package com.pythonmc.mod;

import com.pythonmc.mod.core.EngineMode;
import com.pythonmc.mod.core.ConfigHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("pythonmc")
public class PythonMCMod {
    public static final String MOD_ID = "pythonmc";
    public static final String VERSION = "2.0.0";
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonMCMod.class);

    public PythonMCMod() {
        // Registrar eventos del mod
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        LOGGER.info("PythonMC {} inicializado", VERSION);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Setup de PythonMC completado");
        LOGGER.info("Sistema de Engine Mode listo");
    }

    /**
     * Listener para cuando se crea/carga un mundo
     */
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class WorldEventHandler {

        @SubscribeEvent
        public static void onWorldLoad(LevelEvent.Load event) {
            if (event.getLevel() instanceof ServerLevel world) {
                LOGGER.info("Mundo cargado: {}", world.dimension().location());

                // Activar automáticamente el Engine Mode para mundos nuevos
                EngineMode.autoEnableForNewWorld(world);

                if (EngineMode.isEnabled(world)) {
                    LOGGER.info("Modo Engine ACTIVO en este mundo");
                } else {
                    LOGGER.info("Modo Engine INACTIVO en este mundo");
                }
            }
        }

        @SubscribeEvent
        public static void onWorldUnload(LevelEvent.Unload event) {
            if (event.getLevel() instanceof Level world) {
                LOGGER.info("Mundo descargado, limpiando cache...");
                EngineMode.clearCache(world);
            }
        }
    }
}