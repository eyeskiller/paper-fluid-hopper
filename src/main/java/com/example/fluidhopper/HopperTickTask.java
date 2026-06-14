package com.example.fluidhopper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class HopperTickTask implements Runnable {

    private final FluidHopperPlugin plugin;

    public HopperTickTask(FluidHopperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Iterator<Location> iterator = plugin.getHopperManager().getFluidHoppers().iterator();
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            if (!loc.isWorldLoaded()) continue;

            Block block = loc.getBlock();
            if (block.getType() != Material.HOPPER) {
                iterator.remove();
                plugin.getHopperManager().save();
                continue;
            }

            if (block.getState() instanceof Hopper hopper) {
                processIntake(hopper);
                processOutput(hopper, block);
            }
        }
    }

    private void processIntake(Hopper hopper) {
        Block up = hopper.getBlock().getRelative(BlockFace.UP);
        Material type = up.getType();

        if (type == Material.LAVA_CAULDRON) {
            if (hasSpaceFor(hopper.getInventory(), CustomItemFactory.getLavaFluidItem(plugin))) {
                up.setType(Material.CAULDRON);
                hopper.getInventory().addItem(CustomItemFactory.getLavaFluidItem(plugin));
            }
        } else if (type == Material.WATER_CAULDRON) {
            if (up.getBlockData() instanceof Levelled levelled && levelled.getLevel() == levelled.getMaximumLevel()) {
                if (hasSpaceFor(hopper.getInventory(), CustomItemFactory.getWaterFluidItem(plugin))) {
                    up.setType(Material.CAULDRON);
                    hopper.getInventory().addItem(CustomItemFactory.getWaterFluidItem(plugin));
                }
            }
        }
    }

    private void processOutput(Hopper hopperState, Block hopperBlock) {
        if (!(hopperBlock.getBlockData() instanceof org.bukkit.block.data.type.Hopper hopperData)) return;

        Block target = hopperBlock.getRelative(hopperData.getFacing());
        Inventory hopperInv = hopperState.getInventory();

        ItemStack lavaItem = null;
        for (ItemStack item : hopperInv.getContents()) {
            if ("lava".equals(CustomItemFactory.getFluidType(plugin, item))) {
                lavaItem = item;
                break;
            }
        }

        ItemStack waterItem = null;
        for (ItemStack item : hopperInv.getContents()) {
            if ("water".equals(CustomItemFactory.getFluidType(plugin, item))) {
                waterItem = item;
                break;
            }
        }

        // Furnace Fueling
        if (target.getState() instanceof Furnace furnace) {
            if (lavaItem != null && furnace.getBurnTime() <= 200) {
                // Short max is 32767, lava bucket is 20000
                furnace.setBurnTime((short) (furnace.getBurnTime() + 20000));
                furnace.update();
                
                lavaItem.setAmount(lavaItem.getAmount() - 1);
                return;
            }
        }

        // Filling Cauldrons
        if (target.getType() == Material.CAULDRON) {
            if (lavaItem != null) {
                target.setType(Material.LAVA_CAULDRON);
                lavaItem.setAmount(lavaItem.getAmount() - 1);
                return;
            }
            if (waterItem != null) {
                target.setType(Material.WATER_CAULDRON);
                if (target.getBlockData() instanceof Levelled levelled) {
                    levelled.setLevel(levelled.getMaximumLevel());
                    target.setBlockData(levelled);
                }
                waterItem.setAmount(waterItem.getAmount() - 1);
                return;
            }
        }
    }

    private boolean hasSpaceFor(Inventory inventory, ItemStack item) {
        for (ItemStack slot : inventory.getStorageContents()) {
            if (slot == null || slot.getType() == Material.AIR) return true;
            if (slot.isSimilar(item) && slot.getAmount() < slot.getMaxStackSize()) return true;
        }
        return false;
    }
}
