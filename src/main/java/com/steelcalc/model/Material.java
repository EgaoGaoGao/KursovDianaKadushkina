package com.steelcalc.model;

/**
 * Класс для хранения свойств материалов (сталей, сплавов)
 * Используется как справочник в расчетах
 */
public class Material {
    private int id;
    private String name;              // Наименование материала
    private String category;          // Категория (сталь, чугун, цветной металл)
    private double density;           // ρ - Плотность, кг/м³
    private double meltingPoint;      // Температура плавления, °C
    private double thermalConductivity; // Теплопроводность, Вт/(м·K)
    private double specificHeat;      // Удельная теплоемкость, Дж/(кг·K)
    private String description;       // Описание/применение

    // Конструкторы
    public Material() {}

    public Material(String name, String category, double density,
                    double meltingPoint, double thermalConductivity,
                    double specificHeat, String description) {
        this.name = name;
        this.category = category;
        this.density = density;
        this.meltingPoint = meltingPoint;
        this.thermalConductivity = thermalConductivity;
        this.specificHeat = specificHeat;
        this.description = description;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getDensity() { return density; }
    public void setDensity(double density) { this.density = density; }

    public double getMeltingPoint() { return meltingPoint; }
    public void setMeltingPoint(double meltingPoint) { this.meltingPoint = meltingPoint; }

    public double getThermalConductivity() { return thermalConductivity; }
    public void setThermalConductivity(double thermalConductivity) {
        this.thermalConductivity = thermalConductivity;
    }

    public double getSpecificHeat() { return specificHeat; }
    public void setSpecificHeat(double specificHeat) { this.specificHeat = specificHeat; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format(
                "%s (%s) | ρ=%.0f кг/м³ | Tпл=%.0f°C",
                name, category, density, meltingPoint
        );
    }
}