package com.steelcalc.service;

import com.steelcalc.model.OxygenLance;
import com.steelcalc.model.LavalNozzle;
import com.steelcalc.model.CalculationResult;

/**
 * Сервис для выполнения расчетов согласно учебному пособию
 * Токовой О.К. "Производство стали и сплавов"
 *
 * Внимание: Формулы упрощены для демонстрации.
 * В реальном приложении следует использовать точные формулы из пособия.
 */
public class CalculationService {

    // Константы
    private static final double GAS_CONSTANT = 8.314462618; // Универсальная газовая постоянная, Дж/(моль·K)
    private static final double MOLAR_MASS_O2 = 0.032;      // Молярная масса O2, кг/моль
    private static final double GAMMA_O2 = 1.4;             // Показатель адиабаты для O2
    private static final double STANDARD_PRESSURE = 101325; // Стандартное давление, Па
    private static final double PI = Math.PI;

    /**
     * Расчет параметров кислородной фурмы
     * Основные формулы согласно учебному пособию
     *
     * @param lance объект с входными параметрами
     * @return заполненный объект с результатами расчетов
     */
    public OxygenLance calculateOxygenLance(OxygenLance lance) {
        // Извлечение входных параметров
        double G = lance.getOxygenFlowRate();      // Расход кислорода, м³/ч
        double P = lance.getPressure() * 1e6;      // Давление, преобразуем МПа в Па
        double d = lance.getNozzleDiameter() / 1000; // Диаметр сопла, преобразуем мм в м
        double T = lance.getTemperature() + 273.15; // Температура, преобразуем °C в K
        double purity = lance.getOxygenPurity() / 100; // Чистота кислорода, доля

        // 1. Расчет скорости истечения (упрощенная формула)
        // V = √(2 * (γ/(γ-1)) * (R/M) * T * [1 - (Pe/P)^((γ-1)/γ)])
        // Для сверхзвукового истечения в вакуум Pe/P → 0
        double R_specific = GAS_CONSTANT / MOLAR_MASS_O2; // Удельная газовая постоянная
        double pressureRatio = 0.1; // Отношение давлений (Pe/P), упрощение

        double velocity = Math.sqrt(
                2 * (GAMMA_O2 / (GAMMA_O2 - 1)) *
                        R_specific * T *
                        (1 - Math.pow(pressureRatio, (GAMMA_O2 - 1) / GAMMA_O2))
        ) * purity; // Учет чистоты кислорода

        // 2. Расчет площади сечения сопла
        double area = PI * Math.pow(d / 2, 2); // A = π * (d/2)², м²

        // 3. Расчет силы удара струи
        // F = ρ * V² * A, где ρ - плотность кислорода при данных условиях
        double density = (P * MOLAR_MASS_O2) / (GAS_CONSTANT * T); // Уравнение состояния идеального газа
        double force = density * Math.pow(velocity, 2) * area;

        // 4. Расчет числа Маха
        // Скорость звука: a = √(γ * R * T)
        double speedOfSound = Math.sqrt(GAMMA_O2 * R_specific * T);
        double machNumber = velocity / speedOfSound;

        // 5. Расчет числа Рейнольдса (для оценки режима течения)
        // Re = (ρ * V * d) / μ, где μ - динамическая вязкость
        double viscosity = 2.0e-5; // Приблизительное значение для O2 при 300K, Па·с
        double reynoldsNumber = (density * velocity * d) / viscosity;

        // 6. Расчет эффективности (упрощенно)
        double efficiency = Math.min(95.0, 80.0 + (purity * 15) - (Math.abs(machNumber - 2) * 5));

        // Заполнение результатов
        lance.setExitVelocity(velocity);
        lance.setJetForce(force);
        lance.setMachNumber(machNumber);
        lance.setReynoldsNumber(reynoldsNumber);
        lance.setEfficiency(efficiency);

        // Формирование рекомендаций
        String notes = generateOxygenLanceNotes(lance);
        lance.setNotes(notes);

        return lance;
    }

