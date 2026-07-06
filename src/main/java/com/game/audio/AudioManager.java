package com.game.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiny placeholder audio layer built on {@code javax.sound.sampled} (no extra
 * dependency). One looping music track and one looping ambience track can play
 * at once, plus fire-and-forget SFX. Everything degrades to silence if no audio
 * device is available (e.g. a headless machine), so callers never need to guard.
 */
public final class AudioManager {

    private static final Logger LOGGER = Logger.getLogger(AudioManager.class.getName());

    public static final String MAIN_THEME    = "/audio/main_theme.wav";
    public static final String TENSE_THEME   = "/audio/tense_theme.wav";
    public static final String RIOT_AMBIENCE = "/audio/riot_ambience.wav";
    public static final String UI_CLICK      = "/audio/ui_click.wav";

    private static final Map<String, Clip> CACHE = new HashMap<>();
    private static boolean enabled = true;

    private static Clip musicClip;
    private static String musicTrack;
    private static Clip ambienceClip;
    private static String ambienceTrack;

    private AudioManager() {}

    public static void setEnabled(boolean on) {
        enabled = on;
        if (!on) { stopMusic(); setAmbience(null); }
    }

    /** Calm background music, no ambience — the default for menus and quiet scenes. */
    public static void menuMood() {
        playMusic(MAIN_THEME);
        setAmbience(null);
    }

    /** Loops {@code resource} as background music; no-op if that track is already playing. */
    public static synchronized void playMusic(String resource) {
        if (!enabled || resource == null || resource.equals(musicTrack)) return;
        stopMusic();
        Clip clip = clip(resource);
        if (clip == null) return;
        setGain(clip, -12f);
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        musicClip = clip;
        musicTrack = resource;
    }

    public static synchronized void stopMusic() {
        if (musicClip != null) musicClip.stop();
        musicClip = null;
        musicTrack = null;
    }

    /** Loops a quiet ambience track, or stops it when {@code resource} is null. */
    public static synchronized void setAmbience(String resource) {
        if (resource != null && resource.equals(ambienceTrack)) return;
        if (ambienceClip != null) ambienceClip.stop();
        ambienceClip = null;
        ambienceTrack = null;
        if (!enabled || resource == null) return;
        Clip clip = clip(resource);
        if (clip == null) return;
        setGain(clip, -18f); // "leises Riot-Gebrüll"
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        ambienceClip = clip;
        ambienceTrack = resource;
    }

    /** Plays a one-shot sound effect from the start. */
    public static synchronized void playSfx(String resource) {
        if (!enabled) return;
        Clip clip = clip(resource);
        if (clip == null) return;
        setGain(clip, -6f);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    private static Clip clip(String resource) {
        Clip cached = CACHE.get(resource);
        if (cached != null) return cached;
        try (InputStream raw = AudioManager.class.getResourceAsStream(resource)) {
            if (raw == null) {
                LOGGER.warning("Audio resource not found: " + resource);
                return null;
            }
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                CACHE.put(resource, clip);
                return clip;
            }
        } catch (Exception e) {
            // No mixer / unsupported format / headless — disable audio quietly.
            enabled = false;
            LOGGER.log(Level.INFO, "Audio unavailable; continuing silently (" + e.getMessage() + ")");
            return null;
        }
    }

    private static void setGain(Clip clip, float db) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
            }
        } catch (Exception ignored) {
            // gain control is optional
        }
    }
}
