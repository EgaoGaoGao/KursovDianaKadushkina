package com.steelcalc.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения параметров и результатов расчета кислородной фурмы
 * Согласно учебному пособию Токовой О.К. "Производство стали и сплавов"
 */
public class OxygenLance {
    private int id;
    private int userId;
    private String materialName;
    private double oxygenFlowRate;    // G - Расход кислорода, м³/ч
    private double pressure;          // P - Давление, МПа
    private double nozzleDiameter;    // d - Диаметр сопла, мм
    private double temperature;       // T - Температура, °C
    private double oxygenPurity;      // Чистота кислорода, %

    // Результаты расчета
    private double exitVelocity;      // V - Скорость истечения, м/с
    private double jetForce;          // F - Сила удара струи, Н
    private double efficiency;        // η - Эффективность, %
    private double machNumber;        // Число Маха
    private double reynoldsNumber;    // Число Рейнольдса

    private LocalDateTime calculationDate;
    private String notes;             // Примечания/рекомендации

    // Конструкторы
    public OxygenLance() {
        this.calculationDate = LocalDateTime.now();
    }

    public OxygenLance(double oxygenFlowRate, double pressure, double nozzleDiameter,
                       double temperature, String materialName) {
        this();
        this.oxygenFlowRate = oxygenFlowRate;
        this.pressure = pressure;
        this.nozzleDiameter = nozzleDiameter;
        this.temperature = temperature;
        this.materialName = materialName;
        this.oxygenPurity = 99.5; // Значение по умолчанию
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public double getOxygenFlowRate() { return oxygenFlowRate; }
    public void setOxygenFlowRate(double oxygenFlowRate) {
        this.oxygenFlowRate = oxygenFlowRate;
    }

    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }

    public double getNozzleDiameter() { return nozzleDiameter; }
    public void setNozzleDiameter(double nozzleDiameter) {
        this.nozzleDiameter = nozzleDiameter;
    }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getOxygenPurity() { return oxygenPurity; }
    public void setOxygenPurity(double oxygenPurity) {
        this.oxygenPurity = oxygenPurity;
    }

    public double getExitVelocity() { return exitVelocity; }
    public void setExitVelocity(double exitVelocity) {
        this.exitVelocity = exitVelocity;
    }

    public double getJetForce() { return jetForce; }
    public void setJetForce(double jetForce) { this.jetForce = jetForce; }

    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }

    public double getMachNumber() { return machNumber; }
    public void setMachNumber(double machNumber) { this.machNumber = machNumber; }

    public double getReynoldsNumber() { return reynoldsNumber; }
    public void setReynoldsNumber(double reynoldsNumber) {
        this.reynoldsNumber = reynoldsNumber;
    }

    public LocalDateTime getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Преобразует объект в Map для удобной сериализации в JSON
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("materialName", materialName);
        map.put("oxygenFlowRate", oxygenFlowRate);
        map.put("pressure", pressure);
        map.put("nozzleDiameter", nozzleDiameter);
        map.put("temperature", temperature);
        map.put("oxygenPurity", oxygenPurity);
        map.put("exitVelocity", exitVelocity);
        map.put("jetForce", jetForce);
        map.put("efficiency", efficiency);
        map.put("machNumber", machNumber);
        map.put("reynoldsNumber", reynoldsNumber);
        map.put("calculationDate", calculationDate.toString());
        map.put("notes", notes);
        return map;
    }

    /**
     * Создает объект из Map (десериализация из JSON)
     */
    public static OxygenLance fromMap(Map<String, Object> map) {
        OxygenLance lance = new OxygenLance();
        lance.setId((Integer) map.getOrDefault("id", 0));
        lance.setUserId((Integer) map.getOrDefault("userId", 0));
        lance.setMaterialName((String) map.getOrDefault("materialName", ""));
        lance.setOxygenFlowRate((Double) map.getOrDefault("oxygenFlowRate", 0.0));
        lance.setPressure((Double) map.getOrDefault("pressure", 0.0));
        lance.setNozzleDiameter((Double) map.getOrDefault("nozzleDiameter", 0.0));
        lance.setTemperature((Double) map.getOrDefault("temperature", 0.0));
        lance.setOxygenPurity((Double) map.getOrDefault("oxygenPurity", 99.5));
        lance.setExitVelocity((Double) map.getOrDefault("exitVelocity", 0.0));
        lance.setJetForce((Double) map.getOrDefault("jetForce", 0.0));
        lance.setEfficiency((Double) map.getOrDefault("efficiency", 0.0));
        lance.setMachNumber((Double) map.getOrDefault("machNumber", 0.0));
        lance.setReynoldsNumber((Double) map.getOrDefault("reynoldsNumber", 0.0));
        lance.setNotes((String) map.getOrDefault("notes", ""));
        return lance;
    }

    @Override
    public String toString() {
        return String.format(
                "Кислородная фурма [ID: %d]%n" +
                        "  Материал: %s%n" +
                        "  Расход кислорода: %.2f м³/ч%n" +
                        "  Давление: %.2f МПа%n" +
                        "  Диаметр сопла: %.2f мм%n" +
                        "  Скорость истечения: %.2f м/с%n" +
                        "  Сила удара: %.2f Н%n" +
                        "  Эффективность: %.1f%%",
                id, materialName, oxygenFlowRate, pressure,
                nozzleDiameter, exitVelocity, jetForce, efficiency
        );
    }
}