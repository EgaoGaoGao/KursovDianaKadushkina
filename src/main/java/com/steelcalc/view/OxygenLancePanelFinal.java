package com.steelcalc.view;

import com.steelcalc.model.OxygenLance;
import com.steelcalc.model.CalculationResult;
import com.steelcalc.service.CalculationService;
import com.steelcalc.dao.CalculationDao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OxygenLancePanelFinal extends JPanel {
    // Поля ввода (те же, что и раньше)
    private JTextField flowRateField;
    private JTextField pressureField;
    private JTextField diameterField;
    private JTextField temperatureField;
    private JTextField purityField;
    private JComboBox<String> materialComboBox;
    private JComboBox<String> unitComboBox;
    private JRadioButton standardModeRadio;
    private JRadioButton advancedModeRadio;
    private JCheckBox includeHeatLossCheckbox;
    private JCheckBox optimizeCheckbox;

    // Кнопки
    private JButton calculateButton;
    private JButton clearButton;
    private JButton saveButton;

    // Результаты
    private JTextArea resultTextArea;

    // Текущий расчёт
    private OxygenLance currentLance;

    // Для сохранения
    private CalculationDao calculationDao;
    private int currentUserId;

    public OxygenLancePanelFinal(int userId) {
        this.currentUserId = userId;
        this.calculationDao = new CalculationDao();
        this.currentLance = null;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Создаём компоненты (используем код из предыдущей OxygenLancePanel)
        add(createInputPanel(), BorderLayout.NORTH);
        add(createButtonPanel(), BorderLayout.CENTER);
        add(createResultPanel(), BorderLayout.SOUTH);

        // Предзаполняем значения для тестирования
        flowRateField.setText("1500.0");
        pressureField.setText("2.5");
        diameterField.setText("15.0");
        temperatureField.setText("25.0");
        purityField.setText("99.5");
    }

    private JPanel createInputPanel() {
        // Возвращаем панель ввода из предыдущей версии OxygenLancePanel
        // (полный код слишком длинный, используем ваш существующий)
        // ... [ваш код создания панели ввода] ...
        JPanel panel = new JPanel(new GridBagLayout());
        // ... [ваш код] ...
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Управление"));

        calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(e -> performCalculation());

        clearButton = new JButton("Очистить");
        clearButton.addActionListener(e -> clearFields());

        saveButton = new JButton("Сохранить в историю");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveToHistory());

        panel.add(calculateButton);
        panel.add(clearButton);
        panel.add(saveButton);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Результаты расчёта"));
        panel.setPreferredSize(new Dimension(0, 250));

        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultTextArea.setText("Результаты появятся здесь после расчёта...\n");

        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void performCalculation() {
        try {
            // Собираем данные из полей
            double flowRate = Double.parseDouble(flowRateField.getText());
            double pressure = Double.parseDouble(pressureField.getText());
            double diameter = Double.parseDouble(diameterField.getText());
            double temperature = Double.parseDouble(temperatureField.getText());
            double purity = Double.parseDouble(purityField.getText());
            String material = (String) materialComboBox.getSelectedItem();

            // Создаём объект для расчёта
            currentLance = new OxygenLance(
                    flowRate, pressure, diameter, temperature, material
            );
            currentLance.setOxygenPurity(purity);

            // Выполняем расчёт
            CalculationService service = new CalculationService();
            String error = service.validateOxygenLanceInput(currentLance);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentLance = service.calculateOxygenLance(currentLance);

            // Отображаем результаты
            displayResults(currentLance);

            // Активируем кнопку сохранения
            saveButton.setEnabled(true);

            JOptionPane.showMessageDialog(this,
                    "Расчёт выполнен успешно!", "Готово", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка ввода чисел. Проверьте все поля.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void displayResults(OxygenLance lance) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== РЕЗУЛЬТАТЫ РАСЧЁТА КИСЛОРОДНОЙ ФУРМЫ ===\n\n");

        sb.append("Входные параметры:\n");
        sb.append(String.format("Материал: %s\n", lance.getMaterialName()));
        sb.append(String.format("Расход O₂: %.1f м³/ч\n", lance.getOxygenFlowRate()));
        sb.append(String.format("Давление: %.2f МПа\n", lance.getPressure()));
        sb.append(String.format("Диаметр: %.1f мм\n", lance.getNozzleDiameter()));
        sb.append(String.format("Температура: %.1f °C\n", lance.getTemperature()));
        sb.append(String.format("Чистота O₂: %.1f%%\n\n", lance.getOxygenPurity()));

        sb.append("Результаты:\n");
        sb.append(String.format("Скорость истечения: %.1f м/с\n", lance.getExitVelocity()));
        sb.append(String.format("Сила удара струи: %.1f Н\n", lance.getJetForce()));
        sb.append(String.format("Эффективность: %.1f%%\n", lance.getEfficiency()));
        sb.append(String.format("Число Маха: %.2f\n", lance.getMachNumber()));
        sb.append(String.format("Число Рейнольдса: %.0f\n\n", lance.getReynoldsNumber()));

        if (lance.getNotes() != null) {
            sb.append("Рекомендации:\n").append(lance.getNotes()).append("\n");
        }

        sb.append("Дата расчёта: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

        resultTextArea.setText(sb.toString());
    }

    private void clearFields() {
        flowRateField.setText("");
        pressureField.setText("");
        diameterField.setText("");
        temperatureField.setText("");
        purityField.setText("99.5");
        materialComboBox.setSelectedIndex(0);
        unitComboBox.setSelectedIndex(0);
        resultTextArea.setText("Результаты появятся здесь после расчёта...\n");
        saveButton.setEnabled(false);
        currentLance = null;
    }

    private void saveToHistory() {
        if (currentLance == null) {
            JOptionPane.showMessageDialog(this,
                    "Сначала выполните расчёт", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = JOptionPane.showInputDialog(this,
                "Введите название для сохранения:", "Сохранение расчёта",
                JOptionPane.QUESTION_MESSAGE);

        if (title == null || title.trim().isEmpty()) {
            return;
        }

        try {
            // Преобразуем OxygenLance в CalculationResult
            CalculationService service = new CalculationService();
            CalculationResult result = service.convertToCalculationResult(currentLance, title);
            result.setUserId(currentUserId);

            // Сохраняем в БД
            boolean success = calculationDao.saveCalculation(result);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Расчёт успешно сохранён в истории!\nID: " + result.getId(),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Ошибка при сохранении", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Геттер для MainFrame
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }
}