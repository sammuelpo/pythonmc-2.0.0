package com.pythonmc.mod.gui;

import com.pythonmc.mod.core.EngineMode;
import com.pythonmc.mod.nodes.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Diálogo de input genérico
 */
public class InputDialog extends Screen {
    private final Screen parent;
    private final String prompt;
    private final String defaultValue;
    private final Consumer<String> onConfirm;
    
    private EditBox inputField;
    private Button confirmButton;
    private Button cancelButton;
    
    public InputDialog(Screen parent, String title, String prompt, 
                      String defaultValue, Consumer<String> onConfirm) {
        super(Component.literal(title));
        this.parent = parent;
        this.prompt = prompt;
        this.defaultValue = defaultValue;
        this.onConfirm = onConfirm;
    }
    
    @Override
    protected void init() {
        int dialogWidth = 300;
        int dialogHeight = 120;
        int dialogX = (width - dialogWidth) / 2;
        int dialogY = (height - dialogHeight) / 2;
        
        // Campo de texto
        inputField = new EditBox(font, dialogX + 20, dialogY + 50, dialogWidth - 40, 20, Component.literal(""));
        inputField.setValue(defaultValue);
        inputField.setMaxLength(50);
        addRenderableWidget(inputField);
        setInitialFocus(inputField);
        
        // Botones
        confirmButton = Button.builder(
            Component.literal("Crear"),
            btn -> {
                if (!inputField.getValue().isEmpty()) {
                    onConfirm.accept(inputField.getValue());
                    minecraft.setScreen(parent);
                }
            }
        ).bounds(dialogX + 20, dialogY + 80, 120, 20).build();
        
        cancelButton = Button.builder(
            Component.literal("Cancelar"),
            btn -> minecraft.setScreen(parent)
        ).bounds(dialogX + 160, dialogY + 80, 120, 20).build();
        
        addRenderableWidget(confirmButton);
        addRenderableWidget(cancelButton);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        
        int dialogWidth = 300;
        int dialogHeight = 120;
        int dialogX = (width - dialogWidth) / 2;
        int dialogY = (height - dialogHeight) / 2;
        
        // Fondo del diálogo
        graphics.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xDD2B2B2B);
        graphics.renderOutline(dialogX, dialogY, dialogWidth, dialogHeight, 0xFF000000);
        
        // Título
        graphics.drawCenteredString(font, title, dialogX + dialogWidth / 2, dialogY + 10, 0xFFFFFFFF);
        
        // Prompt
        graphics.drawString(font, prompt, dialogX + 20, dialogY + 35, 0xFFAAAAAA);
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

/**
 * Diálogo de confirmación para desactivar el Engine Mode
 */
class ConfirmDisableScreen extends Screen {
    private final Screen parent;
    private final Level world;
    
    public ConfirmDisableScreen(Screen parent, Level world) {
        super(Component.literal("⚠ Advertencia"));
        this.parent = parent;
        this.world = world;
    }
    
    @Override
    protected void init() {
        int dialogWidth = 400;
        int dialogHeight = 150;
        int dialogX = (width - dialogWidth) / 2;
        int dialogY = (height - dialogHeight) / 2;
        
        // Botón Confirmar
        addRenderableWidget(Button.builder(
            Component.literal("Sí, desactivar Engine Mode"),
            btn -> {
                EngineMode.disable(world);
                minecraft.setScreen(null);
                
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                        Component.literal("§c[PythonMC] Modo Engine DESACTIVADO"),
                        false
                    );
                }
            }
        ).bounds(dialogX + 20, dialogY + 100, 170, 20).build());
        
        // Botón Cancelar
        addRenderableWidget(Button.builder(
            Component.literal("Cancelar"),
            btn -> minecraft.setScreen(parent)
        ).bounds(dialogX + 210, dialogY + 100, 170, 20).build());
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        
        int dialogWidth = 400;
        int dialogHeight = 150;
        int dialogX = (width - dialogWidth) / 2;
        int dialogY = (height - dialogHeight) / 2;
        
