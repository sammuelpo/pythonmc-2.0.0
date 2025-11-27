package com.pythonmc.mod.nodes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nodo que representa el cuerpo físico de un personaje
 * Similar al CharacterBody3D de Godot
 */
public class CharacterBodyNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterBodyNode.class);
    
    // Jugador asignado
    private Player player;
    
    // Física y movimiento
    private Vec3 velocity;
    private double gravity = 0.08;
    private double jumpStrength = 0.42;
    private double moveSpeed = 0.1;
    private double sprintSpeed = 0.15;
    
    // Estado
    private boolean onGround = false;
    private boolean isSprinting = false;
    private boolean canJump = true;
    
    // Colisiones
    private boolean collisionEnabled = true;
    private double collisionRadius = 0.6;
    private double collisionHeight = 1.8;
    
    public CharacterBodyNode(String name) {
        super(name, NodeType.CHARACTER_BODY);
        this.velocity = Vec3.ZERO;
    }
    
    @Override
    public void init() {
        LOGGER.info("CharacterBodyNode '{}' inicializado", getName());
    }
    
    @Override
    public void update() {
        if (!isActive() || player == null) {
            return;
        }
        
        // Actualizar estado del suelo
        updateGroundState();
        
        // Aplicar gravedad
        if (!onGround) {
            applyGravity();
        }
        
        // Actualizar posición del jugador
        if (player != null) {
            player.setPos(x, y, z);
        }
    }
    
    @Override
    public void destroy() {
        if (player != null) {
            detachFromPlayer();
        }
        LOGGER.info("CharacterBodyNode '{}' destruido", getName());
    }
    
    // ========== MOVIMIENTO ==========
    
    /**
     * Mueve el personaje en una dirección
     */
    public void move(double dx, double dy, double dz) {
        if (!isActive()) {
            return;
        }
        
        double speed = isSprinting ? sprintSpeed : moveSpeed;
        
        translate(dx * speed, dy * speed, dz * speed);
        
        if (player != null) {
            player.setPos(x, y, z);
        }
    }
    
    /**
     * Mueve el personaje en dirección forward (relativo a su rotación)
     */
    public void moveForward(double distance) {
        double radYaw = Math.toRadians(yaw);
        double dx = -Math.sin(radYaw) * distance;
        double dz = Math.cos(radYaw) * distance;
        
        move(dx, 0, dz);
    }
    
    /**
     * Mueve el personaje hacia atrás
     */
    public void moveBackward(double distance) {
        moveForward(-distance);
    }
    
    /**
     * Mueve el personaje hacia la izquierda (strafe)
     */
    public void moveLeft(double distance) {
        double radYaw = Math.toRadians(yaw - 90);
        double dx = -Math.sin(radYaw) * distance;
        double dz = Math.cos(radYaw) * distance;
        
        move(dx, 0, dz);
    }
    
    /**
     * Mueve el personaje hacia la derecha (strafe)
     */
    public void moveRight(double distance) {
        moveLeft(-distance);
    }
    
    /**
     * Hace que el personaje salte
     */
    public void jump() {
        if (!canJump || !onGround) {
            return;
        }
        
        velocity = velocity.add(0, jumpStrength, 0);
        onGround = false;
        
        if (player != null) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, jumpStrength, 0));
        }
        
        LOGGER.debug("Personaje '{}' salta", getName());
    }
    
    /**
     * Teletransporta el personaje a una posición
     */
    public void teleport(double x, double y, double z) {
        setPosition(x, y, z);
        
        if (player != null) {
            player.teleportTo(x, y, z);
        }
        
        LOGGER.debug("Personaje '{}' teletransportado a ({}, {}, {})", getName(), x, y, z);
    }
    
    // ========== ASIGNACIÓN DE JUGADOR ==========
    
    /**
     * Asigna este cuerpo a un jugador
     */
    public void attachToPlayer(Player player) {
        if (player == null) {
            LOGGER.warn("Intento de asignar CharacterBody a jugador null");
            return;
        }
        
        this.player = player;
        
        // Sincronizar posición inicial
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.yaw = player.getYRot();
        this.pitch = player.getXRot();
        
        LOGGER.info("CharacterBody '{}' asignado al jugador {}", 
                   getName(), player.getName().getString());
    }
    
    /**
     * Desvincula el cuerpo del jugador
     */
    public void detachFromPlayer() {
        if (player != null) {
            LOGGER.info("CharacterBody '{}' desvinculado del jugador {}", 
                       getName(), player.getName().getString());
            this.player = null;
        }
    }
    
    /**
     * Verifica si está asignado a un jugador
     */
    public boolean isAttachedToPlayer() {
        return player != null;
    }
    
    // ========== FÍSICA ==========
    
    /**
     * Aplica gravedad al personaje
     */
    private void applyGravity() {
        velocity = velocity.add(0, -gravity, 0);
        y += velocity.y;
        
        // Limitar velocidad de caída
        if (velocity.y < -2.0) {
            velocity = new Vec3(velocity.x, -2.0, velocity.z);
        }
    }
    
    /**
     * Actualiza el estado de contacto con el suelo
     */
    private void updateGroundState() {
        if (player != null) {
            onGround = player.onGround();
        } else {
            // Verificación básica si no hay jugador
            onGround = y <= 0.1;
        }
        
        if (onGround) {
            velocity = new Vec3(velocity.x, 0, velocity.z);
        }
    }
    
    // ========== CONFIGURACIÓN ==========
    
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }
    
    public double getGravity() {
        return gravity;
    }
    
    public void setJumpStrength(double jumpStrength) {
        this.jumpStrength = jumpStrength;
    }
    
    public double getJumpStrength() {
        return jumpStrength;
    }
    
    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }
    
    public double getMoveSpeed() {
        return moveSpeed;
    }
    
    public void setSprintSpeed(double sprintSpeed) {
        this.sprintSpeed = sprintSpeed;
    }
    
    public double getSprintSpeed() {
        return sprintSpeed;
    }
    
    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;
    }
    
    public boolean isSprinting() {
        return isSprinting;
    }
    
    public void setCanJump(boolean canJump) {
        this.canJump = canJump;
    }
    
    public boolean canJump() {
        return canJump;
    }
    
    public boolean isOnGround() {
        return onGround;
    }
    
    public Vec3 getVelocity() {
        return velocity;
    }
    
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }
    
    public void setCollisionEnabled(boolean enabled) {
        this.collisionEnabled = enabled;
    }
    
    public boolean isCollisionEnabled() {
        return collisionEnabled;
    }
    
    public Player getPlayer() {
        return player;
    }
}