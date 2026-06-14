package com.example.fluidhopper;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class HopperManager {
    private final FluidHopperPlugin plugin;
    private final Set<Location> fluidHoppers = new HashSet<>();
    private final File dataFile;

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
        YamlConfiguration config = new YamlConfiguration();
        config.set("locations", fluidHoppers.toArray(new Location[0]));
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save hoppers.yml", e);
        }
    }

    private void saveAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::save);
    }
}
