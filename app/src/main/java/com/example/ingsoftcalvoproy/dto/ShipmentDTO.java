package com.example.ingsoftcalvoproy.dto;

import com.google.gson.annotations.SerializedName;

public class ShipmentDTO {

    @SerializedName("id")
    private int id;

    @SerializedName("shipment_code")
    private String shipmentCode;

    @SerializedName("object_desc")
    private String objectDesc;

    @SerializedName("receiver_address")
    private String receiverAddress;

    @SerializedName("weight_kg")
    private double weightKg;

    @SerializedName("volume_m3")
    private double volumeM3;

    @SerializedName("distance_km")
    private double distanceKm;

    @SerializedName("status")
    private String status;

    public ShipmentDTO(int id, String shipmentCode, String objectDesc, String receiverAddress,
                       double weightKg, double volumeM3, double distanceKm, String status) {
        this.id = id;
        this.shipmentCode = shipmentCode;
        this.objectDesc = objectDesc;
        this.receiverAddress = receiverAddress;
        this.weightKg = weightKg;
        this.volumeM3 = volumeM3;
        this.distanceKm = distanceKm;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getShipmentCode() { return shipmentCode; }
    public String getObjectDesc() { return objectDesc; }
    public String getReceiverAddress() { return receiverAddress; }
    public double getWeightKg() { return weightKg; }
    public double getVolumeM3() { return volumeM3; }
    public double getDistanceKm() { return distanceKm; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setShipmentCode(String shipmentCode) { this.shipmentCode = shipmentCode; }
    public void setObjectDesc(String objectDesc) { this.objectDesc = objectDesc; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
    public void setVolumeM3(double volumeM3) { this.volumeM3 = volumeM3; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public void setStatus(String status) { this.status = status; }
}
