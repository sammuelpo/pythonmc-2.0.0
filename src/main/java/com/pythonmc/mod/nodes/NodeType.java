package com.pythonmc.mod.nodes;

/**
 * Tipos de nodos disponibles en el sistema Engine
 * Similar a los nodos de Godot
 */
public enum NodeType {
    // Nodos básicos
    NODE("Node", "Nodo base"),
    
    // Nodos espaciales
    SPATIAL("Spatial", "Nodo con transformación 3D"),
    
    // Nodos de cámara
    CAMERA("Camera", "Nodo de cámara"),
    CAMERA_3D("Camera3D", "Cámara 3D"),
    
    // Nodos de cuerpo físico
    CHARACTER_BODY("CharacterBody", "Cuerpo de personaje"),
    CHARACTER_BODY_3D("CharacterBody3D", "Cuerpo de personaje 3D"),
    RIGID_BODY("RigidBody", "Cuerpo rígido"),
    STATIC_BODY("StaticBody", "Cuerpo estático"),
    
    // Nodos visuales
    MESH_INSTANCE("MeshInstance", "Instancia de malla 3D"),
    SPRITE("Sprite", "Sprite 2D"),
    
    // Nodos de luz
    LIGHT("Light", "Luz básica"),
    DIRECTIONAL_LIGHT("DirectionalLight", "Luz direccional"),
    POINT_LIGHT("PointLight", "Luz puntual"),
    SPOT_LIGHT("SpotLight", "Luz foco"),
    
    // Nodos de audio
    AUDIO_PLAYER("AudioPlayer", "Reproductor de audio"),
    
    // Nodos de control
    TIMER("Timer", "Temporizador"),
    AREA("Area", "Área de detección");
    
    private final String displayName;
    private final String description;
    
    NodeType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Obtiene el NodeType desde un string
     */
    public static NodeType fromString(String typeStr) {
        for (NodeType type : values()) {
            if (type.displayName.equalsIgnoreCase(typeStr) || 
                type.name().equalsIgnoreCase(typeStr)) {
                return type;
            }
        }
        return NODE; // Default
    }
}