package com.steelcalc.test;

import com.steelcalc.view.HistoryPanel;
import javax.swing.*;

public class TestHistoryPanel {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame testFrame = new JFrame("Тест HistoryPanel");
            testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            testFrame.setSize(1100, 700);

            // Создаем панель истории с тестовым user ID
            HistoryPanel historyPanel = new HistoryPanel(1);
            testFrame.add(historyPanel);

            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);

            System.out.println("Тестовая панель истории открыта");
        });
    }
}