package com.steelcalc;

import com.steelcalc.dao.DatabaseInitializer;
import com.steelcalc.view.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. Инициализируем базу данных (создаём таблицы)
        DatabaseInitializer.initializeDatabase();

        // 2. Запускаем окно ВХОДА, а не главное окно напрямую
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}