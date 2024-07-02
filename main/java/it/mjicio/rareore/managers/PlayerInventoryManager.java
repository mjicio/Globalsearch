package it.mjicio.rareore.managers;

import it.mjicio.rareore.GlobalSearch;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerInventoryManager implements Listener {

    private final GlobalSearch plugin;

    public PlayerInventoryManager(GlobalSearch plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        savePlayerInventory(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        deletePlayerInventoryFile(player.getUniqueId());
    }

    private void savePlayerInventory(Player player) {
        File playerFile = new File(plugin.getDataFolder(), "player_inventories/" + player.getUniqueId().toString() + ".yml");
        YamlConfiguration playerConfig = new YamlConfiguration();

        List<ItemStack> inventoryContents = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            inventoryContents.add(item);
        }

        playerConfig.set("inventory", inventoryContents);

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePlayerInventoryFile(UUID playerUUID) {
        File playerFile = new File(plugin.getDataFolder(), "player_inventories/" + playerUUID.toString() + ".yml");
        if (playerFile.exists()) {
            playerFile.delete();
        }
    }
}
