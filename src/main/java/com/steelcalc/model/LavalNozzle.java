package com.steelcalc.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения параметров и результатов расчета сопла Лаваля
 * Согласно учебному пособию Токовой О.К. "Производство стали и сплавов"
 */
public class LavalNozzle {
    private int id;
    private int userId;
    private String gasType;           // Тип рабочего газа
    private double inletPressure;     // P0 - Давление на входе, атм
    private double outletPressure;    // Pe - Давление на выходе, атм
    private double temperature;       // T0 - Температура на входе, K
    private double massFlowRate;      // ṁ - Массовый расход, кг/с
    private double expansionRatio;    // ε - Степень расширения
    private boolean isSupersonic;     // Режим течения (true - сверхзвуковой)

    // Результаты расчета
    private double throatArea;        // A* - Площадь критического сечения, мм²
    private double exitArea;          // Ae - Площадь выходного сечения, мм²
    private double exitVelocity;      // Ve - Скорость истечения, м/с
    private double machNumber;        // M - Число Маха на выходе
    private double thrust;            // F - Тяга, Н
    private double efficiency;        // η - Эффективность, %

    private LocalDateTime calculationDate;
    private String notes;

    // Конструкторы
    public LavalNozzle() {
        this.calculationDate = LocalDateTime.now();
    }

    public LavalNozzle(String gasType, double inletPressure, double outletPressure,
                       double temperature, double massFlowRate, double expansionRatio,
                       boolean isSupersonic) {
        this();
        this.gasType = gasType;
        this.inletPressure = inletPressure;
        this.outletPressure = outletPressure;
        this.temperature = temperature;
        this.massFlowRate = massFlowRate;
        this.expansionRatio = expansionRatio;
        this.isSupersonic = isSupersonic;
    }

    // Геттеры и сеттеры (аналогично OxygenLance, но для своих полей)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getGasType() { return gasType; }
    public void setGasType(String gasType) { this.gasType = gasType; }

    public double getInletPressure() { return inletPressure; }
    public void setInletPressure(double inletPressure) { this.inletPressure = inletPressure; }

    public double getOutletPressure() { return outletPressure; }
    public void setOutletPressure(double outletPressure) { this.outletPressure = outletPressure; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getMassFlowRate() { return massFlowRate; }
    public void setMassFlowRate(double massFlowRate) { this.massFlowRate = massFlowRate; }

    public double getExpansionRatio() { return expansionRatio; }
    public void setExpansionRatio(double expansionRatio) { this.expansionRatio = expansionRatio; }

    public boolean isSupersonic() { return isSupersonic; }
    public void setSupersonic(boolean supersonic) { isSupersonic = supersonic; }

    public double getThroatArea() { return throatArea; }
    public void setThroatArea(double throatArea) { this.throatArea = throatArea; }

    public double getExitArea() { return exitArea; }
    public void setExitArea(double exitArea) { this.exitArea = exitArea; }

    public double getExitVelocity() { return exitVelocity; }
    public void setExitVelocity(double exitVelocity) { this.exitVelocity = exitVelocity; }

    public double getMachNumber() { return machNumber; }
    public void setMachNumber(double machNumber) { this.machNumber = machNumber; }

    public double getThrust() { return thrust; }
    public void setThrust(double thrust) { this.thrust = thrust; }

    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }

    public LocalDateTime getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDateTime calculationDate) { this.calculationDate = calculationDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Методы преобразования в Map и обратно (аналогично OxygenLance)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("gasType", gasType);
        map.put("inletPressure", inletPressure);
        map.put("outletPressure", outletPressure);
        map.put("temperature", temperature);
        map.put("massFlowRate", massFlowRate);
        map.put("expansionRatio", expansionRatio);
        map.put("isSupersonic", isSupersonic);
        map.put("throatArea", throatArea);
        map.put("exitArea", exitArea);
        map.put("exitVelocity", exitVelocity);
        map.put("machNumber", machNumber);
        map.put("thrust", thrust);
        map.put("efficiency", efficiency);
        map.put("calculationDate", calculationDate.toString());
        map.put("notes", notes);
        return map;
    }

    public static LavalNozzle fromMap(Map<String, Object> map) {
        LavalNozzle nozzle = new LavalNozzle();
        nozzle.setId((Integer) map.getOrDefault("id", 0));
        nozzle.setUserId((Integer) map.getOrDefault("userId", 0));
        nozzle.setGasType((String) map.getOrDefault("gasType", "Кислород"));
        nozzle.setInletPressure((Double) map.getOrDefault("inletPressure", 0.0));
        nozzle.setOutletPressure((Double) map.getOrDefault("outletPressure", 0.0));
        nozzle.setTemperature((Double) map.getOrDefault("temperature", 0.0));
        nozzle.setMassFlowRate((Double) map.getOrDefault("massFlowRate", 0.0));
        nozzle.setExpansionRatio((Double) map.getOrDefault("expansionRatio", 0.0));
        nozzle.setSupersonic((Boolean) map.getOrDefault("isSupersonic", false));
        nozzle.setThroatArea((Double) map.getOrDefault("throatArea", 0.0));
        nozzle.setExitArea((Double) map.getOrDefault("exitArea", 0.0));
        nozzle.setExitVelocity((Double) map.getOrDefault("exitVelocity", 0.0));
        nozzle.setMachNumber((Double) map.getOrDefault("machNumber", 0.0));
        nozzle.setThrust((Double) map.getOrDefault("thrust", 0.0));
        nozzle.setEfficiency((Double) map.getOrDefault("efficiency", 0.0));
        nozzle.setNotes((String) map.getOrDefault("notes", ""));
        return nozzle;
    }

    @Override
    public String toString() {
        return String.format(
                "Сопло Лаваля [ID: %d]%n" +
                        "  Газ: %s%n" +
                        "  Давление: %.2f → %.2f атм%n" +
                        "  Температура: %.1f K%n" +
                        "  Расход: %.3f кг/с%n" +
                        "  Крит. сечение: %.2f мм²%n" +
                        "  Скорость истечения: %.1f м/с%n" +
                        "  Число Маха: %.2f%n" +
                        "  Тяга: %.2f Н",
                id, gasType, inletPressure, outletPressure, temperature, massFlowRate,
                throatArea, exitVelocity, machNumber, thrust
        );
    }
}