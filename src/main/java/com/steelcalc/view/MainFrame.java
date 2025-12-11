package com.steelcalc.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private OxygenLancePanel oxygenLancePanel;
    private LavalNozzlePanelFixed lavalNozzlePanel;
    private HistoryPanel historyPanel;

    private int currentUserId;
    private String currentUsername;

    public MainFrame(int userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;

        setTitle("Расчёт параметров МСК: Кислородная фурма и сопло Лаваля - Пользователь: " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Создаём панель вкладок
        tabbedPane = new JTabbedPane();
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Создаём панели для вкладок
        oxygenLancePanel = new OxygenLancePanel();
        lavalNozzlePanel = new LavalNozzlePanelFixed();
        historyPanel = new HistoryPanel(currentUserId);

        // Добавляем вкладки
        tabbedPane.addTab("Кислородная фурма", oxygenLancePanel);
        tabbedPane.addTab("Сопло Лаваля", lavalNozzlePanel);
        tabbedPane.addTab("История расчётов", historyPanel);

        // Добавляем панель статуса внизу окна
        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());

        // Информация о пользователе
        JLabel userLabel = new JLabel(" Пользователь: " + currentUsername);
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(userLabel, BorderLayout.WEST);

        // Информация о времени
        JLabel timeLabel = new JLabel(new java.util.Date().toString() + " ");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusPanel.add(timeLabel, BorderLayout.EAST);

        return statusPanel;
    }
}