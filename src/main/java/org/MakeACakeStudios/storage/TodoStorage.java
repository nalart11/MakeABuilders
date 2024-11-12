package org.MakeACakeStudios.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoStorage {

    private Connection connection;
    private final String dbPath;

    public TodoStorage(String dbPath) {
        this.dbPath = dbPath;
        connectToDatabase();
        createTodoTable();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:sqlite:" + dbPath + "/todo.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Подключение к базе данных SQLite для задач установлено.");
        } catch (SQLException e) {
            System.err.println("Ошибка при подключении к базе данных для задач: " + e.getMessage());
        }
    }

    public void disconnectFromDatabase() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Подключение к базе данных для задач закрыто.");
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии подключения к базе данных для задач: " + e.getMessage());
            }
        }
    }

    private void createTodoTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS todo_tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT)";
        try {
            connection.createStatement().execute(createTableSQL);
            System.out.println("Таблица задач создана или уже существует.");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблицы задач: " + e.getMessage());
        }
    }

    public void addTask(String title, String description) {
        String sql = "INSERT INTO todo_tasks (title, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении задачи: " + e.getMessage());
        }
    }

    public List<String[]> getTasks() {
        List<String[]> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description FROM todo_tasks";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("id"));
                String title = rs.getString("title");
                String description = rs.getString("description");
                tasks.add(new String[]{id, title, description});
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении списка задач: " + e.getMessage());
        }
        return tasks;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
