package com.example.ingsoftcalvoproy.models;

/**
 * Modelo de Guía asociada a un envío.
 */
public class Guide {
    private int id;
    private int shipmentId;
    private String guideNumber;
    private double distanceKm;
    private String createdAt;

    public Guide() {}

    public Guide(int id, int shipmentId, String guideNumber, double distanceKm, String createdAt) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.guideNumber = guideNumber;
        this.distanceKm = distanceKm;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShipmentId() { return shipmentId; }
    public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }

    public String getGuideNumber() { return guideNumber; }
    public void setGuideNumber(String guideNumber) { this.guideNumber = guideNumber; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return guideNumber + " (" + distanceKm + " km)";
    }
}
