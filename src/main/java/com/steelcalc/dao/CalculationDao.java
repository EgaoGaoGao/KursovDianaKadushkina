package com.steelcalc.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.steelcalc.model.CalculationResult;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object для работы с таблицей calculations
 * Реализует CRUD операции и дополнительные функции поиска/фильтрации
 */
public class CalculationDao {

    private final ObjectMapper objectMapper;

    public CalculationDao() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Для работы с LocalDateTime
    }

    /**
     * Сохранение расчета в базу данных
     */
    public boolean saveCalculation(CalculationResult calculation) {
        String sql = """
            INSERT INTO calculations (user_id, calculation_type, title, 
                                     input_parameters, output_results, notes, calculation_date)
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Преобразуем Map в JSON строки
            String inputJson = objectMapper.writeValueAsString(calculation.getInputParameters());
            String outputJson = objectMapper.writeValueAsString(calculation.getOutputResults());

            pstmt.setInt(1, calculation.getUserId());
            pstmt.setString(2, calculation.getCalculationType());
            pstmt.setString(3, calculation.getTitle());
            pstmt.setString(4, inputJson);
            pstmt.setString(5, outputJson);
            pstmt.setString(6, calculation.getNotes());

            if (calculation.getCalculationDate() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(calculation.getCalculationDate()));
            } else {
                pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Получаем сгенерированный ID
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    calculation.setId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (Exception e) {
            System.err.println("Ошибка при сохранении расчета: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Получение всех расчетов пользователя
     */
    public List<CalculationResult> getCalculationsByUser(int userId) {
        return getCalculationsByUser(userId, null, null);
    }

    /**
     * Получение расчетов пользователя с фильтрацией по типу и периоду
     */
    public List<CalculationResult> getCalculationsByUser(int userId, String calculationType,
                                                         LocalDateTime[] dateRange) {
        List<CalculationResult> calculations = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT id, user_id, calculation_type, title, 
                   input_parameters, output_results, notes, calculation_date
            FROM calculations 
            WHERE user_id = ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (calculationType != null && !calculationType.isEmpty()) {
            sql.append(" AND calculation_type = ?");
            params.add(calculationType);
        }

        if (dateRange != null && dateRange.length == 2) {
            sql.append(" AND calculation_date BETWEEN ? AND ?");
            params.add(Timestamp.valueOf(dateRange[0]));
            params.add(Timestamp.valueOf(dateRange[1]));
        }

        sql.append(" ORDER BY calculation_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                calculations.add(mapRowToCalculationResult(rs));
            }

        } catch (Exception e) {
            System.err.println("Ошибка при получении расчетов: " + e.getMessage());
            e.printStackTrace();
        }

        return calculations;
    }

    /**
     * Получение расчета по ID
     */
    public CalculationResult getCalculationById(int id) {
        String sql = """
            SELECT id, user_id, calculation_type, title, 
                   input_parameters, output_results, notes, calculation_date
            FROM calculations WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapRowToCalculationResult(rs);
            }

        } catch (Exception e) {
            System.err.println("Ошибка при получении расчета по ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Поиск расчетов по названию (поиск по подстроке)
     */
    public List<CalculationResult> searchCalculationsByTitle(int userId, String searchTerm) {
        List<CalculationResult> calculations = new ArrayList<>();
        String sql = """
            SELECT id, user_id, calculation_type, title, 
                   input_parameters, output_results, notes, calculation_date
            FROM calculations 
            WHERE user_id = ? AND title LIKE ?
            ORDER BY calculation_date DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, "%" + searchTerm + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                calculations.add(mapRowToCalculationResult(rs));
            }

        } catch (Exception e) {
            System.err.println("Ошибка при поиске расчетов: " + e.getMessage());
            e.printStackTrace();
        }

        return calculations;
    }

    /**
     * Обновление расчета
     */
    public boolean updateCalculation(CalculationResult calculation) {
        String sql = """
            UPDATE calculations 
            SET title = ?, notes = ?, 
                input_parameters = ?, output_results = ?
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String inputJson = objectMapper.writeValueAsString(calculation.getInputParameters());
            String outputJson = objectMapper.writeValueAsString(calculation.getOutputResults());

            pstmt.setString(1, calculation.getTitle());
            pstmt.setString(2, calculation.getNotes());
            pstmt.setString(3, inputJson);
            pstmt.setString(4, outputJson);
            pstmt.setInt(5, calculation.getId());
            pstmt.setInt(6, calculation.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Ошибка при обновлении расчета: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Удаление расчета
     */
    public boolean deleteCalculation(int id, int userId) {
        String sql = "DELETE FROM calculations WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении расчета: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Получение статистики по расчетам пользователя
     */
    public Map<String, Object> getCalculationStatistics(int userId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = """
            SELECT 
                COUNT(*) as total_count,
                COUNT(CASE WHEN calculation_type = 'OXYGEN_LANCE' THEN 1 END) as lance_count,
                COUNT(CASE WHEN calculation_type = 'LAVAL_NOZZLE' THEN 1 END) as nozzle_count,
                MIN(calculation_date) as first_date,
                MAX(calculation_date) as last_date
            FROM calculations 
            WHERE user_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("total", rs.getInt("total_count"));
                stats.put("lanceCount", rs.getInt("lance_count"));
                stats.put("nozzleCount", rs.getInt("nozzle_count"));
                stats.put("firstDate", rs.getTimestamp("first_date"));
                stats.put("lastDate", rs.getTimestamp("last_date"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении статистики: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Экспорт расчетов пользователя в формате JSON
     */
    public String exportToJson(int userId) {
        List<CalculationResult> calculations = getCalculationsByUser(userId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(calculations);
        } catch (Exception e) {
            System.err.println("Ошибка при экспорте в JSON: " + e.getMessage());
            return "[]";
        }
    }

    /**
     * Импорт расчетов из JSON
     */
    public int importFromJson(int userId, String jsonData) {
        try {
            List<CalculationResult> calculations = objectMapper.readValue(
                    jsonData,
                    new TypeReference<List<CalculationResult>>() {}
            );

            int importedCount = 0;
            for (CalculationResult calc : calculations) {
                calc.setId(0); // Сбрасываем ID для создания новых записей
                calc.setUserId(userId);
                if (saveCalculation(calc)) {
                    importedCount++;
                }
            }

            return importedCount;

        } catch (Exception e) {
            System.err.println("Ошибка при импорте из JSON: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Вспомогательный метод для преобразования строки ResultSet в объект CalculationResult
     */
    private CalculationResult mapRowToCalculationResult(ResultSet rs) throws Exception {
        CalculationResult result = new CalculationResult();
        result.setId(rs.getInt("id"));
        result.setUserId(rs.getInt("user_id"));
        result.setCalculationType(rs.getString("calculation_type"));
        result.setTitle(rs.getString("title"));

        // Парсим JSON строки обратно в Map
        String inputJson = rs.getString("input_parameters");
        String outputJson = rs.getString("output_results");

        Map<String, Double> inputParams = objectMapper.readValue(
                inputJson,
                new TypeReference<Map<String, Double>>() {}
        );

        Map<String, Double> outputResults = objectMapper.readValue(
                outputJson,
                new TypeReference<Map<String, Double>>() {}
        );

        result.setInputParameters(inputParams);
        result.setOutputResults(outputResults);
        result.setNotes(rs.getString("notes"));

        Timestamp timestamp = rs.getTimestamp("calculation_date");
        if (timestamp != null) {
            result.setCalculationDate(timestamp.toLocalDateTime());
        }

        return result;
    }
}