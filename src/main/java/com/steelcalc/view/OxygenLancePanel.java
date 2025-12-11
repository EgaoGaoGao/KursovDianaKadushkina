package com.steelcalc.view;

import com.steelcalc.dao.CalculationDao;
import com.steelcalc.model.CalculationResult;
import com.steelcalc.model.OxygenLance;
import com.steelcalc.service.CalculationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OxygenLancePanel extends JPanel {
    // Текстовые поля для ввода
    private JTextField flowRateField;
    private JTextField pressureField;
    private JTextField diameterField;
    private JTextField temperatureField;
    private JTextField purityField;

    // Выпадающие списки
    private JComboBox<String> materialComboBox;
    private JComboBox<String> unitComboBox;

    // Переключатели
    private JRadioButton standardModeRadio;
    private JRadioButton advancedModeRadio;

    // Флажки
    private JCheckBox includeHeatLossCheckbox;
    private JCheckBox optimizeCheckbox;

    // Кнопки
    private JButton calculateButton;
    private JButton clearButton;
    private JButton saveButton;

    // Область результатов
    private JTextArea resultTextArea;

    // Текущий расчет для возможного сохранения
    private OxygenLance currentLance;

    // ID текущего пользователя (пока заглушка, нужно передать из MainFrame)
    private int currentUserId = 1;

    public OxygenLancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Создаём панель ввода параметров
        add(createInputPanel(), BorderLayout.NORTH);

        // Создаём панель кнопок
        add(createButtonPanel(), BorderLayout.CENTER);

        // Создаём панель результатов
        add(createResultPanel(), BorderLayout.SOUTH);

        // Инициализируем поле текущего расчета
        currentLance = null;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Параметры расчёта"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Строка 0: Переключатели режимов
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup modeGroup = new ButtonGroup();
        standardModeRadio = new JRadioButton("Стандартный режим", true);
        advancedModeRadio = new JRadioButton("Расширенный режим");
        modeGroup.add(standardModeRadio);
        modeGroup.add(advancedModeRadio);
        modePanel.add(standardModeRadio);
        modePanel.add(advancedModeRadio);
        inputPanel.add(modePanel, gbc);

        // Строка 1: Расход кислорода
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Расход кислорода:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        flowRateField = new JTextField(10);
        flowRateField.setToolTipText("Введите расход кислорода в м³/ч");
        flowRateField.setText("1500.0");
        flowPanel.add(flowRateField);

        unitComboBox = new JComboBox<>(new String[]{"м³/ч", "л/мин", "кг/с"});
        flowPanel.add(unitComboBox);
        inputPanel.add(flowPanel, gbc);

        // Строка 2: Давление
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Давление (МПа):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        pressureField = new JTextField(10);
        pressureField.setToolTipText("Давление кислорода в мегапаскалях");
        pressureField.setText("2.5");
        inputPanel.add(pressureField, gbc);

        // Строка 3: Диаметр сопла
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Диаметр сопла (мм):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        diameterField = new JTextField(10);
        diameterField.setText("15.0");
        inputPanel.add(diameterField, gbc);

        // Строка 4: Температура
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Температура (°C):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        temperatureField = new JTextField(10);
        temperatureField.setText("25.0");
        inputPanel.add(temperatureField, gbc);

        // Строка 5: Чистота кислорода
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Чистота O₂ (%):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        purityField = new JTextField(10);
        purityField.setText("99.5");
        purityField.setToolTipText("Чистота кислорода в процентах (0-100%)");
        inputPanel.add(purityField, gbc);

        // Строка 6: Материал
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Материал:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        materialComboBox = new JComboBox<>(new String[]{
                "Сталь 20", "Сталь 45", "Нержавеющая сталь",
                "Чугун", "Алюминий", "Медь"
        });
        materialComboBox.setSelectedIndex(0);
        inputPanel.add(materialComboBox, gbc);

        // Строка 7: Флажки
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        includeHeatLossCheckbox = new JCheckBox("Учитывать теплопотери");
        optimizeCheckbox = new JCheckBox("Оптимизировать параметры");
        checkBoxPanel.add(includeHeatLossCheckbox);
        checkBoxPanel.add(optimizeCheckbox);
        inputPanel.add(checkBoxPanel, gbc);

        return inputPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Управление"));

        calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performCalculation();
            }
        });

        clearButton = new JButton("Очистить");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        saveButton = new JButton("Сохранить результат");
        saveButton.setEnabled(false); // Пока не активирована
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCalculation();
            }
        });

        buttonPanel.add(calculateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);

        return buttonPanel;
    }

    private JPanel createResultPanel() {
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Результаты расчёта"));
        resultPanel.setPreferredSize(new Dimension(0, 250)); // Задаём высоту

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultTextArea.setText("Результаты расчёта появятся здесь...\n\n");
        resultTextArea.append("Для начала расчёта введите параметры и нажмите 'Рассчитать'.\n");
        resultTextArea.append("Примеры параметров уже заполнены для тестирования.\n\n");

        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        return resultPanel;
    }

    /**
     * Выполнение расчета кислородной фурмы
     */
    private void performCalculation() {
        try {
            // Создаем объект OxygenLance из введенных данных
            OxygenLance lance = new OxygenLance();

            // Получаем значения из полей ввода с валидацией
            double flowRate = parseDouble(flowRateField.getText(), "Расход кислорода");
            double pressure = parseDouble(pressureField.getText(), "Давление");
            double diameter = parseDouble(diameterField.getText(), "Диаметр сопла");
            double temperature = parseDouble(temperatureField.getText(), "Температура");
            double purity = parseDouble(purityField.getText(), "Чистота кислорода");
            String material = (String) materialComboBox.getSelectedItem();

            // Конвертируем единицы измерения при необходимости
            String selectedUnit = (String) unitComboBox.getSelectedItem();
            if ("л/мин".equals(selectedUnit)) {
                flowRate = flowRate * 60 / 1000; // л/мин → м³/ч
            } else if ("кг/с".equals(selectedUnit)) {
                // Приближенная конвертация: 1 кг/с O2 ≈ 0.7 м³/ч при нормальных условиях
                flowRate = flowRate * 0.7 * 3600;
            }

            // Устанавливаем параметры
            lance.setOxygenFlowRate(flowRate);
            lance.setPressure(pressure);
            lance.setNozzleDiameter(diameter);
            lance.setTemperature(temperature);
            lance.setOxygenPurity(purity);
            lance.setMaterialName(material);

            // Учитываем дополнительные опции
            if (includeHeatLossCheckbox.isSelected()) {
                lance.setTemperature(lance.getTemperature() + 50); // Учет теплопотерь
            }

            // Валидация входных данных
            CalculationService service = new CalculationService();
            String validationError = service.validateOxygenLanceInput(lance);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка валидации:\n" + validationError,
                        "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Выполнение расчета
            lance = service.calculateOxygenLance(lance);

            // Дополнительная оптимизация если выбрана
            if (optimizeCheckbox.isSelected()) {
                optimizeResults(lance);
            }

            // Отображение результатов
            displayResults(lance);

            // Активация кнопки сохранения
            saveButton.setEnabled(true);

            // Сохранение текущего расчета для возможного сохранения в БД
            currentLance = lance;

            // Успешное сообщение
            JOptionPane.showMessageDialog(this,
                    "Расчёт успешно выполнен!\nРезультаты отображены ниже.",
                    "Успех", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Некорректный формат числа.\n" +
                            "Используйте точку как десятичный разделитель.\n" +
                            "Пример: 1500.0 или 2.5",
                    "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка ввода: " + e.getMessage(),
                    "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при выполнении расчёта:\n" + e.getMessage(),
                    "Ошибка расчета", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Парсинг строки в число с обработкой ошибок
     */
    private double parseDouble(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }

        // Заменяем запятую на точку для корректного парсинга
        String normalizedText = text.trim().replace(',', '.');

        try {
            return Double.parseDouble(normalizedText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некорректное значение для " + fieldName + ": '" + text + "'\n" +
                            "Введите число, например: 1500.0"
            );
        }
    }

    /**
     * Отображение результатов расчета в текстовой области
     */
    private void displayResults(OxygenLance lance) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== РЕЗУЛЬТАТЫ РАСЧЁТА КИСЛОРОДНОЙ ФУРМЫ ===\n\n");

        sb.append("ВВЕДЕННЫЕ ПАРАМЕТРЫ:\n");
        sb.append(String.format("  Материал:                 %s\n", lance.getMaterialName()));
        sb.append(String.format("  Расход кислорода:        %.2f м³/ч\n", lance.getOxygenFlowRate()));
        sb.append(String.format("  Давление:                 %.2f МПа\n", lance.getPressure()));
        sb.append(String.format("  Диаметр сопла:           %.2f мм\n", lance.getNozzleDiameter()));
        sb.append(String.format("  Температура:             %.1f °C\n", lance.getTemperature()));
        sb.append(String.format("  Чистота O₂:              %.1f%%\n", lance.getOxygenPurity()));
        sb.append(String.format("  Режим:                   %s\n",
                standardModeRadio.isSelected() ? "Стандартный" : "Расширенный"));

        sb.append("\nРЕЗУЛЬТАТЫ РАСЧЁТА:\n");
        sb.append(String.format("  1. Скорость истечения:    %.1f м/с\n", lance.getExitVelocity()));
        sb.append(String.format("  2. Сила удара струи:      %.1f Н\n", lance.getJetForce()));
        sb.append(String.format("  3. Эффективность:         %.1f%%\n", lance.getEfficiency()));
        sb.append(String.format("  4. Число Маха:            %.2f\n", lance.getMachNumber()));
        sb.append(String.format("  5. Число Рейнольдса:      %.0f\n", lance.getReynoldsNumber()));

        // Дополнительная информация в зависимости от режима
        if (advancedModeRadio.isSelected()) {
            sb.append("\nДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ:\n");
            if (lance.getMachNumber() > 1) {
                sb.append("  - Режим течения: СВЕРХЗВУКОВОЙ\n");
            } else {
                sb.append("  - Режим течения: ДОЗВУКОВОЙ\n");
            }

            if (lance.getReynoldsNumber() > 4000) {
                sb.append("  - Режим течения: ТУРБУЛЕНТНЫЙ\n");
            } else {
                sb.append("  - Режим течения: ЛАМИНАРНЫЙ\n");
            }

            // Оценка качества
            if (lance.getEfficiency() > 90) {
                sb.append("  - Качество: ОТЛИЧНОЕ\n");
            } else if (lance.getEfficiency() > 80) {
                sb.append("  - Качество: ХОРОШЕЕ\n");
            } else {
                sb.append("  - Качество: УДОВЛЕТВОРИТЕЛЬНОЕ\n");
            }
        }

        sb.append("\nРЕКОМЕНДАЦИИ:\n");
        if (lance.getNotes() != null && !lance.getNotes().isEmpty()) {
            sb.append(lance.getNotes());
        } else {
            sb.append("  Нет особых рекомендаций. Параметры в норме.\n");
        }

        // Добавляем время расчета
        sb.append(String.format("\n\nРасчёт выполнен: %s",
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))));

        resultTextArea.setText(sb.toString());
        resultTextArea.setCaretPosition(0); // Прокрутка к началу
    }

    /**
     * Простая оптимизация результатов (демонстрационная)
     */
    private void optimizeResults(OxygenLance lance) {
        if (lance.getEfficiency() < 85) {
            // Увеличиваем скорость истечения на 5% если эффективность низкая
            lance.setExitVelocity(lance.getExitVelocity() * 1.05);
            lance.setEfficiency(lance.getEfficiency() * 1.03);

            if (lance.getNotes() == null) {
                lance.setNotes("");
            }
            lance.setNotes(lance.getNotes() +
                    "\nПрименена оптимизация: скорость увеличена на 5%.\n");
        }
    }

    /**
     * Очистка полей ввода
     */
    private void clearFields() {
        flowRateField.setText("");
        pressureField.setText("");
        diameterField.setText("");
        temperatureField.setText("");
        purityField.setText("99.5");
        materialComboBox.setSelectedIndex(0);
        unitComboBox.setSelectedIndex(0);
        standardModeRadio.setSelected(true);
        includeHeatLossCheckbox.setSelected(false);
        optimizeCheckbox.setSelected(false);

        resultTextArea.setText("Результаты расчёта появятся здесь...\n\n");
        resultTextArea.append("Для начала расчёта введите параметры и нажмите 'Рассчитать'.\n");
        resultTextArea.append("Примеры параметров уже заполнены для тестирования.\n\n");

        saveButton.setEnabled(false);
        currentLance = null;

        JOptionPane.showMessageDialog(this,
                "Все поля очищены.",
                "Очистка", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Сохранение расчета в базу данных
     */
    private void saveCalculation() {
        if (currentLance == null) {
            JOptionPane.showMessageDialog(this,
                    "Нет данных для сохранения.\nСначала выполните расчёт.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = JOptionPane.showInputDialog(this,
                "Введите название для сохранения расчета:",
                "Сохранение расчета", JOptionPane.QUESTION_MESSAGE);

        if (title != null && !title.trim().isEmpty()) {
            try {
                // Устанавливаем ID пользователя (в реальном приложении нужно передать из MainFrame)
                currentLance.setUserId(currentUserId);

                // Создаем CalculationResult из OxygenLance
                CalculationService service = new CalculationService();
                CalculationResult result = service.convertToCalculationResult(currentLance, title);
                result.setUserId(currentUserId);

                // Сохраняем в БД
                CalculationDao calculationDao = new CalculationDao();
                boolean saved = calculationDao.saveCalculation(result);

                if (saved) {
                    JOptionPane.showMessageDialog(this,
                            "Расчёт успешно сохранён в истории!\n" +
                                    "ID: " + result.getId() + "\n" +
                                    "Перейдите во вкладку 'История расчётов' чтобы увидеть.",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при сохранении расчёта в БД.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при сохранении: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    /**
     * Получение текущего расчета (для тестирования)
     */
    public OxygenLance getCurrentLance() {
        return currentLance;
    }
}