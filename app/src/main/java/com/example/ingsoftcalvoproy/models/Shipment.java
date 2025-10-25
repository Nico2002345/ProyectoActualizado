package com.example.ingsoftcalvoproy.models;

/**
 * Modelo de Envío.
 */

public class Shipment {
    private int id;
    private String shipmentCode;
    private String objectDesc;
    private String receiverAddress;
    private double weightKg;
    private double distanceKm;
    private String status;
    private String createdAt;

    public Shipment() {}

    public Shipment(int id, String shipmentCode, String objectDesc, String receiverAddress,
                    double weightKg, double distanceKm, String status, String createdAt) {
        this.id = id;
        this.shipmentCode = shipmentCode;
        this.objectDesc = objectDesc;
        this.receiverAddress = receiverAddress;
        this.weightKg = weightKg;
        this.distanceKm = distanceKm;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getShipmentCode() { return shipmentCode; }
    public void setShipmentCode(String shipmentCode) { this.shipmentCode = shipmentCode; }

    public String getObjectDesc() { return objectDesc; }
    public void setObjectDesc(String objectDesc) { this.objectDesc = objectDesc; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return shipmentCode + " - " + status;
    }
}
