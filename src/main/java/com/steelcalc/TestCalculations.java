package com.steelcalc;

import com.steelcalc.model.OxygenLance;
import com.steelcalc.model.LavalNozzle;
import com.steelcalc.service.CalculationService;

public class TestCalculations {
    public static void main(String[] args) {
        CalculationService service = new CalculationService();

        System.out.println("=== ТЕСТ РАСЧЕТА КИСЛОРОДНОЙ ФУРМЫ ===\n");

        // Тест 1: Кислородная фурма
        OxygenLance lance = new OxygenLance(
                1500.0,  // расход кислорода, м³/ч
                2.5,     // давление, МПа
                15.0,    // диаметр сопла, мм
                25.0,    // температура, °C
                "Сталь 45"
        );

        lance = service.calculateOxygenLance(lance);
        System.out.println(lance);
        System.out.println("\nРекомендации:\n" + lance.getNotes());

        System.out.println("\n=== ТЕСТ РАСЧЕТА СОПЛА ЛАВАЛЯ ===\n");

        // Тест 2: Сопло Лаваля
        LavalNozzle nozzle = new LavalNozzle(
                "Кислород",  // тип газа
                10.0,        // давление на входе, атм
                1.0,         // давление на выходе, атм
                300.0,       // температура, K
                1.0,         // массовый расход, кг/с
                5.0,         // степень расширения
                true         // сверхзвуковой режим
        );

        nozzle = service.calculateLavalNozzle(nozzle);
        System.out.println(nozzle);
        System.out.println("\nРекомендации:\n" + nozzle.getNotes());

        System.out.println("\n=== ТЕСТ ВАЛИДАЦИИ ===\n");

        // Тест 3: Валидация
        OxygenLance invalidLance = new OxygenLance(-100, 2.5, 15, 25, "Сталь");
        String error = service.validateOxygenLanceInput(invalidLance);
        System.out.println("Ошибка валидации: " + error);
    }
}