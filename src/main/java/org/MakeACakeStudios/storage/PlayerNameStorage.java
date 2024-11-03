package org.MakeACakeStudios.storage;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.MakeABuilders;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerNameStorage {

    private final MakeABuilders plugin;
    private Connection connection;
    private static final Map<String, Integer> groupWeights = new HashMap<>();

    static {
        groupWeights.put("iam", 5);
        groupWeights.put("javadper", 4);
        groupWeights.put("yosya", 4);
        groupWeights.put("admin", 3);
        groupWeights.put("developer", 2);
        groupWeights.put("moderator", 1);
        groupWeights.put("sponsor", 1);
    }

    public PlayerNameStorage(MakeABuilders plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/player_data.db");
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "player_name TEXT PRIMARY KEY, " +
                    "prefix TEXT, " +
                    "suffix TEXT)");
            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось подключиться к базе данных SQLite!", e);
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getPlayerPrefix(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String highestGroup = "default";
        int highestWeight = 0;

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (groupWeights.containsKey(groupName) && groupWeights.get(groupName) > highestWeight) {
                        highestWeight = groupWeights.get(groupName);
                        highestGroup = groupName;
                    }
                }
            }
        }

        String prefix;
        switch (highestGroup) {
            case "iam":
                prefix = "<yellow>\uD83D\uDC51</yellow> <gradient:#DEA903:#FFEDB5>";
                break;
            case "javadper":
                prefix = "<blue>\uD83D\uDEE0</blue> <gradient:#E0E0E0:#808080>";
                break;
            case "yosya":
                prefix = "<light_purple>\uD83D\uDC08</light_purple> <gradient:#DD00CC:#FFC8F6>";
                break;
            case "admin":
                prefix = "<blue>\uD83D\uDDE1</blue> <gradient:#FF2323:#FF7878>";
                break;
            case "developer":
                prefix = "<blue>\uD83D\uDEE0</blue> <gradient:#141378:#97ABFF>";
                break;
            case "moderator":
                prefix = "<red>\uD83D\uDD31</red> <gradient:#23DBFF:#C8E9FF>";
                break;
            case "sponsor":
                prefix = "<gold>$</gold> <gradient:#00A53E:#C8FFD4>";
                break;
            default:
                prefix = "";
        }

        setPlayerPrefix(player.getName(), prefix);
        return prefix;
    }

    public String getPlayerSuffix(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String highestGroup = "default";
        int highestWeight = 0;

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (groupWeights.containsKey(groupName) && groupWeights.get(groupName) > highestWeight) {
                        highestWeight = groupWeights.get(groupName);
                        highestGroup = groupName;
                    }
                }
            }
        }

        String suffix = highestGroup.equals("default") ? "" : "</gradient>";
        setPlayerSuffix(player.getName(), suffix);
        return suffix;
    }

    public String getPlayerPrefixByName(String playerName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT prefix FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("prefix");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении префикса из базы данных", e);
        }
        return "";
    }

    public String getPlayerSuffixByName(String playerName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT suffix FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("suffix");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при получении суффикса из базы данных", e);
        }
        return "";
    }

    public void setPlayerPrefix(String playerName, String prefix) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_data (player_name, prefix, suffix) VALUES (?, ?, (SELECT suffix FROM player_data WHERE player_name = ?))")) {
            stmt.setString(1, playerName);
            stmt.setString(2, prefix);
            stmt.setString(3, playerName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при сохранении префикса в базе данных", e);
        }
    }

    public void setPlayerSuffix(String playerName, String suffix) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_data (player_name, prefix, suffix) VALUES (?, (SELECT prefix FROM player_data WHERE player_name = ?), ?)")) {
            stmt.setString(1, playerName);
            stmt.setString(2, playerName);
            stmt.setString(3, suffix);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при сохранении суффикса в базе данных", e);
        }
    }
}