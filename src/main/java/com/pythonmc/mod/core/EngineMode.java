package com.pythonmc.mod.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona el estado del Modo Engine para cada mundo
 */
public class EngineMode {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineMode.class);

    // Mapa de mundos y su estado de Engine Mode
    private static final Map<String, Boolean> worldEngineStatus = new HashMap<>();

    /**
     * Activa el Modo Engine para un mundo
     */
    public static void enable(Level world) {
        String worldName = getWorldName(world);
        worldEngineStatus.put(worldName, true);

        // Guardar en config
        ConfigHandler.saveEngineState(world, true);

        LOGGER.info("Modo Engine ACTIVADO para el mundo: {}", worldName);
    }

    /**
     * Desactiva el Modo Engine para un mundo
     */
    public static void disable(Level world) {
        String worldName = getWorldName(world);
        worldEngineStatus.put(worldName, false);

        // Guardar en config
        ConfigHandler.saveEngineState(world, false);

        LOGGER.info("Modo Engine DESACTIVADO para el mundo: {}", worldName);
    }

    /**
     * Verifica si el Modo Engine está activo en un mundo
     */
    public static boolean isEnabled(Level world) {
        String worldName = getWorldName(world);

        // Si no está en cache, cargar desde config
        if (!worldEngineStatus.containsKey(worldName)) {
            boolean state = ConfigHandler.loadEngineState(world);
            worldEngineStatus.put(worldName, state);
        }

        return worldEngineStatus.getOrDefault(worldName, false);
    }

    /**
     * Activa automáticamente el Engine Mode al crear un mundo nuevo
     */
    public static void autoEnableForNewWorld(Level world) {
        String worldName = getWorldName(world);

        // Verificar si es la primera vez que se carga el mundo
        if (!ConfigHandler.configExists(world)) {
            LOGGER.info("Mundo nuevo detectado: {}. Activando Modo Engine automáticamente.", worldName);
            enable(world);

            // Crear estructura de proyecto
            ProjectManager.createProjectStructure(world);
        }
    }

    /**
     * Obtiene el nombre único del mundo
     */
    private static String getWorldName(Level world) {
        if (world instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getWorldPath(LevelResource.ROOT).toString();
        }
        return "unknown_world";
    }

    /**
     * Limpia el cache al cerrar el mundo
     */
    public static void clearCache(Level world) {
        String worldName = getWorldName(world);
        worldEngineStatus.remove(worldName);
        LOGGER.info("Cache limpiado para el mundo: {}", worldName);
    }
}