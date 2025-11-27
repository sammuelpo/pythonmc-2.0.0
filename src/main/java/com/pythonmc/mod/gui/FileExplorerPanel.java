package com.pythonmc.mod.gui;

import com.pythonmc.mod.core.ProjectManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel del explorador de archivos del proyecto
 * Ubicación: saves/world_name/filesproject/
 */
public class FileExplorerPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileExplorerPanel.class);
    
    private final EngineScreen parent;
    private final Level world;
    private final int x, y, width, height;
    
    // Estado
    private File currentFolder;
    private List<FileEntry> entries;
    private int scrollOffset = 0;
    private FileEntry selectedEntry = null;
    
    // Botones
    private Button createScriptBtn;
    private Button createFolderBtn;
    private Button createNodeBtn;
    
    // Colores
    private static final int BG_COLOR = 0xFF252525;
    private static final int HEADER_COLOR = 0xFF2B2B2B;
    private static final int SELECTED_COLOR = 0xFF3A3A3A;
    private static final int HOVER_COLOR = 0xFF2F2F2F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    
    public FileExplorerPanel(EngineScreen parent, Level world, int x, int y, int width, int height) {
        this.parent = parent;
        this.world = world;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.entries = new ArrayList<>();
        
        // Cargar carpeta inicial
        this.currentFolder = ProjectManager.getProjectRoot(world);
        refresh();
        
        initButtons();
    }
    
    private void initButtons() {
        int btnWidth = width - 10;
        int btnHeight = 20;
        int btnX = x + 5;
        int btnY = y + height - 75;
        
        createScriptBtn = Button.builder(
            Component.literal("Crear Script"),
            btn -> createScript()
        ).bounds(btnX, btnY, btnWidth, btnHeight).build();
        
        createFolderBtn = Button.builder(
            Component.literal("Crear Carpeta"),
            btn -> createFolder()
        ).bounds(btnX, btnY + 25, btnWidth, btnHeight).build();
        
        createNodeBtn = Button.builder(
            Component.literal("Crear Nodo ▾"),
            btn -> showNodeMenu()
        ).bounds(btnX, btnY + 50, btnWidth, btnHeight).build();
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        // Header
        graphics.fill(x, y, x + width, y + 25, HEADER_COLOR);
        graphics.drawString(parent.getMinecraft().font, "File Explorer", x + 5, y + 8, TEXT_COLOR);
        
        // Ruta actual
        String path = getRelativePath();
        graphics.drawString(parent.getMinecraft().font, path, x + 5, y + 30, 0xFFAAAAAA);
        
        // Entradas de archivos/carpetas
        int entryY = y + 50;
        int visibleHeight = height - 130;
        int entryHeight = 20;
        
        for (int i = scrollOffset; i < entries.size(); i++) {
            if (entryY + entryHeight > y + 50 + visibleHeight) break;
            
            FileEntry entry = entries.get(i);
            boolean isSelected = entry == selectedEntry;
            boolean isHovered = isHovering(mouseX, mouseY, x, entryY, width, entryHeight);
            
            // Fondo
            if (isSelected) {
                graphics.fill(x, entryY, x + width, entryY + entryHeight, SELECTED_COLOR);
            } else if (isHovered) {
                graphics.fill(x, entryY, x + width, entryY + entryHeight, HOVER_COLOR);
            }
            
            // Icono
            String icon = entry.isDirectory ? "📁" : "📄";
            graphics.drawString(parent.getMinecraft().font, icon, x + 5, entryY + 6, TEXT_COLOR);
            
            // Nombre
            String name = entry.file.getName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            graphics.drawString(parent.getMinecraft().font, name, x + 25, entryY + 6, TEXT_COLOR);
            
            entryY += entryHeight;
        }
        
        // Botones
        createScriptBtn.render(graphics, mouseX, mouseY, partialTick);
        createFolderBtn.render(graphics, mouseX, mouseY, partialTick);
        createNodeBtn.render(graphics, mouseX, mouseY, partialTick);
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Botones
        if (createScriptBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (createFolderBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (createNodeBtn.mouseClicked(mouseX, mouseY, button)) return true;
        
        // Click en entradas
        if (button == 0) {
            int entryY = y + 50;
            int entryHeight = 20;
            
            for (int i = scrollOffset; i < entries.size(); i++) {
                if (entryY + entryHeight > y + height - 130) break;
                
                if (isHovering((int)mouseX, (int)mouseY, x, entryY, width, entryHeight)) {
                    FileEntry entry = entries.get(i);
                    
                    if (selectedEntry == entry) {
                        // Doble click: abrir
                        openEntry(entry);
                    } else {
                        // Single click: seleccionar
                        selectedEntry = entry;
                    }
                    return true;
                }
                
                entryY += entryHeight;
            }
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isHovering((int)mouseX, (int)mouseY, x, y, width, height)) {
            return false;
        }
        
        scrollOffset -= (int) delta;
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, entries.size() - 10)));
        
        return true;
    }
    
    public void refresh() {
        entries.clear();
        
        if (currentFolder == null || !currentFolder.exists()) {
            LOGGER.warn("Carpeta de proyecto no existe");
            return;
        }
        
        File[] files = currentFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                entries.add(new FileEntry(file));
            }
        }
        
        LOGGER.debug("Explorer actualizado: {} entradas", entries.size());
    }
    
    private void createScript() {
        parent.getMinecraft().setScreen(new InputDialog(
            parent,
            "Crear Script",
            "Nombre del script:",
            "nuevo_script",
            name -> {
                if (ProjectManager.createScript(world, name)) {
                    refresh();
                    LOGGER.info("Script creado: {}", name);
                }
            }
        ));
    }
    
    private void createFolder() {
        parent.getMinecraft().setScreen(new InputDialog(
            parent,
            "Crear Carpeta",
            "Nombre de la carpeta:",
            "nueva_carpeta",
            name -> {
                File newFolder = new File(currentFolder, name);
                if (newFolder.mkdir()) {
                    refresh();
                    LOGGER.info("Carpeta creada: {}", name);
                }
            }
        ));
    }
    
    private void showNodeMenu() {
        parent.getMinecraft().setScreen(new NodeCreationDialog(parent, world));
    }
    
    private void openEntry(FileEntry entry) {
        if (entry.isDirectory) {
            currentFolder = entry.file;
            refresh();
        } else if (entry.file.getName().endsWith(".py")) {
            parent.openScript(entry.file.getAbsolutePath());
        }
    }
    
    private String getRelativePath() {
        if (currentFolder == null) return "/";
        
        File projectRoot = ProjectManager.getProjectRoot(world);
        String fullPath = currentFolder.getAbsolutePath();
        String rootPath = projectRoot.getAbsolutePath();
        
        if (fullPath.startsWith(rootPath)) {
            String relative = fullPath.substring(rootPath.length());
            return relative.isEmpty() ? "/" : relative.replace("\\", "/");
        }
        
        return "/";
    }
    
    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    private static class FileEntry {
        final File file;
        final boolean isDirectory;
        
        FileEntry(File file) {
            this.file = file;
            this.isDirectory = file.isDirectory();
        }
    }
}