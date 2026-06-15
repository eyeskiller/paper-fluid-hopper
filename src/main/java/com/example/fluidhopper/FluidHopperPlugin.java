package com.example.fluidhopper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import online.bechatbot.analytics.AnalyticsTracker;

public class FluidHopperPlugin extends JavaPlugin {

    private HopperManager hopperManager;

    @Override
    public void onEnable() {
        AnalyticsTracker analytics = new AnalyticsTracker(this, "https://analytics.bechatbot.online/api/track");
        analytics.sendEvent("STARTUP");

        // Save default config if needed, we just use a data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        hopperManager = new HopperManager(this);
        hopperManager.load();

        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Run tick task every 20 ticks (1 second)
        getServer().getScheduler().runTaskTimer(this, new HopperTickTask(this), 20L, 20L);

        // Register Crafting Recipe
        NamespacedKey recipeKey = new NamespacedKey(this, "fluid_hopper");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, CustomItemFactory.getFluidHopperItem(this));
        recipe.shape("I I", "IBI", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.BUCKET);
        getServer().addRecipe(recipe);

        getCommand("fluidhopper").setExecutor(this);

        getLogger().info("FluidHopper has been enabled!");
    }

    @Override
    public void onDisable() {
        if (hopperManager != null) {
            hopperManager.save();
        }
        getLogger().info("FluidHopper has been disabled!");
    }

    public HopperManager getHopperManager() {
        return hopperManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("fluidhopper")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("give")) {
                if (sender instanceof Player player) {
                    if (player.hasPermission("fluidhopper.admin")) {
                        player.getInventory().addItem(CustomItemFactory.getFluidHopperItem(this));
                        player.sendMessage("§aYou have been given a Fluid Hopper!");
                    } else {
                        player.sendMessage("§cYou do not have permission.");
                    }
                } else {
                    sender.sendMessage("This command is for players only.");
                }
                return true;
            }
        }
        return false;
    }
}
