package com.steelcalc.view;

import com.steelcalc.dao.CalculationDao;
import com.steelcalc.model.CalculationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class HistoryPanel extends JPanel {
    private JTable calculationsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private CalculationDao calculationDao;
    private int currentUserId;

    // Компоненты фильтрации и поиска
    private JComboBox<String> typeFilterComboBox;
    private JTextField searchField;
    private JComboBox<String> dateFilterComboBox;

    // Кнопки
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton deleteButton;
    private JButton deleteAllButton;
    private JButton exportCsvButton;
    private JButton exportJsonButton;
    private JButton importJsonButton;
    private JButton generateReportButton;
    private JButton showStatsButton;

    // Статусная панель
    private JLabel statusLabel;

    // Форматтеры дат
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy_HH_mm");

    public HistoryPanel(int userId) {
        this.currentUserId = userId;
        this.calculationDao = new CalculationDao();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Создаем верхнюю панель с фильтрами
        add(createFilterPanel(), BorderLayout.NORTH);

        // Создаем центральную панель с таблицей
        add(createTablePanel(), BorderLayout.CENTER);

        // Создаем нижнюю панель с кнопками
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Загружаем данные
        loadCalculations();

        // Обновляем статус
        updateStatus();
    }

    /**
     * Создание панели фильтров и поиска
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Фильтры и поиск"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Фильтр по типу расчета
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        filterPanel.add(new JLabel("Тип расчета:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        typeFilterComboBox = new JComboBox<>(new String[]{"Все", "Кислородная фурма", "Сопло Лаваля"});
        typeFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(typeFilterComboBox, gbc);

        // Фильтр по дате
        gbc.gridx = 2;
        gbc.weightx = 0;
        filterPanel.add(new JLabel("Период:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        dateFilterComboBox = new JComboBox<>(new String[]{
                "Все время", "Сегодня", "Вчера", "Эта неделя", "Этот месяц", "Этот год"
        });
        dateFilterComboBox.addActionListener(e -> applyFilters());
        filterPanel.add(dateFilterComboBox, gbc);

        // Поиск по названию
        gbc.gridx = 4;
        gbc.weightx = 0;
        filterPanel.add(new JLabel("Поиск:"), gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.4;
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterPanel.add(searchField, gbc);

        return filterPanel;
    }

    /**
     * Создание панели с таблицей
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("История расчетов"));

        // Создаем модель таблицы
        String[] columnNames = {
                "ID", "Тип", "Название", "Дата расчета",
                "Параметры", "Результаты", "Примечания"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование ячеек
            }
        };

        calculationsTable = new JTable(tableModel);
        calculationsTable.setRowHeight(25);
        calculationsTable.setAutoCreateRowSorter(true);
        calculationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Настраиваем сортировщик
        sorter = new TableRowSorter<>(tableModel);
        calculationsTable.setRowSorter(sorter);

        // Настраиваем ширину столбцов
        calculationsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        calculationsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Тип
        calculationsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Название
        calculationsTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Дата
        calculationsTable.getColumnModel().getColumn(4).setPreferredWidth(250); // Параметры
        calculationsTable.getColumnModel().getColumn(5).setPreferredWidth(250); // Результаты
        calculationsTable.getColumnModel().getColumn(6).setPreferredWidth(300); // Примечания

        // Добавляем прокрутку
        JScrollPane scrollPane = new JScrollPane(calculationsTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Статусная строка
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tablePanel.add(statusLabel, BorderLayout.SOUTH);

        return tablePanel;
    }

    /**
     * Создание панели кнопок
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Операции"));

        // Кнопка обновления
        refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> loadCalculations());

        // Кнопка просмотра деталей
        viewDetailsButton = new JButton("Просмотр");
        viewDetailsButton.addActionListener(e -> viewCalculationDetails());

        // Кнопка удаления
        deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedCalculation());

        // Кнопка удаления всех
        deleteAllButton = new JButton("Удалить все");
        deleteAllButton.addActionListener(e -> deleteAllCalculations());

        // Кнопка экспорта в CSV
        exportCsvButton = new JButton("Экспорт CSV");
        exportCsvButton.addActionListener(e -> exportToCsv());

        // Кнопка экспорта в JSON
        exportJsonButton = new JButton("Экспорт JSON");
        exportJsonButton.addActionListener(e -> exportToJson());

        // Кнопка импорта из JSON
        importJsonButton = new JButton("Импорт JSON");
        importJsonButton.addActionListener(e -> importFromJson());

        // Кнопка генерации отчета
        generateReportButton = new JButton("Отчет (HTML)");
        generateReportButton.addActionListener(e -> generateHtmlReport());

        // Кнопка статистики
        showStatsButton = new JButton("Статистика");
        showStatsButton.addActionListener(e -> showStatistics());

        // Добавляем кнопки в панель
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(exportCsvButton);
        buttonPanel.add(exportJsonButton);
        buttonPanel.add(importJsonButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(generateReportButton);
        buttonPanel.add(showStatsButton);

        return buttonPanel;
    }

    /**
     * Загрузка расчетов из базы данных
     */
    private void loadCalculations() {
        try {
            // Получаем все расчеты пользователя
            List<CalculationResult> calculations = calculationDao.getCalculationsByUser(currentUserId);

            // Очищаем таблицу
            tableModel.setRowCount(0);

            // Заполняем таблицу данными
            for (CalculationResult calc : calculations) {
                Object[] row = {
                        calc.getId(),
                        getCalculationTypeName(calc.getCalculationType()),
                        calc.getTitle(),
                        formatDate(calc.getCalculationDate()),
                        formatParameters(calc.getInputParameters()),
                        formatParameters(calc.getOutputResults()),
                        calc.getNotes() != null ? calc.getNotes() : ""
                };
                tableModel.addRow(row);
            }

            updateStatus();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Применение фильтров к таблице
     */
    private void applyFilters() {
        try {
            List<RowFilter<Object, Object>> filters = new ArrayList<>();

            // Фильтр по типу расчета
            String selectedType = (String) typeFilterComboBox.getSelectedItem();
            if (!"Все".equals(selectedType)) {
                String typeValue = "Кислородная фурма".equals(selectedType) ? "OXYGEN_LANCE" : "LAVAL_NOZZLE";
                String displayType = "Кислородная фурма".equals(selectedType) ? "Кислородная фурма" : "Сопло Лаваля";
                filters.add(RowFilter.regexFilter("^" + displayType + "$", 1)); // Колонка 1 - Тип
            }

            // Фильтр по периоду
            String selectedPeriod = (String) dateFilterComboBox.getSelectedItem();
            if (!"Все время".equals(selectedPeriod)) {
                LocalDateTime[] dateRange = getDateRange(selectedPeriod);
                if (dateRange != null) {
                    filters.add(new DateRangeFilter(dateRange[0], dateRange[1], 3)); // Колонка 3 - Дата
                }
            }

            // Фильтр по поиску
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + searchText, 2)); // Колонка 2 - Название
            }

            // Комбинируем фильтры
            if (filters.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }

            updateStatus();

        } catch (Exception e) {
            System.err.println("Ошибка при применении фильтров: " + e.getMessage());
        }
    }

    /**
     * Просмотр деталей выбранного расчета
     */
    private void viewCalculationDetails() {
        int selectedRow = calculationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите расчет для просмотра деталей",
                    "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Получаем ID расчета из таблицы (учитываем сортировку)
            int modelRow = calculationsTable.convertRowIndexToModel(selectedRow);
            int calculationId = (Integer) tableModel.getValueAt(modelRow, 0);

            // Получаем расчет из базы данных
            CalculationResult calculation = calculationDao.getCalculationById(calculationId);
            if (calculation == null) {
                JOptionPane.showMessageDialog(this,
                        "Расчет не найден в базе данных",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Создаем диалог с деталями
            showCalculationDetailsDialog(calculation);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при получении деталей расчета: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Удаление выбранного расчета
     */
    private void deleteSelectedCalculation() {
        int selectedRow = calculationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите расчет для удаления",
                    "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Получаем ID расчета из таблицы
            int modelRow = calculationsTable.convertRowIndexToModel(selectedRow);
            int calculationId = (Integer) tableModel.getValueAt(modelRow, 0);
            String calculationTitle = (String) tableModel.getValueAt(modelRow, 2);

            // Подтверждение удаления
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Вы действительно хотите удалить расчет:\n\"" + calculationTitle + "\"?",
                    "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Удаляем расчет
                boolean deleted = calculationDao.deleteCalculation(calculationId, currentUserId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this,
                            "Расчет успешно удален",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadCalculations(); // Перезагружаем данные
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при удалении расчета",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при удалении расчета: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Удаление всех расчетов пользователя
     */
    private void deleteAllCalculations() {
        // Получаем количество расчетов
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "Нет расчетов для удаления",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Подтверждение удаления
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы действительно хотите удалить ВСЕ расчеты (" + rowCount + " записей)?\n" +
                        "Это действие нельзя отменить!",
                "Подтверждение удаления всех записей",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Удаляем все расчеты
                boolean success = true;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int modelRow = calculationsTable.convertRowIndexToModel(i);
                    int calculationId = (Integer) tableModel.getValueAt(modelRow, 0);
                    if (!calculationDao.deleteCalculation(calculationId, currentUserId)) {
                        success = false;
                    }
                }

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Все расчеты успешно удалены",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                    loadCalculations(); // Перезагружаем данные
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Некоторые расчеты не были удалены",
                            "Предупреждение", JOptionPane.WARNING_MESSAGE);
                    loadCalculations(); // Все равно перезагружаем
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при удалении расчетов: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Экспорт данных в CSV файл
     */
    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт в CSV");
        fileChooser.setSelectedFile(new File("calculations_" +
                REPORT_DATE_FORMAT.format(new Date()) + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(file);
                 CSVPrinter csvPrinter = new CSVPrinter(writer,
                         CSVFormat.DEFAULT.withHeader("ID", "Тип", "Название", "Дата",
                                 "Параметры", "Результаты", "Примечания"))) {

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    List<String> row = new ArrayList<>();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        row.add(value != null ? value.toString() : "");
                    }
                    csvPrinter.printRecord(row);
                }

                csvPrinter.flush();

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в файл:\n" + file.getAbsolutePath(),
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при экспорте в CSV: " + e.getMessage(),
                        "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Экспорт данных в JSON файл
     */
    private void exportToJson() {
        try {
            // Получаем все расчеты пользователя
            List<CalculationResult> calculations = calculationDao.getCalculationsByUser(currentUserId);

            if (calculations.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Нет данных для экспорта",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Экспорт в JSON");
            fileChooser.setSelectedFile(new File("calculations_" +
                    REPORT_DATE_FORMAT.format(new Date()) + ".json"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Создаем ObjectMapper для форматированного вывода
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                // Записываем данные в файл
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, calculations);

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в файл:\n" + file.getAbsolutePath(),
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при экспорте в JSON: " + e.getMessage(),
                    "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Импорт данных из JSON файла
     */
    private void importFromJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Импорт из JSON");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JSON файлы", "json"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                // Читаем JSON файл
                String jsonContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

                // Импортируем данные
                int importedCount = calculationDao.importFromJson(currentUserId, jsonContent);

                if (importedCount > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Успешно импортировано расчетов: " + importedCount,
                            "Импорт завершен", JOptionPane.INFORMATION_MESSAGE);
                    loadCalculations(); // Перезагружаем данные
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Не удалось импортировать данные. Проверьте формат файла.",
                            "Ошибка импорта", JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при импорте из JSON: " + e.getMessage(),
                        "Ошибка импорта", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Генерация HTML отчета
     */
    private void generateHtmlReport() {
        try {
            // Получаем статистику
            Map<String, Object> stats = calculationDao.getCalculationStatistics(currentUserId);
            int totalCount = (int) stats.getOrDefault("total", 0);

            if (totalCount == 0) {
                JOptionPane.showMessageDialog(this,
                        "Нет данных для генерации отчета",
                        "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Генерация HTML отчета");
            fileChooser.setSelectedFile(new File("report_" +
                    REPORT_DATE_FORMAT.format(new Date()) + ".html"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Получаем все расчеты
                List<CalculationResult> calculations = calculationDao.getCalculationsByUser(currentUserId);

                // Генерируем HTML отчет
                String htmlContent = generateHtmlContent(calculations, stats);

                // Сохраняем в файл
                Files.writeString(file.toPath(), htmlContent);

                // Показываем успешное сообщение с возможностью открыть файл
                int response = JOptionPane.showConfirmDialog(this,
                        "HTML отчет успешно сгенерирован:\n" + file.getAbsolutePath() +
                                "\n\nОткрыть отчет в браузере?",
                        "Отчет сгенерирован", JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().browse(file.toURI());
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при генерации отчета: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Показать статистику расчетов
     */
    private void showStatistics() {
        try {
            Map<String, Object> stats = calculationDao.getCalculationStatistics(currentUserId);

            int total = (int) stats.getOrDefault("total", 0);
            int lanceCount = (int) stats.getOrDefault("lanceCount", 0);
            int nozzleCount = (int) stats.getOrDefault("nozzleCount", 0);

            String message = String.format("Статистика расчетов:%n%n" +
                            "Всего расчетов: %d%n" +
                            "• Кислородная фурма: %d (%.1f%%)%n" +
                            "• Сопло Лаваля: %d (%.1f%%)%n%n" +
                            "Первый расчет: %s%n" +
                            "Последний расчет: %s%n",
                    total,
                    lanceCount, total > 0 ? (lanceCount * 100.0 / total) : 0,
                    nozzleCount, total > 0 ? (nozzleCount * 100.0 / total) : 0,
                    formatObject(stats.get("firstDate")),
                    formatObject(stats.get("lastDate"))
            );

            JOptionPane.showMessageDialog(this, message, "Статистика",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при получении статистики: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Вспомогательные методы
     */

    private String getCalculationTypeName(String type) {
        return "OXYGEN_LANCE".equals(type) ? "Кислородная фурма" : "Сопло Лаваля";
    }

    private String formatDate(LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    private String formatParameters(Map<String, Double> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Double> entry : params.entrySet()) {
            if (count > 2) {
                sb.append("...");
                break;
            }
            if (count > 0) sb.append(", ");
            sb.append(entry.getKey()).append(": ").append(String.format("%.2f", entry.getValue()));
            count++;
        }
        return sb.toString();
    }

    private String formatObject(Object obj) {
        return obj != null ? obj.toString() : "нет данных";
    }

    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = null;
        LocalDateTime end = now;

        switch (period) {
            case "Сегодня":
                start = now.toLocalDate().atStartOfDay();
                break;
            case "Вчера":
                start = now.minusDays(1).toLocalDate().atStartOfDay();
                end = now.toLocalDate().atStartOfDay();
                break;
            case "Эта неделя":
                start = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
                break;
            case "Этот месяц":
                start = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            case "Этот год":
                start = now.withDayOfYear(1).toLocalDate().atStartOfDay();
                break;
        }

        return start != null ? new LocalDateTime[]{start, end} : null;
    }

    private void updateStatus() {
        int totalRows = tableModel.getRowCount();
        int filteredRows = calculationsTable.getRowCount();

        if (totalRows == filteredRows) {
            statusLabel.setText(" Всего записей: " + totalRows);
        } else {
            statusLabel.setText(String.format(" Показано: %d из %d записей", filteredRows, totalRows));
        }
    }

    /**
     * Диалог для отображения деталей расчета
     */
    private void showCalculationDetailsDialog(CalculationResult calculation) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Детали расчета: " + calculation.getTitle(), true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        // Панель с информацией
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addInfoRow(infoPanel, gbc, "ID:", String.valueOf(calculation.getId()), 0);
        addInfoRow(infoPanel, gbc, "Тип:", getCalculationTypeName(calculation.getCalculationType()), 1);
        addInfoRow(infoPanel, gbc, "Название:", calculation.getTitle(), 2);
        addInfoRow(infoPanel, gbc, "Дата:", formatDate(calculation.getCalculationDate()), 3);

        // Входные параметры
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        infoPanel.add(new JLabel("Входные параметры:"), gbc);

        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea inputArea = new JTextArea(formatParametersForDisplay(calculation.getInputParameters()));
        inputArea.setEditable(false);
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoPanel.add(new JScrollPane(inputArea), gbc);

        // Выходные результаты
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.add(new JLabel("Результаты:"), gbc);

        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea outputArea = new JTextArea(formatParametersForDisplay(calculation.getOutputResults()));
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoPanel.add(new JScrollPane(outputArea), gbc);

        // Примечания
        if (calculation.getNotes() != null && !calculation.getNotes().isEmpty()) {
            gbc.gridy = 8;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            infoPanel.add(new JLabel("Примечания:"), gbc);

            gbc.gridy = 9;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;
            JTextArea notesArea = new JTextArea(calculation.getNotes());
            notesArea.setEditable(false);
            infoPanel.add(new JScrollPane(notesArea), gbc);
        }

        dialog.add(infoPanel, BorderLayout.CENTER);

        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
    }

    private String formatParametersForDisplay(Map<String, Double> params) {
        if (params == null || params.isEmpty()) return "Нет данных";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : params.entrySet()) {
            sb.append("• ").append(entry.getKey()).append(": ")
                    .append(String.format("%.4f", entry.getValue())).append("\n");
        }
        return sb.toString();
    }

    /**
     * Генерация HTML контента для отчета
     */
    private String generateHtmlContent(List<CalculationResult> calculations, Map<String, Object> stats) {
        String timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
        int total = calculations.size();
        int lanceCount = (int) stats.getOrDefault("lanceCount", 0);
        int nozzleCount = (int) stats.getOrDefault("nozzleCount", 0);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Отчет по расчетам</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 40px; color: #333; }\n");
        html.append("        h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #34495e; margin-top: 30px; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("        th { background-color: #3498db; color: white; padding: 12px; text-align: left; }\n");
        html.append("        td { padding: 10px; border-bottom: 1px solid #ddd; }\n");
        html.append("        tr:nth-child(even) { background-color: #f8f9fa; }\n");
        html.append("        tr:hover { background-color: #f1f8ff; }\n");
        html.append("        .stats { background-color: #e8f4fc; padding: 15px; border-radius: 5px; margin: 20px 0; }\n");
        html.append("        .footer { margin-top: 40px; font-size: 0.9em; color: #7f8c8d; text-align: center; }\n");
        html.append("        .type-lance { color: #27ae60; }\n");
        html.append("        .type-nozzle { color: #e74c3c; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>📊 Отчет по расчетам параметров МСК</h1>\n");
        html.append("    <div class=\"stats\">\n");
        html.append("        <p><strong>Дата формирования отчета:</strong> ").append(timestamp).append("</p>\n");
        html.append("        <p><strong>Общее количество расчетов:</strong> ").append(total).append("</p>\n");
        html.append("        <p><strong>Кислородная фурма:</strong> <span class='type-lance'>").append(lanceCount).append("</span> расчетов</p>\n");
        html.append("        <p><strong>Сопло Лаваля:</strong> <span class='type-nozzle'>").append(nozzleCount).append("</span> расчетов</p>\n");
        html.append("    </div>\n");
        html.append("    <h2>📋 Детали расчетов</h2>\n");
        html.append("    <table>\n");
        html.append("        <tr>\n");
        html.append("            <th>ID</th>\n");
        html.append("            <th>Тип</th>\n");
        html.append("            <th>Название</th>\n");
        html.append("            <th>Дата</th>\n");
        html.append("            <th>Кол-во параметров</th>\n");
        html.append("            <th>Кол-во результатов</th>\n");
        html.append("        </tr>\n");

        for (CalculationResult calc : calculations) {
            String typeClass = "OXYGEN_LANCE".equals(calc.getCalculationType()) ? "type-lance" : "type-nozzle";
            String typeName = getCalculationTypeName(calc.getCalculationType());

            html.append("        <tr>\n");
            html.append("            <td>").append(calc.getId()).append("</td>\n");
            html.append("            <td class='").append(typeClass).append("'>").append(typeName).append("</td>\n");
            html.append("            <td>").append(escapeHtml(calc.getTitle())).append("</td>\n");
            html.append("            <td>").append(formatDate(calc.getCalculationDate())).append("</td>\n");
            html.append("            <td>").append(calc.getInputParameters().size()).append("</td>\n");
            html.append("            <td>").append(calc.getOutputResults().size()).append("</td>\n");
            html.append("        </tr>\n");
        }

        html.append("    </table>\n");
        html.append("    <div class=\"footer\">\n");
        html.append("        <p>Отчет сгенерирован приложением \"Расчет параметров МСК\"</p>\n");
        html.append("        <p>© ").append(new SimpleDateFormat("yyyy").format(new Date())).append(" - Курсовая работа</p>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Класс для фильтрации по диапазону дат
     */
    private class DateRangeFilter extends RowFilter<Object, Object> {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final int columnIndex;

        public DateRangeFilter(LocalDateTime startDate, LocalDateTime endDate, int columnIndex) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.columnIndex = columnIndex;
        }

        @Override
        public boolean include(Entry<? extends Object, ? extends Object> entry) {
            try {
                String dateStr = (String) entry.getValue(columnIndex);
                LocalDateTime date = LocalDateTime.parse(dateStr, DATE_FORMATTER);
                return !date.isBefore(startDate) && !date.isAfter(endDate);
            } catch (Exception e) {
                return false;
            }
        }
    }
}