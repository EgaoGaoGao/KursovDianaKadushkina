package com.steelcalc.dao;

import com.steelcalc.util.PasswordHasher;
import java.sql.*;

public class UserDao {

    // Метод для создания нового пользователя (регистрация)
    public boolean createUser(String username, String plainPassword) {
        // Проверяем, не существует ли уже пользователь с таким логином
        if (userExists(username)) {
            System.err.println("Пользователь с таким именем уже существует: " + username);
            return false;
        }

        // Хэшируем пароль перед сохранением
        String passwordHash = PasswordHasher.hashPassword(plainPassword);

        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();

            System.out.println("Пользователь создан: " + username);
            return true;

        } catch (SQLException e) {
            System.err.println("Ошибка при создании пользователя: " + e.getMessage());
            return false;
        }
    }

    // Метод для проверки существования пользователя
    public boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            // Если есть хотя бы одна строка в результате, пользователь существует
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования пользователя: " + e.getMessage());
            return false;
        }
    }

    // Метод для аутентификации (проверки логина и пароля)
    public boolean authenticate(String username, String plainPassword) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Получаем сохранённый хэш из базы данных
                String storedHash = rs.getString("password_hash");
                // Сравниваем введённый пароль с хэшем
                return PasswordHasher.checkPassword(plainPassword, storedHash);
            } else {
                // Пользователь с таким логином не найден
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при аутентификации: " + e.getMessage());
            return false;
        }
    }

    // (Опционально) Метод для получения ID пользователя по логину
    public int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID пользователя: " + e.getMessage());
        }
        return -1; // Возвращаем -1, если пользователь не найден
    }
}