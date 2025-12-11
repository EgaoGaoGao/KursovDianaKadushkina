package com.steelcalc.dao;

import com.steelcalc.model.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object для работы со справочником материалов
 */
public class MaterialDao {

    /**
     * Получение всех материалов
     */
    public List<Material> getAllMaterials() {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT * FROM materials ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                materials.add(mapRowToMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении материалов: " + e.getMessage());
            e.printStackTrace();
        }

        return materials;
    }

    /**
     * Поиск материалов по названию
     */
    public List<Material> searchMaterialsByName(String searchTerm) {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT * FROM materials WHERE name LIKE ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                materials.add(mapRowToMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске материалов: " + e.getMessage());
            e.printStackTrace();
        }

        return materials;
    }

    /**
     * Получение материала по ID
     */
    public Material getMaterialById(int id) {
        String sql = "SELECT * FROM materials WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToMaterial(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении материала по ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Добавление нового материала
     */
    public boolean addMaterial(Material material) {
        String sql = """
            INSERT INTO materials (name, category, density, melting_point, 
                                  thermal_conductivity, specific_heat, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, material.getName());
            pstmt.setString(2, material.getCategory());
            pstmt.setDouble(3, material.getDensity());
            pstmt.setDouble(4, material.getMeltingPoint());
            pstmt.setDouble(5, material.getThermalConductivity());
            pstmt.setDouble(6, material.getSpecificHeat());
            pstmt.setString(7, material.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    material.setId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении материала: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Обновление материала
     */
    public boolean updateMaterial(Material material) {
        String sql = """
            UPDATE materials 
            SET name = ?, category = ?, density = ?, melting_point = ?, 
                thermal_conductivity = ?, specific_heat = ?, description = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, material.getName());
            pstmt.setString(2, material.getCategory());
            pstmt.setDouble(3, material.getDensity());
            pstmt.setDouble(4, material.getMeltingPoint());
            pstmt.setDouble(5, material.getThermalConductivity());
            pstmt.setDouble(6, material.getSpecificHeat());
            pstmt.setString(7, material.getDescription());
            pstmt.setInt(8, material.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении материала: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Удаление материала
     */
    public boolean deleteMaterial(int id) {
        String sql = "DELETE FROM materials WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении материала: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Получение материалов по категории
     */
    public List<Material> getMaterialsByCategory(String category) {
        List<Material> materials = new ArrayList<>();
        String sql = "SELECT * FROM materials WHERE category = ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                materials.add(mapRowToMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении материалов по категории: " + e.getMessage());
            e.printStackTrace();
        }

        return materials;
    }

    /**
     * Получение уникальных категорий материалов
     */
    public List<String> getMaterialCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM materials ORDER BY category";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении категорий материалов: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    /**
     * Экспорт материалов в CSV формат
     */
    public String exportToCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Наименование;Категория;Плотность (кг/м³);Температура плавления (°C);")
                .append("Теплопроводность (Вт/(м·K));Удельная теплоемкость (Дж/(кг·K));Описание\n");

        List<Material> materials = getAllMaterials();
        for (Material material : materials) {
            csv.append(material.getId()).append(";")
                    .append(material.getName()).append(";")
                    .append(material.getCategory()).append(";")
                    .append(material.getDensity()).append(";")
                    .append(material.getMeltingPoint()).append(";")
                    .append(material.getThermalConductivity()).append(";")
                    .append(material.getSpecificHeat()).append(";")
                    .append("\"").append(material.getDescription()).append("\"\n");
        }

        return csv.toString();
    }

    /**
     * Вспомогательный метод для преобразования строки ResultSet в объект Material
     */
    private Material mapRowToMaterial(ResultSet rs) throws SQLException {
        Material material = new Material();
        material.setId(rs.getInt("id"));
        material.setName(rs.getString("name"));
        material.setCategory(rs.getString("category"));
        material.setDensity(rs.getDouble("density"));
        material.setMeltingPoint(rs.getDouble("melting_point"));
        material.setThermalConductivity(rs.getDouble("thermal_conductivity"));
        material.setSpecificHeat(rs.getDouble("specific_heat"));
        material.setDescription(rs.getString("description"));
        return material;
    }
}