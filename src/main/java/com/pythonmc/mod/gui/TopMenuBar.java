package com.pythonmc.mod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Barra de menú superior con opciones File, Edit, View, etc.
 */
public class TopMenuBar {
    private final EngineScreen parent;
    private final int x, y, width, height;
    
    // Menús
    private boolean fileMenuOpen = false;
    private boolean editMenuOpen = false;
    private boolean viewMenuOpen = false;
    private boolean helpMenuOpen = false;
    
    // Colores
    private static final int BG_COLOR = 0xFF2B2B2B;
    private static final int HOVER_COLOR = 0xFF3C3F41;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MENU_BG = 0xFF3C3F41;
    
    public TopMenuBar(EngineScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo de la barra
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        int menuX = x + 5;
        int menuY = y + 5;
        
        // Menú File
        menuX = drawMenuItem(graphics, "File", menuX, menuY, mouseX, mouseY, fileMenuOpen);
        
        // Menú Edit
        menuX = drawMenuItem(graphics, "Edit", menuX, menuY, mouseX, mouseY, editMenuOpen);
        
        // Menú View
        menuX = drawMenuItem(graphics, "View", menuX, menuY, mouseX, mouseY, viewMenuOpen);
        
        // Menú Help
        menuX = drawMenuItem(graphics, "Help", menuX, menuY, mouseX, mouseY, helpMenuOpen);
        
        // Renderizar menús desplegables
        if (fileMenuOpen) {
            renderFileMenu(graphics, x + 5, y + height);
        }
        
        if (editMenuOpen) {
            renderEditMenu(graphics, x + 50, y + height);
        }
        
        if (viewMenuOpen) {
            renderViewMenu(graphics, x + 95, y + height);
        }
        
        if (helpMenuOpen) {
            renderHelpMenu(graphics, x + 140, y + height);
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        
        int menuY = y + 5;
        
        // File
        if (isHovering((int)mouseX, (int)mouseY, x + 5, menuY, 40, 10)) {
            fileMenuOpen = !fileMenuOpen;
            editMenuOpen = false;
            viewMenuOpen = false;
            helpMenuOpen = false;
            return true;
        }
        
        // Edit
        if (isHovering((int)mouseX, (int)mouseY, x + 50, menuY, 40, 10)) {
            editMenuOpen = !editMenuOpen;
            fileMenuOpen = false;
            viewMenuOpen = false;
            helpMenuOpen = false;
            return true;
        }
        
        // View
        if (isHovering((int)mouseX, (int)mouseY, x + 95, menuY, 40, 10)) {
            viewMenuOpen = !viewMenuOpen;
            fileMenuOpen = false;
            editMenuOpen = false;
            helpMenuOpen = false;
            return true;
        }
        
        // Help
        if (isHovering((int)mouseX, (int)mouseY, x + 140, menuY, 40, 10)) {
            helpMenuOpen = !helpMenuOpen;
            fileMenuOpen = false;
            editMenuOpen = false;
            viewMenuOpen = false;
            return true;
        }
        
        // Clicks en menús desplegables
        if (editMenuOpen && handleEditMenuClick((int)mouseX, (int)mouseY)) {
            return true;
        }
        
        // Cerrar menús si se hace click fuera
        if (fileMenuOpen || editMenuOpen || viewMenuOpen || helpMenuOpen) {
            fileMenuOpen = false;
            editMenuOpen = false;
            viewMenuOpen = false;
            helpMenuOpen = false;
        }
        
        return false;
    }
    
    private int drawMenuItem(GuiGraphics graphics, String text, int x, int y, 
                            int mouseX, int mouseY, boolean isOpen) {
        int textWidth = graphics.guiWidth();
        int itemWidth = 40;
        
        // Hover o abierto
        if (isOpen || isHovering(mouseX, mouseY, x, y, itemWidth, 10)) {
            graphics.fill(x - 2, y - 2, x + itemWidth, y + 12, HOVER_COLOR);
        }
        
        graphics.drawString(parent.getMinecraft().font, text, x, y, TEXT_COLOR);
        
        return x + itemWidth + 5;
    }
    
    private void renderFileMenu(GuiGraphics graphics, int x, int y) {
        int menuWidth = 150;
        int menuHeight = 80;
        
        // Fondo
        graphics.fill(x, y, x + menuWidth, y + menuHeight, MENU_BG);
        graphics.renderOutline(x, y, menuWidth, menuHeight, 0xFF000000);
        
        // Items
        int itemY = y + 5;
        graphics.drawString(parent.getMinecraft().font, "New Script", x + 5, itemY, TEXT_COLOR);
        itemY += 15;
        graphics.drawString(parent.getMinecraft().font, "New Folder", x + 5, itemY, TEXT_COLOR);
        itemY += 15;
        graphics.drawString(parent.getMinecraft().font, "Save", x + 5, itemY, TEXT_COLOR);
        itemY += 15;
        graphics.drawString(parent.getMinecraft().font, "Close", x + 5, itemY, TEXT_COLOR);
    }
    
    private void renderEditMenu(GuiGraphics graphics, int x, int y) {
        int menuWidth = 180;
        int menuHeight = 50;
        
        // Fondo
        graphics.fill(x, y, x + menuWidth, y + menuHeight, MENU_BG);
        graphics.renderOutline(x, y, menuWidth, menuHeight, 0xFF000000);
        
        // Items
        int itemY = y + 5;
        graphics.drawString(parent.getMinecraft().font, "Preferences", x + 5, itemY, TEXT_COLOR);
        itemY += 20;
        
        // OPCIÓN IMPORTANTE: Modo Engine OFF
        graphics.drawString(parent.getMinecraft().font, "⚠ Modo Engine OFF", x + 5, itemY, 0xFFFF5555);
    }
    
    private void renderViewMenu(GuiGraphics graphics, int x, int y) {
        int menuWidth = 150;
        int menuHeight = 65;
        
        graphics.fill(x, y, x + menuWidth, y + menuHeight, MENU_BG);
        graphics.renderOutline(x, y, menuWidth, menuHeight, 0xFF000000);
        
        int itemY = y + 5;
        graphics.drawString(parent.getMinecraft().font, "File Explorer", x + 5, itemY, TEXT_COLOR);
        itemY += 15;
        graphics.drawString(parent.getMinecraft().font, "Node Inspector", x + 5, itemY, TEXT_COLOR);
        itemY += 15;
        graphics.drawString(parent.getMinecraft().font, "Build Toolbar", x + 5, itemY, TEXT_COLOR);
    }
    
    private void renderHelpMenu(GuiGraphics graphics, int x, int y) {
        int menuWidth = 150;
        int menuHeight = 50;
        
        graphics.fill(x, y, x + menuWidth, y + menuHeight, MENU_BG);
        graphics.renderOutline(x, y, menuWidth, menuHeight, 0xFF000000);
        
        int itemY = y + 5;
        graphics.drawString(parent.getMinecraft().font, "Documentation", x + 5, itemY, TEXT_COLOR);
        itemY += 15;
        graphics.drawString(parent.getMinecraft().font, "About PythonMC", x + 5, itemY, TEXT_COLOR);
    }
    
    private boolean handleEditMenuClick(int mouseX, int mouseY) {
        int menuX = x + 50;
        int menuY = y + height + 25;
        int menuWidth = 180;
        
        // Click en "Modo Engine OFF"
        if (isHovering(mouseX, mouseY, menuX + 5, menuY, menuWidth - 10, 15)) {
            parent.disableEngineMode();
            editMenuOpen = false;
            return true;
        }
        
        return false;
    }
    
    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}