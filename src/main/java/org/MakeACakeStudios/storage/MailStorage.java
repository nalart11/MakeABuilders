package org.MakeACakeStudios.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MailStorage {

    private Connection connection;
    private final String dbPath;

    public MailStorage(String dbPath) {
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

    // Метод для отключения от базы данных
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
                "senderPrefix TEXT, " +
                "sender TEXT, " +
                "senderSuffix TEXT, " +
                "message TEXT, " +
                "is_read BOOLEAN DEFAULT 0)";
        try {
            connection.createStatement().execute(createTableSQL);
            System.out.println("Таблица для хранения сообщений создана или уже существует.");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы сообщений: " + e.getMessage());
        }
    }

    // Проверка подключения
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addMessage(String recipient, String senderPrefix, String sender, String senderSuffix, String message) {
        String sql = "INSERT INTO mail_messages (recipient, senderPrefix, sender, senderSuffix, message, is_read) VALUES (?, ?, ?, ?, ?, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, recipient);
            pstmt.setString(2, senderPrefix);
            pstmt.setString(3, sender);
            pstmt.setString(4, senderSuffix);
            pstmt.setString(5, message);
            pstmt.executeUpdate();

            // Получение сгенерированного ID для проверки
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
        String sql = "DELETE FROM mail_messages WHERE recipient = ? AND is_read = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recipient);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String[]> getMessages(String recipient) {
        String sql = "SELECT id, senderPrefix, sender, senderSuffix, message, is_read FROM mail_messages WHERE recipient = ?";
        List<String[]> messages = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recipient);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id");
                String senderPrefix = rs.getString("senderPrefix");
                String sender = rs.getString("sender");
                String senderSuffix = rs.getString("senderSuffix");
                String message = rs.getString("message");
                String isRead = rs.getBoolean("is_read") ? "Прочитано" : "Непрочитано";
                messages.add(new String[]{id, senderPrefix, sender, senderSuffix, message, isRead});
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