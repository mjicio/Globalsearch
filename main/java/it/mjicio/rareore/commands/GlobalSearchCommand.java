package it.mjicio.rareore.commands;

import it.mjicio.rareore.GlobalSearch;
import it.mjicio.rareore.managers.DatabaseManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class GlobalSearchCommand implements CommandExecutor {

    private final DatabaseManager databaseManager;
    private final GlobalSearch plugin;

    public GlobalSearchCommand(DatabaseManager databaseManager, GlobalSearch plugin) {
        this.databaseManager = databaseManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("only-player")));
            return true;
        }

        if(sender.hasPermission("globalsearch.use")) {
        Player player = (Player) sender;
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || !itemInHand.hasItemMeta()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("item-in-hand")));
            return true;
        }


        String serializedItem = serializeItem(itemInHand);

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "GLOBALSEARCH");
        List<Location> containerLocations = databaseManager.findContainersWithItem(serializedItem);
        if (!containerLocations.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("container-found")));
            for (Location location : containerLocations) {
                String coordinates = location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
                TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("container-coordinates")).replace("%coordinates%", coordinates));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + coordinates));
                player.spigot().sendMessage(message);
            }
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("container-not-found")));
        }


        boolean foundInPlayers = false;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (searchInPlayerInventory(onlinePlayer, itemInHand)) {
                TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("player-found")).replace("%player-found%", onlinePlayer.getName()));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openinv " + onlinePlayer.getName()));
                player.spigot().sendMessage(message);
                foundInPlayers = true;
            }
        }


        if (!searchInOfflinePlayerInventories(itemInHand, player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("offline-player-not-found")));
        }
    }

    return true;

}

    private boolean searchInPlayerInventory(Player player, ItemStack targetItem) {
        for (ItemStack item : player.getInventory()) {
            if (item != null && item.isSimilar(targetItem)) {
                return true;
            }
        }
        return false;
    }

    private boolean searchInOfflinePlayerInventories(ItemStack targetItem, Player sender) {
        boolean found = false;
        File playerInventoriesFolder = new File(plugin.getDataFolder(), "player_inventories");

        for (File file : playerInventoriesFolder.listFiles()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<ItemStack> inventoryContents = (List<ItemStack>) config.get("inventory");

            if (inventoryContents != null) {
                for (ItemStack item : inventoryContents) {
                    if (item != null && item.isSimilar(targetItem)) {
                        String playerName = Bukkit.getOfflinePlayer(UUID.fromString(file.getName().replace(".yml", ""))).getName();
                        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("offline-player-found")).replace("%offline-player-found%", playerName));
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openinv " + playerName));
                        sender.spigot().sendMessage(message);
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
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
