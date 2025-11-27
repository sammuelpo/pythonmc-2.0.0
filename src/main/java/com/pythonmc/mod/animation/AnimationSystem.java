package com.pythonmc.mod.animation;

import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Sistema de animaciones para modelos 3D
 * Gestiona timelines, keyframes y reproducción de animaciones
 */
public class AnimationSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnimationSystem.class);
    
    // Instancia global por mundo
    private static final Map<Level, AnimationSystem> INSTANCES = new HashMap<>();
    
    // Datos de animación
    private final Map<String, Animation> animations = new HashMap<>();
    private final Map<String, AnimationPlayer> players = new HashMap<>();
    private String currentAnimation = "";
    private boolean isPlaying = false;
    private float currentTime = 0f;
    private float timeScale = 1f;
    
    private final Level world;
    
    public AnimationSystem(Level world) {
        this.world = world;
        loadDefaultAnimations();
    }
    
    public static AnimationSystem getInstance(Level world) {
        return INSTANCES.computeIfAbsent(world, AnimationSystem::new);
    }
    
    private void loadDefaultAnimations() {
        // Animación por defecto: idle
        Animation idle = new Animation("idle", 2.0f, true);
        
        // Keyframes para idle
        Keyframe kf0 = new Keyframe(0f);
        kf0.addChannelValue("position.x", 0f);
        kf0.addChannelValue("position.y", 0f);
        kf0.addChannelValue("position.z", 0f);
        kf0.addChannelValue("rotation.y", 0f);
        
        Keyframe kf1 = new Keyframe(1.0f);
        kf1.addChannelValue("position.x", 0f);
        kf1.addChannelValue("position.y", 0.1f);
        kf1.addChannelValue("position.z", 0f);
        kf1.addChannelValue("rotation.y", 0f);
        
        Keyframe kf2 = new Keyframe(2.0f);
        kf2.addChannelValue("position.x", 0f);
        kf2.addChannelValue("position.y", 0f);
        kf2.addChannelValue("position.z", 0f);
        kf2.addChannelValue("rotation.y", 0f);
        
        idle.addKeyframe(kf0);
        idle.addKeyframe(kf1);
        idle.addKeyframe(kf2);
        
        animations.put("idle", idle);
        
        // Animación walk
        Animation walk = new Animation("walk", 1.0f, true);
        
        Keyframe walkKf0 = new Keyframe(0f);
        walkKf0.addChannelValue("position.x", 0f);
        walkKf0.addChannelValue("rotation.y", 0f);
        
        Keyframe walkKf1 = new Keyframe(0.5f);
        walkKf1.addChannelValue("position.x", 1f);
        walkKf1.addChannelValue("rotation.y", 0.1f);
        
        Keyframe walkKf2 = new Keyframe(1.0f);
        walkKf2.addChannelValue("position.x", 2f);
        walkKf2.addChannelValue("rotation.y", 0f);
        
        walk.addKeyframe(walkKf0);
        walk.addKeyframe(walkKf1);
        walk.addKeyframe(walkKf2);
        
        animations.put("walk", walk);
    }
    
    /**
     * Actualiza el sistema de animaciones
     */
    public void tick(float deltaTime) {
        if (isPlaying && !currentAnimation.isEmpty()) {
            Animation animation = animations.get(currentAnimation);
            if (animation != null) {
                currentTime += deltaTime * timeScale;
                
                if (currentTime >= animation.duration) {
                    if (animation.loop) {
                        currentTime = currentTime % animation.duration;
                    } else {
                        currentTime = animation.duration;
                        stop();
                    }
                }
                
                // Actualizar players
                for (AnimationPlayer player : players.values()) {
                    player.update(currentTime, animation);
                }
            }
        }
    }
    
    /**
     * Reproduce una animación
     */
    public void play(String animationName) {
        if (animations.containsKey(animationName)) {
            currentAnimation = animationName;
            isPlaying = true;
            currentTime = 0f;
            LOGGER.info("Reproduciendo animación: {}", animationName);
        } else {
            LOGGER.warn("Animación no encontrada: {}", animationName);
        }
    }
    
    /**
     * Detiene la animación actual
     */
    public void stop() {
        isPlaying = false;
        currentTime = 0f;
        LOGGER.info("Animación detenida");
    }
    
    /**
     * Pausa la animación actual
     */
    public void pause() {
        isPlaying = false;
        LOGGER.info("Animación pausada en tiempo: {}", currentTime);
    }
    
    /**
     * Reanuda la animación pausada
     */
    public void resume() {
        if (!currentAnimation.isEmpty()) {
            isPlaying = true;
            LOGGER.info("Animación reanudada desde tiempo: {}", currentTime);
        }
    }
    
    /**
     * Establece el tiempo actual de la animación
     */
    public void setTime(float time) {
        Animation animation = animations.get(currentAnimation);
        if (animation != null) {
            currentTime = Math.max(0f, Math.min(time, animation.duration));
        }
    }
    
    /**
     * Añade una nueva animación
     */
    public void addAnimation(Animation animation) {
        animations.put(animation.name, animation);
        LOGGER.info("Animación añadida: {}", animation.name);
    }
    
    /**
     * Crea un player para un objeto
     */
    public AnimationPlayer createPlayer(String objectId) {
        AnimationPlayer player = new AnimationPlayer(objectId);
        players.put(objectId, player);
        return player;
    }
    
    /**
     * Obtiene el valor interpolado de un canal en el tiempo actual
     */
    public float getChannelValue(String objectId, String channel, float defaultValue) {
        Animation animation = animations.get(currentAnimation);
        if (animation != null) {
            return animation.getChannelValue(channel, currentTime, defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Guarda todas las animaciones a archivo
     */
    public void saveAnimations(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("{");
            writer.println("  \"animations\": [");
            
            boolean first = true;
            for (Animation animation : animations.values()) {
                if (!first) writer.println(",");
                animation.save(writer);
                first = false;
            }
            
            writer.println();
            writer.println("  ]");
            writer.println("}");
            
            LOGGER.info("Animaciones guardadas en: {}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error al guardar animaciones", e);
        }
    }
    
    /**
     * Carga animaciones desde archivo
     */
    public void loadAnimations(File file) {
        // TODO: Implementar carga desde JSON
        LOGGER.info("Carga de animaciones desde archivo no implementada aún");
    }
    
    // ========== GETTERS ==========
    
    public Map<String, Animation> getAnimations() {
        return new HashMap<>(animations);
    }
    
    public String getCurrentAnimation() {
        return currentAnimation;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public float getCurrentTime() {
        return currentTime;
    }
    
    public float getTimeScale() {
        return timeScale;
    }
    
    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }
    
    public Animation getCurrentAnimationObject() {
        return animations.get(currentAnimation);
    }
    
    // ========== CLASES INTERNAS ==========
    
    /**
     * Representa una animación completa
     */
    public static class Animation {
        public final String name;
        public final float duration;
        public final boolean loop;
        private final List<Keyframe> keyframes = new ArrayList<>();
        
        public Animation(String name, float duration, boolean loop) {
            this.name = name;
            this.duration = duration;
            this.loop = loop;
        }
        
        public void addKeyframe(Keyframe keyframe) {
            keyframes.add(keyframe);
            keyframes.sort(Comparator.comparing(k -> k.time));
        }
        
        public float getChannelValue(String channel, float time, float defaultValue) {
            if (keyframes.isEmpty()) return defaultValue;
            
            // Encontrar keyframes anteriores y posteriores
            Keyframe previous = null;
            Keyframe next = null;
            
            for (Keyframe kf : keyframes) {
                if (kf.time <= time) {
                    previous = kf;
                } else if (kf.time > time && next == null) {
                    next = kf;
                    break;
                }
            }
            
            if (previous == null && next != null) {
                return next.getChannelValue(channel, defaultValue);
            }
            
            if (previous != null && next == null) {
                return previous.getChannelValue(channel, defaultValue);
            }
            
            if (previous != null && next != null) {
                float prevValue = previous.getChannelValue(channel, defaultValue);
                float nextValue = next.getChannelValue(channel, defaultValue);
                
                // Interpolación lineal
                float t = (time - previous.time) / (next.time - previous.time);
                return prevValue + (nextValue - prevValue) * t;
            }
            
            return defaultValue;
        }
        
        public List<Keyframe> getKeyframes() {
            return new ArrayList<>(keyframes);
        }
        
        public void save(PrintWriter writer) throws IOException {
            writer.println("    {");
            writer.println("      \"name\": \"" + name + "\",");
            writer.println("      \"duration\": " + duration + ",");
            writer.println("      \"loop\": " + loop + ",");
            writer.println("      \"keyframes\": [");
            
            for (int i = 0; i < keyframes.size(); i++) {
                Keyframe kf = keyframes.get(i);
                kf.save(writer);
                if (i < keyframes.size() - 1) writer.print(",");
                writer.println();
            }
            
            writer.println("      ]");
            writer.print("    }");
        }
    }
    
    /**
     * Representa un keyframe en una animación
     */
    public static class Keyframe {
        public final float time;
        private final Map<String, Float> channels = new HashMap<>();
        
        public Keyframe(float time) {
            this.time = time;
        }
        
        public void addChannelValue(String channel, float value) {
            channels.put(channel, value);
        }
        
        public float getChannelValue(String channel, float defaultValue) {
            return channels.getOrDefault(channel, defaultValue);
        }
        
        public Map<String, Float> getChannels() {
            return new HashMap<>(channels);
        }
        
        public void save(PrintWriter writer) throws IOException {
            writer.println("        {");
            writer.println("          \"time\": " + time + ",");
            writer.println("          \"channels\": {");
            
            boolean first = true;
            for (Map.Entry<String, Float> entry : channels.entrySet()) {
                if (!first) writer.println(",");
                writer.print("            \"" + entry.getKey() + "\": " + entry.getValue());
                first = false;
            }
            
            writer.println();
            writer.println("          }");
            writer.print("        }");
        }
    }
    
    /**
     * Reproduce animaciones para un objeto específico
     */
    public static class AnimationPlayer {
        public final String objectId;
        private final Map<String, Float> currentValues = new HashMap<>();
        
        public AnimationPlayer(String objectId) {
            this.objectId = objectId;
        }
        
        public void update(float time, Animation animation) {
            // Actualizar todos los canales
            for (Keyframe kf : animation.getKeyframes()) {
                for (String channel : kf.getChannels().keySet()) {
                    float value = animation.getChannelValue(channel, time, 0f);
                    currentValues.put(channel, value);
                }
            }
        }
        
        public float getValue(String channel, float defaultValue) {
            return currentValues.getOrDefault(channel, defaultValue);
        }
        
        public Map<String, Float> getAllValues() {
            return new HashMap<>(currentValues);
        }
    }
}