        // Fondo
        graphics.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xDD2B2B2B);
        graphics.renderOutline(dialogX, dialogY, dialogWidth, dialogHeight, 0xFFFF0000);
        
        // Título
        graphics.drawCenteredString(font, "⚠ ADVERTENCIA", dialogX + dialogWidth / 2, dialogY + 15, 0xFFFF5555);
        
        // Mensaje
        String[] lines = {
            "¿Estás seguro de que quieres desactivar el",
            "Modo Engine?",
            "",
            "Esto cerrará el editor y volverás al modo de",
            "juego normal. Todos los nodos seguirán activos."
        };
        
        int textY = dialogY + 40;
        for (String line : lines) {
            graphics.drawCenteredString(font, line, dialogX + dialogWidth / 2, textY, 0xFFFFFFFF);
            textY += 12;
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

/**
 * Diálogo para crear nodos
 */
class NodeCreationDialog extends Screen {
    private final Screen parent;
    private final Level world;
    
    private EditBox nameField;
    private NodeType selectedType = NodeType.NODE;
    
    public NodeCreationDialog(Screen parent, Level world) {
        super(Component.literal("Crear Nodo"));
        this.parent = parent;
        this.world = world;
    }
    
    @Override
    protected void init() {
        int dialogWidth = 350;
        int dialogHeight = 250;
        int dialogX = (width - dialogWidth) / 2;
        int dialogY = (height - dialogHeight) / 2;
        
        // Campo nombre
        nameField = new EditBox(font, dialogX + 20, dialogY + 50, dialogWidth - 40, 20, Component.literal(""));
        nameField.setValue("NuevoNodo");
        nameField.setMaxLength(30);
        addRenderableWidget(nameField);
        setInitialFocus(nameField);
        
        // Botones de tipo de nodo
        int btnY = dialogY + 85;
        int btnWidth = 100;
        int btnHeight = 20;
        
        NodeType[] commonTypes = {
            NodeType.NODE,
            NodeType.CAMERA,
            NodeType.CHARACTER_BODY,
            NodeType.MESH_INSTANCE,
            NodeType.AUDIO_PLAYER
        };
        
        int col = 0;
        for (NodeType type : commonTypes) {
            int btnX = dialogX + 20 + (col % 3) * (btnWidth + 5);
            if (col > 0 && col % 3 == 0) btnY += 25;
            
            addRenderableWidget(Button.builder(
                Component.literal(type.getDisplayName()),
                btn -> selectedType = type
            ).bounds(btnX, btnY, btnWidth, btnHeight).build());
            
            col++;
        }
        
        // Botones
        addRenderableWidget(Button.builder(
            Component.literal("Crear"),
            btn -> createNode()
        ).bounds(dialogX + 20, dialogY + dialogHeight - 40, 150, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.literal("Cancelar"),
            btn -> minecraft.setScreen(parent)
        ).bounds(dialogX + 180, dialogY + dialogHeight - 40, 150, 20).build());
    }
    
    private void createNode() {
        String name = nameField.getValue();
        if (name.isEmpty()) return;
        
        Node newNode = switch (selectedType) {
            case CAMERA -> new CameraNode(name);
            case CHARACTER_BODY -> new CharacterBodyNode(name);
            case AUDIO_PLAYER -> new AudioPlayerNode(name);
            default -> new Node(name, selectedType) {
                @Override
                public void update() {}
                @Override
                public void init() {}
                @Override
                public void destroy() {}
            };
        };
        
        NodeRegistry.addNode(world, newNode, null);

        // Guardar representación básica del nodo en filesproject/nodes
        try {
            java.io.File nodesFolder = com.pythonmc.mod.core.ProjectManager.getNodesFolder(world);
            if (!nodesFolder.exists()) {
                nodesFolder.mkdirs();
            }

            java.io.File nodeFile = new java.io.File(nodesFolder, name + ".json");
            if (!nodeFile.exists()) {
                try (java.io.FileWriter writer = new java.io.FileWriter(nodeFile)) {
                    writer.write("{\n");
                    writer.write("  \"name\": \"" + name.replace("\"", "\\\"") + "\",\n");
                    writer.write("  \"type\": \"" + selectedType.getDisplayName().replace("\"", "\\\"") + "\"\n");
                    writer.write("}\n");
                }
            }
        } catch (java.io.IOException e) {
            // No romper la creación del nodo si falla el guardado en disco
        }
        
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                Component.literal("§aNodo creado: " + name + " [" + selectedType.getDisplayName() + "]"),
                false
            );
        }
        
        minecraft.setScreen(parent);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        
        int dialogWidth = 350;
        int dialogHeight = 250;
        int dialogX = (width - dialogWidth) / 2;
        int dialogY = (height - dialogHeight) / 2;
        
        // Fondo
        graphics.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xDD2B2B2B);
        graphics.renderOutline(dialogX, dialogY, dialogWidth, dialogHeight, 0xFF000000);
        
        // Título
        graphics.drawCenteredString(font, "Crear Nuevo Nodo", dialogX + dialogWidth / 2, dialogY + 15, 0xFFFFFFFF);
        
        // Labels
        graphics.drawString(font, "Nombre:", dialogX + 20, dialogY + 38, 0xFFAAAAAA);
        graphics.drawString(font, "Tipo:", dialogX + 20, dialogY + 73, 0xFFAAAAAA);
        graphics.drawString(font, "Seleccionado: " + selectedType.getDisplayName(), 
                          dialogX + 20, dialogY + dialogHeight - 65, 0xFF6A9955);
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}