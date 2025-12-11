package com.steelcalc;

import com.steelcalc.dao.CalculationDao;
import com.steelcalc.dao.DatabaseInitializer;
import com.steelcalc.dao.MaterialDao;
import com.steelcalc.model.CalculationResult;
import com.steelcalc.model.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDataAccess {
    public static void main(String[] args) {
        System.out.println("=== ТЕСТИРОВАНИЕ РАБОТЫ С ДАННЫМИ ===\n");

        // 1. Инициализация БД
        DatabaseInitializer.initializeDatabase();

        // 2. Генерация тестовых данных (100+ записей)
        DatabaseInitializer.generateTestCalculations(120);
        System.out.println("1. База данных инициализирована, созданы тестовые данные.\n");

        // 3. Тестирование MaterialDao
        MaterialDao materialDao = new MaterialDao();
        List<Material> materials = materialDao.getAllMaterials();
        System.out.println("2. MaterialDao - получено материалов: " + materials.size());
        if (!materials.isEmpty()) {
            System.out.println("   Первый материал: " + materials.get(0));
        }

        // 4. Тестирование CalculationDao
        CalculationDao calculationDao = new CalculationDao();

        // Получение всех расчетов
        List<CalculationResult> allCalculations = calculationDao.getCalculationsByUser(1);
        System.out.println("\n3. CalculationDao - всего расчетов: " + allCalculations.size());

        // Поиск расчетов
        List<CalculationResult> searchResults = calculationDao.searchCalculationsByTitle(1, "оптимизация");
        System.out.println("   Найдено расчетов по слову 'оптимизация': " + searchResults.size());

        // Статистика
        Map<String, Object> stats = calculationDao.getCalculationStatistics(1);
        System.out.println("\n4. Статистика расчетов:");
        System.out.println("   Всего: " + stats.get("total"));
        System.out.println("   Фурмы: " + stats.get("lanceCount"));
        System.out.println("   Сопла: " + stats.get("nozzleCount"));

        // Экспорт/импорт
        String jsonExport = calculationDao.exportToJson(1);
        System.out.println("\n5. Экспорт в JSON успешен, длина: " + jsonExport.length() + " символов");

        // Создание нового расчета
        CalculationResult newCalc = new CalculationResult("OXYGEN_LANCE", "Новый тестовый расчет");
        newCalc.setUserId(1);
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("flowRate", 2000.0);
        inputs.put("pressure", 3.0);
        newCalc.setInputParameters(inputs);

        Map<String, Double> outputs = new HashMap<>();
        outputs.put("velocity", 500.0);
        outputs.put("force", 15000.0);
        newCalc.setOutputResults(outputs);

        boolean saved = calculationDao.saveCalculation(newCalc);
        System.out.println("\n6. Сохранение нового расчета: " + (saved ? "УСПЕХ" : "ОШИБКА"));

        if (saved) {
            System.out.println("   ID нового расчета: " + newCalc.getId());

            // Удаление расчета
            boolean deleted = calculationDao.deleteCalculation(newCalc.getId(), 1);
            System.out.println("   Удаление расчета: " + (deleted ? "УСПЕХ" : "ОШИБКА"));
        }

        // Экспорт материалов в CSV
        String csvExport = materialDao.exportToCsv();
        System.out.println("\n7. Экспорт материалов в CSV:");
        System.out.println(csvExport.substring(0, Math.min(500, csvExport.length())) + "...");

        System.out.println("\n=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===");
    }
}