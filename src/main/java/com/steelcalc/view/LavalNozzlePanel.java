package com.steelcalc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LavalNozzlePanel extends JPanel {
    // Компоненты ввода
    private JTextField inletPressureField;
    private JTextField outletPressureField;
    private JTextField temperatureField;
    private JTextField massFlowField;
    private JComboBox<String> gasTypeComboBox;

    // Графическая панель для визуализации
    private DrawingPanel drawingPanel;

    // Переключатели
    private JRadioButton subsonicRadio;
    private JRadioButton supersonicRadio;

    // Слайдер
    private JSlider expansionRatioSlider;

    public LavalNozzlePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Левая панель - параметры
        add(createParameterPanel(), BorderLayout.WEST);

        // Центральная панель - визуализация
        add(createVisualizationPanel(), BorderLayout.CENTER);

        // Нижняя панель - управление
        add(createControlPanel(), BorderLayout.SOUTH);
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

        sliderPanel.add(expansionRatioSlider, BorderLayout.CENTER);

        JLabel sliderValue = new JLabel("Текущее значение: 5");
        expansionRatioSlider.addChangeListener(e -> {
            sliderValue.setText("Текущее значение: " + expansionRatioSlider.getValue());
        });
        sliderPanel.add(sliderValue, BorderLayout.SOUTH);

        paramPanel.add(sliderPanel, gbc);

        return paramPanel;
    }

    private JPanel createVisualizationPanel() {
        JPanel visPanel = new JPanel(new BorderLayout());
        visPanel.setBorder(BorderFactory.createTitledBorder("Визуализация профиля сопла"));

        drawingPanel = new DrawingPanel();
        visPanel.add(drawingPanel, BorderLayout.CENTER);

        // Панель с информацией
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoPanel.add(createInfoLabel("Критическое сечение:", "25.4 мм²"));
        infoPanel.add(createInfoLabel("Число Маха:", "2.3"));
        infoPanel.add(createInfoLabel("Скорость истечения:", "680 м/с"));
        infoPanel.add(createInfoLabel("Коэфф. ускорения:", "4.8"));

        visPanel.add(infoPanel, BorderLayout.SOUTH);

        return visPanel;
    }

    private JPanel createInfoLabel(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        valueLabel.setForeground(Color.BLUE);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        return panel;
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
        JButton exportButton = new JButton("Экспорт данных");

        controlPanel.add(calculateButton);
        controlPanel.add(animateButton);
        controlPanel.add(exportButton);

        return controlPanel;
    }

    private void calculateProfile() {
        // Обновляем визуализацию
        drawingPanel.repaint();

        // Здесь будет реальный расчёт, пока заглушка
        JOptionPane.showMessageDialog(this,
                "Расчёт профиля сопла выполнен!\n" +
                        "Степень расширения: " + expansionRatioSlider.getValue() + "\n" +
                        "Режим: " + (subsonicRadio.isSelected() ? "Дозвуковой" : "Сверхзвуковой"),
                "Результаты расчёта",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Внутренний класс для рисования профиля сопла
    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Рисуем сетку
            g2d.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < width; i += 20) {
                g2d.drawLine(i, 0, i, height);
            }
            for (int i = 0; i < height; i += 20) {
                g2d.drawLine(0, i, width, i);
            }

            // Рисуем ось X
            g2d.setColor(Color.BLACK);
            g2d.drawLine(50, height/2, width-50, height/2);
            g2d.drawString("Длина сопла", width/2, height/2 + 20);

            // Рисуем ось Y
            g2d.drawLine(width/2, height-50, width/2, 50);
            g2d.drawString("Площадь сечения", width/2 - 40, 30);

            // Рисуем профиль сопла Лаваля
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));

            int centerY = height / 2;
            int[] xPoints = new int[width];
            int[] yPoints = new int[width];

            for (int x = 0; x < width; x++) {
                xPoints[x] = x;
                // Уравнение профиля сопла Лаваля (упрощённое)
                double t = (double) x / width;
                double radius = 30 + 100 * Math.sin(t * Math.PI) * Math.exp(-2 * (t - 0.5) * (t - 0.5));
                yPoints[x] = centerY - (int) radius;
            }

            // Рисуем верхнюю половину
            g2d.drawPolyline(xPoints, yPoints, width);

            // Рисуем нижнюю половину (симметрично)
            for (int x = 0; x < width; x++) {
                yPoints[x] = centerY + (int) (centerY - yPoints[x]);
            }
            g2d.drawPolyline(xPoints, yPoints, width);

            // Помечаем критическое сечение
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5}, 0)); // Пунктирная линия
            g2d.drawLine(width/2, centerY - 30, width/2, centerY + 30);
            g2d.drawString("Критическое сечение", width/2 - 60, centerY - 40);
        }
    }
}