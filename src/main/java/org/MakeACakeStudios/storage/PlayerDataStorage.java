package org.MakeACakeStudios.storage;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.MakeACakeStudios.MakeABuilders;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class PlayerDataStorage {

    public static PlayerDataStorage instance;
    private Connection connection;
    private static final Map<String, Integer> groupWeights = new HashMap<>();
    private static final Map<String, String> groupRoles = new HashMap<>();
    private static final Map<String, String> groupBadges = new HashMap<>();

    static {
        groupWeights.put("iam", 6);
        groupWeights.put("javadper", 5);
        groupWeights.put("lemon", 5);
        groupWeights.put("admin", 4);
        groupWeights.put("developer", 4);
        groupWeights.put("moderator", 3);
        groupWeights.put("sponsor", 2);
        groupWeights.put("donator", 1);

        groupRoles.put("iam", "owner");
        groupRoles.put("javadper", "custom");
        groupRoles.put("lemon", "custom");
        groupRoles.put("admin", "admin");
        groupRoles.put("developer", "developer");
        groupRoles.put("moderator", "moderator");
        groupRoles.put("sponsor", "sponsor");
        groupRoles.put("donator", "donator");

        groupBadges.put("iam", "<yellow>✦</yellow>");
        groupBadges.put("javadper", "<blue>\uD83D\uDEE0</blue>");
        groupBadges.put("admin", "<blue>⛨</blue>");
        groupBadges.put("developer", "<blue>\uD83D\uDEE0</blue>");
        groupBadges.put("moderator", "<blue>⛨</blue>");
        groupBadges.put("sponsor", "<gold>$</gold>");
        groupBadges.put("donator", "<color:#03d7fc>$</color>");
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
                    "role TEXT, " +
                    "badges TEXT, " +
                    "highest_badge TEXT DEFAULT '', " +
                    "donate INTEGER DEFAULT 0)");
            stmt.close();

            try {
                Statement alterStmt = connection.createStatement();
                alterStmt.execute("ALTER TABLE player_data ADD COLUMN donate INTEGER DEFAULT 0");
                alterStmt.close();
            } catch (SQLException ignored) {
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка подключения к базе данных", e);
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
        List<String> badgesList = new ArrayList<>();
        String highestBadge = "";

        if (user != null) {
            for (Node node : user.getNodes()) {
                if (node instanceof InheritanceNode) {
                    InheritanceNode inheritanceNode = (InheritanceNode) node;
                    String groupName = inheritanceNode.getGroupName();
                    if (groupWeights.containsKey(groupName)) {
                        int weight = groupWeights.get(groupName);

                        if (weight > highestWeight) {
                            highestWeight = weight;
                            highestGroup = groupName;
                            highestBadge = groupBadges.getOrDefault(groupName, "");
                        }

                        String badge = groupBadges.get(groupName);
                        if (badge != null && !badge.isEmpty() && !badgesList.contains(badge)) {
                            badgesList.add(badge);
                        }
                    }
                }
            }
        }

        if (badgesList.isEmpty()) {
            highestBadge = "";
        }

        String prefix, suffix;
        switch (highestGroup) {
            case "iam":
                prefix = "<gradient:#914EFF:#97ABFF>";
                suffix = "</gradient>";
                break;
            case "javadper":
                prefix = "<gradient:#E0E0E0:#808080>";
                suffix = "</gradient>";
                break;
            case "lemon":
                prefix = "<gradient:#FFFB3D:#FCF9BD>";
                suffix = "</gradient>";
                break;
            case "admin":
                prefix = "<gradient:#FF2323:#FF7878>";
                suffix = "</gradient>";
                break;
            case "developer":
                prefix = "<gradient:#E43A96:#FF0000>";
                suffix = "</gradient>";
                break;
            case "moderator":
                prefix = "<gradient:#23DBFF:#C8E9FF>";
                suffix = "</gradient>";
                break;
            case "sponsor":
                prefix = "<gradient:#00A53E:#C8FFD4>";
                suffix = "</gradient>";
                break;
            default:
                prefix = "";
                suffix = "";
        }

        String role = getRoleByGroup(highestGroup);
        String badges = String.join(", ", badgesList);

        setPlayerData(player.getName(), prefix, suffix, role, badges, highestBadge);
    }

    private String getRoleByGroup(String group) {
        return groupRoles.getOrDefault(group, "player");
    }

    public void setPlayerData(String playerName, String prefix, String suffix, String role, String badges, String highestBadge) {
        String query = "INSERT OR REPLACE INTO player_data (player_name, prefix, suffix, role, badges, highest_badge) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, playerName);
            stmt.setString(2, prefix);
            stmt.setString(3, suffix);
            stmt.setString(4, role);
            stmt.setString(5, badges);
            stmt.setString(6, highestBadge);
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

    public List<String> getPlayerBadges(String playerName) {
        Map<String, Integer> badgeWeightMap = new HashMap<>(); // Бейдж -> вес роли

        try (PreparedStatement stmt = connection.prepareStatement("SELECT badges FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String badges = rs.getString("badges");
                if (badges != null && !badges.isEmpty()) {
                    for (String badge : badges.split(", ")) {
                        for (Map.Entry<String, String> entry : groupBadges.entrySet()) {
                            if (entry.getValue().equals(badge)) {
                                int weight = groupWeights.getOrDefault(entry.getKey(), 0);
                                badgeWeightMap.put(badge, weight);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении бейджей игрока из базы данных", e);
        }

        return badgeWeightMap.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public String getHighestBadge(String playerName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT highest_badge FROM player_data WHERE player_name = ?")) {
            stmt.setString(1, playerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("highest_badge");
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении главного бейджа игрока", e);
        }
        return "";
    }
}
