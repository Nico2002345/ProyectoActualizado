package com.example.ingsoftcalvoproy.models;

/**
 * Modelo de Camión.
 */
public class Truck {
    private int id;
    private String plate;
    private double capacityKg;
    private boolean active;

    public Truck() {}

    public Truck(int id, String plate, double capacityKg, boolean active) {
        this.id = id;
        this.plate = plate;
        this.capacityKg = capacityKg;
        this.active = active;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public double getCapacityKg() { return capacityKg; }
    public void setCapacityKg(double capacityKg) { this.capacityKg = capacityKg; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return plate + " (cap: " + capacityKg + "kg)";
    }
}
