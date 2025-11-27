package com.pythonmc.mod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Maneja la lectura y escritura del archivo engine_config.json
 */
public class ConfigHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHandler.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "engine_config.json";

    /**
     * Guarda el estado del Engine Mode en el archivo de configuración
     */
    public static void saveEngineState(Level world, boolean enabled) {
        File configFile = getConfigFile(world);

        try {
            JsonObject config = loadOrCreateConfig(configFile);

            // Actualizar estado
            config.addProperty("engine_mode_enabled", enabled);
            config.addProperty("last_modified", getCurrentTimestamp());

            // Guardar archivo
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }

            LOGGER.info("Configuración guardada: engine_mode_enabled = {}", enabled);

        } catch (Exception e) {
            LOGGER.error("Error al guardar configuración", e);
        }
    }

    /**
     * Carga el estado del Engine Mode desde el archivo de configuración
     */
    public static boolean loadEngineState(Level world) {
        File configFile = getConfigFile(world);

        if (!configFile.exists()) {
            return false; // Por defecto desactivado si no existe config
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = GSON.fromJson(reader, JsonObject.class);

            if (config.has("engine_mode_enabled")) {
                boolean state = config.get("engine_mode_enabled").getAsBoolean();
                LOGGER.info("Estado del Engine Mode cargado: {}", state);
                return state;
            }

        } catch (Exception e) {
            LOGGER.error("Error al cargar configuración", e);
        }

        return false;
    }

    /**
     * Verifica si existe el archivo de configuración
     */
    public static boolean configExists(Level world) {
        return getConfigFile(world).exists();
    }

    /**
     * Crea la configuración inicial para un mundo nuevo
     */
    public static void createInitialConfig(Level world) {
        File configFile = getConfigFile(world);

        try {
            JsonObject config = new JsonObject();
            config.addProperty("engine_mode_enabled", true);
            config.addProperty("project_name", getWorldName(world));
            config.addProperty("created_date", getCurrentTimestamp());
            config.addProperty("python_version", "3.11");
            config.addProperty("mod_version", "2.0.0");

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            }

            LOGGER.info("Configuración inicial creada para: {}", getWorldName(world));

        } catch (Exception e) {
            LOGGER.error("Error al crear configuración inicial", e);
        }
    }

    /**
     * Obtiene el archivo de configuración para un mundo
     */
    private static File getConfigFile(Level world) {
        File worldFolder = getWorldFolder(world);
        return new File(worldFolder, CONFIG_FILE);
    }

    /**
     * Obtiene la carpeta del mundo
     */
    private static File getWorldFolder(Level world) {
        if (world instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getWorldPath(LevelResource.ROOT).toFile();
        }

        // Lado cliente (singleplayer): intentar usar el servidor integrado
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.getSingleplayerServer() != null) {
                return mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toFile();
            }
        } catch (Throwable t) {
            LOGGER.error("Error al obtener la carpeta del mundo en cliente", t);
        }

        // Fallback seguro (por ejemplo en multijugador remoto): usar carpeta local separada
        String dimName = world.dimension().location().toString().replace(':', '_').replace('/', '_');
        File fallback = new File("pythonmc_remote_" + dimName);
        if (!fallback.exists() && !fallback.mkdirs()) {
            LOGGER.warn("No se pudo crear carpeta fallback para mundo remoto: {}", fallback.getAbsolutePath());
        }
        LOGGER.warn("Usando carpeta fallback para mundo remoto: {}", fallback.getAbsolutePath());
        return fallback;
    }

    /**
     * Carga o crea un archivo de configuración
     */
    private static JsonObject loadOrCreateConfig(File configFile) {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                return GSON.fromJson(reader, JsonObject.class);
            } catch (Exception e) {
                LOGGER.warn("Error al leer config existente, creando nueva", e);
            }
        }

        return new JsonObject();
    }

    /**
     * Obtiene el timestamp actual
     */
    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Obtiene el nombre del mundo
     */
    private static String getWorldName(Level world) {
        if (world instanceof ServerLevel serverLevel) {
            return serverLevel.getServer().getWorldPath(LevelResource.ROOT)
                    .getFileName().toString();
        }
        return "unknown";
    }
}