    /**
     * Генерация рекомендаций для кислородной фурмы
     */
    private String generateOxygenLanceNotes(OxygenLance lance) {
        StringBuilder notes = new StringBuilder();

        if (lance.getMachNumber() < 1) {
            notes.append("Внимание: Режим дозвуковой. Рекомендуется увеличить давление.\n");
        } else if (lance.getMachNumber() > 3) {
            notes.append("Внимание: Слишком высокое число Маха. Возможны потери энергии.\n");
        } else {
            notes.append("Режим течения оптимальный.\n");
        }

        if (lance.getEfficiency() < 85) {
            notes.append("Эффективность ниже оптимальной. Проверьте чистоту кислорода.\n");
        }

        if (lance.getReynoldsNumber() > 4000) {
            notes.append("Турбулентный режим течения - хорошее перемешивание.\n");
        } else {
            notes.append("Ламинарный режим течения.\n");
        }

        // Расчет рекомендуемого давления
        double recommendedPressure = lance.getPressure() * 1.1;
        notes.append(String.format("Рекомендуемое давление: %.1f МПа\n", recommendedPressure));

        return notes.toString();
    }

    /**
     * Расчет параметров сопла Лаваля
     * Основные формулы для сопла Лаваля
     *
     * @param nozzle объект с входными параметрами
     * @return заполненный объект с результатами расчетов
     */
    public LavalNozzle calculateLavalNozzle(LavalNozzle nozzle) {
        // Извлечение входных параметров
        String gasType = nozzle.getGasType();
        double P0 = nozzle.getInletPressure() * 101325; // Преобразуем атм в Па
        double Pe = nozzle.getOutletPressure() * 101325;
        double T0 = nozzle.getTemperature(); // Уже в K
        double m_dot = nozzle.getMassFlowRate();
        double epsilon = nozzle.getExpansionRatio();
        boolean supersonic = nozzle.isSupersonic();

        // Определение свойств газа
        double gamma = getGammaForGas(gasType);
        double R = getGasConstant(gasType);
        double M = getMolarMass(gasType);
        double R_specific = R / M;

        // 1. Расчет критических параметров (параметры в горле сопла)
        // Критическое давление: P* = P0 * (2/(γ+1))^(γ/(γ-1))
        double criticalPressureRatio = Math.pow(2 / (gamma + 1), gamma / (gamma - 1));
        double P_star = P0 * criticalPressureRatio;

        // Критическая температура: T* = T0 * (2/(γ+1))
        double T_star = T0 * (2 / (gamma + 1));

        // Критическая скорость (скорость звука в горле): a* = √(γ * R_specific * T*)
        double a_star = Math.sqrt(gamma * R_specific * T_star);

        // 2. Расчет площади критического сечения из уравнения расхода
        // ṁ = (A* * P0) / √(T0) * √(γ/R) * (2/(γ+1))^((γ+1)/(2*(γ-1)))
        double massFlowFactor = Math.sqrt(gamma / R_specific) *
                Math.pow(2 / (gamma + 1), (gamma + 1) / (2 * (gamma - 1)));

        double A_star = (m_dot * Math.sqrt(T0)) / (P0 * massFlowFactor);
        double A_star_mm2 = A_star * 1e6; // Преобразуем м² в мм²

        // 3. Расчет площади выходного сечения
        double A_exit = A_star * epsilon;
        double A_exit_mm2 = A_exit * 1e6;

        // 4. Расчет скорости истечения
        // Для сверхзвукового сопла
        double exitVelocity;
        if (supersonic) {
            // Ve = √(2 * (γ/(γ-1)) * R_specific * T0 * [1 - (Pe/P0)^((γ-1)/γ)])
            exitVelocity = Math.sqrt(
                    2 * (gamma / (gamma - 1)) *
                            R_specific * T0 *
                            (1 - Math.pow(Pe / P0, (gamma - 1) / gamma))
            );
        } else {
            // Для дозвукового течения
            exitVelocity = a_star * 0.7; // Упрощение
        }

        // 5. Расчет числа Маха на выходе
        double speedOfSoundExit = Math.sqrt(gamma * R_specific * T0 *
                Math.pow(Pe / P0, (gamma - 1) / gamma));
        double machNumberExit = exitVelocity / speedOfSoundExit;

        // 6. Расчет тяги
        // F = ṁ * Ve + (Pe - P_amb) * Ae, где P_amb - атмосферное давление
        double P_amb = 101325; // Стандартное атмосферное давление
        double thrust = m_dot * exitVelocity + (Pe - P_amb) * A_exit;

        // 7. Расчет эффективности
        double idealThrust = m_dot * a_star * 2.0; // Упрощенный идеальный случай
        double efficiency = Math.min(98.0, (thrust / idealThrust) * 100);

        // Заполнение результатов
        nozzle.setThroatArea(A_star_mm2);
        nozzle.setExitArea(A_exit_mm2);
        nozzle.setExitVelocity(exitVelocity);
        nozzle.setMachNumber(machNumberExit);
        nozzle.setThrust(thrust);
        nozzle.setEfficiency(efficiency);

        // Формирование рекомендаций
        String notes = generateLavalNozzleNotes(nozzle);
        nozzle.setNotes(notes);

        return nozzle;
    }

