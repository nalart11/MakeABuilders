package org.MakeACakeStudios.storage;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.MakeABuilders;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PlayerDataStorage {

    public static PlayerDataStorage instance;
    private Connection connection;
    private static final Map<String, Integer> groupWeights = new HashMap<>();
    private static final Map<String, String> groupRoles = new HashMap<>();

    static {
        groupWeights.put("iam", 6);
        groupWeights.put("javadper", 5);
        groupWeights.put("yosya", 5);
        groupWeights.put("admin", 4);
        groupWeights.put("developer", 3);
        groupWeights.put("moderator", 2);
        groupWeights.put("sponsor", 1);

        groupRoles.put("iam", "owner");
        groupRoles.put("javadper", "main_developer");
        groupRoles.put("yosya", "yosya");
        groupRoles.put("admin", "admin");
        groupRoles.put("developer", "developer");
        groupRoles.put("moderator", "moderator");
        groupRoles.put("sponsor", "sponsor");
    }

    public PlayerDataStorage() {
        instance = this;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + MakeABuilders.instance.getDataFolder() + "/player_data.db");
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS player_data (" +
                    "player_name TEXT PRIMARY KEY, " +
                    "prefix TEXT, " +
                    "suffix TEXT, " +
                    "role TEXT)");
            stmt.close();
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Не удалось подключиться к базе данных SQLite!", e);
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

    public void updatePlayerData(Player player) {
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

        String prefix, suffix;
        switch (highestGroup) {
            case "iam":
                prefix = "<yellow>✦</yellow> <gradient:#914EFF:#97ABFF>";
                suffix = "</gradient>";
                break;
            case "javadper":
                prefix = "<blue>\uD83D\uDEE0</blue> <gradient:#E0E0E0:#808080>";
                suffix = "</gradient>";
                break;
            case "yosya":
                prefix = "<color:#CBC3E3>\uD83D\uDC08</color> <gradient:#DD00CC:#FFC8F6>";
                suffix = "</gradient>";
                break;
            case "admin":
                prefix = "<blue>⛨</blue> <gradient:#FF2323:#FF7878>";
                suffix = "</gradient>";
                break;
            case "developer":
                prefix = "<blue>\uD83D\uDEE0</blue> <gradient:#E43A96:#FF0000>";
                suffix = "</gradient>";
                break;
            case "moderator":
                prefix = "<blue>⛨</blue> <gradient:#23DBFF:#C8E9FF>";
                suffix = "</gradient>";
                break;
            case "sponsor":
                prefix = "<gold>$</gold> <gradient:#00A53E:#C8FFD4>";
                suffix = "</gradient>";
                break;
            default:
                prefix = "";
                suffix = "";
        }

        String role = getRoleByGroup(highestGroup);
        setPlayerData(player.getName(), prefix, suffix, role);
    }

    private String getRoleByGroup(String group) {
        return groupRoles.getOrDefault(group, "player");
    }

    public void setPlayerData(String playerName, String prefix, String suffix, String role) {
        String query = "INSERT OR REPLACE INTO player_data (player_name, prefix, suffix, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);
            stmt.setString(2, prefix);
            stmt.setString(3, suffix);
            stmt.setString(4, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при сохранении данных игрока в базе данных", e);
        }
    }

    public String getPlayerPrefixByName(String playerName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT prefix FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("prefix");
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении префикса из базы данных", e);
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
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении суффикса из базы данных", e);
        }
        return "";
    }

    public String getPlayerRoleByName(String playerName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT role FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении роли из базы данных", e);
        }
        return "player";
    }

    public boolean playerExistsInDatabase(String playerName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при проверке наличия игрока в базе данных", e);
        }
        return false;
    }

    public List<String> getAllPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT player_name FROM player_data")) {
            while (rs.next()) {
                playerNames.add(rs.getString("player_name"));
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении имен игроков из базы данных", e);
        }
        return playerNames;
    }

    public int getGroupWeight(Player player) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            int highestWeight = 0;
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (groupWeights.containsKey(groupName)) {
                        highestWeight = Math.max(highestWeight, groupWeights.get(groupName));
                    }
                }
            }
            return highestWeight;
        }
        return 0;
    }
}
