package org.MakeACakeStudios.storage;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

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
                "reason TEXT," +
                "player TEXT, " +
                "admin TEXT, " +
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

    public void addMute(String player, String admin, String reason, String endTime) {
        String insertSQL = "INSERT INTO punishments (type, endTime, reason, player, admin, is_valid) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, "MUTE");
            pstmt.setString(2, endTime);
            pstmt.setString(3, reason);
            pstmt.setString(4, player);
            pstmt.setString(5, admin);
            pstmt.setBoolean(6, true);
            pstmt.executeUpdate();
            System.out.println("Mute added successfully for player: " + player);
            System.out.println("End time:" + endTime);
        } catch (SQLException e) {
            System.err.println("Error adding mute: " + e.getMessage());
        }
    }

    public void addBan(String player, String admin, String reason, String endTime) {
        int nextBanNumber = getNextBanNumber();
        String insertSQL = "INSERT INTO punishments (type, endTime, reason, player, admin, ban_number, is_valid) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, "BAN");
            pstmt.setString(2, endTime);
            pstmt.setString(3, reason);
            pstmt.setString(4, player);
            pstmt.setString(5, admin);
            pstmt.setInt(6, nextBanNumber);
            pstmt.setBoolean(7, true);
            pstmt.executeUpdate();
            System.out.println("Ban added successfully for player: " + player + " with ban number: " + nextBanNumber);
            System.out.println("End time:" + endTime);
        } catch (SQLException e) {
            System.err.println("Error adding ban: " + e.getMessage());
        }
    }

    public void pardonPlayer(String player) {
        String updateSQL = "UPDATE punishments SET is_valid = ? WHERE player = ? AND is_valid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setBoolean(1, false);
            pstmt.setString(2, player);
            pstmt.setBoolean(3, true);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Игрок " + player + " был разбанен.");
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(player);
            } else {
                System.out.println("Игрок " + player + " не забанен.");
            }
        } catch (SQLException e) {
            System.err.println("Error pardoning player: " + e.getMessage());
        }
    }

    public String checkMute(String player) {
        String query = "SELECT admin, endTime, reason FROM punishments WHERE player = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Игрок " + player + " замьючен " + rs.getString("admin") +
                        " до " + rs.getString("endTime") + " по причине: " + rs.getString("reason");
            } else {
                return "Игрок " + player + " не замьючен.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking mute: " + e.getMessage());
            return "Error checking mute for player " + player;
        }
    }

    public boolean isMuted(String player) {
        String muteStatus = PunishmentStorage.instance.checkMute(player);
        return !muteStatus.contains("не замьючен");
    }

    public boolean isBanned(String playerName) {
        String banStatus = PunishmentStorage.instance.checkBan(playerName);
        return !banStatus.contains("не забанен");
    }

    public long getMuteEndTime(String player) {
        String query = "SELECT endTime FROM punishments WHERE player = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
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

    public long getBanEndTime(String player) {
        String query = "SELECT endTime FROM punishments WHERE player = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
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

    public void unmutePlayer(String player) {
        String updateSQL = "UPDATE punishments SET is_valid = false WHERE player = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, player);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Игрок " + player + " был успешно размьючен.");
            } else {
                System.out.println("Игрок " + player + " не замьючен.");
            }
        } catch (SQLException e) {
            System.err.println("Error unmuting player " + player + ": " + e.getMessage());
        }
    }

    public String checkBan(String player) {
        String query = "SELECT admin, endTime, reason, ban_number FROM punishments WHERE player = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Игрок " + player + " забанен " + rs.getString("admin") +
                        " (номер бана: " + rs.getInt("ban_number") + ") до " + rs.getString("endTime") +
                        " по причине: " + rs.getString("reason");
            } else {
                return "Игрок " + player + " не забанен.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking ban: " + e.getMessage());
            return "Error checking ban for player " + player;
        }
    }

    public String getBanAdmin(String player) {
        String query = "SELECT admin FROM punishments WHERE player = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("admin");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении администратора бана: " + e.getMessage());
        }
        return "Неизвестно";
    }

    public String getBanReason(String player) {
        String query = "SELECT reason FROM punishments WHERE player = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("reason");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении причины бана: " + e.getMessage());
        }
        return "Не указана";
    }

    public int getBanNumber(String player) {
        String query = "SELECT ban_number FROM punishments WHERE player = ? AND type = 'BAN' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
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
        String query = "SELECT DISTINCT player FROM punishments WHERE type = 'BAN' AND is_valid = true";
        List<String> bannedPlayers = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                bannedPlayers.add(rs.getString("player"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка забаненных игроков: " + e.getMessage());
        }
        return bannedPlayers;
    }

    public String getFormattedBanEndTime(String player) {
        long endTime = getBanEndTime(player);
        return formatTime(endTime);
    }

    public String getFormattedMuteEndTime(String player) {
        long endTime = getMuteEndTime(player);
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
