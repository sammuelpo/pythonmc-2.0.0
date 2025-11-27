package com.pythonmc.mod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Barra de herramientas para construcción y edición
 * Selector de materiales, ancho, alto, herramientas y pestañas de editor
 */
public class BuildToolbar {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildToolbar.class);
    
    private final EngineScreen parent;
    private final int x, y, width, height;
    
    // Estado
    private BuildMaterial selectedMaterial = BuildMaterial.STONE;
    private int buildWidth = 1;
    private int buildHeight = 1;
    private BuildTool selectedTool = BuildTool.CUBE;
    
    // Pestañas de editor
    public enum EditorTab { SCRIPT, TEXTURE, MODEL }
    private EditorTab currentTab = EditorTab.SCRIPT;
    
    // UI
    private boolean materialMenuOpen = false;
    
    // Colores
    private static final int BG_COLOR = 0xFF3C3F41;
    private static final int BUTTON_COLOR = 0xFF4A4A4A;
    private static final int HOVER_COLOR = 0xFF5A5A5A;
    private static final int SELECTED_COLOR = 0xFF6A9955;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    
    public BuildToolbar(EngineScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        int currentX = x + 10;
        int btnY = y + 8;
        int btnHeight = height - 16;
        
        // Pestañas de editor
        currentX = renderEditorTabs(graphics, currentX, btnY, btnHeight, mouseX, mouseY);
        currentX += 20;
        
        // Solo mostrar herramientas de construcción si estamos en la pestaña de script
        if (currentTab == EditorTab.SCRIPT) {
            // Material selector
            currentX = renderMaterialSelector(graphics, currentX, btnY, btnHeight, mouseX, mouseY);
            
            currentX += 20;
            
            // Ancho
            currentX = renderSizeControl(graphics, "Ancho:", buildWidth, currentX, btnY, btnHeight, mouseX, mouseY, true);
            
            currentX += 10;
            
            // Alto
            currentX = renderSizeControl(graphics, "Alto:", buildHeight, currentX, btnY, btnHeight, mouseX, mouseY, false);
            
            currentX += 20;
            
            // Herramientas
            currentX = renderToolButtons(graphics, currentX, btnY, btnHeight, mouseX, mouseY);
        }
        
        // Menú de materiales
        if (materialMenuOpen) {
            renderMaterialMenu(graphics, x + 10, y + height);
        }
    }
    
    private int renderEditorTabs(GuiGraphics graphics, int x, int y, int height, int mouseX, int mouseY) {
        int tabWidth = 80;
        int spacing = 5;
        
        // Pestaña Script
        boolean scriptSelected = currentTab == EditorTab.SCRIPT;
        boolean scriptHovered = isHovering(mouseX, mouseY, x, y, tabWidth, height);
        int scriptColor = scriptSelected ? SELECTED_COLOR : (scriptHovered ? HOVER_COLOR : BUTTON_COLOR);
        graphics.fill(x, y, x + tabWidth, y + height, scriptColor);
        graphics.drawString(parent.getMinecraft().font, "Script", x + 20, y + 8, TEXT_COLOR);
        
        x += tabWidth + spacing;
        
        // Pestaña Textura
        boolean textureSelected = currentTab == EditorTab.TEXTURE;
        boolean textureHovered = isHovering(mouseX, mouseY, x, y, tabWidth, height);
        int textureColor = textureSelected ? SELECTED_COLOR : (textureHovered ? HOVER_COLOR : BUTTON_COLOR);
        graphics.fill(x, y, x + tabWidth, y + height, textureColor);
        graphics.drawString(parent.getMinecraft().font, "Textura", x + 15, y + 8, TEXT_COLOR);
        
        x += tabWidth + spacing;
        
        // Pestaña Modelo
        boolean modelSelected = currentTab == EditorTab.MODEL;
        boolean modelHovered = isHovering(mouseX, mouseY, x, y, tabWidth, height);
        int modelColor = modelSelected ? SELECTED_COLOR : (modelHovered ? HOVER_COLOR : BUTTON_COLOR);
        graphics.fill(x, y, x + tabWidth, y + height, modelColor);
        graphics.drawString(parent.getMinecraft().font, "Modelo", x + 15, y + 8, TEXT_COLOR);
        
        return x + tabWidth;
    }
    
    private int renderMaterialSelector(GuiGraphics graphics, int x, int y, int height, int mouseX, int mouseY) {
        int buttonWidth = 120;
        
        boolean isHovered = isHovering(mouseX, mouseY, x, y, buttonWidth, height);
        int color = isHovered ? HOVER_COLOR : BUTTON_COLOR;
        
        graphics.fill(x, y, x + buttonWidth, y + height, color);
        graphics.renderOutline(x, y, buttonWidth, height, 0xFF000000);
        
        // Icono del material
        String icon = selectedMaterial.icon;
        graphics.drawString(parent.getMinecraft().font, icon, x + 5, y + 8, TEXT_COLOR);
        
        // Nombre del material
        graphics.drawString(parent.getMinecraft().font, 
            selectedMaterial.displayName, x + 25, y + 8, TEXT_COLOR);
        
        // Flecha dropdown
        graphics.drawString(parent.getMinecraft().font, "▾", x + buttonWidth - 15, y + 8, TEXT_COLOR);
        
        return x + buttonWidth;
    }
    
    private int renderSizeControl(GuiGraphics graphics, String label, int value, 
                                  int x, int y, int height, int mouseX, int mouseY, boolean isWidth) {
        // Label
        graphics.drawString(parent.getMinecraft().font, label, x, y + 8, TEXT_COLOR);
        int labelWidth = parent.getMinecraft().font.width(label);
        x += labelWidth + 5;
        
        // Botón -
        int btnSize = 20;
        boolean minusHovered = isHovering(mouseX, mouseY, x, y, btnSize, height);
        graphics.fill(x, y, x + btnSize, y + height, minusHovered ? HOVER_COLOR : BUTTON_COLOR);
        graphics.drawString(parent.getMinecraft().font, "-", x + 7, y + 8, TEXT_COLOR);
        
        x += btnSize + 2;
        
        // Valor
        String valueStr = String.valueOf(value);
        int valueWidth = 30;
        graphics.fill(x, y, x + valueWidth, y + height, 0xFF2B2B2B);
        int textX = x + (valueWidth - parent.getMinecraft().font.width(valueStr)) / 2;
        graphics.drawString(parent.getMinecraft().font, valueStr, textX, y + 8, TEXT_COLOR);
        
        x += valueWidth + 2;
        
        // Botón +
        boolean plusHovered = isHovering(mouseX, mouseY, x, y, btnSize, height);
        graphics.fill(x, y, x + btnSize, y + height, plusHovered ? HOVER_COLOR : BUTTON_COLOR);
        graphics.drawString(parent.getMinecraft().font, "+", x + 6, y + 8, TEXT_COLOR);
        
        return x + btnSize;
    }
    
    private int renderToolButtons(GuiGraphics graphics, int x, int y, int height, int mouseX, int mouseY) {
        int btnSize = height;
        int spacing = 5;
        
        for (BuildTool tool : BuildTool.values()) {
            boolean isSelected = tool == selectedTool;
            boolean isHovered = isHovering(mouseX, mouseY, x, y, btnSize, height);
            
            int color = isSelected ? SELECTED_COLOR : (isHovered ? HOVER_COLOR : BUTTON_COLOR);
            graphics.fill(x, y, x + btnSize, y + height, color);
            graphics.renderOutline(x, y, btnSize, height, 0xFF000000);
            
            // Icono de la herramienta
            graphics.drawString(parent.getMinecraft().font, tool.icon, x + 8, y + 8, TEXT_COLOR);
            
            x += btnSize + spacing;
        }
        
        return x;
    }
    
    private void renderMaterialMenu(GuiGraphics graphics, int x, int y) {
        int menuWidth = 150;
        int menuHeight = BuildMaterial.values().length * 25 + 10;
        
        // Fondo
        graphics.fill(x, y, x + menuWidth, y + menuHeight, 0xFF2B2B2B);
        graphics.renderOutline(x, y, menuWidth, menuHeight, 0xFF000000);
        
        // Materiales
        int itemY = y + 5;
        for (BuildMaterial material : BuildMaterial.values()) {
            boolean isSelected = material == selectedMaterial;
            int color = isSelected ? SELECTED_COLOR : 0x00000000;
            
            graphics.fill(x + 5, itemY, x + menuWidth - 5, itemY + 20, color);
            
            // Icono
            graphics.drawString(parent.getMinecraft().font, material.icon, x + 10, itemY + 6, TEXT_COLOR);
            
            // Nombre
            graphics.drawString(parent.getMinecraft().font, 
                material.displayName, x + 30, itemY + 6, TEXT_COLOR);
            
            itemY += 25;
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        
        int btnY = y + 8;
        int btnHeight = height - 16;
        int currentX = x + 10;
        int tabWidth = 80;
        int spacing = 5;
        
        // Click en pestañas
        if (isHovering((int)mouseX, (int)mouseY, currentX, btnY, tabWidth, btnHeight)) {
            currentTab = EditorTab.SCRIPT;
            return true;
        }
        currentX += tabWidth + spacing;
        
        if (isHovering((int)mouseX, (int)mouseY, currentX, btnY, tabWidth, btnHeight)) {
            currentTab = EditorTab.TEXTURE;
            return true;
        }
        currentX += tabWidth + spacing;
        
        if (isHovering((int)mouseX, (int)mouseY, currentX, btnY, tabWidth, btnHeight)) {
            currentTab = EditorTab.MODEL;
            return true;
        }
        
        // Solo procesar clicks de construcción si estamos en la pestaña de script
        if (currentTab != EditorTab.SCRIPT) {
            materialMenuOpen = false;
            return false;
        }
        
        // Click en selector de material
        if (isHovering((int)mouseX, (int)mouseY, x + 10, btnY, 120, btnHeight)) {
            materialMenuOpen = !materialMenuOpen;
            return true;
        }
        
        // Click en menú de materiales
        if (materialMenuOpen) {
            int menuX = x + 10;
            int menuY = y + height + 5;
            int itemY = menuY;
            
            for (BuildMaterial material : BuildMaterial.values()) {
                if (isHovering((int)mouseX, (int)mouseY, menuX + 5, itemY, 140, 20)) {
                    selectedMaterial = material;
                    materialMenuOpen = false;
                    LOGGER.info("Material seleccionado: {}", material.displayName);
                    return true;
                }
                itemY += 25;
            }
            
            materialMenuOpen = false;
            return true;
        }
        
        // Click en controles de ancho
        int anchoX = x + 150;
        if (isHovering((int)mouseX, (int)mouseY, anchoX, btnY, 20, btnHeight)) {
            buildWidth = Math.max(1, buildWidth - 1);
            return true;
        }
        if (isHovering((int)mouseX, (int)mouseY, anchoX + 52, btnY, 20, btnHeight)) {
            buildWidth = Math.min(32, buildWidth + 1);
            return true;
        }
        
        // Click en controles de alto
        int altoX = anchoX + 90;
        if (isHovering((int)mouseX, (int)mouseY, altoX, btnY, 20, btnHeight)) {
            buildHeight = Math.max(1, buildHeight - 1);
            return true;
        }
        if (isHovering((int)mouseX, (int)mouseY, altoX + 52, btnY, 20, btnHeight)) {
            buildHeight = Math.min(32, buildHeight + 1);
            return true;
        }
        
        // Click en herramientas
        int toolX = altoX + 110;
        int btnSize = btnHeight;
        for (BuildTool tool : BuildTool.values()) {
            if (isHovering((int)mouseX, (int)mouseY, toolX, btnY, btnSize, btnHeight)) {
                selectedTool = tool;
                LOGGER.info("Herramienta seleccionada: {}", tool.name());
                return true;
            }
            toolX += btnSize + 5;
        }
        
        return false;
    }
    
    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    // ========== GETTERS ==========
    
    public BuildMaterial getSelectedMaterial() {
        return selectedMaterial;
    }
    
    public int getBuildWidth() {
        return buildWidth;
    }
    
    public int getBuildHeight() {
        return buildHeight;
    }
    
    public BuildTool getSelectedTool() {
        return selectedTool;
    }
    
    public EditorTab getCurrentTab() {
        return currentTab;
    }
    
    // ========== ENUMS ==========
    
    public enum BuildMaterial {
        STONE("🧱", "Stone", Blocks.STONE),
        DIRT("🟫", "Dirt", Blocks.DIRT),
        WOOD("🪵", "Wood", Blocks.OAK_PLANKS),
        GLASS("⬜", "Glass", Blocks.GLASS),
        BRICK("🧱", "Brick", Blocks.BRICKS),
        CONCRETE("⬜", "Concrete", Blocks.WHITE_CONCRETE),
        METAL("⚙", "Iron", Blocks.IRON_BLOCK),
        GOLD("🟨", "Gold", Blocks.GOLD_BLOCK);
        
        final String icon;
        final String displayName;
        final net.minecraft.world.level.block.Block block;
        
        BuildMaterial(String icon, String displayName, net.minecraft.world.level.block.Block block) {
            this.icon = icon;
            this.displayName = displayName;
            this.block = block;
        }
    }
    
    public enum BuildTool {
        CUBE("■"),
        SPHERE("●"),
        CYLINDER("▮"),
        FREE("✎");
        
        final String icon;
        
        BuildTool(String icon) {
            this.icon = icon;
        }
    }
}