    /**
     * Получение показателя адиабаты для различных газов
     */
    private double getGammaForGas(String gasType) {
        switch (gasType.toLowerCase()) {
            case "кислород": return 1.4;
            case "азот": return 1.4;
            case "воздух": return 1.4;
            case "водяной пар": return 1.33;
            case "гелий": return 1.66;
            default: return 1.4;
        }
    }

    /**
     * Получение газовой постоянной для различных газов
     */
    private double getGasConstant(String gasType) {
        return GAS_CONSTANT; // Универсальная газовая постоянная
    }

    /**
     * Получение молярной массы для различных газов
     */
    private double getMolarMass(String gasType) {
        switch (gasType.toLowerCase()) {
            case "кислород": return 0.032;
            case "азот": return 0.028;
            case "воздух": return 0.029;
            case "водяной пар": return 0.018;
            case "гелий": return 0.004;
            default: return 0.029;
        }
    }

    /**
     * Генерация рекомендаций для сопла Лаваля
     */
    private String generateLavalNozzleNotes(LavalNozzle nozzle) {
        StringBuilder notes = new StringBuilder();

        if (nozzle.isSupersonic() && nozzle.getMachNumber() < 1) {
            notes.append("Внимание: Режим дозвуковой при ожидаемом сверхзвуковом. ");
            notes.append("Проверьте степень расширения.\n");
        }

        if (!nozzle.isSupersonic() && nozzle.getMachNumber() > 0.8) {
            notes.append("Внимание: Приближение к скорости звука. ");
            notes.append("Рассмотрите переход на сверхзвуковое сопло.\n");
        }

        if (nozzle.getEfficiency() < 90) {
            notes.append("Эффективность ниже оптимальной. ");
            notes.append("Рекомендуется оптимизировать степень расширения.\n");
        }

        // Расчет оптимальной степени расширения
        double optimalEpsilon = Math.sqrt(nozzle.getInletPressure() / nozzle.getOutletPressure());
        notes.append(String.format("Оптимальная степень расширения: %.2f\n", optimalEpsilon));

        if (Math.abs(nozzle.getExpansionRatio() - optimalEpsilon) > 2) {
            notes.append("Текущая степень расширения далека от оптимальной.\n");
        }

        return notes.toString();
    }

