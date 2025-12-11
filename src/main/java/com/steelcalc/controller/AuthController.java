package com.steelcalc.controller;

import com.steelcalc.dao.UserDao;
import com.steelcalc.view.LoginPanel;
import com.steelcalc.view.RegistrationDialog;
import com.steelcalc.view.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AuthController {
    private LoginPanel loginPanel;
    private RegistrationDialog registrationDialog;
    private UserDao userDao;
    private JFrame loginFrame;

    public AuthController(JFrame loginFrame, LoginPanel loginPanel) {
        this.loginFrame = loginFrame;
        this.loginPanel = loginPanel;
        this.userDao = new UserDao();

        setupListeners();
    }

    private void setupListeners() {
        // Обработчик кнопки "Вход"
        loginPanel.setLoginButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // Обработчик кнопки "Регистрация"
        loginPanel.setRegisterButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationDialog();
            }
        });
    }

    private void performLogin() {
        String username = loginPanel.getUsername();
        String password = loginPanel.getPassword();

        // Валидация ввода
        if (username.isEmpty() || password.isEmpty()) {
            loginPanel.setStatus("Заполните все поля", true);
            return;
        }

        // Аутентификация
        if (userDao.authenticate(username, password)) {
            loginPanel.setStatus("Успешный вход!", false);

            // Закрываем окно входа после небольшой задержки
            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginFrame.dispose();
                    openMainApplication();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            loginPanel.setStatus("Неверный логин или пароль", true);
        }
    }

    private void showRegistrationDialog() {
        // Создаём диалог регистрации
        registrationDialog = new RegistrationDialog(loginFrame);

        // Обработчик кнопки "Зарегистрировать"
        registrationDialog.setRegisterButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration();
            }
        });

        // Обработчик кнопки "Отмена"
        registrationDialog.setCancelButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrationDialog.dispose();
            }
        });

        registrationDialog.setVisible(true);

        // Если регистрация успешна, заполняем логин в основном окне
        if (registrationDialog.isSuccess()) {
            loginPanel.clearFields();
            loginPanel.setStatus("Регистрация успешна! Теперь вы можете войти.", false);
        }
    }

    private void performRegistration() {
        String username = registrationDialog.getUsername();
        String password = registrationDialog.getPassword();
        String confirmPassword = registrationDialog.getConfirmedPassword();

        // Валидация
        if (username.isEmpty() || password.isEmpty()) {
            registrationDialog.setStatus("Заполните все поля", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            registrationDialog.setStatus("Пароли не совпадают", true);
            return;
        }

        if (password.length() < 4) {
            registrationDialog.setStatus("Пароль должен содержать минимум 4 символа", true);
            return;
        }

        // Попытка создания пользователя
        if (userDao.createUser(username, password)) {
            registrationDialog.setStatus("Пользователь успешно создан!", false);
            registrationDialog.setSuccess(true);

            // Закрываем диалог после задержки
            Timer timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    registrationDialog.dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            registrationDialog.setStatus("Ошибка: пользователь с таким именем уже существует", true);
        }
    }

    private void openMainApplication() {
        // Создаём и показываем главное окно приложения
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Получаем ID пользователя (в реальном приложении нужно получить из БД)
                int userId = userDao.getUserId(loginPanel.getUsername());

                MainFrame mainFrame = new MainFrame(userId, loginPanel.getUsername());
                mainFrame.setVisible(true);
            }
        });
    }
}