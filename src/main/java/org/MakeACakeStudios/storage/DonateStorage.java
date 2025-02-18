package org.MakeACakeStudios.storage;

import java.sql.*;
import java.util.*;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class DonateStorage {
    private static final String URL = "jdbc:sqlite:donations.db";
    public static DonateStorage instance;

    public DonateStorage() {
        instance = this;
        initializeDatabase();
    }

    public static final Map<String, Integer> DONATE_EFFECTS = new HashMap<>() {{
        put("Zeus", 1);
        put("Star", 2);
        put("Sakura", 3);
        put("Vanila", 4);
    }};

    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS donations (" +
                "player_name TEXT PRIMARY KEY, " +
                "total_donations INTEGER DEFAULT 0, " +
                "purchased_donations TEXT DEFAULT '')";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("База данных готова!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getTotalDonations(String playerName) {
        String sql = "SELECT total_donations FROM donations WHERE player_name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerName);
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
        String sql = "INSERT INTO donations (player_name, total_donations, purchased_donations) " +
                "VALUES (?, ?, '') " +
                "ON CONFLICT(player_name) DO UPDATE SET total_donations = excluded.total_donations";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerName);
            stmt.setInt(2, newAmount);
            stmt.executeUpdate();
            System.out.println("Сумма донатов для " + playerName + " обновлена: " + newAmount);

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
        String sql = "SELECT purchased_donations FROM donations WHERE player_name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerName);
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
        String sql = "INSERT INTO donations (player_name, total_donations, purchased_donations) " +
                "VALUES (?, 0, ?) " +
                "ON CONFLICT(player_name) DO UPDATE SET purchased_donations = excluded.purchased_donations";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerName);
            stmt.setString(2, String.join(",", donationSet));
            stmt.executeUpdate();
            System.out.println("Донаты для " + playerName + " обновлены: " + donationSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerGroup(String playerName, int donationAmount) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        Player player = Bukkit.getPlayerExact(playerName);
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
            } else if (donationAmount >= 150) {
                user.data().add(Node.builder("group.donator").build());
                System.out.println("Игроку " + playerName + " назначена группа: Donator");
            }

            luckPerms.getUserManager().saveUser(user);
        });
    }
}