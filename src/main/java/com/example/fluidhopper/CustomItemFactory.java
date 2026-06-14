package com.example.fluidhopper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomItemFactory {

    public static final String FLUID_HOPPER_KEY = "is_fluid_hopper";
    public static final String FLUID_TYPE_KEY = "fluid_type";

    public static ItemStack getFluidHopperItem(FluidHopperPlugin plugin) {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Fluid Hopper").color(NamedTextColor.GOLD));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, FLUID_HOPPER_KEY), PersistentDataType.BYTE, (byte) 1);
            meta.setCustomModelData(1001); // Can be used for resource packs
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getLavaFluidItem(FluidHopperPlugin plugin) {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("1000mB Lava").color(NamedTextColor.RED));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, FLUID_TYPE_KEY), PersistentDataType.STRING, "lava");
            meta.setCustomModelData(2001);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack getWaterFluidItem(FluidHopperPlugin plugin) {
        ItemStack item = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("1000mB Water").color(NamedTextColor.BLUE));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, FLUID_TYPE_KEY), PersistentDataType.STRING, "water");
            meta.setCustomModelData(2002);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isFluidHopperItem(FluidHopperPlugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, FLUID_HOPPER_KEY), PersistentDataType.BYTE);
    }

    public static String getFluidType(FluidHopperPlugin plugin, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, FLUID_TYPE_KEY), PersistentDataType.STRING);
    }
}
