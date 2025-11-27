package com.pythonmc.mod.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Ejecuta scripts Python desde el Engine.
 *
 * Nota: usa el ejecutable configurado en:
 * - Propiedad de sistema: -Dpythonmc.python=/ruta/a/python
 * - o variable de entorno: PYTHONMC_PYTHON
 * - si no, intenta "python" del PATH.
 */
public class PythonExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonExecutor.class);
    private static final int MAX_OUTPUT_LINES = 200;
    private static final List<String> OUTPUT_LINES = Collections.synchronizedList(new ArrayList<>());

    public static void appendOutput(String line) {
        synchronized (OUTPUT_LINES) {
            OUTPUT_LINES.add(line);
            // Limitar tamaño del buffer
            while (OUTPUT_LINES.size() > MAX_OUTPUT_LINES) {
                OUTPUT_LINES.remove(0);
            }
        }
    }
    
    // Método público para uso desde GUI
    public static void appendOutputPublic(String line) {
        appendOutput(line);
    }

    public static List<String> getOutputSnapshot() {
        synchronized (OUTPUT_LINES) {
            return new ArrayList<>(OUTPUT_LINES);
        }
    }

    public static void clearOutput() {
        synchronized (OUTPUT_LINES) {
            OUTPUT_LINES.clear();
        }
    }

    private static String resolvePythonExecutable() {
        String prop = System.getProperty("pythonmc.python");
        if (prop != null && !prop.isEmpty()) {
            return prop;
        }

        String env = System.getenv("PYTHONMC_PYTHON");
        if (env != null && !env.isEmpty()) {
            return env;
        }

        return "python"; // Por defecto, usa "python" del PATH
    }

    public static void runScript(File scriptFile) {
        if (scriptFile == null) {
            LOGGER.error("Script Python nulo");
            return;
        }
        if (!scriptFile.exists() || !scriptFile.isFile()) {
            LOGGER.error("Script Python no encontrado: {}", scriptFile.getAbsolutePath());
            return;
        }

        String pythonExe = resolvePythonExecutable();

        clearOutput();
        appendOutput("Ejecutando " + scriptFile.getName() + "...");
        appendOutput("Python detectado: " + pythonExe);

        Thread thread = new Thread(() -> {
            try {
                ProcessBuilder builder = new ProcessBuilder(pythonExe, scriptFile.getAbsolutePath());
                builder.directory(scriptFile.getParentFile());
                builder.redirectErrorStream(true);

                Map<String, String> env = builder.environment();
                env.putIfAbsent("PYTHONUNBUFFERED", "1");
                env.put("PYTHONMC_SCRIPT_NAME", scriptFile.getName());
                env.put("PYTHONMC_SCRIPT_PATH", scriptFile.getAbsolutePath());

                LOGGER.info("Lanzando Python: {} {}", pythonExe, scriptFile.getAbsolutePath());
                appendOutput("Lanzando proceso...");
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("[PYTHON] {}", line);
                        appendOutput(line);
                    }
                }

                int exitCode = process.waitFor();
                LOGGER.info("Script '{}' finalizado con código {}", scriptFile.getName(), exitCode);
                appendOutput("[exit] " + exitCode);
            } catch (IOException e) {
                LOGGER.error("Error al ejecutar script Python '{}'", scriptFile.getAbsolutePath(), e);
                appendOutput("[error] " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Ejecución interrumpida de script '{}'", scriptFile.getName(), e);
                appendOutput("[interrumpido]");
            }
        }, "PythonMC-Executor-" + scriptFile.getName());

        thread.setDaemon(true);
        thread.start();
    }
}
