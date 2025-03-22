package org.MakeACakeStudios.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PunishmentStorage {

    private Connection connection;
    private final String dbPath;
    public static PunishmentStorage instance;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public PunishmentStorage(String dbPath) {
        this.dbPath = dbPath;
        instance = this;
        connectToDatabase();
        createPunishmentTable();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:sqlite:" + dbPath + "/punishments.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Подключение к базе данных SQLite установлено.");
        } catch (SQLException e) {
            System.err.println("Ошибка при подключении к базе данных: " + e.getMessage());
        }
    }

    private void createPunishmentTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, " +
                "endTime INTEGER, " +
                "reason TEXT, " +
                "player TEXT, " +
                "player_uuid TEXT, " +
                "admin TEXT, " +
                "admin_uuid TEXT, " +
                "ban_number INTEGER, " +
                "is_valid BOOLEAN DEFAULT 0)";
        try {
            connection.createStatement().execute(createTableSQL);
            System.out.println("Таблица для хранения сообщений создана или уже существует.");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы сообщений: " + e.getMessage());
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

    public int getNextBanNumber() {
        String query = "SELECT MAX(ban_number) FROM punishments WHERE type = 'BAN'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error getting next ban number: " + e.getMessage());
        }
        return 1;
    }

    public void addMute(String playerName, String adminName, String reason, String endTime) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        OfflinePlayer adminObj = Bukkit.getOfflinePlayer(adminName);
        String playerUuid = playerObj.getUniqueId().toString();
        String adminUuid = adminObj.getUniqueId().toString();

        String insertSQL = "INSERT INTO punishments (type, endTime, reason, player, player_uuid, admin, admin_uuid, is_valid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, "MUTE");
            pstmt.setString(2, endTime);
            pstmt.setString(3, reason);
            pstmt.setString(4, playerName);
            pstmt.setString(5, playerUuid);
            pstmt.setString(6, adminName);
            pstmt.setString(7, adminUuid);
            pstmt.setBoolean(8, true);
            pstmt.executeUpdate();
            System.out.println("Mute добавлен для игрока: " + playerName);
            System.out.println("End time: " + endTime);
        } catch (SQLException e) {
            System.err.println("Error adding mute: " + e.getMessage());
        }
    }

    public void addBan(String playerName, String adminName, String reason, String endTime) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        OfflinePlayer adminObj = Bukkit.getOfflinePlayer(adminName);
        String playerUuid = playerObj.getUniqueId().toString();
        String adminUuid = adminObj.getUniqueId().toString();

        int nextBanNumber = getNextBanNumber();
        String insertSQL = "INSERT INTO punishments (type, endTime, reason, player, player_uuid, admin, admin_uuid, ban_number, is_valid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, "BAN");
            pstmt.setString(2, endTime);
            pstmt.setString(3, reason);
            pstmt.setString(4, playerName);
            pstmt.setString(5, playerUuid);
            pstmt.setString(6, adminName);
            pstmt.setString(7, adminUuid);
            pstmt.setInt(8, nextBanNumber);
            pstmt.setBoolean(9, true);
            pstmt.executeUpdate();
            System.out.println("Ban добавлен для игрока: " + playerName + " с номером бана: " + nextBanNumber);
            System.out.println("End time: " + endTime);
        } catch (SQLException e) {
            System.err.println("Error adding ban: " + e.getMessage());
        }
    }

    public void pardonPlayer(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();

        String updateSQL = "UPDATE punishments SET is_valid = ? WHERE player_uuid = ? AND is_valid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setBoolean(1, false);
            pstmt.setString(2, playerUuid);
            pstmt.setBoolean(3, true);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Игрок " + playerName + " был разбанен.");
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
            } else {
                System.out.println("Игрок " + playerName + " не забанен.");
            }
        } catch (SQLException e) {
            System.err.println("Error pardoning player: " + e.getMessage());
        }
    }

    public String checkMute(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT admin, admin_uuid, endTime, reason FROM punishments WHERE player_uuid = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Игрок " + playerName + " замьючен " + rs.getString("admin") +
                        " (UUID: " + rs.getString("admin_uuid") + ") до " + rs.getString("endTime") +
                        " по причине: " + rs.getString("reason");
            } else {
                return "Игрок " + playerName + " не замьючен.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking mute: " + e.getMessage());
            return "Error checking mute for player " + playerName;
        }
    }

    public boolean isMuted(String playerName) {
        String muteStatus = PunishmentStorage.instance.checkMute(playerName);
        return !muteStatus.contains("не замьючен");
    }

    public boolean isBanned(String playerName) {
        String banStatus = PunishmentStorage.instance.checkBan(playerName);
        return !banStatus.contains("не забанен");
    }

    public long getMuteEndTime(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT endTime FROM punishments WHERE player_uuid = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String endTime = rs.getString("endTime");
                if ("Fv".equals(endTime)) {
                    return Long.MAX_VALUE;
                }
                return Long.parseLong(endTime);
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Ошибка при получении времени окончания мьюта: " + e.getMessage());
        }
        return -1;
    }

    public long getBanEndTime(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT endTime FROM punishments WHERE player_uuid = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String endTime = rs.getString("endTime");
                if ("Fv".equals(endTime)) {
                    return Long.MAX_VALUE;
                }
                return Long.parseLong(endTime);
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Ошибка при получении времени окончания бана: " + e.getMessage());
        }
        return -1;
    }

    public void unmutePlayer(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String updateSQL = "UPDATE punishments SET is_valid = false WHERE player_uuid = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, playerUuid);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Игрок " + playerName + " был успешно размьючен.");
            } else {
                System.out.println("Игрок " + playerName + " не замьючен.");
            }
        } catch (SQLException e) {
            System.err.println("Error unmuting player " + playerName + ": " + e.getMessage());
        }
    }

    public String checkBan(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT admin, admin_uuid, endTime, reason, ban_number FROM punishments WHERE player_uuid = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Игрок " + playerName + " забанен " + rs.getString("admin") +
                        " (UUID: " + rs.getString("admin_uuid") + ", номер бана: " + rs.getInt("ban_number") + ") до " + rs.getString("endTime") +
                        " по причине: " + rs.getString("reason");
            } else {
                return "Игрок " + playerName + " не забанен.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking ban: " + e.getMessage());
            return "Error checking ban for player " + playerName;
        }
    }

    public String getBanAdmin(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT admin, admin_uuid FROM punishments WHERE player_uuid = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("admin") + " (UUID: " + rs.getString("admin_uuid") + ")";
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении администратора бана: " + e.getMessage());
        }
        return "Неизвестно";
    }

    public String getBanReason(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT reason FROM punishments WHERE player_uuid = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("reason");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении причины бана: " + e.getMessage());
        }
        return "Не указана";
    }

    public int getBanNumber(String playerName) {
        OfflinePlayer playerObj = Bukkit.getOfflinePlayer(playerName);
        String playerUuid = playerObj.getUniqueId().toString();
        String query = "SELECT ban_number FROM punishments WHERE player_uuid = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ban_number");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении номера бана: " + e.getMessage());
        }
        return -1;
    }

    public List<String> getBannedPlayerNames() {
        String query = "SELECT DISTINCT player, player_uuid FROM punishments WHERE type = 'BAN' AND is_valid = true";
        List<String> bannedPlayers = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                bannedPlayers.add(rs.getString("player") + " (UUID: " + rs.getString("player_uuid") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка забаненных игроков: " + e.getMessage());
        }
        return bannedPlayers;
    }

    public String getFormattedBanEndTime(String playerName) {
        long endTime = getBanEndTime(playerName);
        return formatTime(endTime);
    }

    public String getFormattedMuteEndTime(String playerName) {
        long endTime = getMuteEndTime(playerName);
        return formatTime(endTime);
    }

    private String formatTime(long endTime) {
        if (endTime == -1) {
            return "неизвестно";
        }
        if (endTime == Long.MAX_VALUE) {
            return "навсегда";
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());
        return dateTime.format(FORMATTER);
    }
}
