package com.steelcalc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:steel_calc.db"; // Файл БД создастся в папке проекта

    // Создаём единственный (синглтон) экземпляр подключения
    private static Connection connection;

    // Метод для получения подключения. Создаёт его при первом вызове.
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Регистрируем драйвер и создаём подключение к файлу БД
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Подключение к базе данных установлено.");
            } catch (SQLException e) {
                System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
                throw e; // Пробрасываем исключение дальше
            }
        }
        return connection;
    }

    // Метод для закрытия подключения (вызовем при завершении программы)
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Подключение к базе данных закрыто.");
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии подключения: " + e.getMessage());
            }
        }
    }
}