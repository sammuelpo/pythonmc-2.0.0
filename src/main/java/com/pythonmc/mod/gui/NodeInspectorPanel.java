package com.pythonmc.mod.gui;

import com.pythonmc.mod.nodes.Node;
import com.pythonmc.mod.nodes.NodeRegistry;
import com.pythonmc.mod.nodes.NodeType;
import com.pythonmc.mod.nodes.AudioPlayerNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.util.List;

/**
 * Panel inspector de nodos
 * Muestra el árbol de escena y propiedades del nodo seleccionado
 */
public class NodeInspectorPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeInspectorPanel.class);
    
    private final EngineScreen parent;
    private final Level world;
    private final int x, y, width, height;
    
    // Estado
    private Node selectedNode = null;
    private int scrollOffset = 0;
    
    // Colores
    private static final int BG_COLOR = 0xFF252525;
    private static final int HEADER_COLOR = 0xFF2B2B2B;
    private static final int SELECTED_COLOR = 0xFF3A3A3A;
    private static final int HOVER_COLOR = 0xFF2F2F2F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int PROPERTY_COLOR = 0xFFAAAAAA;
    
    public NodeInspectorPanel(EngineScreen parent, Level world, int x, int y, int width, int height) {
        this.parent = parent;
        this.world = world;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Fondo
        graphics.fill(x, y, x + width, y + height, BG_COLOR);
        
        // Header
        graphics.fill(x, y, x + width, y + 25, HEADER_COLOR);
        graphics.drawString(parent.getMinecraft().font, "Node Inspector", x + 5, y + 8, TEXT_COLOR);
        
        int contentY = y + 30;
        
        // Árbol de escena
        graphics.drawString(parent.getMinecraft().font, "Scene Tree:", x + 5, contentY, PROPERTY_COLOR);
        contentY += 20;
        
        Node root = NodeRegistry.getRoot(world);
        if (root != null) {
            contentY = renderNodeTree(graphics, root, x + 10, contentY, 0, mouseX, mouseY);
        } else {
            graphics.drawString(parent.getMinecraft().font, "No hay nodos", x + 10, contentY, TEXT_COLOR);
        }
        
        contentY += 20;
        
        // Propiedades del nodo seleccionado
        if (selectedNode != null) {
            graphics.fill(x, contentY, x + width, contentY + 1, 0xFF3C3F41);
            contentY += 10;
            
            graphics.drawString(parent.getMinecraft().font, 
                "Propiedades: " + selectedNode.getName(), x + 5, contentY, TEXT_COLOR);
            contentY += 20;
            
            renderNodeProperties(graphics, selectedNode, x + 10, contentY);
        }
    }
    
    private int renderNodeTree(GuiGraphics graphics, Node node, int x, int y, 
                               int depth, int mouseX, int mouseY) {
        if (y > this.y + height - 50) return y; // Límite de scroll
        
        String indent = "  ".repeat(depth);
        String icon = getNodeIcon(node.getType());
        String text = indent + icon + " " + node.getName();
        
        boolean isSelected = node == selectedNode;
        boolean isHovered = isHovering(mouseX, mouseY, this.x, y, width, 15);
        
        // Fondo
        if (isSelected) {
            graphics.fill(this.x, y, this.x + width, y + 15, SELECTED_COLOR);
        } else if (isHovered) {
            graphics.fill(this.x, y, this.x + width, y + 15, HOVER_COLOR);
        }
        
        // Texto
        graphics.drawString(parent.getMinecraft().font, text, x, y + 3, TEXT_COLOR);
        
        y += 15;
        
        // Renderizar hijos
        for (Node child : node.getChildren()) {
            y = renderNodeTree(graphics, child, x, y, depth + 1, mouseX, mouseY);
        }
        
        return y;
    }
    
    private void renderNodeProperties(GuiGraphics graphics, Node node, int x, int y) {
        int lineHeight = 12;
        
        // Tipo
        drawProperty(graphics, "Type:", node.getType().getDisplayName(), x, y);
        y += lineHeight;
        
        // ID
        drawProperty(graphics, "ID:", node.getId().substring(0, 8) + "...", x, y);
        y += lineHeight;
        
        // Activo
        drawProperty(graphics, "Active:", String.valueOf(node.isActive()), x, y);
        y += lineHeight;
        
        // Visible
        drawProperty(graphics, "Visible:", String.valueOf(node.isVisible()), x, y);
        y += lineHeight;
        
        y += 10;
        
        // Transform
        graphics.drawString(parent.getMinecraft().font, "Transform:", x, y, PROPERTY_COLOR);
        y += lineHeight;
        
        drawProperty(graphics, "  X:", String.format("%.2f", node.getX()), x, y);
        y += lineHeight;
        
        drawProperty(graphics, "  Y:", String.format("%.2f", node.getY()), x, y);
        y += lineHeight;
        
        drawProperty(graphics, "  Z:", String.format("%.2f", node.getZ()), x, y);
        y += lineHeight;
        
        drawProperty(graphics, "  Yaw:", String.format("%.1f°", node.getYaw()), x, y);
        y += lineHeight;
        
        drawProperty(graphics, "  Pitch:", String.format("%.1f°", node.getPitch()), x, y);
        y += lineHeight;
        
        // Jerarquía
        y += 10;
        graphics.drawString(parent.getMinecraft().font, "Hierarchy:", x, y, PROPERTY_COLOR);
        y += lineHeight;
        
        drawProperty(graphics, "  Parent:", 
            node.getParent() != null ? node.getParent().getName() : "None", x, y);
        y += lineHeight;
        
        drawProperty(graphics, "  Children:", String.valueOf(node.getChildren().size()), x, y);
        
        // Controles especiales para AudioPlayer
        if (node instanceof AudioPlayerNode audioNode) {
            y += 15;
            graphics.drawString(parent.getMinecraft().font, "Audio Controls:", x, y, PROPERTY_COLOR);
            y += lineHeight;
            
            // Botón Play
            int btnX = x + 5;
            int btnY = y - 2;
            int btnWidth = 40;
            int btnHeight = 14;
            
            graphics.fill(btnX, btnY, btnX + btnWidth, btnY + btnHeight, 0xFF4CAF50);
            graphics.drawString(parent.getMinecraft().font, "Play", btnX + 6, btnY + 2, 0xFFFFFFFF);
            
            // Guardar bounds para clic
            this.playButtonBounds = new Rectangle(btnX, btnY, btnWidth, btnHeight);
        } else {
            this.playButtonBounds = null;
        }
    }
    
    private Rectangle playButtonBounds = null;
    
    private void drawProperty(GuiGraphics graphics, String key, String value, int x, int y) {
        graphics.drawString(parent.getMinecraft().font, key, x, y, PROPERTY_COLOR);
        int keyWidth = parent.getMinecraft().font.width(key);
        graphics.drawString(parent.getMinecraft().font, value, x + keyWidth + 5, y, TEXT_COLOR);
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        
        // Click en el botón Play de AudioPlayer
        if (playButtonBounds != null && playButtonBounds.contains(mouseX, mouseY)) {
            if (selectedNode instanceof AudioPlayerNode audioNode) {
                audioNode.playOnce();
                LOGGER.info("Reproduciendo sonido en nodo: {}", audioNode.getName());
                return true;
            }
        }
        
        // Click en el árbol de nodos
        Node root = NodeRegistry.getRoot(world);
        if (root != null) {
            Node clicked = findClickedNode(root, (int)mouseX, (int)mouseY, x + 10, y + 50, 0);
            if (clicked != null) {
                selectNode(clicked);
                return true;
            }
        }
        
        return false;
    }
    
    private Node findClickedNode(Node node, int mouseX, int mouseY, int x, int y, int depth) {
        if (y > this.y + height - 50) return null;
        
        if (isHovering(mouseX, mouseY, this.x, y, width, 15)) {
            return node;
        }
        
        y += 15;
        
        for (Node child : node.getChildren()) {
            Node found = findClickedNode(child, mouseX, mouseY, x, y, depth + 1);
            if (found != null) return found;
            y += 15 * (1 + countDescendants(child));
        }
        
        return null;
    }
    
    private int countDescendants(Node node) {
        int count = 0;
        for (Node child : node.getChildren()) {
            count += 1 + countDescendants(child);
        }
        return count;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isHovering((int)mouseX, (int)mouseY, x, y, width, height)) {
            return false;
        }
        
        scrollOffset -= (int) delta;
        scrollOffset = Math.max(0, scrollOffset);
        
        return true;
    }
    
    public void selectNode(Node node) {
        this.selectedNode = node;
        LOGGER.info("Nodo seleccionado: {}", node != null ? node.getName() : "None");
    }
    
    public void selectNode(String nodeName) {
        Node node = NodeRegistry.findNode(world, nodeName);
        selectNode(node);
    }
    
    public void refresh() {
        // Refresh se llama cuando cambia el árbol de nodos
    }
    
    private String getNodeIcon(NodeType type) {
        return switch (type) {
            case CAMERA, CAMERA_3D -> "📷";
            case CHARACTER_BODY, CHARACTER_BODY_3D -> "🚶";
            case MESH_INSTANCE -> "📦";
            case AUDIO_PLAYER -> "🔊";
            case LIGHT, DIRECTIONAL_LIGHT, POINT_LIGHT, SPOT_LIGHT -> "💡";
            case STATIC_BODY, RIGID_BODY -> "🔲";
            default -> "📍";
        };
    }
    
    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public Node getSelectedNode() {
        return selectedNode;
    }
}