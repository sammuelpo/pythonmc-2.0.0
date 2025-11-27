package com.pythonmc.mod.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.pythonmc.mod.core.EngineMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registro de teclas para abrir el Engine
 * Tecla por defecto: E (Engine)
 */
@Mod.EventBusSubscriber(modid = "pythonmc", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EngineKeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineKeyBindings.class);
    
    public static final String CATEGORY = "key.categories.pythonmc";
    
    public static KeyMapping openEngineKey;
    
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        openEngineKey = new KeyMapping(
            "key.pythonmc.open_engine",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY
        );
        
        event.register(openEngineKey);
        
        LOGGER.info("Teclas del Engine registradas");
    }
    
    /**
     * Listener para detectar cuando se presiona la tecla
     */
    @Mod.EventBusSubscriber(modid = "pythonmc", value = Dist.CLIENT)
    public static class ClientEvents {
        
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            
            Minecraft mc = Minecraft.getInstance();
            
            // Solo funciona si hay un mundo cargado y un jugador
            if (mc.level == null || mc.player == null) return;
            
            // Verificar si el Engine Mode está activo
            if (!EngineMode.isEnabled(mc.level)) return;
            
            // Detectar tecla presionada
            while (openEngineKey.consumeClick()) {
                // Abrir la pantalla del Engine
                mc.setScreen(new EngineScreen(mc.level));
                LOGGER.info("Engine Screen abierto");
            }
        }
    }
}