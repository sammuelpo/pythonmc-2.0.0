package com.pythonmc.mod.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestiona la estructura de carpetas del proyecto Engine
 * Ubicación: saves/world_name/filesproject/
 */
public class ProjectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectManager.class);
    private static final String PROJECT_FOLDER = "filesproject";

    /**
     * Crea la estructura completa de carpetas para un proyecto nuevo
     */
    public static void createProjectStructure(Level world) {
        File worldFolder = getWorldFolder(world);
        File projectRoot = new File(worldFolder, PROJECT_FOLDER);

        // Crear carpetas principales
        createFolder(projectRoot, "scripts");
        createFolder(projectRoot, "sounds");
        createFolder(projectRoot, "nodes");
        createFolder(projectRoot, "assets");
        createFolder(new File(projectRoot, "assets"), "models");
        createFolder(new File(projectRoot, "assets"), "textures");
        createFolder(new File(projectRoot, "assets"), "animations");

        // Crear archivo de ejemplo
        createExampleScript(new File(projectRoot, "scripts"));

        // Crear archivo scene_tree.json
        createSceneTree(new File(projectRoot, "nodes"));

        LOGGER.info("Estructura de proyecto creada en: {}", projectRoot.getAbsolutePath());
    }

    /**
     * Obtiene la carpeta raíz del proyecto
     */
    public static File getProjectRoot(Level world) {
        File worldFolder = getWorldFolder(world);
        return new File(worldFolder, PROJECT_FOLDER);
    }

    /**
     * Obtiene la carpeta de scripts
     */
    public static File getScriptsFolder(Level world) {
        return new File(getProjectRoot(world), "scripts");
    }

    /**
     * Obtiene la carpeta de texturas
     */
    public static File getAssetsTexturesFolder() {
        // Para el editor de texturas, usamos el mundo actual del cliente
        Level world = net.minecraft.client.Minecraft.getInstance().level;
        if (world != null) {
            File projectRoot = getProjectRoot(world);
            File assetsFolder = new File(projectRoot, "assets");
            return new File(assetsFolder, "textures");
        }
        // Fallback: crear carpeta temporal
        File tempTextures = new File("temp_textures");
        tempTextures.mkdirs();
        return tempTextures;
    }

    /**
     * Obtiene la carpeta de modelos
     */
    public static File getAssetsModelsFolder() {
        // Para el editor de modelos, usamos el mundo actual del cliente
        Level world = net.minecraft.client.Minecraft.getInstance().level;
        if (world != null) {
            File projectRoot = getProjectRoot(world);
            File assetsFolder = new File(projectRoot, "assets");
            return new File(assetsFolder, "models");
        }
        // Fallback: crear carpeta temporal
        File tempModels = new File("temp_models");
        tempModels.mkdirs();
        return tempModels;
    }

    /**
     * Obtiene la carpeta de animaciones
     */
    public static File getAssetsAnimationsFolder() {
        // Para el sistema de animaciones, usamos el mundo actual del cliente
        Level world = net.minecraft.client.Minecraft.getInstance().level;
        if (world != null) {
            File projectRoot = getProjectRoot(world);
            File assetsFolder = new File(projectRoot, "assets");
            return new File(assetsFolder, "animations");
        }
        // Fallback: crear carpeta temporal
        File tempAnimations = new File("temp_animations");
        tempAnimations.mkdirs();
        return tempAnimations;
    }

    /**
     * Obtiene la carpeta de nodos
     */
    public static File getNodesFolder(Level world) {
        return new File(getProjectRoot(world), "nodes");
    }

    /**
     * Obtiene la carpeta de assets
     */
    public static File getAssetsFolder(Level world) {
        return new File(getProjectRoot(world), "assets");
    }

    /**
     * Obtiene la carpeta de sonidos del proyecto
     * Ubicación: saves/world_name/filesproject/sounds
     */
    public static File getSoundsFolder(Level world) {
        return new File(getProjectRoot(world), "sounds");
    }

    /**
     * Crea un script Python en la carpeta de scripts
     */
    public static boolean createScript(Level world, String scriptName) {
        if (!scriptName.endsWith(".py")) {
            scriptName += ".py";
        }

        File scriptsFolder = getScriptsFolder(world);
        File scriptFile = new File(scriptsFolder, scriptName);

        try {
            if (scriptFile.createNewFile()) {
                // Escribir template básico
                writeScriptTemplate(scriptFile);
                LOGGER.info("Script creado: {}", scriptFile.getName());
                return true;
            } else {
                LOGGER.warn("El script ya existe: {}", scriptName);
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Error al crear script: {}", scriptName, e);
            return false;
        }
    }

    /**
     * Crea una carpeta en el proyecto
     */
    public static boolean createProjectFolder(Level world, String folderPath) {
        File projectRoot = getProjectRoot(world);
        File newFolder = new File(projectRoot, folderPath);

        if (newFolder.mkdirs()) {
            LOGGER.info("Carpeta creada: {}", folderPath);
            return true;
        } else {
            LOGGER.warn("No se pudo crear la carpeta: {}", folderPath);
            return false;
        }
    }

    /**
     * Verifica si existe la estructura del proyecto
     */
    public static boolean projectExists(Level world) {
        return getProjectRoot(world).exists();
    }

    // ========== MÉTODOS PRIVADOS ==========

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

    private static void createFolder(File parent, String name) {
        File folder = new File(parent, name);
        if (folder.mkdirs()) {
            LOGGER.debug("Carpeta creada: {}", folder.getAbsolutePath());
        }
    }

    private static void createExampleScript(File scriptsFolder) {
        File exampleScript = new File(scriptsFolder, "example.py");
        try (FileWriter writer = new FileWriter(exampleScript)) {
            writer.write("# PythonMC Script Example\n");
            writer.write("# Este es un script de ejemplo\n\n");
            writer.write("def init():\n");
            writer.write("    print(\"Script inicializado\")\n\n");
            writer.write("def update():\n");
            writer.write("    # Este método se llama cada tick\n");
            writer.write("    pass\n");
            LOGGER.debug("Script de ejemplo creado");
        } catch (IOException e) {
            LOGGER.error("Error al crear script de ejemplo", e);
        }
    }

    private static void createSceneTree(File nodesFolder) {
        File sceneTree = new File(nodesFolder, "scene_tree.json");
        try (FileWriter writer = new FileWriter(sceneTree)) {
            writer.write("{\n");
            writer.write("  \"root\": {\n");
            writer.write("    \"type\": \"Node\",\n");
            writer.write("    \"children\": []\n");
            writer.write("  }\n");
            writer.write("}\n");
            LOGGER.debug("scene_tree.json creado");
        } catch (IOException e) {
            LOGGER.error("Error al crear scene_tree.json", e);
        }
    }

    private static void writeScriptTemplate(File scriptFile) throws IOException {
        try (FileWriter writer = new FileWriter(scriptFile)) {
            writer.write("# PythonMC Script\n");
            writer.write("# Nombre: " + scriptFile.getName() + "\n\n");
            writer.write("def init():\n");
            writer.write("    \"\"\"Se ejecuta al cargar el script\"\"\"\n");
            writer.write("    pass\n\n");
            writer.write("def update():\n");
            writer.write("    \"\"\"Se ejecuta cada tick\"\"\"\n");
            writer.write("    pass\n");
        }
    }
}