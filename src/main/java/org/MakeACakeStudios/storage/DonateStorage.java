package org.MakeACakeStudios.storage;

import java.sql.*;
import java.util.*;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class DonateStorage {

    private Connection connection;
    private final String dbPath;
    public static DonateStorage instance;

    // Таблица эффектов донатов
    public static final Map<String, Integer> DONATE_EFFECTS = new HashMap<>() {{
        put("Zeus", 1);
        put("Star", 2);
        put("Sakura", 3);
        put("Iam", 11);
        put("Birthday", 12);
        put("Vanila", 13);
    }};

    public DonateStorage(String dbPath) {
        this.dbPath = dbPath;
        instance = this;
        connectToDatabase();
        initializeDatabase();
    }

    private void connectToDatabase() {
        try {
            // Формируем URL подключения
            String url = "jdbc:sqlite:" + dbPath + "/donations.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Подключение к базе данных SQLite установлено.");
        } catch (SQLException e) {
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
        }
    }

    private void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS donations (" +
                "player_uuid TEXT PRIMARY KEY, " +
                "player_name TEXT, " +
                "total_donations INTEGER DEFAULT 0, " +
                "purchased_donations TEXT DEFAULT ''" +
                ")";
        try {
            connection.createStatement().execute(createTableSQL);
            System.out.println("Таблица для хранения донатов создана или уже существует.");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы донатов: " + e.getMessage());
        }
    }

    private String getPlayerUUID(String playerName) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return op.getUniqueId().toString();
    }

    private String getPlayerName(String playerName) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        return op.getName();
    }

    public int getTotalDonations(String playerName) {
        String uuid = getPlayerUUID(playerName);
        String sql = "SELECT total_donations FROM donations WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_donations");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addDonationAmount(String playerName, int amount) {
        int currentAmount = getTotalDonations(playerName);
        updateDonationAmount(playerName, currentAmount + amount);
    }

    public void removeDonationAmount(String playerName, int amount) {
        int currentAmount = getTotalDonations(playerName);
        updateDonationAmount(playerName, Math.max(0, currentAmount - amount));
    }

    private void updateDonationAmount(String playerName, int newAmount) {
        String uuid = getPlayerUUID(playerName);
        String currentName = getPlayerName(playerName);
        String sql = "INSERT INTO donations (player_uuid, player_name, total_donations, purchased_donations) " +
                "VALUES (?, ?, ?, '') " +
                "ON CONFLICT(player_uuid) DO UPDATE SET total_donations = excluded.total_donations, " +
                "player_name = excluded.player_name";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, currentName);
            stmt.setInt(3, newAmount);
            stmt.executeUpdate();
            System.out.println("Сумма донатов для " + currentName + " обновлена: " + newAmount);

            updatePlayerGroup(playerName, newAmount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDonation(String playerName, int donationId) {
        Set<String> donationSet = getPurchasedDonations(playerName);
        donationSet.add(String.valueOf(donationId));
        updatePurchasedDonations(playerName, donationSet);
    }

    public void removeDonation(String playerName, int donationId) {
        Set<String> donationSet = getPurchasedDonations(playerName);
        donationSet.remove(String.valueOf(donationId));
        updatePurchasedDonations(playerName, donationSet);
    }

    public Set<String> getPurchasedDonations(String playerName) {
        String uuid = getPlayerUUID(playerName);
        String sql = "SELECT purchased_donations FROM donations WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String purchased = rs.getString("purchased_donations");
                if (purchased != null && !purchased.isEmpty()) {
                    return new HashSet<>(Arrays.asList(purchased.split(",")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

    public boolean hasDonation(String playerName, int donationId) {
        Set<String> donationSet = getPurchasedDonations(playerName);
        return donationSet.contains(String.valueOf(donationId));
    }

    private void updatePurchasedDonations(String playerName, Set<String> donationSet) {
        String uuid = getPlayerUUID(playerName);
        String currentName = getPlayerName(playerName);
        String sql = "INSERT INTO donations (player_uuid, player_name, total_donations, purchased_donations) " +
                "VALUES (?, ?, 0, ?) " +
                "ON CONFLICT(player_uuid) DO UPDATE SET purchased_donations = excluded.purchased_donations, " +
                "player_name = excluded.player_name";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, currentName);
            stmt.setString(3, String.join(",", donationSet));
            stmt.executeUpdate();
            System.out.println("Донаты для " + currentName + " обновлены: " + donationSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerGroup(String playerName, int donationAmount) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        Player player = Bukkit.getPlayer(op.getUniqueId());
        if (player == null) {
            System.out.println("LuckPerms: Игрок " + playerName + " не найден на сервере!");
            return;
        }

        UUID playerUUID = player.getUniqueId();
        CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(playerUUID);

        userFuture.thenAccept(user -> {
            if (user == null) {
                System.out.println("LuckPerms: Не удалось загрузить пользователя " + playerName);
                return;
            }

            user.data().clear(node -> node.getKey().equals("group.donator") || node.getKey().equals("group.sponsor"));

            if (donationAmount >= 2000) {
                user.data().add(Node.builder("group.sponsor").build());
                System.out.println("Игроку " + playerName + " назначена группа: Sponsor");
            } else if (donationAmount >= 149) {
                user.data().add(Node.builder("group.donator").build());
                System.out.println("Игроку " + playerName + " назначена группа: Donator");
            }

            luckPerms.getUserManager().saveUser(user);
        });
    }
}