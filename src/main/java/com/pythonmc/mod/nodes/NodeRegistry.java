package com.pythonmc.mod.nodes;

import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Registro y gestión de todos los nodos del sistema
 * Mantiene el árbol de escena (scene tree) similar a Godot
 */
public class NodeRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistry.class);
    
    // Mapa de mundos y sus árboles de nodos
    private static final Map<String, Node> worldRoots = new HashMap<>();
    
    // Mapa de IDs a nodos para búsqueda rápida
    private static final Map<String, Node> nodeById = new HashMap<>();
    
    // Mapa de nombres a nodos
    private static final Map<String, List<Node>> nodesByName = new HashMap<>();
    
    /**
     * Inicializa el árbol de nodos para un mundo
     */
    public static void initWorld(Level world) {
        String worldName = getWorldName(world);
        
        if (worldRoots.containsKey(worldName)) {
            LOGGER.warn("El mundo '{}' ya tiene un árbol de nodos", worldName);
            return;
        }
        
        // Crear nodo raíz
        Node root = new RootNode(worldName);
        root.setWorld(world);
        root.init();
        
        worldRoots.put(worldName, root);
        registerNode(root);
        
        LOGGER.info("Árbol de nodos inicializado para el mundo: {}", worldName);
    }
    
    /**
     * Obtiene el nodo raíz de un mundo
     */
    public static Node getRoot(Level world) {
        String worldName = getWorldName(world);
        return worldRoots.get(worldName);
    }
    
    /**
     * Añade un nodo al árbol
     */
    public static void addNode(Level world, Node node, Node parent) {
        if (node == null) {
            throw new IllegalArgumentException("El nodo no puede ser null");
        }
        
        // Si no hay padre especificado, añadir a la raíz
        if (parent == null) {
            parent = getRoot(world);
        }
        
        if (parent == null) {
            LOGGER.error("No se puede añadir nodo: no existe raíz para el mundo");
            return;
        }
        
        // Añadir al árbol
        parent.addChild(node);
        node.setWorld(world);
        node.init();
        
        // Registrar en los mapas
        registerNode(node);
        
        LOGGER.info("Nodo '{}' añadido al árbol en '{}'", node.getName(), parent.getName());
    }
    
    /**
     * Elimina un nodo del árbol
     */
    public static void removeNode(Node node) {
        if (node == null) {
            return;
        }
        
        // Destruir todos los hijos primero
        for (Node child : new ArrayList<>(node.getChildren())) {
            removeNode(child);
        }
        
        // Eliminar del padre
        Node parent = node.getParent();
        if (parent != null) {
            parent.removeChild(node);
        }
        
        // Destruir el nodo
        node.destroy();
        
        // Eliminar de los registros
        unregisterNode(node);
        
        LOGGER.info("Nodo '{}' eliminado del árbol", node.getName());
    }
    
    /**
     * Busca un nodo por ID
     */
    public static Node getNodeById(String id) {
        return nodeById.get(id);
    }
    
    /**
     * Busca nodos por nombre
     */
    public static List<Node> getNodesByName(String name) {
        return nodesByName.getOrDefault(name, new ArrayList<>());
    }
    
    /**
     * Busca un nodo en un mundo específico
     */
    public static Node findNode(Level world, String nodeName) {
        Node root = getRoot(world);
        if (root == null) {
            return null;
        }
        
        return root.findNode(nodeName);
    }
    
    /**
     * Obtiene todos los nodos de un tipo específico
     */
    public static List<Node> getNodesByType(Level world, NodeType type) {
        List<Node> result = new ArrayList<>();
        Node root = getRoot(world);
        
        if (root != null) {
            collectNodesByType(root, type, result);
        }
        
        return result;
    }
    
    /**
     * Actualiza todos los nodos de un mundo
     */
    public static void updateAll(Level world) {
        Node root = getRoot(world);
        if (root != null) {
            updateNodeTree(root);
        }
    }
    
    /**
     * Limpia todos los nodos de un mundo
     */
    public static void clearWorld(Level world) {
        String worldName = getWorldName(world);
        Node root = worldRoots.remove(worldName);
        
        if (root != null) {
            removeNode(root);
            LOGGER.info("Árbol de nodos limpiado para: {}", worldName);
        }
    }
    
    /**
     * Imprime el árbol de nodos de un mundo
     */
    public static void printTree(Level world) {
        Node root = getRoot(world);
        if (root != null) {
            System.out.println("=== Scene Tree ===");
            root.printTree(0);
        } else {
            System.out.println("No hay árbol de nodos para este mundo");
        }
    }
    
    /**
     * Obtiene estadísticas del registro
     */
    public static Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("worlds", worldRoots.size());
        stats.put("total_nodes", nodeById.size());
        stats.put("unique_names", nodesByName.size());
        return stats;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private static void registerNode(Node node) {
        nodeById.put(node.getId(), node);
        
        List<Node> nodesWithName = nodesByName.computeIfAbsent(
            node.getName(), k -> new ArrayList<>()
        );
        nodesWithName.add(node);
    }
    
    private static void unregisterNode(Node node) {
        nodeById.remove(node.getId());
        
        List<Node> nodesWithName = nodesByName.get(node.getName());
        if (nodesWithName != null) {
            nodesWithName.remove(node);
            if (nodesWithName.isEmpty()) {
                nodesByName.remove(node.getName());
            }
        }
    }
    
    private static void updateNodeTree(Node node) {
        if (node.isActive()) {
            node.update();
            
            for (Node child : node.getChildren()) {
                updateNodeTree(child);
            }
        }
    }
    
    private static void collectNodesByType(Node node, NodeType type, List<Node> result) {
        if (node.getType() == type) {
            result.add(node);
        }
        
        for (Node child : node.getChildren()) {
            collectNodesByType(child, type, result);
        }
    }
    
    private static String getWorldName(Level world) {
        return world.dimension().location().toString();
    }
    
    /**
     * Nodo raíz especial para cada mundo
     */
    private static class RootNode extends Node {
        public RootNode(String worldName) {
            super("Root_" + worldName, NodeType.NODE);
        }
        
        @Override
        public void update() {
            // El nodo raíz no hace nada en update
        }
        
        @Override
        public void init() {
            LOGGER.debug("Nodo raíz inicializado");
        }
        
        @Override
        public void destroy() {
            LOGGER.debug("Nodo raíz destruido");
        }
    }
}