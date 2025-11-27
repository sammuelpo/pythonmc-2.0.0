package com.pythonmc.mod.gui;

import com.pythonmc.mod.core.PythonExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de scripts Python con syntax highlighting básico
 */
public class ScriptEditorPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptEditorPanel.class);
    
    private final EngineScreen parent;
    private final int x, y, width, height;
    
    // Estado del editor
    private File currentFile = null;
    private List<String> lines;
    private int cursorLine = 0;
    private int cursorColumn = 0;
    private int scrollOffset = 0;
    private boolean hasUnsavedChanges = false;
    
    // UI
    private Button saveButton;
    private Button runButton;
    
    // Colores
    private static final int BG_COLOR = 0xFF1E1E1E;
    private static final int HEADER_COLOR = 0xFF2B2B2B;
    private static final int LINE_NUMBER_BG = 0xFF252526;
    private static final int TEXT_COLOR = 0xFFD4D4D4;
    private static final int LINE_NUMBER_COLOR = 0xFF858585;
    private static final int CURSOR_COLOR = 0xFFFFFFFF;
    private static final int KEYWORD_COLOR = 0xFF569CD6;
    private static final int STRING_COLOR = 0xFFCE9178;
    private static final int COMMENT_COLOR = 0xFF6A9955;
    
    private static final int LINE_HEIGHT = 12;
    private static final int LINE_NUMBER_WIDTH = 40;
    private static final int CONSOLE_BG = 0xFF202020;
    private static final int CONSOLE_TEXT_COLOR = 0xFFCCCCCC;
    private static final int CONSOLE_HEIGHT = 60;
    
    public ScriptEditorPanel(EngineScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.lines = new ArrayList<>();
        
        // Línea vacía por defecto
        lines.add("");
        
        initButtons();
    }
    
    private void initButtons() {
        int btnWidth = 80;
        int btnHeight = 20;
        int btnY = y + 5;
        
        saveButton = Button.builder(
            Component.literal(" Guardar"),
            btn -> saveFile()
        ).bounds(x + width - btnWidth - 95, btnY, btnWidth, btnHeight).build();
        
        runButton = Button.builder(
            Component.literal(" Ejecutar"),
            btn -> runScript()
        ).bounds(x + width - btnWidth - 10, btnY, btnWidth, btnHeight).build();
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        // Header
        graphics.fill(x, y, x + width, y + 30, HEADER_COLOR);
        
        // Nombre del archivo
        String fileName = currentFile != null ? currentFile.getName() : "Sin archivo";
        if (hasUnsavedChanges) fileName += " *";
        graphics.drawString(parent.getMinecraft().font, fileName, x + 10, y + 10, TEXT_COLOR);
        
        // Botones
        saveButton.render(graphics, mouseX, mouseY, partialTick);
        runButton.render(graphics, mouseX, mouseY, partialTick);
        
        int consoleTop = y + height - CONSOLE_HEIGHT;

        // Área de números de línea
        graphics.fill(x, y + 30, x + LINE_NUMBER_WIDTH, consoleTop, LINE_NUMBER_BG);
        
        // Contenido del editor
        renderEditorContent(graphics, consoleTop);

        renderConsole(graphics, consoleTop);
    }
    
    private void renderEditorContent(GuiGraphics graphics, int editorBottom) {
        int contentX = x + LINE_NUMBER_WIDTH + 5;
        int contentY = y + 35;
        int visibleLines = (editorBottom - contentY) / LINE_HEIGHT;
        
        for (int i = scrollOffset; i < Math.min(lines.size(), scrollOffset + visibleLines); i++) {
            int lineY = contentY + (i - scrollOffset) * LINE_HEIGHT;
            
            // Número de línea
            String lineNum = String.valueOf(i + 1);
            int lineNumX = x + LINE_NUMBER_WIDTH - parent.getMinecraft().font.width(lineNum) - 5;
            graphics.drawString(parent.getMinecraft().font, lineNum, lineNumX, lineY, LINE_NUMBER_COLOR);
            
            // Contenido de la línea con syntax highlighting
            String line = lines.get(i);
            renderLineWithHighlighting(graphics, line, contentX, lineY);
            
            // Cursor
            if (i == cursorLine) {
                int cursorX = contentX + parent.getMinecraft().font.width(
                    line.substring(0, Math.min(cursorColumn, line.length()))
                );
                graphics.fill(cursorX, lineY, cursorX + 1, lineY + LINE_HEIGHT, CURSOR_COLOR);
            }
        }
    }
    
    private void renderConsole(GuiGraphics graphics, int consoleTop) {
        int headerHeight = 14;
        graphics.fill(x, consoleTop, x + width, y + height, CONSOLE_BG);
        graphics.drawString(parent.getMinecraft().font, "Console", x + 5, consoleTop + 2, CONSOLE_TEXT_COLOR);

        java.util.List<String> output = PythonExecutor.getOutputSnapshot();
        int maxLines = (height - consoleTop - headerHeight) / LINE_HEIGHT;

        int lineY = consoleTop + headerHeight;

        if (output.isEmpty()) {
            String hint = "No hay salida. Asegúrate de que tu script hace print() y que Python está configurado.";
            graphics.drawString(parent.getMinecraft().font, hint, x + 5, lineY, CONSOLE_TEXT_COLOR);
            return;
        }

        int start = Math.max(0, output.size() - maxLines);
        for (int i = start; i < output.size(); i++) {
            String line = output.get(i);
            graphics.drawString(parent.getMinecraft().font, line, x + 5, lineY, CONSOLE_TEXT_COLOR);
            lineY += LINE_HEIGHT;
        }
    }
    
    private void renderLineWithHighlighting(GuiGraphics graphics, String line, int x, int y) {
        if (line.isEmpty()) return;
        
        int currentX = x;
        
        // Detección simple de comentarios
        if (line.trim().startsWith("#")) {
            graphics.drawString(parent.getMinecraft().font, line, currentX, y, COMMENT_COLOR);
            return;
        }
        
        // Detección de strings
        if (line.contains("\"") || line.contains("'")) {
            graphics.drawString(parent.getMinecraft().font, line, currentX, y, STRING_COLOR);
            return;
        }
        
        // Detección de keywords
        String[] keywords = {"def", "class", "import", "from", "if", "else", "elif", 
                            "for", "while", "return", "pass", "break", "continue"};
        
        String[] parts = line.split(" ");
        for (String part : parts) {
            boolean isKeyword = false;
            for (String keyword : keywords) {
                if (part.equals(keyword)) {
                    graphics.drawString(parent.getMinecraft().font, part, currentX, y, KEYWORD_COLOR);
                    isKeyword = true;
                    break;
                }
            }
            
            if (!isKeyword) {
                graphics.drawString(parent.getMinecraft().font, part, currentX, y, TEXT_COLOR);
            }
            
            currentX += parent.getMinecraft().font.width(part + " ");
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Botones
        if (saveButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (runButton.mouseClicked(mouseX, mouseY, button)) return true;
        
        // Click en el editor para posicionar cursor
        if (button == 0 && mouseX >= x + LINE_NUMBER_WIDTH && mouseY >= y + 35) {
            int clickedLine = (int)((mouseY - y - 35) / LINE_HEIGHT) + scrollOffset;
            if (clickedLine >= 0 && clickedLine < lines.size()) {
                cursorLine = clickedLine;
                
                // Estimar columna (simplificado)
                String line = lines.get(cursorLine);
                int relativeX = (int)(mouseX - x - LINE_NUMBER_WIDTH - 5);
                cursorColumn = Math.min(line.length(), relativeX / 6); // Aproximado
            }
            return true;
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            return false;
        }
        
        scrollOffset -= (int) delta;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, lines.size() - 20)));
        
        return true;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        
        // Ctrl+S para guardar
        if (ctrlPressed && keyCode == GLFW.GLFW_KEY_S) {
            saveFile();
            return true;
        }
        
        // Ctrl+V para pegar
        if (ctrlPressed && keyCode == GLFW.GLFW_KEY_V) {
            pasteFromClipboard();
            return true;
        }
        
        // Navegación
        if (keyCode == GLFW.GLFW_KEY_UP) {
            cursorLine = Math.max(0, cursorLine - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            cursorLine = Math.min(lines.size() - 1, cursorLine + 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            cursorColumn = Math.max(0, cursorColumn - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            cursorColumn = Math.min(lines.get(cursorLine).length(), cursorColumn + 1);
            return true;
        }
        
        // Enter
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String currentLine = lines.get(cursorLine);
            String before = currentLine.substring(0, cursorColumn);
            String after = currentLine.substring(cursorColumn);
            
            lines.set(cursorLine, before);
            lines.add(cursorLine + 1, after);
            cursorLine++;
            cursorColumn = 0;
            hasUnsavedChanges = true;
            return true;
        }
        
        // Backspace
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorColumn > 0) {
                String line = lines.get(cursorLine);
                lines.set(cursorLine, line.substring(0, cursorColumn - 1) + line.substring(cursorColumn));
                cursorColumn--;
                hasUnsavedChanges = true;
            } else if (cursorLine > 0) {
                String currentLine = lines.remove(cursorLine);
                cursorLine--;
                cursorColumn = lines.get(cursorLine).length();
                lines.set(cursorLine, lines.get(cursorLine) + currentLine);
                hasUnsavedChanges = true;
            }
            return true;
        }
        
        return false;
    }
    
    private void pasteFromClipboard() {
        try {
            // En Forge 1.20.1, usamos Minecraft.getInstance().keyboardHandler
            String clipboardText = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clipboardText == null || clipboardText.isEmpty()) {
                return;
            }
            
            // Dividir el texto pegado por líneas
            String[] pastedLines = clipboardText.split("\\r?\\n");
            
            if (pastedLines.length == 1) {
                // Solo una línea: insertar en la posición actual
                String line = lines.get(cursorLine);
                String newLine = line.substring(0, cursorColumn) + pastedLines[0] + line.substring(cursorColumn);
                lines.set(cursorLine, newLine);
                cursorColumn += pastedLines[0].length();
            } else {
                // Múltiples líneas: dividir y reorganizar
                String firstPart = lines.get(cursorLine).substring(0, cursorColumn);
                String lastPart = lines.get(cursorLine).substring(cursorColumn);
                
                // Reemplazar la línea actual con la primera parte del pegado
                lines.set(cursorLine, firstPart + pastedLines[0]);
                
                // Insertar las líneas intermedias
                for (int i = 1; i < pastedLines.length - 1; i++) {
                    lines.add(cursorLine + i, pastedLines[i]);
                }
                
                // Insertar la última línea con el resto de la línea original
                lines.add(cursorLine + pastedLines.length - 1, pastedLines[pastedLines.length - 1] + lastPart);
                
                // Mover cursor al final de la última línea pegada
                cursorLine += pastedLines.length - 1;
                cursorColumn = pastedLines[pastedLines.length - 1].length();
            }
            
            hasUnsavedChanges = true;
        } catch (Exception e) {
            LOGGER.error("Error al pegar desde el portapapeles", e);
        }
    }
    
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint >= 32 && codePoint <= 126) { // Caracteres imprimibles
            String line = lines.get(cursorLine);
            String newLine = line.substring(0, cursorColumn) + codePoint + line.substring(cursorColumn);
            lines.set(cursorLine, newLine);
            cursorColumn++;
            hasUnsavedChanges = true;
            return true;
        }
        return false;
    }
    
    public void openScript(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            LOGGER.error("Archivo no encontrado: {}", filePath);
            return;
        }
        
        try {
            lines.clear();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            
            if (lines.isEmpty()) {
                lines.add("");
            }
            
            currentFile = file;
            cursorLine = 0;
            cursorColumn = 0;
            scrollOffset = 0;
            hasUnsavedChanges = false;
            
            LOGGER.info("Script abierto: {}", file.getName());
            
        } catch (IOException e) {
            LOGGER.error("Error al abrir script", e);
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            LOGGER.warn("No hay archivo para guardar");
            return;
        }
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            
            hasUnsavedChanges = false;
            LOGGER.info("Archivo guardado: {}", currentFile.getName());
            
            if (parent.getMinecraft().player != null) {
                parent.getMinecraft().player.displayClientMessage(
                    Component.literal("§aArchivo guardado: " + currentFile.getName()),
                    true
                );
            }
            
        } catch (IOException e) {
            LOGGER.error("Error al guardar archivo", e);
        }
    }
    
    private void runScript() {
        if (currentFile == null) {
            LOGGER.warn("No hay script para ejecutar");
            appendOutput("ERROR: No hay ningún script abierto. Crea o abre un archivo primero.");
            return;
        }

        LOGGER.info("Ejecutando script: {}", currentFile.getName());

        // Guardar antes de ejecutar si hay cambios pendientes
        if (hasUnsavedChanges) {
            saveFile();
        }

        // Mensaje visible inmediato en la consola
        appendOutput("=== INICIANDO EJECUCIÓN ===");

        // Ejecutar el script con el intérprete Python configurado
        PythonExecutor.runScript(currentFile);

        if (parent.getMinecraft().player != null) {
            parent.getMinecraft().player.displayClientMessage(
                Component.literal("§e[Python] Ejecutando: " + currentFile.getName()),
                false
            );
        }
    }
    
    // Método helper para añadir salida directamente desde el panel
    private void appendOutput(String line) {
        PythonExecutor.appendOutputPublic(line);
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // TODO: Implementar drag para selección de texto
        return false;
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // TODO: Implementar release para selección de texto
        return false;
    }
}
