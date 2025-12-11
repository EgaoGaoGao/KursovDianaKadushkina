package com.steelcalc.view;

import com.steelcalc.controller.AuthController;

import javax.swing.*;

public class LoginFrame extends JFrame {
    private LoginPanel loginPanel;
    private AuthController authController;

    public LoginFrame() {
        setTitle("Аутентификация - Расчёт параметров МСК");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Центрирование окна

        // Создаём панель входа
        loginPanel = new LoginPanel();
        add(loginPanel);

        // Создаём контроллер
        authController = new AuthController(this, loginPanel);
    }
}