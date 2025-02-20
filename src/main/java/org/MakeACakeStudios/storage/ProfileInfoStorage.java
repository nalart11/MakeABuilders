package org.MakeACakeStudios.storage;

import org.MakeACakeStudios.MakeABuilders;

import java.sql.*;
import java.util.logging.Level;

public class ProfileInfoStorage {

    private static ProfileInfoStorage instance;
    private Connection connection;

    public ProfileInfoStorage() {
        instance = this;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + MakeABuilders.instance.getDataFolder() + "/profile_info.db");
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS profile_info (" +
                    "player TEXT PRIMARY KEY, " +
                    "birthday TEXT DEFAULT '', " +
                    "telegram TEXT DEFAULT '', " +
                    "discord TEXT DEFAULT '')");
            stmt.close();
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при инициализации базы данных", e);
        }
    }

    public static ProfileInfoStorage getInstance() {
        if (instance == null) {
            instance = new ProfileInfoStorage();
        }
        return instance;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setPlayerInfo(String player, String column, String value) {
        if (!isValidColumn(column)) {
            MakeABuilders.instance.getLogger().log(Level.WARNING, "Попытка обновления несуществующего столбца: " + column);
            return;
        }

        String query = "INSERT INTO profile_info (player, " + column + ") VALUES (?, ?) " +
                "ON CONFLICT(player) DO UPDATE SET " + column + " = excluded." + column;
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player);
            stmt.setString(2, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при обновлении информации игрока", e);
        }
    }

    public String getPlayerInfo(String player, String column) {
        if (!isValidColumn(column)) {
            MakeABuilders.instance.getLogger().log(Level.WARNING, "Попытка получения несуществующего столбца: " + column);
            return "";
        }

        String query = "SELECT " + column + " FROM profile_info WHERE player = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(column);
            }
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при получении информации игрока", e);
        }
        return "";
    }

    public boolean playerExists(String player) {
        String query = "SELECT COUNT(*) FROM profile_info WHERE player = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, player);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            MakeABuilders.instance.getLogger().log(Level.SEVERE, "Ошибка при проверке существования игрока", e);
        }
        return false;
    }

    private boolean isValidColumn(String column) {
        return column.equals("birthday") || column.equals("telegram") || column.equals("discord");
    }
}