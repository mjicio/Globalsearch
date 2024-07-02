package it.mjicio.rareore.managers;

import it.mjicio.rareore.GlobalSearch;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private final GlobalSearch plugin;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    public DatabaseManager(GlobalSearch plugin) {
        this.plugin = plugin;

        this.host = plugin.getConfig().getString("database.host");
        this.port = plugin.getConfig().getInt("database.port");
        this.database = plugin.getConfig().getString("database.database");
        this.username = plugin.getConfig().getString("database.username");
        this.password = plugin.getConfig().getString("database.password");


        connect();
        createTables();
    }

    private void connect() {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("Connected to database!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {

            String createContainersTable = "CREATE TABLE IF NOT EXISTS containers (" +
                    "type VARCHAR(255), " +
                    "world VARCHAR(255), " +
                    "x INT, " +
                    "y INT, " +
                    "z INT, " +
                    "PRIMARY KEY (world, x, y, z)" +
                    ")";
            statement.executeUpdate(createContainersTable);


            String createContainerContentsTable = "CREATE TABLE IF NOT EXISTS container_contents (" +
                    "container_type VARCHAR(255), " +
                    "world VARCHAR(255), " +
                    "x INT, " +
                    "y INT, " +
                    "z INT, " +
                    "slot INT, " +
                    "item TEXT, " +
                    "PRIMARY KEY (world, x, y, z, slot)" +
                    ")";
            statement.executeUpdate(createContainerContentsTable);

            plugin.getLogger().info("Database tables created or already exist!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Disconnected from database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveContainer(BlockState blockState) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO containers (type, world, x, y, z) VALUES (?, ?, ?, ?, ?)"
            );

            if (blockState instanceof Chest) {
                statement.setString(1, "chest");
            } else if (blockState instanceof Barrel) {
                statement.setString(1, "barrel");
            } else if (blockState instanceof ShulkerBox) {
                statement.setString(1, "shulker_box");
            }

            statement.setString(2, blockState.getWorld().getName());
            statement.setInt(3, blockState.getX());
            statement.setInt(4, blockState.getY());
            statement.setInt(5, blockState.getZ());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeContainer(Location location) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM containers WHERE world = ? AND x = ? AND y = ? AND z = ?"
            );
            statement.setString(1, location.getWorld().getName());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockY());
            statement.setInt(4, location.getBlockZ());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveItem(BlockState blockState, int slot, String serializedItem) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO container_contents (container_type, world, x, y, z, slot, item) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );

            if (blockState instanceof Chest) {
                statement.setString(1, "chest");
            } else if (blockState instanceof Barrel) {
                statement.setString(1, "barrel");
            } else if (blockState instanceof ShulkerBox) {
                statement.setString(1, "shulker_box");
            }

            statement.setString(2, blockState.getWorld().getName());
            statement.setInt(3, blockState.getX());
            statement.setInt(4, blockState.getY());
            statement.setInt(5, blockState.getZ());
            statement.setInt(6, slot);
            statement.setString(7, serializedItem);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeItem(BlockState blockState, int slot) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM container_contents WHERE container_type = ? AND world = ? AND x = ? AND y = ? AND z = ? AND slot = ?"
            );

            if (blockState instanceof Chest) {
                statement.setString(1, "chest");
            } else if (blockState instanceof Barrel) {
                statement.setString(1, "barrel");
            } else if (blockState instanceof ShulkerBox) {
                statement.setString(1, "shulker_box");
            }

            statement.setString(2, blockState.getWorld().getName());
            statement.setInt(3, blockState.getX());
            statement.setInt(4, blockState.getY());
            statement.setInt(5, blockState.getZ());
            statement.setInt(6, slot);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Location> findContainersWithItem(String serializedItem) {
        List<Location> locations = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT world, x, y, z FROM container_contents WHERE item = ?"
            );
            statement.setString(1, serializedItem);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String worldName = resultSet.getString("world");
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                locations.add(location);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }
}
