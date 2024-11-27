package com.resourcegame.core;

import java.io.*;
import java.util.Properties;

public class GameSettings {
    private static final String SETTINGS_FILE = "settings.properties";
    private static GameSettings instance;
    private Properties properties;
    
    // Settings keys
    public static final String SOUND_ENABLED = "sound.enabled";
    public static final String MUSIC_ENABLED = "music.enabled";
    public static final String VOLUME_LEVEL = "volume.level";
    public static final String AUTOSAVE_INTERVAL = "autosave.interval"; // in minutes
    
    private GameSettings() {
        properties = new Properties();
        loadDefaults();
        loadSettings();
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    private void loadDefaults() {
        properties.setProperty(SOUND_ENABLED, "true");
        properties.setProperty(MUSIC_ENABLED, "true");
        properties.setProperty(VOLUME_LEVEL, "50");
        properties.setProperty(AUTOSAVE_INTERVAL, "5");
    }
    
    private void loadSettings() {
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            properties.load(in);
        } catch (IOException e) {
            System.out.println("No settings file found, using defaults");
        }
    }
    
    public void saveSettings() {
        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(out, "Game Settings");
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
    
    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(properties.getProperty(SOUND_ENABLED));
    }
    
    public void setSoundEnabled(boolean enabled) {
        properties.setProperty(SOUND_ENABLED, String.valueOf(enabled));
    }
    
    public boolean isMusicEnabled() {
        return Boolean.parseBoolean(properties.getProperty(MUSIC_ENABLED));
    }
    
    public void setMusicEnabled(boolean enabled) {
        properties.setProperty(MUSIC_ENABLED, String.valueOf(enabled));
    }
    
    public int getVolumeLevel() {
        return Integer.parseInt(properties.getProperty(VOLUME_LEVEL));
    }
    
    public void setVolumeLevel(int level) {
        properties.setProperty(VOLUME_LEVEL, String.valueOf(level));
    }
    
    public int getAutosaveInterval() {
        return Integer.parseInt(properties.getProperty(AUTOSAVE_INTERVAL));
    }
    
    public void setAutosaveInterval(int minutes) {
        properties.setProperty(AUTOSAVE_INTERVAL, String.valueOf(minutes));
    }
}