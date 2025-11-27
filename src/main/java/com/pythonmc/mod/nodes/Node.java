package com.pythonmc.mod.nodes;

import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase base abstracta para todos los nodos del sistema Engine
 * Similar al sistema de nodos de Godot
 */
public abstract class Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(Node.class);
    
    // Identificación
    private final String id;
    private String name;
    private NodeType type;
    
    // Jerarquía
    private Node parent;
    private final List<Node> children;
    
    // Estado
    private boolean active;
    private boolean visible;
    private Level world;
    
    // Transform (posición y rotación básicas)
    protected double x, y, z;
    protected float yaw, pitch, roll;
    
    public Node(String name, NodeType type) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("El nombre del nodo no puede ser vacío");
        }
        
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.children = new ArrayList<>();
        this.active = true;
        this.visible = true;
        
        // Transform inicial
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.roll = 0.0f;
        
        LOGGER.debug("Nodo creado: {} ({})", name, type);
    }
    
    // ========== MÉTODOS ABSTRACTOS ==========
    
    /**
     * Se llama cada tick para actualizar el nodo
     */
    public abstract void update();
    
    /**
     * Se llama al inicializar el nodo
     */
    public abstract void init();
    
    /**
     * Se llama al destruir el nodo
     */
    public abstract void destroy();
    
    // ========== JERARQUÍA DE NODOS ==========
    
    /**
     * Añade un nodo hijo
     */
    public void addChild(Node child) {
        if (child == null) {
            throw new IllegalArgumentException("El hijo no puede ser null");
        }
        
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        
        child.parent = this;
        children.add(child);
        LOGGER.debug("Hijo añadido: {} -> {}", this.name, child.name);
    }
    
    /**
     * Elimina un nodo hijo
     */
    public void removeChild(Node child) {
        if (children.remove(child)) {
            child.parent = null;
            LOGGER.debug("Hijo eliminado: {} -x- {}", this.name, child.name);
        }
    }
    
    /**
     * Obtiene un hijo por nombre
     */
    public Node getChild(String childName) {
        for (Node child : children) {
            if (child.name.equals(childName)) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todos los hijos
     */
    public List<Node> getChildren() {
        return new ArrayList<>(children);
    }
    
    /**
     * Obtiene el nodo padre
     */
    public Node getParent() {
        return parent;
    }
    
    /**
     * Obtiene el nodo raíz del árbol
     */
    public Node getRoot() {
        Node root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }
    
    /**
     * Busca un nodo en todo el árbol por nombre
     */
    public Node findNode(String nodeName) {
        if (this.name.equals(nodeName)) {
            return this;
        }
        
        for (Node child : children) {
            Node found = child.findNode(nodeName);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
    
    // ========== TRANSFORM ==========
    
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void setRotation(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }
    
    public void translate(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }
    
    public void rotate(float dyaw, float dpitch, float droll) {
        this.yaw += dyaw;
        this.pitch += dpitch;
        this.roll += droll;
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public Level getWorld() {
        return world;
    }
    
    public void setWorld(Level world) {
        this.world = world;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getRoll() { return roll; }
    
    // ========== UTILIDADES ==========
    
    /**
     * Obtiene la ruta completa del nodo en el árbol
     */
    public String getPath() {
        if (parent == null) {
            return "/" + name;
        }
        return parent.getPath() + "/" + name;
    }
    
    /**
     * Imprime el árbol de nodos desde este nodo
     */
    public void printTree(int level) {
        String indent = "  ".repeat(level);
        System.out.println(indent + "├─ " + name + " [" + type + "]");
        
        for (Node child : children) {
            child.printTree(level + 1);
        }
    }
    
    @Override
    public String toString() {
        return String.format("Node{name='%s', type=%s, pos=(%.1f,%.1f,%.1f)}", 
                           name, type, x, y, z);
    }
}