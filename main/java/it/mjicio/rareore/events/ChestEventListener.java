package it.mjicio.rareore.events;

import it.mjicio.rareore.managers.DatabaseManager;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestEventListener implements Listener {

    private final DatabaseManager databaseManager;

    public ChestEventListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getState() instanceof Chest || block.getState() instanceof Barrel || block.getState() instanceof ShulkerBox) {
            databaseManager.saveContainer(block.getState());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof Chest || block.getState() instanceof Barrel || block.getState() instanceof ShulkerBox) {
            // Remove the container and its contents from the database
            databaseManager.removeContainer(block.getState().getLocation());
            Inventory inventory = ((Container) block.getState()).getInventory();
            for (int i = 0; i < inventory.getSize(); i++) {
                databaseManager.removeItem(block.getState(), i);
            }
        }
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();

        if (clickedInventory == null || topInventory == null) {
            return;
        }

        if (topInventory.getHolder() instanceof Chest || topInventory.getHolder() instanceof Barrel || topInventory.getHolder() instanceof ShulkerBox) {
            int slot = event.getRawSlot();

            // Shift click from player inventory to container
            if (event.isShiftClick() && clickedInventory.equals(event.getWhoClicked().getInventory())) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && !currentItem.getType().isAir()) {
                    // Find an empty slot in the container
                    for (int i = 0; i < topInventory.getSize(); i++) {
                        if (topInventory.getItem(i) == null || topInventory.getItem(i).getType().isAir()) {
                            String serializedItem = serializeItem(currentItem);
                            databaseManager.saveItem((BlockState) topInventory.getHolder(), i, serializedItem);
                            break;
                        }
                    }
                }
            }
            // Shift click from container to player inventory
            else if (event.isShiftClick() && clickedInventory.equals(topInventory)) {
                ItemStack currentItem = event.getCurrentItem();
                if (currentItem != null && !currentItem.getType().isAir()) {
                    databaseManager.removeItem((BlockState) clickedInventory.getHolder(), slot);
                }
            }
            // Regular click within container
            else if (slot < topInventory.getSize()) { // Ensure we are dealing with the container inventory
                ItemStack currentItem = event.getCurrentItem();
                ItemStack cursorItem = event.getCursor();

                // Handle removing the current item from the container
                if (currentItem != null && !currentItem.getType().isAir()) {
                    databaseManager.removeItem((BlockState) clickedInventory.getHolder(), slot);
                }

                // Handle adding the cursor item to the container
                if (cursorItem != null && !cursorItem.getType().isAir()) {
                    String serializedItem = serializeItem(cursorItem);
                    databaseManager.saveItem((BlockState) clickedInventory.getHolder(), slot, serializedItem);
                }
            }
        }
    }

    private String serializeItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String serializedItem = item.getType().name();

        if (meta != null) {
            if (meta.hasDisplayName()) {
                serializedItem += ";" + meta.getDisplayName();
            }
            if (meta.hasLore()) {
                serializedItem += ";" + String.join(",", meta.getLore());
            }
            if (meta.hasCustomModelData()) {
                serializedItem += ";" + meta.getCustomModelData();
            }
        }

        return serializedItem;
    }
}
