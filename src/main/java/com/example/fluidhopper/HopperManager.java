package com.example.fluidhopper;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class HopperManager {
    private final FluidHopperPlugin plugin;
    private final Set<Location> fluidHoppers = ConcurrentHashMap.newKeySet();
    private final File dataFile;
    private final Object saveLock = new Object();

    public HopperManager(FluidHopperPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "hoppers.yml");
    }

    public void addHopper(Location location) {
        fluidHoppers.add(location);
        saveAsync();
    }

    public void removeHopper(Location location) {
        fluidHoppers.remove(location);
        saveAsync();
    }

    public boolean isFluidHopper(Location location) {
        return fluidHoppers.contains(location);
    }

    public Set<Location> getFluidHoppers() {
        return fluidHoppers;
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        List<?> list = config.getList("locations");
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Location) {
                    fluidHoppers.add((Location) obj);
                }
            }
        }
    }

    public void save() {
        Location[] locationsToSave = fluidHoppers.toArray(new Location[0]);
        synchronized (saveLock) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("locations", locationsToSave);
            try {
                config.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save hoppers.yml", e);
            }
        }
    }

    private void saveAsync() {
        Location[] locationsToSave = fluidHoppers.toArray(new Location[0]);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (saveLock) {
                YamlConfiguration config = new YamlConfiguration();
                config.set("locations", locationsToSave);
                try {
                    config.save(dataFile);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save hoppers.yml", e);
                }
            }
        });
    }
}
