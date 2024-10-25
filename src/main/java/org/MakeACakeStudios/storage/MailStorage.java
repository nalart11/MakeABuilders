package org.MakeACakeStudios.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MailStorage {

    private final Connection connection;

    public MailStorage(Connection connection) {
        this.connection = connection;
    }

    // Добавление нового сообщения
    public void addMessage(String recipient, String senderPrefix, String sender, String senderSuffix, String message) {
        String sql = "INSERT INTO mail_messages (recipient, senderPrefix, sender, senderSuffix, message) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recipient);
            pstmt.setString(2, senderPrefix);
            pstmt.setString(3, sender);
            pstmt.setString(4, senderSuffix);
            pstmt.setString(5, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получение всех сообщений для конкретного игрока
    public List<String[]> getMessages(String recipient) {
        String sql = "SELECT senderPrefix, sender, senderSuffix, message FROM mail_messages WHERE recipient = ?";
        List<String[]> messages = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, recipient);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String senderPrefix = rs.getString("senderPrefix");
                String sender = rs.getString("sender");
                String senderSuffix = rs.getString("senderSuffix");
                String message = rs.getString("message");
                messages.add(new String[]{senderPrefix, sender, senderSuffix, message});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}