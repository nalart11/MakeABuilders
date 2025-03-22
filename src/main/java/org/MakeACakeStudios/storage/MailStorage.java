package org.MakeACakeStudios.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MailStorage {

    public static MailStorage instance;
    private Connection connection;
    private final String dbPath;

    public MailStorage(String dbPath) {
        instance = this;
        this.dbPath = dbPath;
        connectToDatabase();
        createMailTable();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:sqlite:" + dbPath + "/mail.db";
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

    private void createMailTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS mail_messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipient TEXT, " +
                "recipient_uuid TEXT, " +
                "senderName TEXT, " +
                "sender_uuid TEXT, " +
                "message TEXT, " +
                "is_read BOOLEAN DEFAULT 0, " +
                "sent_at TEXT)";
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

    public void addMessage(String recipient, String senderName, String message) {
        OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipient);
        String recipientUuid = recipientPlayer.getUniqueId().toString();
        OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(senderName);
        String senderUuid = senderPlayer.getUniqueId().toString();

        String timestamp = new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(new Date());

        String sql = "INSERT INTO mail_messages (recipient, recipient_uuid, senderName, sender_uuid, message, is_read, sent_at) VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, recipient);
            pstmt.setString(2, recipientUuid);
            pstmt.setString(3, senderName);
            pstmt.setString(4, senderUuid);
            pstmt.setString(5, message);
            pstmt.setString(6, timestamp);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long messageId = generatedKeys.getLong(1);
                    System.out.println("Message added with ID: " + messageId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteReadMessages(String recipient) {
        OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipient);
        String recipientUuid = recipientPlayer.getUniqueId().toString();
        String sql = "DELETE FROM mail_messages WHERE recipient_uuid = ? AND is_read = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recipientUuid);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String[]> getMessages(String recipient) {
        OfflinePlayer recipientPlayer = Bukkit.getOfflinePlayer(recipient);
        String recipientUuid = recipientPlayer.getUniqueId().toString();
        String sql = "SELECT id, senderName, message, is_read, sent_at FROM mail_messages WHERE recipient_uuid = ?";
        List<String[]> messages = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recipientUuid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String senderName = rs.getString("senderName");
                String message = rs.getString("message");
                String isRead = rs.getBoolean("is_read") ? "Прочитано" : "Непрочитано";
                String sentAt = rs.getString("sent_at");
                messages.add(new String[]{id, senderName, message, isRead, sentAt});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public boolean markMessageAsRead(long messageId) {
        String sql = "UPDATE mail_messages SET is_read = 1 WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
