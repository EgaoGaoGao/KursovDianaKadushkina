package com.steelcalc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        // SQL-запросы для создания всех таблиц
        String[] createTablesSQL = {
                // Таблица пользователей (уже была, оставляем для полноты)
                """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL
            );
            """,

                // Таблица материалов (справочник)
                """
            CREATE TABLE IF NOT EXISTS materials (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                category TEXT NOT NULL,
                density REAL NOT NULL,
                melting_point REAL NOT NULL,
                thermal_conductivity REAL,
                specific_heat REAL,
                description TEXT
            );
            """,

                // Основная таблица для хранения истории расчетов
                """
            CREATE TABLE IF NOT EXISTS calculations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                calculation_type TEXT NOT NULL CHECK (calculation_type IN ('OXYGEN_LANCE', 'LAVAL_NOZZLE')),
                title TEXT NOT NULL,
                input_parameters TEXT NOT NULL, -- JSON строка
                output_results TEXT NOT NULL,    -- JSON строка
                notes TEXT,
                calculation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """,

                // Индексы для ускорения поиска
                """
            CREATE INDEX IF NOT EXISTS idx_calculations_user_id ON calculations(user_id);
            """,
                """
            CREATE INDEX IF NOT EXISTS idx_calculations_type ON calculations(calculation_type);
            """,
                """
            CREATE INDEX IF NOT EXISTS idx_calculations_date ON calculations(calculation_date);
            """
        };

        // Выполняем все запросы в транзакции
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Включаем поддержку внешних ключей для SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            for (String sql : createTablesSQL) {
                stmt.execute(sql);
            }

            System.out.println("Все таблицы базы данных проверены/созданы успешно.");

            // Заполняем справочник материалов начальными данными
            populateInitialMaterials();

        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Заполнение справочника материалов начальными данными
     */
    private static void populateInitialMaterials() {
        String[][] materialsData = {
                // name, category, density, melting_point, thermal_conductivity, specific_heat, description
                {"Сталь 20", "Конструкционная сталь", "7850", "1520", "50", "480", "Низкоуглеродистая конструкционная сталь"},
                {"Сталь 45", "Конструкционная сталь", "7820", "1490", "48", "470", "Среднеуглеродистая конструкционная сталь"},
                {"Нержавеющая сталь 304", "Нержавеющая сталь", "8000", "1400", "16", "500", "Аустенитная нержавеющая сталь"},
                {"Чугун СЧ20", "Чугун", "7200", "1150", "50", "460", "Серый чугун"},
                {"Алюминий А5", "Цветной металл", "2700", "660", "237", "900", "Чистый алюминий"},
                {"Медь М1", "Цветной металл", "8940", "1085", "401", "385", "Чистая медь"},
                {"Латунь Л63", "Сплав", "8500", "900", "120", "380", "Медно-цинковый сплав"},
                {"Бронза БрА5", "Сплав", "8800", "1050", "75", "370", "Оловянная бронза"},
                {"Титан ВТ1-0", "Титан", "4500", "1668", "22", "520", "Технически чистый титан"},
                {"Магний Мг90", "Магний", "1740", "650", "156", "1020", "Чистый магний"}
        };

        String insertSQL = """
            INSERT OR IGNORE INTO materials (name, category, density, melting_point, 
                                            thermal_conductivity, specific_heat, description)
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             var pstmt = conn.prepareStatement(insertSQL)) {

            for (String[] material : materialsData) {
                pstmt.setString(1, material[0]);
                pstmt.setString(2, material[1]);
                pstmt.setDouble(3, Double.parseDouble(material[2]));
                pstmt.setDouble(4, Double.parseDouble(material[3]));
                pstmt.setDouble(5, Double.parseDouble(material[4]));
                pstmt.setDouble(6, Double.parseDouble(material[5]));
                pstmt.setString(7, material[6]);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            int insertedCount = 0;
            for (int result : results) {
                if (result >= 0) insertedCount++;
            }

            System.out.println("Добавлено материалов в справочник: " + insertedCount);

        } catch (SQLException e) {
            System.err.println("Ошибка при заполнении справочника материалов: " + e.getMessage());
        }
    }

    /**
     * Генерация тестовых данных расчетов (для демонстрации)
     */
    public static void generateTestCalculations(int count) {
        String insertSQL = """
            INSERT INTO calculations (user_id, calculation_type, title, 
                                     input_parameters, output_results, notes)
            VALUES (?, ?, ?, ?, ?, ?);
            """;

        String[] calculationTypes = {"OXYGEN_LANCE", "LAVAL_NOZZLE"};
        String[] titles = {
                "Тестовый расчет фурмы", "Оптимизация параметров",
                "Сравнение материалов", "Контрольный расчет",
                "Исследование эффективности"
        };

        try (Connection conn = DatabaseConnection.getConnection();
             var pstmt = conn.prepareStatement(insertSQL)) {

            for (int i = 0; i < count; i++) {
                String type = calculationTypes[i % 2];
                String inputJson = generateTestInputJson(type);
                String outputJson = generateTestOutputJson(type);

                pstmt.setInt(1, 1); // Предполагаем, что есть пользователь с ID=1
                pstmt.setString(2, type);
                pstmt.setString(3, titles[i % titles.length] + " #" + (i + 1));
                pstmt.setString(4, inputJson);
                pstmt.setString(5, outputJson);
                pstmt.setString(6, "Тестовый расчет для демонстрации");
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            System.out.println("Сгенерировано тестовых расчетов: " + results.length);

        } catch (SQLException e) {
            System.err.println("Ошибка при генерации тестовых данных: " + e.getMessage());
        }
    }

    private static String generateTestInputJson(String type) {
        if ("OXYGEN_LANCE".equals(type)) {
            return """
                {
                  "oxygenFlowRate": 1500.0,
                  "pressure": 2.5,
                  "nozzleDiameter": 15.0,
                  "temperature": 25.0,
                  "oxygenPurity": 99.5,
                  "materialName": "Сталь 45"
                }
                """;
        } else {
            return """
                {
                  "gasType": "Кислород",
                  "inletPressure": 10.0,
                  "outletPressure": 1.0,
                  "temperature": 300.0,
                  "massFlowRate": 1.0,
                  "expansionRatio": 5.0,
                  "isSupersonic": true
                }
                """;
        }
    }

    private static String generateTestOutputJson(String type) {
        if ("OXYGEN_LANCE".equals(type)) {
            return """
                {
                  "exitVelocity": 450.5,
                  "jetForce": 12560.8,
                  "efficiency": 87.3,
                  "machNumber": 1.8,
                  "reynoldsNumber": 125000.0
                }
                """;
        } else {
            return """
                {
                  "throatArea": 25.4,
                  "exitArea": 127.0,
                  "exitVelocity": 680.2,
                  "machNumber": 2.3,
                  "thrust": 1500.5,
                  "efficiency": 92.1
                }
                """;
        }
    }
}