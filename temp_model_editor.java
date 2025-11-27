package com.pythonmc.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.pythonmc.mod.core.ProjectManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de modelos 3D básico para el Engine
 * Permite crear y editar modelos con cubos
 */
public class ModelEditorPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorPanel.class);
    
    private final EngineScreen parent;
    private final int x, y, width, height;
    
    // Estado del editor
    private List<ModelCube> cubes = new ArrayList<>();
    private ModelCube selectedCube = null;
    private String currentFileName = "model.json";
    private boolean hasUnsavedChanges = false;
    
    // Cámara 3D
    private float cameraRotationX = 30f;
    private float cameraRotationY = 45f;
    private float cameraZoom = 5f;
    private boolean isDragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    
    // Herramientas
    private enum Tool { SELECT, ADD_CUBE, DELETE, MOVE, SCALE, ROTATE }
    private Tool currentTool = Tool.SELECT;
    
    // Grid
    private boolean showGrid = true;
    private int gridSize = 20;
    
    // UI
    private List<Button> toolButtons = new ArrayList<>();
    private Button saveButton;
    private Button newButton;
    private Button clearButton;
    private Button gridButton;
    
    // Colores UI
    private static final int BG_COLOR = 0xFF2B2B2B;
    private static final int TOOLBAR_COLOR = 0xFF3C3F41;
    private static final int GRID_COLOR = 0xFF404040;
    private static final int SELECTED_COLOR = 0xFF4A90E2;
    private static final int CUBE_COLOR = 0xFF6A9955;
    
    public ModelEditorPanel(EngineScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        initButtons();
        createDefaultCube();
    }
    
    private void createDefaultCube() {
        cubes.add(new ModelCube(0, 0, 0, 1, 1, 1));
        selectedCube = cubes.get(0);
    }
    
    private void initButtons() {
        int btnY = y + 5;
        int btnX = x + 5;
        int btnWidth = 70;
        int btnHeight = 20;
        int spacing = 5;
        
        // Botones de herramientas
        toolButtons.add(Button.builder(Component.literal("Seleccionar"), btn -> setTool(Tool.SELECT))
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Añadir Cubo"), btn -> setTool(Tool.ADD_CUBE))
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Mover"), btn -> setTool(Tool.MOVE))
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Escalar"), btn -> setTool(Tool.SCALE))
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Rotar"), btn -> setTool(Tool.ROTATE))
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        toolButtons.add(Button.builder(Component.literal("Eliminar"), btn -> setTool(Tool.DELETE))
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        
        // Botones de acción
        btnX = x + width - 250;
        
        newButton = Button.builder(Component.literal("Nuevo"), btn -> newModel())
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        saveButton = Button.builder(Component.literal("Guardar"), btn -> saveModel())
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        clearButton = Button.builder(Component.literal("Limpiar"), btn -> clearModel())
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
        btnX += btnWidth + spacing;
        
        gridButton = Button.builder(Component.literal("Grid: ON"), btn -> toggleGrid())
            .bounds(btnX, btnY, btnWidth, btnHeight).build();
    }
    
    private void setTool(Tool tool) {
        this.currentTool = tool;
    }
    
    private void toggleGrid() {
        showGrid = !showGrid;
        gridButton.setMessage(Component.literal("Grid: " + (showGrid ? "ON" : "OFF")));
    }
    
    private void newModel() {
        if (hasUnsavedChanges) {
            // TODO: Mostrar diálogo de confirmación
        }
        cubes.clear();
        createDefaultCube();
        hasUnsavedChanges = false;
    }
    
    private void clearModel() {
        cubes.clear();
        selectedCube = null;
        hasUnsavedChanges = true;
    }
    
    private void saveModel() {
        try {
            File modelsDir = ProjectManager.getAssetsModelsFolder();
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            
            File modelFile = new File(modelsDir, currentFileName);
            saveModelToFile(modelFile);
            
            hasUnsavedChanges = false;
            LOGGER.info("Modelo guardado: {}", modelFile.getAbsolutePath());
            
            if (parent.getMinecraft().player != null) {
                parent.getMinecraft().player.displayClientMessage(
                    Component.literal("§aModelo guardado: " + currentFileName),
                    true
                );
            }
        } catch (IOException e) {
            LOGGER.error("Error al guardar modelo", e);
        }
    }
    
    private void saveModelToFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("{");
            writer.println("  \"format_version\": 1,");
            writer.println("  \"cubes\": [");
            
            for (int i = 0; i < cubes.size(); i++) {
                ModelCube cube = cubes.get(i);
                writer.println("    {");
                writer.println("      \"position\": [" + cube.x + ", " + cube.y + ", " + cube.z + "],");
                writer.println("      \"size\": [" + cube.width + ", " + cube.height + ", " + cube.depth + "],");
                writer.println("      \"color\": " + cube.color + ",");
                writer.println("      \"texture\": \"" + cube.texture + "\"");
                writer.print("    }");
                if (i < cubes.size() - 1) writer.print(",");
                writer.println();
            }
            
            writer.println("  ]");
            writer.println("}");
        }
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        // Barra de herramientas
        graphics.fill(x, y, x + width, y + 60, TOOLBAR_COLOR);
        
        // Botones
        renderToolButtons(graphics, mouseX, mouseY, partialTick);
        
        newButton.render(graphics, mouseX, mouseY, partialTick);
        saveButton.render(graphics, mouseX, mouseY, partialTick);
        clearButton.render(graphics, mouseX, mouseY, partialTick);
        gridButton.render(graphics, mouseX, mouseY, partialTick);
        
        // Área 3D
        int editorX = x + 10;
        int editorY = y + 70;
        int editorSize = Math.min(width - 20, height - 120);
        
        render3DViewport(graphics, editorX, editorY, editorSize, mouseX, mouseY);
        
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
                graphics.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), SELECTED_COLOR);
            }
        }
    }
    
    private void render3DViewport(GuiGraphics graphics, int viewportX, int viewportY, int viewportSize, int mouseX, int mouseY) {
        // Fondo del viewport
        graphics.fill(viewportX, viewportY, viewportX + viewportSize, viewportY + viewportSize, 0xFF1E1E1E);
        
        // Configurar matriz de proyección
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        
        // Centrar en el viewport
        poseStack.translate(viewportX + viewportSize/2f, viewportY + viewportSize/2f, 0);
        
        // Aplicar transformaciones de cámara
        poseStack.scale(cameraZoom, cameraZoom, cameraZoom);
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraRotationX));
        poseStack.mulPose(Axis.YP.rotationDegrees(cameraRotationY));
        
        // Renderizar grid
        if (showGrid) {
            renderGrid(graphics, poseStack, gridSize);
        }
        
        // Renderizar cubos
        renderCubes(graphics, poseStack);
        
        // Renderizar cubo seleccionado
        if (selectedCube != null) {
            renderSelectionBox(graphics, poseStack, selectedCube);
        }
        
        poseStack.popPose();
        
        // Cursor 3D
        if (isMouseInViewport(mouseX, mouseY, viewportX, viewportY, viewportSize)) {
            render3DCursor(graphics, viewportX, viewportY, viewportSize, mouseX, mouseY);
        }
    }
    
    private void renderGrid(GuiGraphics graphics, PoseStack poseStack, int size) {
        MultiBufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Grid en el plano Y=0
        for (int i = -size; i <= size; i++) {
            // Líneas X
            consumer.vertex(matrix, i, 0, -size).color(GRID_COLOR).endVertex();
            consumer.vertex(matrix, i, 0, size).color(GRID_COLOR).endVertex();
            
            // Líneas Z
            consumer.vertex(matrix, -size, 0, i).color(GRID_COLOR).endVertex();
            consumer.vertex(matrix, size, 0, i).color(GRID_COLOR).endVertex();
        }
        
        bufferSource.endBatch();
    }
    
    private void renderCubes(GuiGraphics graphics, PoseStack poseStack) {
        MultiBufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        
        for (ModelCube cube : cubes) {
            renderCube(graphics, poseStack, consumer, cube);
        }
        
        bufferSource.endBatch();
    }
    
    private void renderCube(GuiGraphics graphics, PoseStack poseStack, VertexConsumer consumer, ModelCube cube) {
        poseStack.pushPose();
        
        // Posicionar el cubo
        poseStack.translate(cube.x + cube.width/2f, cube.y + cube.height/2f, cube.z + cube.depth/2f);
        
        // Escalar el cubo
        poseStack.scale(cube.width, cube.height, cube.depth);
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Renderizar las 6 caras del cubo
        // Front face
        consumer.vertex(matrix, -0.5f, -0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0.5f).color(cube.color).endVertex();
        
        // Back face
        consumer.vertex(matrix, -0.5f, -0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, -0.5f).color(cube.color).endVertex();
        
        // Top face
        consumer.vertex(matrix, -0.5f, 0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, -0.5f).color(cube.color).endVertex();
        
        // Bottom face
        consumer.vertex(matrix, -0.5f, -0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, -0.5f, 0.5f).color(cube.color).endVertex();
        
        // Right face
        consumer.vertex(matrix, 0.5f, -0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0.5f).color(cube.color).endVertex();
        
        // Left face
        consumer.vertex(matrix, -0.5f, -0.5f, -0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, -0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0.5f).color(cube.color).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, -0.5f).color(cube.color).endVertex();
        
        poseStack.popPose();
    }
    
    private void renderSelectionBox(GuiGraphics graphics, PoseStack poseStack, ModelCube cube) {
        MultiBufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        
        poseStack.pushPose();
        poseStack.translate(cube.x + cube.width/2f, cube.y + cube.height/2f, cube.z + cube.depth/2f);
        poseStack.scale(cube.width + 0.1f, cube.height + 0.1f, cube.depth + 0.1f);
        
        Matrix4f matrix = poseStack.last().pose();
        
        // Renderizar borde de selección
        float s = 0.5f;
        int color = 0xFFFFFF00; // Amarillo
        
        // Bottom edges
        consumer.vertex(matrix, -s, -s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, -s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, -s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, -s, s).color(color).endVertex();
        consumer.vertex(matrix, s, -s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, -s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, -s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, -s, -s).color(color).endVertex();
        
        // Top edges
        consumer.vertex(matrix, -s, s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, s, s).color(color).endVertex();
        consumer.vertex(matrix, s, s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, s, -s).color(color).endVertex();
        
        // Vertical edges
        consumer.vertex(matrix, -s, -s, -s).color(color).endVertex();
        consumer.vertex(matrix, -s, s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, -s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, s, -s).color(color).endVertex();
        consumer.vertex(matrix, s, -s, s).color(color).endVertex();
        consumer.vertex(matrix, s, s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, -s, s).color(color).endVertex();
        consumer.vertex(matrix, -s, s, s).color(color).endVertex();
        
        bufferSource.endBatch();
        poseStack.popPose();
    }
    
    private void render3DCursor(GuiGraphics graphics, int viewportX, int viewportY, int viewportSize, int mouseX, int mouseY) {
        // TODO: Implementar cursor 3D que muestra dónde se colocará un nuevo cubo
    }
    
    private void renderInfo(GuiGraphics graphics, int infoX, int infoY) {
        String info = String.format("Cubos: %d | Herramienta: %s | Archivo: %s%s", 
            cubes.size(), 
            currentTool.name(), 
            currentFileName,
            hasUnsavedChanges ? " *" : "");
        
        graphics.drawString(parent.getMinecraft().font, info, infoX, infoY, 0xFFCCCCCC);
        
        if (selectedCube != null) {
            String cubeInfo = String.format("Cubo seleccionado: pos(%.1f,%.1f,%.1f) size(%.1f,%.1f,%.1f)", 
                selectedCube.x, selectedCube.y, selectedCube.z,
                selectedCube.width, selectedCube.height, selectedCube.depth);
            graphics.drawString(parent.getMinecraft().font, cubeInfo, infoX, infoY + 15, 0xFFAAAAAA);
        }
    }
    
    private boolean isMouseInViewport(int mouseX, int mouseY, int viewportX, int viewportY, int viewportSize) {
        return mouseX >= viewportX && mouseX < viewportX + viewportSize &&
               mouseY >= viewportY && mouseY < viewportY + viewportSize;
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Botones de herramientas
        for (Button btn : toolButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) return true;
        }
        
        // Botones de acción
        if (newButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (saveButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (clearButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (gridButton.mouseClicked(mouseX, mouseY, button)) return true;
        
        // Viewport 3D
        int viewportX = x + 10;
        int viewportY = y + 70;
        int viewportSize = Math.min(width - 20, height - 120);
        
        if (isMouseInViewport((int)mouseX, (int)mouseY, viewportX, viewportY, viewportSize)) {
            handleViewportClick((int)mouseX, (int)mouseY, button, viewportX, viewportY, viewportSize);
            return true;
        }
        
        return false;
    }
    
    private void handleViewportClick(int mouseX, int mouseY, int button, int viewportX, int viewportY, int viewportSize) {
        if (button == 1) { // Botón derecho - rotar cámara
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (button == 0) { // Botón izquierdo - herramientas
            switch (currentTool) {
                case SELECT:
                    selectCubeAt(mouseX, mouseY, viewportX, viewportY, viewportSize);
                    break;
                case ADD_CUBE:
                    addCubeAt(mouseX, mouseY, viewportX, viewportY, viewportSize);
                    break;
                case DELETE:
                    deleteCubeAt(mouseX, mouseY, viewportX, viewportY, viewportSize);
                    break;
                case MOVE:
                case SCALE:
                case ROTATE:
                    // TODO: Implementar transformaciones
                    break;
            }
        }
    }
    
    private void selectCubeAt(int mouseX, int mouseY, int viewportX, int viewportY, int viewportSize) {
        // TODO: Implementar selección por raycast
        // Por ahora, seleccionar el primer cubo
        if (!cubes.isEmpty()) {
            selectedCube = cubes.get(0);
        }
    }
    
    private void addCubeAt(int mouseX, int mouseY, int viewportX, int viewportY, int viewportSize) {
        // TODO: Implementar placement en 3D
        // Por ahora, añadir en origen
        ModelCube newCube = new ModelCube(0, 1, 0, 1, 1, 1);
        cubes.add(newCube);
        selectedCube = newCube;
        hasUnsavedChanges = true;
    }
    
    private void deleteCubeAt(int mouseX, int mouseY, int viewportX, int viewportY, int viewportSize) {
        if (selectedCube != null) {
            cubes.remove(selectedCube);
            selectedCube = cubes.isEmpty() ? null : cubes.get(0);
            hasUnsavedChanges = true;
        }
    }
    
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 1) {
            // Rotar cámara
            cameraRotationY += deltaX * 0.5f;
            cameraRotationX += deltaY * 0.5f;
            
            // Limitar rotación X
            cameraRotationX = Math.max(-89f, Math.min(89f, cameraRotationX));
            
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }
    
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) {
            isDragging = false;
            return true;
        }
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int viewportX = x + 10;
        int viewportY = y + 70;
        int viewportSize = Math.min(width - 20, height - 120);
        
        if (isMouseInViewport((int)mouseX, (int)mouseY, viewportX, viewportY, viewportSize)) {
            // Zoom
            cameraZoom -= delta * 0.5f;
            cameraZoom = Math.max(1f, Math.min(20f, cameraZoom));
            return true;
        }
        return false;
    }
    
    // Clase interna para representar un cubo
    public static class ModelCube {
        public float x, y, z;
        public float width, height, depth;
        public int color = 0xFF6A9955;
        public String texture = "";
        
        public ModelCube(float x, float y, float z, float width, float height, float depth) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
        
        public AABB getBounds() {
            return new AABB(x, y, z, x + width, y + height, z + depth);
        }
    }
}
