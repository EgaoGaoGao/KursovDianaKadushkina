package com.steelcalc.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Утилитарный класс для безопасного хеширования и проверки паролей.
 * Использует алгоритм BCrypt с автоматической генерацией "соли" (salt).
 *
 * <p>BCrypt - это адаптивная криптографическая хеш-функция, предназначенная
 * для хеширования паролей. Основные особенности:</p>
 * <ul>
 *   <li>Встроенная "соль" для защиты от rainbow-таблиц</li>
 *   <li>Адаптивная сложность вычислений (можно увеличивать со временем)</li>
 *   <li>Защита от timing - атак</li>
 * </ul>
 *
 * <p><b>Пример использования:</b></p>
 * <pre>{@code
 * // Хеширование пароля
 * String hash = PasswordHasher.hashPassword("myPassword123");
 *
 * // Проверка пароля
 * boolean isValid = PasswordHasher.checkPassword("myPassword123", hash);
 * }</pre>
 *
 * @author Разработчик курсовой работы
 * @version 1.0
 * @see BCrypt
 * @since 2025
 */
public class PasswordHasher {

    /**
     * Создает безопасный хеш пароля с использованием алгоритма BCrypt.
     * Автоматически генерирует случайную "соль" для каждого пароля.
     *
     * @param plainPassword исходный пароль в виде открытого текста
     * @return хеш пароля в формате BCrypt (включает соль и стоимость вычислений)
     * @throws IllegalArgumentException если пароль null, пустой или состоит только из пробелов
     *
     * @implNote Формат хеша BCrypt: {@code $2a$cost$salt_hash}
     *           где cost - фактор сложности (10-31), salt - 22 символа Base64
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Проверяет соответствие введенного пароля сохраненному хешу.
     *
     * @param plainPassword пароль для проверки (открытый текст)
     * @param hashedPassword хеш пароля, полученный методом {@link #hashPassword(String)}
     * @return {@code true} если пароль соответствует хешу, {@code false} в противном случае
     *
     * @implNote Метод безопасен к timing-атакам - время выполнения не зависит
     *           от того, насколько пароль похож на правильный.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Логируем ошибку, если хэш имеет неверный формат
            System.err.println("Ошибка при проверке пароля: " + e.getMessage());
            return false;
        }
    }
}