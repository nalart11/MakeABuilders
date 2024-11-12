package org.MakeACakeStudios.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PunishmentStorage {

    private Connection connection;
    private final String dbPath;

    public PunishmentStorage(String dbPath) {
        this.dbPath = dbPath;
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

    public void disconnectFromDatabase() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Подключение к базе данных закрыто.");
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии подключения к базе данных: " + e.getMessage());
            }
        }
    }

    private void createPunishmentTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS punishments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, " +
                "endTime TEXT, " +
                "reason TEXT," +
                "player TEXT, " +
                "admin TEXT, " +
                "ban_nuber INTEGER, " +
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

    private int getNextBanNumber() {
        String query = "SELECT MAX(ban_number) FROM punishments WHERE type = 'BAN'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error getting next ban number: " + e.getMessage());
        }
        return 1; // Default to 1 if no previous bans
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
        } catch (SQLException e) {
            System.err.println("Error adding ban: " + e.getMessage());
        }
    }

    public String checkMute(String player) {
        String query = "SELECT admin, endTime, reason FROM punishments WHERE player = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "Player " + player + " is muted by " + rs.getString("admin") +
                        " until " + rs.getString("endTime") + " for reason: " + rs.getString("reason");
            } else {
                return "Игрок " + player + " не замьючен.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking mute: " + e.getMessage());
            return "Error checking mute for player " + player;
        }
    }

    public void unmutePlayer(String player) {
        String updateSQL = "UPDATE punishments SET is_valid = false WHERE player = ? AND type = 'MUTE' AND is_valid = true";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, player);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Player " + player + " has been successfully unmuted.");
            } else {
                System.out.println("No active mute found for player " + player + ".");
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
                return "Player " + player + " is banned by " + rs.getString("admin") +
                        " (Ban number: " + rs.getInt("ban_number") + ") until " + rs.getString("endTime") +
                        " for reason: " + rs.getString("reason");
            } else {
                return "Player " + player + " is not banned.";
            }
        } catch (SQLException e) {
            System.err.println("Error checking ban: " + e.getMessage());
            return "Error checking ban for player " + player;
        }
    }
}
