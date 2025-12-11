package com.steelcalc;

import com.steelcalc.dao.DatabaseConnection;
import com.steelcalc.dao.DatabaseInitializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDatabase {
    public static void main(String[] args) {
        try {
            System.out.println("=== ПРОВЕРКА БАЗЫ ДАННЫХ ===\n");

            // 1. Инициализируем БД
            DatabaseInitializer.initializeDatabase();

            // 2. Подключаемся к БД
            Connection conn = DatabaseConnection.getConnection();

            // 3. Проверяем таблицы
            String[] tables = {"users", "materials", "calculations"};
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table)) {
                    if (rs.next()) {
                        System.out.println("Таблица '" + table + "': " + rs.getInt("count") + " записей");
                    }
                }
            }

            // 4. Закрываем соединение
            conn.close();
            System.out.println("\n=== БАЗА ДАННЫХ РАБОТАЕТ КОРРЕКТНО ===");

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}