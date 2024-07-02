package it.mjicio.rareore;

import it.mjicio.rareore.commands.GlobalSearchCommand;
import it.mjicio.rareore.events.ChestEventListener;
import it.mjicio.rareore.managers.DatabaseManager;
import it.mjicio.rareore.managers.PlayerInventoryManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class GlobalSearch extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {

        File playerInventoriesFolder = new File(getDataFolder(), "player_inventories");
        if (!playerInventoriesFolder.exists()) {
            playerInventoriesFolder.mkdirs();
        }

        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);

        getServer().getPluginManager().registerEvents(new ChestEventListener(databaseManager), this);

        getCommand("globalsearch").setExecutor(new GlobalSearchCommand(databaseManager, this));
        getServer().getPluginManager().registerEvents(new PlayerInventoryManager(this), this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }
}
