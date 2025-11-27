package com.pythonmc.mod.nodes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nodo de audio sencillo para el Engine.
 * 
 * Versión inicial: reproduce sonidos existentes de Minecraft/
 * otros mods usando su ResourceLocation (ej: "minecraft:block.note_block.pling").
 * Más adelante podremos mapear sonidos personalizados desde
 * filesproject/sounds.
 */
public class AudioPlayerNode extends Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioPlayerNode.class);

    // Identificador del sonido (ResourceLocation en forma de string)
    private String soundId = "minecraft:block.note_block.pling";
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private boolean loop = false;
    private boolean playing = false;

    public AudioPlayerNode(String name) {
        super(name, NodeType.AUDIO_PLAYER);
    }

    @Override
    public void init() {
        LOGGER.info("AudioPlayerNode '{}' inicializado con sonido {}", getName(), soundId);
    }

    @Override
    public void update() {
        // En esta versión no hay lógica por tick salvo que queramos loop.
        if (loop && playing) {
            // El loop real requeriría gestionar tiempos/ticks; se añadirá más adelante.
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("AudioPlayerNode '{}' destruido", getName());
    }

    // ========== REPRODUCCIÓN ==========

    public void playOnce() {
        playInternal(false);
    }

    public void playLoop() {
        this.loop = true;
        playInternal(true);
    }

    public void stop() {
        this.playing = false;
        this.loop = false;
    }

    private void playInternal(boolean markPlaying) {
        Level world = getWorld();
        if (!(world instanceof ServerLevel serverLevel)) {
            LOGGER.warn("AudioPlayerNode '{}' intentó reproducir sonido en mundo no-servidor", getName());
            return;
        }

        SoundEvent soundEvent = resolveSoundEvent();
        if (soundEvent == null) {
            LOGGER.warn("AudioPlayerNode '{}' no pudo resolver el sonido '{}'", getName(), soundId);
            return;
        }

        serverLevel.playSound(
            null,
            getX(), getY(), getZ(),
            soundEvent,
            SoundSource.RECORDS,
            volume,
            pitch
        );

        this.playing = markPlaying;
    }

    private SoundEvent resolveSoundEvent() {
        try {
            ResourceLocation id = new ResourceLocation(soundId);
            return BuiltInRegistries.SOUND_EVENT.get(id);
        } catch (Exception e) {
            LOGGER.error("Error al resolver ResourceLocation '{}'", soundId, e);
            return null;
        }
    }

    // ========== GETTERS/SETTERS ==========

    public String getSoundId() {
        return soundId;
    }

    public void setSoundId(String soundId) {
        this.soundId = soundId;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(4.0f, volume));
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = Math.max(0.1f, Math.min(2.0f, pitch));
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isPlaying() {
        return playing;
    }
}
