package com.pythonmc.mod.gui;

import com.pythonmc.mod.core.EngineMode;
import com.pythonmc.mod.nodes.NodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pantalla principal del Engine Mode
 * Contiene: FileExplorer, ScriptEditor, TextureEditor, ModelEditor, NodeInspector, BuildToolbar
 */
public class EngineScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineScreen.class);
    
    // Componentes de la interfaz
    private FileExplorerPanel fileExplorer;
    private ScriptEditorPanel scriptEditor;
    private TextureEditorPanel textureEditor;
    private ModelEditorPanel modelEditor;
    private NodeInspectorPanel nodeInspector;
    private BuildToolbar buildToolbar;
    private TopMenuBar menuBar;
    
    // Estado
    private final Level world;
    private boolean isFirstOpen = true;
    private boolean showTextureEditor = false; // Control de pestañas
    
    // Layout
    private static final int EXPLORER_WIDTH = 200;
    private static final int INSPECTOR_WIDTH = 250;
    private static final int TOOLBAR_HEIGHT = 40;
    private static final int MENU_HEIGHT = 20;
    
    public EngineScreen(Level world) {
        super(Component.literal("PythonMC Engine"));
        this.world = world;
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (!EngineMode.isEnabled(world)) {
            LOGGER.warn("Intento de abrir Engine Screen con Engine Mode desactivado");
            onClose();
            return;
        }
        
        // Inicializar el árbol de nodos si es necesario
        if (NodeRegistry.getRoot(world) == null) {
            NodeRegistry.initWorld(world);
        }
        
        // Calcular dimensiones
        int explorerX = 0;
        int explorerY = MENU_HEIGHT;
        int explorerHeight = height - MENU_HEIGHT;
        
        int editorX = EXPLORER_WIDTH;
        int editorY = MENU_HEIGHT + TOOLBAR_HEIGHT;
        int editorWidth = width - EXPLORER_WIDTH - INSPECTOR_WIDTH;
        int editorHeight = height - MENU_HEIGHT - TOOLBAR_HEIGHT;
        
        int inspectorX = width - INSPECTOR_WIDTH;
        int inspectorY = MENU_HEIGHT;
        int inspectorHeight = height - MENU_HEIGHT;
        
        int toolbarX = EXPLORER_WIDTH;
        int toolbarY = MENU_HEIGHT;
        int toolbarWidth = width - EXPLORER_WIDTH - INSPECTOR_WIDTH;
        
        // Crear componentes
        menuBar = new TopMenuBar(this, 0, 0, width, MENU_HEIGHT);
        
        fileExplorer = new FileExplorerPanel(
            this, world, explorerX, explorerY, EXPLORER_WIDTH, explorerHeight
        );
        
        scriptEditor = new ScriptEditorPanel(
            this, editorX, editorY, editorWidth, editorHeight
        );
        
        textureEditor = new TextureEditorPanel(
            this, editorX, editorY, editorWidth, editorHeight
        );
        
        modelEditor = new ModelEditorPanel(
            this, editorX, editorY, editorWidth, editorHeight
        );
        
        nodeInspector = new NodeInspectorPanel(
            this, world, inspectorX, inspectorY, INSPECTOR_WIDTH, inspectorHeight
        );
        
        buildToolbar = new BuildToolbar(
            this, toolbarX, toolbarY, toolbarWidth, TOOLBAR_HEIGHT
        );
        
        LOGGER.info("EngineScreen inicializado");
        
        if (isFirstOpen) {
            showWelcomeMessage();
            isFirstOpen = false;
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo semi-transparente
        renderBackground(graphics);
        
        // Renderizar componentes
        menuBar.render(graphics, mouseX, mouseY, partialTick);
        fileExplorer.render(graphics, mouseX, mouseY, partialTick);
        buildToolbar.render(graphics, mouseX, mouseY, partialTick);
        
        // Renderizar el editor correspondiente según la pestaña
        if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.SCRIPT) {
            scriptEditor.render(graphics, mouseX, mouseY, partialTick);
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.TEXTURE) {
            textureEditor.render(graphics, mouseX, mouseY, partialTick);
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.MODEL) {
            modelEditor.render(graphics, mouseX, mouseY, partialTick);
        }
        
        nodeInspector.render(graphics, mouseX, mouseY, partialTick);
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Propagar eventos a los componentes
        if (menuBar.mouseClicked(mouseX, mouseY, button)) return true;
        if (fileExplorer.mouseClicked(mouseX, mouseY, button)) return true;
        if (buildToolbar.mouseClicked(mouseX, mouseY, button)) return true;
        
        // Propagar al editor correspondiente
        if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.SCRIPT) {
            if (scriptEditor.mouseClicked(mouseX, mouseY, button)) return true;
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.TEXTURE) {
            if (textureEditor.mouseClicked(mouseX, mouseY, button)) return true;
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.MODEL) {
            if (modelEditor.mouseClicked(mouseX, mouseY, button)) return true;
        }
        
        if (nodeInspector.mouseClicked(mouseX, mouseY, button)) return true;
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Propagar al editor correspondiente
        if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.SCRIPT) {
            if (scriptEditor.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.TEXTURE) {
            if (textureEditor.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.MODEL) {
            if (modelEditor.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Propagar al editor correspondiente
        if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.SCRIPT) {
            if (scriptEditor.mouseReleased(mouseX, mouseY, button)) return true;
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.TEXTURE) {
            if (textureEditor.mouseReleased(mouseX, mouseY, button)) return true;
        } else if (buildToolbar.getCurrentTab() == BuildToolbar.EditorTab.MODEL) {
            if (modelEditor.mouseReleased(mouseX, mouseY, button)) return true;
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (fileExplorer.mouseScrolled(mouseX, mouseY, delta)) return true;
        if (scriptEditor.mouseScrolled(mouseX, mouseY, delta)) return true;
        if (nodeInspector.mouseScrolled(mouseX, mouseY, delta)) return true;
        
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (scriptEditor.keyPressed(keyCode, scanCode, modifiers)) return true;
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (scriptEditor.charTyped(codePoint, modifiers)) return true;
        
        return super.charTyped(codePoint, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // No pausar el juego
    }
    
    @Override
    public void onClose() {
        super.onClose();
        LOGGER.info("EngineScreen cerrado");
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
    /**
     * Abre un script en el editor
     */
    public void openScript(String scriptPath) {
        scriptEditor.openScript(scriptPath);
    }
    
    /**
     * Selecciona un nodo en el inspector
     */
    public void selectNode(String nodeName) {
        nodeInspector.selectNode(nodeName);
    }
    
    /**
     * Desactiva el Engine Mode con advertencia
     */
    public void disableEngineMode() {
        Minecraft.getInstance().setScreen(new ConfirmDisableScreen(this, world));
    }
    
    /**
     * Actualiza el estado de la interfaz
     */
    public void refresh() {
        fileExplorer.refresh();
        nodeInspector.refresh();
    }
    
    private void showWelcomeMessage() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.displayClientMessage(
                Component.literal("§6[PythonMC Engine] §aBienvenido al modo Engine!"),
                false
            );
            minecraft.player.displayClientMessage(
                Component.literal("§7Presiona §eESC§7 para volver al juego"),
                false
            );
        }
    }
    
    public Level getWorld() {
        return world;
    }
    
    public FileExplorerPanel getFileExplorer() {
        return fileExplorer;
    }

    public ScriptEditorPanel getScriptEditor() {
        return scriptEditor;
    }

    public TextureEditorPanel getTextureEditor() {
        return textureEditor;
    }

    public ModelEditorPanel getModelEditor() {
        return modelEditor;
    }

    public NodeInspectorPanel getNodeInspector() {
        return nodeInspector;
    }

    public BuildToolbar getBuildToolbar() {
        return buildToolbar;
    }
}