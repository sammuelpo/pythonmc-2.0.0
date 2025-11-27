package com.pythonmc.mod.nodes;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nodo de cámara que controla la vista del jugador
 * Se representa como un ArmorStand invisible en el mundo
 * Controla la cámara REAL del jugador (no modo espectador)
 */
public class CameraNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraNode.class);
    
    // Entidad que representa la cámara en el mundo
    private ArmorStand cameraEntity;
    
    // Jugador al que está asignada la cámara
    private Player attachedPlayer;
    
    // Configuración de cámara
    private float fov = 70.0f;
    private float sensitivity = 1.0f;
    private boolean smoothMovement = true;
    
    // Control de movimiento con click derecho
    private boolean isControlling = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    
    public CameraNode(String name) {
        super(name, NodeType.CAMERA);
    }
    
    @Override
    public void init() {
        LOGGER.info("CameraNode '{}' inicializado", getName());
        
        // Crear el ArmorStand invisible que representa la cámara
        if (getWorld() != null) {
            createCameraEntity();
        }
    }
    
    @Override
    public void update() {
        if (!isActive() || cameraEntity == null) {
            return;
        }
        
        // Actualizar posición del ArmorStand
        cameraEntity.setPos(x, y, z);
        cameraEntity.setYRot(yaw);
        cameraEntity.setXRot(pitch);
        
        // Si está asignado a un jugador, sincronizar su cámara
        if (attachedPlayer != null) {
            syncWithPlayer();
        }
    }
    
    @Override
    public void destroy() {
        if (cameraEntity != null) {
            cameraEntity.discard();
            cameraEntity = null;
        }
        
        if (attachedPlayer != null) {
            detachFromPlayer();
        }
        
        LOGGER.info("CameraNode '{}' destruido", getName());
    }
    
    // ========== CONTROL DE CÁMARA ==========
    
    /**
     * Rota la cámara (usado con click derecho del mouse)
     */
    public void rotateCamera(float deltaYaw, float deltaPitch) {
        this.yaw += deltaYaw * sensitivity;
        this.pitch += deltaPitch * sensitivity;
        
        // Limitar pitch para evitar gimbal lock
        this.pitch = Math.max(-89.0f, Math.min(89.0f, this.pitch));
        
        if (attachedPlayer != null) {
            attachedPlayer.setYRot(this.yaw);
            attachedPlayer.setXRot(this.pitch);
        }
    }
    
    /**
     * Mueve la cámara a una posición específica
     */
    public void moveCamera(double x, double y, double z) {
        setPosition(x, y, z);
        
        if (attachedPlayer != null) {
            attachedPlayer.setPos(x, y, z);
        }
    }
    
    /**
     * Mueve la cámara relativamente
     */
    public void moveCameraRelative(double dx, double dy, double dz) {
        translate(dx, dy, dz);
        
        if (attachedPlayer != null) {
            attachedPlayer.setPos(this.x, this.y, this.z);
        }
    }
    
    // ========== ASIGNACIÓN DE JUGADOR ==========
    
    /**
     * Asigna esta cámara al jugador (controla su cámara real)
     */
    public void attachToPlayer(Player player) {
        if (player == null) {
            LOGGER.warn("Intento de asignar cámara a jugador null");
            return;
        }
        
        this.attachedPlayer = player;
        
        // Sincronizar posición inicial
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.yaw = player.getYRot();
        this.pitch = player.getXRot();
        
        LOGGER.info("Cámara '{}' asignada al jugador {}", getName(), player.getName().getString());
    }
    
    /**
     * Desvincula la cámara del jugador
     */
    public void detachFromPlayer() {
        if (attachedPlayer != null) {
            LOGGER.info("Cámara '{}' desvinculada del jugador {}", 
                       getName(), attachedPlayer.getName().getString());
            this.attachedPlayer = null;
        }
    }
    
    /**
     * Verifica si la cámara está asignada a un jugador
     */
    public boolean isAttachedToPlayer() {
        return attachedPlayer != null;
    }
    
    // ========== CONTROL CON CLICK DERECHO ==========
    
    /**
     * Inicia el control de cámara con click derecho
     */
    public void startControl(double mouseX, double mouseY) {
        isControlling = true;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }
    
    /**
     * Detiene el control de cámara
     */
    public void stopControl() {
        isControlling = false;
    }
    
    /**
     * Actualiza la rotación según el movimiento del mouse
     */
    public void updateMouseControl(double mouseX, double mouseY) {
        if (!isControlling) {
            return;
        }
        
        double deltaX = mouseX - lastMouseX;
        double deltaY = mouseY - lastMouseY;
        
        rotateCamera((float) deltaX * 0.15f, (float) -deltaY * 0.15f);
        
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }
    
    // ========== CONFIGURACIÓN ==========
    
    public void setFOV(float fov) {
        this.fov = Math.max(30.0f, Math.min(120.0f, fov));
    }
    
    public float getFOV() {
        return fov;
    }
    
    public void setSensitivity(float sensitivity) {
        this.sensitivity = Math.max(0.1f, Math.min(5.0f, sensitivity));
    }
    
    public float getSensitivity() {
        return sensitivity;
    }
    
    public void setSmoothMovement(boolean smooth) {
        this.smoothMovement = smooth;
    }
    
    public boolean isSmoothMovement() {
        return smoothMovement;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private void createCameraEntity() {
        Level world = getWorld();
        if (world != null) {
            cameraEntity = new ArmorStand(world, x, y, z);
            cameraEntity.setInvisible(true);
            cameraEntity.setNoGravity(true);
            cameraEntity.setInvulnerable(true);
            cameraEntity.setSilent(true);
            cameraEntity.setCustomNameVisible(false);
            
            world.addFreshEntity(cameraEntity);
            LOGGER.debug("CameraEntity creado para '{}'", getName());
        }
    }
    
    private void syncWithPlayer() {
        if (attachedPlayer == null || !attachedPlayer.isAlive()) {
            detachFromPlayer();
            return;
        }
        
        // Actualizar posición del jugador con la de la cámara
        attachedPlayer.setPos(this.x, this.y, this.z);
        attachedPlayer.setYRot(this.yaw);
        attachedPlayer.setXRot(this.pitch);
    }
    
    public Player getAttachedPlayer() {
        return attachedPlayer;
    }
    
    public boolean isControlling() {
        return isControlling;
    }
}