package com.pythonmc.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.NativeImage;
import com.pythonmc.mod.core.ProjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de texturas de píxeles para el Engine
 */
public class TextureEditorPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextureEditorPanel.class);
    
    private final EngineScreen parent;
    private final int x, y, width, height;
    
    // Estado del editor
    private BufferedImage currentTexture;
    private int textureSize = 16; // 16x16 por defecto
    private int pixelSize = 16; // Tamaño de cada píxel en pantalla
    private int selectedColor = 0xFF000000; // Negro por defecto
    private String currentFileName = "texture.png";
    private boolean hasUnsavedChanges = false;
    
    // Herramientas
    private enum Tool { PENCIL, FILL, ERASER, EYEDROPPER }
    private Tool currentTool = Tool.PENCIL;
    
    // Paleta de colores
    private final int[] colorPalette = {
        0xFF000000, // Negro
        0xFFFFFFFF, // Blanco
        0xFFFF0000, // Rojo
        0xFF00FF00, // Verde
        0xFF0000FF, // Azul
        0xFFFFFF00, // Amarillo
        0xFFFF00FF, // Magenta
        0xFF00FFFF, // Cian
        0xFF808080, // Gris
        0xFF800000, // Marrón
        0xFF808000, // Oliva
        0xFF008000, // Verde oscuro
        0xFF800080, // Púrpura
        0xFF008080, // Teal
        0xFF000080, // Azul marino
        0xFFC0C0C0  // Plata
    };
    
    // UI
    private List<Button> toolButtons = new ArrayList<>();
    private List<Button> sizeButtons = new ArrayList<>();
    private Button saveButton;
    private Button newButton;
    private Button clearButton;
    
    // Render
    private DynamicTexture dynamicTexture;
    private ResourceLocation textureResource;
    
    // Colores UI
    private static final int BG_COLOR = 0xFF2B2B2B;
    private static final int TOOLBAR_COLOR = 0xFF3C3F41;
    private static final int GRID_COLOR = 0xFF404040;
    private static final int SELECTED_TOOL_COLOR = 0xFF4A90E2;
    
    public TextureEditorPanel(EngineScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        initNewTexture();
        initButtons();
    }
    
    private void initNewTexture() {
        currentTexture = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
        clearTexture();
    }
    
    private void clearTexture() {
        for (int py = 0; py < textureSize; py++) {
            for (int px = 0; px < textureSize; px++) {
                currentTexture.setRGB(px, py, 0x00000000); // Transparente
            }
        }
        updateTexture();
    }
    
    private void initButtons() {
        int btnY = y + 5;
        int btnX = x + 5;
        int btnWidth = 60;
        int btnHeight = 20;
        int spacing = 5;
        
        // Botones de herramientas
        toolButtons.add(Button.builder(Component.literal("Lápiz"), btn -> setTool(Tool.PENCIL))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Relleno"), btn -> setTool(Tool.FILL))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Borrar"), btn -> setTool(Tool.ERASER))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Gotero"), btn -> setTool(Tool.EYEDROPPER))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        
        // Botones de tamaño
        btnX = x + 5;
        btnY += btnHeight + spacing;
        
        sizeButtons.add(Button.builder(Component.literal("16x16"), btn -> setTextureSize(16))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        btnX += btnWidth + spacing;
        
        sizeButtons.add(Button.builder(Component.literal("32x32"), btn -> setTextureSize(32))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        btnX += btnWidth + spacing;
        
        sizeButtons.add(Button.builder(Component.literal("64x64"), btn -> setTextureSize(64))
            .bounds(btnX, btnY, btnWidth, btnHeight).build());
        
        // Botones de acción
        btnX = x + width - 200;
        btnY = y + 5;
        
        newButton = Button.builder(Component.literal("Nuevo"), btn -> newTexture())
            .bounds(btnX, btnY, 60, btnHeight).build();
        btnX += btnWidth + spacing;
        
        saveButton = Button.builder(Component.literal("Guardar"), btn -> saveTexture())
            .bounds(btnX, btnY, 60, btnHeight).build();
        btnX += btnWidth + spacing;
        
        clearButton = Button.builder(Component.literal("Limpiar"), btn -> clearTexture())
            .bounds(btnX, btnY, 60, btnHeight).build();
    }
    
    private void setTool(Tool tool) {
        this.currentTool = tool;
    }
    
    private void setTextureSize(int size) {
        if (size != textureSize) {
            BufferedImage oldTexture = currentTexture;
            textureSize = size;
            currentTexture = new BufferedImage(textureSize, textureSize, BufferedImage.TYPE_INT_ARGB);
            clearTexture();
            
            // Copiar contenido antiguo si es posible
            if (oldTexture != null) {
                int minSize = Math.min(oldTexture.getWidth(), textureSize);
                for (int py = 0; py < minSize; py++) {
                    for (int px = 0; px < minSize; px++) {
                        currentTexture.setRGB(px, py, oldTexture.getRGB(px, py));
                    }
                }
            }
            
            updateTexture();
        }
    }
    
    private void newTexture() {
        if (hasUnsavedChanges) {
            // TODO: Mostrar diálogo de confirmación
        }
        initNewTexture();
        hasUnsavedChanges = false;
    }
    
    private void saveTexture() {
        try {
            File texturesDir = ProjectManager.getAssetsTexturesFolder();
            if (!texturesDir.exists()) {
                texturesDir.mkdirs();
            }
            
            File textureFile = new File(texturesDir, currentFileName);
            ImageIO.write(currentTexture, "PNG", textureFile);
            
            hasUnsavedChanges = false;
            LOGGER.info("Textura guardada: {}", textureFile.getAbsolutePath());
            
            if (parent.getMinecraft().player != null) {
                parent.getMinecraft().player.displayClientMessage(
                    Component.literal("§aTextura guardada: " + currentFileName),
                    true
                );
            }
        } catch (IOException e) {
            LOGGER.error("Error al guardar textura", e);
        }
    }
    
    private void updateTexture() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
        }
        
        try {
            // Convertir BufferedImage a NativeImage
            NativeImage nativeImage = new NativeImage(currentTexture.getWidth(), currentTexture.getHeight(), false);
            
            for (int y = 0; y < currentTexture.getHeight(); y++) {
                for (int x = 0; x < currentTexture.getWidth(); x++) {
                    int rgb = currentTexture.getRGB(x, y);
                    nativeImage.setPixelRGBA(x, y, rgb);
                }
            }
            
            dynamicTexture = new DynamicTexture(nativeImage);
            textureResource = Minecraft.getInstance().getTextureManager().register("pythonmc_texture", dynamicTexture);
        } catch (Exception e) {
            LOGGER.error("Error al actualizar textura", e);
        }
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        // Barra de herramientas
        graphics.fill(x, y, x + width, y + 60, TOOLBAR_COLOR);
        
        // Botones
        renderToolButtons(graphics, mouseX, mouseY, partialTick);
        renderSizeButtons(graphics, mouseX, mouseY, partialTick);
        
        newButton.render(graphics, mouseX, mouseY, partialTick);
        saveButton.render(graphics, mouseX, mouseY, partialTick);
        clearButton.render(graphics, mouseX, mouseY, partialTick);
        
        // Área del editor
        int editorX = x + 10;
        int editorY = y + 70;
        int editorSize = Math.min(width - 20, height - 120);
        
        renderTextureEditor(graphics, editorX, editorY, editorSize, mouseX, mouseY);
        
        // Paleta de colores
        renderColorPalette(graphics, editorX + editorSize + 10, editorY);
        
        // Información
        renderInfo(graphics, editorX, editorY + editorSize + 10);
    }
    
    private void renderToolButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < toolButtons.size(); i++) {
            Button btn = toolButtons.get(i);
            btn.render(graphics, mouseX, mouseY, partialTick);
            
            // Resaltar herramienta seleccionada
            Tool tool = Tool.values()[i];
            if (tool == currentTool) {
                graphics.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), SELECTED_TOOL_COLOR);
            }
        }
    }
    
    private void renderSizeButtons(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (Button btn : sizeButtons) {
            btn.render(graphics, mouseX, mouseY, partialTick);
        }
    }
    
    private void renderTextureEditor(GuiGraphics graphics, int editorX, int editorY, int editorSize, int mouseX, int mouseY) {
        // Fondo del editor
        graphics.fill(editorX, editorY, editorX + editorSize, editorY + editorSize, 0xFF1E1E1E);
        
        // Calcular tamaño de píxel
        pixelSize = editorSize / textureSize;
        
        // Renderizar textura
        if (textureResource != null) {
            RenderSystem.setShaderTexture(0, textureResource);
            graphics.blit(textureResource, editorX, editorY, 0, 0, editorSize, editorSize, textureSize, textureSize);
        }
        
        // Renderizar cuadrícula
        renderGrid(graphics, editorX, editorY, editorSize);
        
        // Cursor del píxel actual
        if (isMouseInEditor(mouseX, mouseY, editorX, editorY, editorSize)) {
            int pixelX = (mouseX - editorX) / pixelSize;
            int pixelY = (mouseY - editorY) / pixelSize;
            
            if (pixelX >= 0 && pixelX < textureSize && pixelY >= 0 && pixelY < textureSize) {
                graphics.fill(
                    editorX + pixelX * pixelSize,
                    editorY + pixelY * pixelSize,
                    editorX + (pixelX + 1) * pixelSize,
                    editorY + (pixelY + 1) * pixelSize,
                    0x80FFFFFF
                );
            }
        }
    }
    
    private void renderGrid(GuiGraphics graphics, int editorX, int editorY, int editorSize) {
        for (int i = 0; i <= textureSize; i++) {
            int pos = i * pixelSize;
            // Líneas verticales
            graphics.fill(editorX + pos, editorY, editorX + pos + 1, editorY + editorSize, GRID_COLOR);
            // Líneas horizontales
            graphics.fill(editorX, editorY + pos, editorX + editorSize, editorY + pos + 1, GRID_COLOR);
        }
    }
    
    private void renderColorPalette(GuiGraphics graphics, int paletteX, int paletteY) {
        graphics.drawString(parent.getMinecraft().font, "Paleta:", paletteX, paletteY - 15, 0xFFFFFFFF);
        
        for (int i = 0; i < colorPalette.length; i++) {
            int colorX = paletteX + (i % 4) * 25;
            int colorY = paletteY + (i / 4) * 25;
            
            graphics.fill(colorX, colorY, colorX + 20, colorY + 20, colorPalette[i]);
            
            // Resaltar color seleccionado
            if (colorPalette[i] == selectedColor) {
                graphics.fill(colorX - 2, colorY - 2, colorX + 22, colorY + 22, 0xFFFFFFFF);
                graphics.fill(colorX - 1, colorY - 1, colorX + 21, colorY + 21, 0xFF000000);
            }
        }
        
        // Color actual
        graphics.drawString(parent.getMinecraft().font, "Actual:", paletteX, paletteY + 120, 0xFFFFFFFF);
        graphics.fill(paletteX, paletteY + 130, paletteX + 40, paletteY + 170, selectedColor);
    }
    
    private void renderInfo(GuiGraphics graphics, int infoX, int infoY) {
        String info = String.format("Tamaño: %dx%d | Herramienta: %s | Archivo: %s%s", 
            textureSize, textureSize, 
            currentTool.name(), 
            currentFileName,
            hasUnsavedChanges ? " *" : "");
        
        graphics.drawString(parent.getMinecraft().font, info, infoX, infoY, 0xFFCCCCCC);
    }
    
    private boolean isMouseInEditor(int mouseX, int mouseY, int editorX, int editorY, int editorSize) {
        return mouseX >= editorX && mouseX < editorX + editorSize &&
               mouseY >= editorY && mouseY < editorY + editorSize;
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Botones de herramientas
        for (Button btn : toolButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        }
        
        // Botones de tamaño
        for (Button btn : sizeButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        }
        
        // Botones de acción
        if (newButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (saveButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (clearButton.mouseClicked(mouseX, mouseY, button)) return true;
        
        // Paleta de colores
        int paletteX = x + width - 150;
        int paletteY = y + 70;
        
        for (int i = 0; i < colorPalette.length; i++) {
            int colorX = paletteX + (i % 4) * 25;
            int colorY = paletteY + (i / 4) * 25;
            
            if (mouseX >= colorX && mouseX < colorX + 20 &&
                mouseY >= colorY && mouseY < colorY + 20) {
                selectedColor = colorPalette[i];
                return true;
            }
        }
        
        // Editor de textura
        int editorX = x + 10;
        int editorY = y + 70;
        int editorSize = Math.min(width - 20, height - 120);
        
        if (isMouseInEditor((int)mouseX, (int)mouseY, editorX, editorY, editorSize)) {
            int pixelX = ((int)mouseX - editorX) / pixelSize;
            int pixelY = ((int)mouseY - editorY) / pixelSize;
            
            if (pixelX >= 0 && pixelX < textureSize && pixelY >= 0 && pixelY < textureSize) {
                paintPixel(pixelX, pixelY, button == 1); // Botón derecho = eyedropper
                return true;
            }
        }
        
        return false;
    }
    
    private void paintPixel(int pixelX, int pixelY, boolean rightClick) {
        if (rightClick) {
            // Gotero - recoger color
            selectedColor = currentTexture.getRGB(pixelX, pixelY);
            currentTool = Tool.PENCIL;
        } else {
            switch (currentTool) {
                case PENCIL:
                    currentTexture.setRGB(pixelX, pixelY, selectedColor);
                    updateTexture();
                    hasUnsavedChanges = true;
                    break;
                case ERASER:
                    currentTexture.setRGB(pixelX, pixelY, 0x00000000); // Transparente
                    updateTexture();
                    hasUnsavedChanges = true;
                    break;
                case FILL:
                    floodFill(pixelX, pixelY, currentTexture.getRGB(pixelX, pixelY), selectedColor);
                    updateTexture();
                    hasUnsavedChanges = true;
                    break;
                case EYEDROPPER:
                    selectedColor = currentTexture.getRGB(pixelX, pixelY);
                    currentTool = Tool.PENCIL;
                    break;
            }
        }
    }
    
    private void floodFill(int x, int y, int targetColor, int replacementColor) {
        if (targetColor == replacementColor) return;
        if (x < 0 || x >= textureSize || y < 0 || y >= textureSize) return;
        if (currentTexture.getRGB(x, y) != targetColor) return;
        
        currentTexture.setRGB(x, y, replacementColor);
        
        floodFill(x + 1, y, targetColor, replacementColor);
        floodFill(x - 1, y, targetColor, replacementColor);
        floodFill(x, y + 1, targetColor, replacementColor);
        floodFill(x, y - 1, targetColor, replacementColor);
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int editorX = x + 10;
        int editorY = y + 70;
        int editorSize = Math.min(width - 20, height - 120);
        
        if (isMouseInEditor((int)mouseX, (int)mouseY, editorX, editorY, editorSize)) {
            int pixelX = ((int)mouseX - editorX) / pixelSize;
            int pixelY = ((int)mouseY - editorY) / pixelSize;
            
            if (pixelX >= 0 && pixelX < textureSize && pixelY >= 0 && pixelY < textureSize) {
                if (currentTool == Tool.PENCIL || currentTool == Tool.ERASER) {
                    paintPixel(pixelX, pixelY, false);
                }
            }
        }
        
        return false;
    }
    
    public void close() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
        }
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // TODO: Implementar drag para pintar continuo
        return false;
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // TODO: Implementar release para detener pintado
        return false;
    }
}
