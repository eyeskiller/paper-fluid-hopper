package com.example.fluidhopper;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {

    private final FluidHopperPlugin plugin;

    public EventListener(FluidHopperPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (CustomItemFactory.isFluidHopperItem(plugin, item)) {
            plugin.getHopperManager().addHopper(event.getBlock().getLocation());
        }

        // Prevent placing the dummy fluid items
        String fluidType = CustomItemFactory.getFluidType(plugin, item);
        if (fluidType != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.HOPPER && plugin.getHopperManager().isFluidHopper(block.getLocation())) {
            plugin.getHopperManager().removeHopper(block.getLocation());
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), CustomItemFactory.getFluidHopperItem(plugin));
            // Let the internal inventory drop normally
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null) {
            String fluidType = CustomItemFactory.getFluidType(plugin, clickedItem);
            if (fluidType != null && event.getWhoClicked() instanceof Player player && !player.hasPermission("fluidhopper.admin")) {
                event.setCancelled(true);
                player.sendMessage(net.kyori.adventure.text.Component.text("You cannot extract fluids directly as an item!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(org.bukkit.event.inventory.InventoryMoveItemEvent event) {
        if (event.getDestination().getLocation() != null && plugin.getHopperManager().isFluidHopper(event.getDestination().getLocation())) {
            // It's moving into a fluid hopper
            if (CustomItemFactory.getFluidType(plugin, event.getItem()) == null) {
                // Not a fluid item
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryPickupItem(org.bukkit.event.inventory.InventoryPickupItemEvent event) {
        if (event.getInventory().getLocation() != null && plugin.getHopperManager().isFluidHopper(event.getInventory().getLocation())) {
            // Fluid hopper picking up item from ground
            if (CustomItemFactory.getFluidType(plugin, event.getItem().getItemStack()) == null) {
                event.setCancelled(true);
            }
        }
    }
}
