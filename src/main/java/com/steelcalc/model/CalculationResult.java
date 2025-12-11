package com.steelcalc.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Универсальный класс для хранения результатов любых расчетов
 * Используется для сохранения в базу данных
 */
public class CalculationResult {
    private int id;
    private int userId;
    private String calculationType;   // "OXYGEN_LANCE" или "LAVAL_NOZZLE"
    private String title;             // Название расчета
    private Map<String, Double> inputParameters;  // Входные параметры
    private Map<String, Double> outputResults;    // Выходные результаты
    private LocalDateTime calculationDate;
    private String notes;

    // Конструкторы
    public CalculationResult() {
        this.calculationDate = LocalDateTime.now();
        this.inputParameters = new HashMap<>();
        this.outputResults = new HashMap<>();
    }

    public CalculationResult(String calculationType, String title) {
        this();
        this.calculationType = calculationType;
        this.title = title;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCalculationType() { return calculationType; }
    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Map<String, Double> getInputParameters() { return inputParameters; }
    public void setInputParameters(Map<String, Double> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public Map<String, Double> getOutputResults() { return outputResults; }
    public void setOutputResults(Map<String, Double> outputResults) {
        this.outputResults = outputResults;
    }

    // Методы для удобной работы с параметрами
    public void addInputParameter(String key, Double value) {
        this.inputParameters.put(key, value);
    }

    public void addOutputResult(String key, Double value) {
        this.outputResults.put(key, value);
    }

    public Double getInputParameter(String key) {
        return this.inputParameters.get(key);
    }

    public Double getOutputResult(String key) {
        return this.outputResults.get(key);
    }

    public LocalDateTime getCalculationDate() { return calculationDate; }
    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return String.format(
                "Расчет [%s] - %s%n" +
                        "Дата: %s%n" +
                        "Параметров ввода: %d | Параметров вывода: %d",
                calculationType, title,
                calculationDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                inputParameters.size(), outputResults.size()
        );
    }
}