    /**
     * Преобразование OxygenLance в CalculationResult для сохранения в БД
     */
    public CalculationResult convertToCalculationResult(OxygenLance lance, String title) {
        CalculationResult result = new CalculationResult("OXYGEN_LANCE", title);
        result.setUserId(lance.getUserId());

        // Добавление входных параметров
        result.addInputParameter("oxygenFlowRate", lance.getOxygenFlowRate());
        result.addInputParameter("pressure", lance.getPressure());
        result.addInputParameter("nozzleDiameter", lance.getNozzleDiameter());
        result.addInputParameter("temperature", lance.getTemperature());
        result.addInputParameter("oxygenPurity", lance.getOxygenPurity());

        // Добавление результатов
        result.addOutputResult("exitVelocity", lance.getExitVelocity());
        result.addOutputResult("jetForce", lance.getJetForce());
        result.addOutputResult("efficiency", lance.getEfficiency());
        result.addOutputResult("machNumber", lance.getMachNumber());
        result.addOutputResult("reynoldsNumber", lance.getReynoldsNumber());

        result.setNotes(lance.getNotes());
        result.setCalculationDate(lance.getCalculationDate());

        return result;
    }

    /**
     * Преобразование LavalNozzle в CalculationResult для сохранения в БД
     */
    public CalculationResult convertToCalculationResult(LavalNozzle nozzle, String title) {
        CalculationResult result = new CalculationResult("LAVAL_NOZZLE", title);
        result.setUserId(nozzle.getUserId());

        // Добавление входных параметров
        result.addInputParameter("inletPressure", nozzle.getInletPressure());
        result.addInputParameter("outletPressure", nozzle.getOutletPressure());
        result.addInputParameter("temperature", nozzle.getTemperature());
        result.addInputParameter("massFlowRate", nozzle.getMassFlowRate());
        result.addInputParameter("expansionRatio", nozzle.getExpansionRatio());
        result.addInputParameter("isSupersonic", nozzle.isSupersonic() ? 1.0 : 0.0);

        // Добавление результатов
        result.addOutputResult("throatArea", nozzle.getThroatArea());
        result.addOutputResult("exitArea", nozzle.getExitArea());
        result.addOutputResult("exitVelocity", nozzle.getExitVelocity());
        result.addOutputResult("machNumber", nozzle.getMachNumber());
        result.addOutputResult("thrust", nozzle.getThrust());
        result.addOutputResult("efficiency", nozzle.getEfficiency());

        result.setNotes(nozzle.getNotes());
        result.setCalculationDate(nozzle.getCalculationDate());

        return result;
    }

    /**
     * Валидация входных параметров для кислородной фурмы
     */
    public String validateOxygenLanceInput(OxygenLance lance) {
        if (lance.getOxygenFlowRate() <= 0) {
            return "Расход кислорода должен быть положительным";
        }
        if (lance.getPressure() <= 0) {
            return "Давление должно быть положительным";
        }
        if (lance.getNozzleDiameter() <= 0) {
            return "Диаметр сопла должен быть положительным";
        }
        if (lance.getTemperature() < -273) {
            return "Температура не может быть ниже абсолютного нуля";
        }
        if (lance.getOxygenPurity() < 0 || lance.getOxygenPurity() > 100) {
            return "Чистота кислорода должна быть в диапазоне 0-100%";
        }
        return null; // Все корректно
    }

    /**
     * Валидация входных параметров для сопла Лаваля
     */
    public String validateLavalNozzleInput(LavalNozzle nozzle) {
        if (nozzle.getInletPressure() <= 0) {
            return "Давление на входе должно быть положительным";
        }
        if (nozzle.getOutletPressure() <= 0) {
            return "Давление на выходе должно быть положительным";
        }
        if (nozzle.getInletPressure() <= nozzle.getOutletPressure()) {
            return "Давление на входе должно быть больше давления на выходе";
        }
        if (nozzle.getTemperature() <= 0) {
            return "Температура должна быть положительной";
        }
        if (nozzle.getMassFlowRate() <= 0) {
            return "Массовый расход должен быть положительным";
        }
        if (nozzle.getExpansionRatio() < 1) {
            return "Степень расширения должна быть не менее 1";
        }
        return null; // Все корректно
    }
}