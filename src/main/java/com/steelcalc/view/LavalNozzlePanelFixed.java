package com.steelcalc.view;

import com.steelcalc.model.LavalNozzle;
import com.steelcalc.service.CalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LavalNozzlePanelFixed extends JPanel {
    // Компоненты ввода
    private JTextField inletPressureField;
    private JTextField outletPressureField;
    private JTextField temperatureField;
    private JTextField massFlowField;
    private JComboBox<String> gasTypeComboBox;

    // Графическая панель
    private DrawingPanel drawingPanel;

    // Переключатели
    private JRadioButton subsonicRadio;
    private JRadioButton supersonicRadio;

    // Слайдер
    private JSlider expansionRatioSlider;
    private JLabel sliderValueLabel;

    // Для хранения текущего расчета
    private LavalNozzle currentNozzle;

    public LavalNozzlePanelFixed() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Левая панель - параметры
        add(createParameterPanel(), BorderLayout.WEST);

        // Центральная панель - визуализация
        add(createVisualizationPanel(), BorderLayout.CENTER);

        // Нижняя панель - управление
        add(createControlPanel(), BorderLayout.SOUTH);

        // Инициализация текущего расчета
        currentNozzle = null;
    }

    private JPanel createParameterPanel() {
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Параметры сопла Лаваля"));
        paramPanel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Строка 0: Тип газа
        gbc.gridx = 0;
        gbc.gridy = 0;
        paramPanel.add(new JLabel("Рабочий газ:"), gbc);

        gbc.gridx = 1;
        gasTypeComboBox = new JComboBox<>(new String[]{
                "Кислород", "Воздух", "Азот", "Водяной пар", "Гелий"
        });
        paramPanel.add(gasTypeComboBox, gbc);

        // Строка 1: Давление на входе
        gbc.gridy = 1;
        gbc.gridx = 0;
        paramPanel.add(new JLabel("Давление на входе (атм):"), gbc);

        gbc.gridx = 1;
        inletPressureField = new JTextField(10);
        inletPressureField.setText("10.0");
        paramPanel.add(inletPressureField, gbc);

        // Строка 2: Давление на выходе
        gbc.gridy = 2;
        gbc.gridx = 0;
        paramPanel.add(new JLabel("Давление на выходе (атм):"), gbc);

        gbc.gridx = 1;
        outletPressureField = new JTextField(10);
        outletPressureField.setText("1.0");
        paramPanel.add(outletPressureField, gbc);

        // Строка 3: Температура
        gbc.gridy = 3;
        gbc.gridx = 0;
        paramPanel.add(new JLabel("Температура (K):"), gbc);

        gbc.gridx = 1;
        temperatureField = new JTextField(10);
        temperatureField.setText("300");
        paramPanel.add(temperatureField, gbc);

        // Строка 4: Массовый расход
        gbc.gridy = 4;
        gbc.gridx = 0;
        paramPanel.add(new JLabel("Массовый расход (кг/с):"), gbc);

        gbc.gridx = 1;
        massFlowField = new JTextField(10);
        massFlowField.setText("1.0");
        paramPanel.add(massFlowField, gbc);

        // Строка 5: Переключатели режима
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        modePanel.setBorder(BorderFactory.createTitledBorder("Режим течения"));

        ButtonGroup flowGroup = new ButtonGroup();
        subsonicRadio = new JRadioButton("Дозвуковой", true);
        supersonicRadio = new JRadioButton("Сверхзвуковой");

        flowGroup.add(subsonicRadio);
        flowGroup.add(supersonicRadio);

        modePanel.add(subsonicRadio);
        modePanel.add(supersonicRadio);

        paramPanel.add(modePanel, gbc);

        // Строка 6: Слайдер степени расширения
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBorder(BorderFactory.createTitledBorder("Степень расширения"));

        expansionRatioSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 5);
        expansionRatioSlider.setMajorTickSpacing(5);
        expansionRatioSlider.setMinorTickSpacing(1);
        expansionRatioSlider.setPaintTicks(true);
        expansionRatioSlider.setPaintLabels(true);

        // Слушатель изменения слайдера - обновляет график
        expansionRatioSlider.addChangeListener(e -> {
            updateSliderValue();
            if (drawingPanel != null) {
                drawingPanel.setExpansionRatio(expansionRatioSlider.getValue());
                drawingPanel.repaint();
            }
        });

        sliderPanel.add(expansionRatioSlider, BorderLayout.CENTER);

        sliderValueLabel = new JLabel("Текущее значение: 5");
        sliderPanel.add(sliderValueLabel, BorderLayout.SOUTH);

        paramPanel.add(sliderPanel, gbc);

        return paramPanel;
    }

    private void updateSliderValue() {
        int value = expansionRatioSlider.getValue();
        sliderValueLabel.setText("Текущее значение: " + value);
    }

    private JPanel createVisualizationPanel() {
        JPanel visPanel = new JPanel(new BorderLayout());
        visPanel.setBorder(BorderFactory.createTitledBorder("Визуализация профиля сопла"));

        drawingPanel = new DrawingPanel();
        drawingPanel.setExpansionRatio(expansionRatioSlider.getValue());
        visPanel.add(drawingPanel, BorderLayout.CENTER);

        // Панель с информацией (будет обновляться после расчета)
        infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoPanel.add(createInfoLabel("Критическое сечение:", "—"));
        infoPanel.add(createInfoLabel("Число Маха:", "—"));
        infoPanel.add(createInfoLabel("Скорость истечения:", "—"));
        infoPanel.add(createInfoLabel("Коэфф. ускорения:", "—"));

        visPanel.add(infoPanel, BorderLayout.SOUTH);

        return visPanel;
    }

    private JPanel infoPanel;

    private JPanel createInfoLabel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        valueLabel.setForeground(Color.BLUE);
        valueLabel.setName(title); // Для поиска метки по названию

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        return panel;
    }

    private void updateInfoPanel(String title, String value) {
        // Ищем нужную метку по имени и обновляем её значение
        for (Component comp : infoPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                for (Component subComp : panel.getComponents()) {
                    if (subComp instanceof JLabel && subComp.getName() != null &&
                            subComp.getName().equals(title)) {
                        ((JLabel) subComp).setText(value);
                        return;
                    }
                }
            }
        }
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton calculateButton = new JButton("Рассчитать профиль");
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateProfile();
            }
        });

        JButton animateButton = new JButton("Анимировать поток");
        animateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateFlow();
            }
        });

        JButton exportButton = new JButton("Экспорт данных");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportData();
            }
        });

        JButton saveButton = new JButton("Сохранить в историю");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToHistory();
            }
        });

        controlPanel.add(calculateButton);
        controlPanel.add(animateButton);
        controlPanel.add(exportButton);
        controlPanel.add(saveButton);

        return controlPanel;
    }

    private void calculateProfile() {
        try {
            // Получаем данные из полей
            String gasType = (String) gasTypeComboBox.getSelectedItem();
            double inletPressure = parseDouble(inletPressureField.getText(), "Давление на входе");
            double outletPressure = parseDouble(outletPressureField.getText(), "Давление на выходе");
            double temperature = parseDouble(temperatureField.getText(), "Температура");
            double massFlow = parseDouble(massFlowField.getText(), "Массовый расход");
            double expansionRatio = expansionRatioSlider.getValue();
            boolean isSupersonic = supersonicRadio.isSelected();

            // Создаем объект для расчета
            LavalNozzle nozzle = new LavalNozzle(
                    gasType, inletPressure, outletPressure,
                    temperature, massFlow, expansionRatio, isSupersonic
            );

            // Валидация входных данных
            CalculationService service = new CalculationService();
            String validationError = service.validateLavalNozzleInput(nozzle);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка ввода:\n" + validationError,
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Выполняем расчет
            nozzle = service.calculateLavalNozzle(nozzle);
            currentNozzle = nozzle;

            // Обновляем график с новыми данными
            drawingPanel.setNozzleParameters(
                    nozzle.getThroatArea(),
                    nozzle.getExitArea(),
                    nozzle.getExpansionRatio()
            );
            drawingPanel.repaint();

            // Обновляем информационную панель
            updateInfoPanel("Критическое сечение:", String.format("%.2f мм²", nozzle.getThroatArea()));
            updateInfoPanel("Число Маха:", String.format("%.2f", nozzle.getMachNumber()));
            updateInfoPanel("Скорость истечения:", String.format("%.1f м/с", nozzle.getExitVelocity()));
            updateInfoPanel("Коэфф. ускорения:", String.format("%.1f", nozzle.getExpansionRatio()));

            // Показываем результаты в диалоговом окне
            JOptionPane.showMessageDialog(this,
                    "✅ Расчёт выполнен успешно!\n\n" +
                            "📊 Результаты:\n" +
                            String.format("• Критическое сечение: %.2f мм²\n", nozzle.getThroatArea()) +
                            String.format("• Выходное сечение: %.2f мм²\n", nozzle.getExitArea()) +
                            String.format("• Скорость истечения: %.1f м/с\n", nozzle.getExitVelocity()) +
                            String.format("• Число Маха: %.2f\n", nozzle.getMachNumber()) +
                            String.format("• Тяга: %.2f Н\n", nozzle.getThrust()) +
                            String.format("• Эффективность: %.1f%%\n\n", nozzle.getEfficiency()) +
                            "💡 " + (nozzle.getNotes() != null ? nozzle.getNotes().split("\n")[0] : "Рекомендации в примечаниях"),
                    "Результаты расчёта",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка ввода чисел!\n\n" +
                            "Проверьте, что все поля заполнены правильно:\n" +
                            "• Используйте точку как разделитель (например: 10.5)\n" +
                            "• Не оставляйте поля пустыми\n" +
                            "• Используйте только цифры и точку",
                    "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "❌ Ошибка при расчёте:\n" + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private double parseDouble(String text, String fieldName) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException(fieldName + " не может быть пустым");
        }
        return Double.parseDouble(text.trim().replace(',', '.'));
    }

    private void animateFlow() {
        if (currentNozzle == null) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Сначала выполните расчёт профиля!\n\n" +
                            "1. Введите параметры в левой панели\n" +
                            "2. Нажмите 'Рассчитать профиль'\n" +
                            "3. Затем нажмите 'Анимировать поток'",
                    "Расчёт не выполнен", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Создаем диалоговое окно для анимации
        JDialog animationDialog = new JDialog(
                (Frame)SwingUtilities.getWindowAncestor(this),
                "Анимация потока в сопле Лаваля",
                false
        );
        animationDialog.setSize(700, 500);
        animationDialog.setLocationRelativeTo(this);

        AnimationPanel animationPanel = new AnimationPanel(currentNozzle);
        animationDialog.add(animationPanel);

        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть анимацию");
        closeButton.addActionListener(e -> animationDialog.dispose());
        animationDialog.add(closeButton, BorderLayout.SOUTH);

        animationDialog.setVisible(true);
    }

    private void exportData() {
        if (currentNozzle == null) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Нет данных для экспорта!\n\n" +
                            "Сначала выполните расчёт, затем экспортируйте результаты.",
                    "Нет данных", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт данных сопла Лаваля");
        fileChooser.setSelectedFile(new File(
                "сопло_лаваля_" +
                        new SimpleDateFormat("dd-MM-yyyy_HH-mm").format(new Date()) +
                        ".json"
        ));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                // Создаем ObjectMapper для форматированного JSON
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                // Преобразуем объект в красивый JSON
                String json = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(currentNozzle);

                // Записываем в файл
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
                }

                // Показываем успешное сообщение
                int choice = JOptionPane.showConfirmDialog(this,
                        "✅ Данные успешно экспортированы!\n\n" +
                                "Файл: " + file.getName() + "\n" +
                                "Путь: " + file.getParent() + "\n\n" +
                                "Открыть папку с файлом?",
                        "Экспорт завершён",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    // Пытаемся открыть папку в проводнике
                    Desktop.getDesktop().open(file.getParentFile());
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "❌ Ошибка при экспорте:\n" + e.getMessage(),
                        "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void saveToHistory() {
        if (currentNozzle == null) {
            JOptionPane.showMessageDialog(this,
                    "Сначала выполните расчёт",
                    "Нет данных", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = JOptionPane.showInputDialog(this,
                "Введите название для сохранения в историю:",
                "Сохранение расчёта",
                JOptionPane.QUESTION_MESSAGE);

        if (title != null && !title.trim().isEmpty()) {
            try {
                // Здесь будет код для сохранения в БД
                // Пока просто показываем сообщение
                JOptionPane.showMessageDialog(this,
                        "Функция сохранения в историю будет реализована\n" +
                                "после интеграции с модулем работы с БД.\n\n" +
                                "Расчёт готов к сохранению:\n" +
                                "• Название: " + title + "\n" +
                                "• Тип газа: " + currentNozzle.getGasType() + "\n" +
                                "• Скорость: " + String.format("%.1f", currentNozzle.getExitVelocity()) + " м/с",
                        "Готово к сохранению",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Внутренний класс для анимации
    class AnimationPanel extends JPanel {
        private LavalNozzle nozzle;
        private int animationFrame = 0;
        private Timer animationTimer;

        public AnimationPanel(LavalNozzle nozzle) {
            this.nozzle = nozzle;
            setPreferredSize(new Dimension(680, 400));
            setBackground(Color.WHITE);

            // Запускаем таймер анимации (обновление каждые 50мс)
            animationTimer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animationFrame++;
                    if (animationFrame > 200) animationFrame = 0;
                    repaint();
                }
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerY = height / 2;

            // Рисуем фон
            g2d.setColor(new Color(240, 248, 255));
            g2d.fillRect(0, 0, width, height);

            // Рисуем сопло
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));

            // Верхняя линия профиля
            int[] xPoints = new int[width];
            int[] yPoints = new int[width];
            for (int x = 0; x < width; x++) {
                xPoints[x] = x;
                double t = (double) x / width;
                double radius = 30 + 80 * Math.sin(t * Math.PI) *
                        Math.exp(-2 * (t - 0.5) * (t - 0.5));
                yPoints[x] = centerY - (int) radius;
            }
            g2d.drawPolyline(xPoints, yPoints, width);

            // Нижняя линия профиля
            for (int x = 0; x < width; x++) {
                yPoints[x] = centerY + (int) (centerY - yPoints[x]);
            }
            g2d.drawPolyline(xPoints, yPoints, width);

            // Анимация частиц газа
            g2d.setColor(Color.RED);
            for (int i = 0; i < 30; i++) {
                int particleX = (animationFrame * 3 + i * 15) % (width + 100) - 50;
                if (particleX >= 50 && particleX < width - 50) {
                    double t = (double) (particleX - 50) / (width - 100);
                    double radius = 30 + 80 * Math.sin(t * Math.PI) *
                            Math.exp(-2 * (t - 0.5) * (t - 0.5));

                    // Частицы движутся по центру канала
                    int particleY = centerY - (int) radius / 2 + i % 3 * 10;
                    g2d.fillOval(particleX - 3, particleY - 3, 6, 6);

                    // Хвостик частицы для эффекта движения
                    g2d.setColor(new Color(255, 100, 100, 150));
                    g2d.fillOval(particleX - 8, particleY - 2, 6, 4);
                    g2d.setColor(Color.RED);
                }
            }

            // Подписи
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Анимация потока в сопле Лаваля", 20, 30);

            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("Режим: " +
                            (nozzle.isSupersonic() ? "СВЕРХЗВУКОВОЙ" : "дозвуковой"),
                    20, height - 40);
            g2d.drawString("Красные точки - условные частицы газа", 20, height - 20);

            // Скорость потока в разных сечениях
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("Медленнее", 50, centerY - 50);
            g2d.drawString("Быстрее", width - 100, centerY - 50);

            // Стрелки направления
            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
            for (int i = 0; i < 5; i++) {
                int arrowX = 70 + i * 100;
                g2d.drawLine(arrowX, centerY, arrowX + 30, centerY);
                g2d.drawLine(arrowX + 30, centerY, arrowX + 20, centerY - 5);
                g2d.drawLine(arrowX + 30, centerY, arrowX + 20, centerY + 5);
            }
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (animationTimer != null && !animationTimer.isRunning()) {
                animationTimer.start();
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
    }

    // Обновленный DrawingPanel с поддержкой параметров
    class DrawingPanel extends JPanel {
        private double throatArea = 25.4;
        private double exitArea = 127.0;
        private double expansionRatio = 5.0;

        public void setNozzleParameters(double throatArea, double exitArea, double expansionRatio) {
            this.throatArea = throatArea;
            this.exitArea = exitArea;
            this.expansionRatio = expansionRatio;
        }

        public void setExpansionRatio(double expansionRatio) {
            this.expansionRatio = expansionRatio;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Фон
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Сетка
            g2d.setColor(new Color(240, 240, 240));
            for (int i = 0; i < width; i += 20) {
                g2d.drawLine(i, 0, i, height);
            }
            for (int i = 0; i < height; i += 20) {
                g2d.drawLine(0, i, width, i);
            }

            // Оси
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(50, height/2, width-50, height/2); // ось X
            g2d.drawLine(width/2, height-50, width/2, 50);  // ось Y

            // Подписи осей
            g2d.drawString("Длина сопла", width/2 - 30, height/2 + 20);
            g2d.drawString("Сечение", width/2 + 10, 40);

            // Профиль сопла
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));

            int centerY = height / 2;
            int[] xPoints = new int[width];
            int[] yPoints = new int[width];

            // Рассчитываем профиль на основе параметров
            double throatWidth = Math.sqrt(throatArea / Math.PI) * 2;
            double exitWidth = Math.sqrt(exitArea / Math.PI) * 2;
            double ratio = expansionRatio;

            for (int x = 0; x < width; x++) {
                xPoints[x] = x;
                double t = (double) x / width;

                // Более точная модель профиля сопла Лаваля
                double normalizedWidth = throatWidth +
                        (exitWidth - throatWidth) * Math.pow(t, ratio * 0.3);
                double radius = normalizedWidth * 5 +
                        20 * Math.sin(t * Math.PI) * Math.exp(-ratio * 0.2 * (t - 0.5) * (t - 0.5));

                yPoints[x] = centerY - (int) radius;
            }

            // Верхняя половина
            g2d.drawPolyline(xPoints, yPoints, width);

            // Нижняя половина (симметрично)
            for (int x = 0; x < width; x++) {
                yPoints[x] = centerY + (int) (centerY - yPoints[x]);
            }
            g2d.drawPolyline(xPoints, yPoints, width);

            // Критическое сечение (горловина)
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));

            int throatX = width / 3; // Горловина примерно на 1/3 длины
            g2d.drawLine(throatX, centerY - 40, throatX, centerY + 40);

            // Подписи
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("Горловина", throatX - 35, centerY - 50);

            g2d.setColor(Color.BLUE);
            g2d.drawString(String.format("Расширение: %.1f", expansionRatio),
                    width/2 - 40, centerY + 70);

            // Области сопла
            g2d.setColor(new Color(0, 100, 0, 100));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawString("Сходящаяся часть", width/4 - 40, centerY - 80);
            g2d.drawString("Расходящаяся часть", 3*width/4 - 50, centerY - 80);
        }
    }
}