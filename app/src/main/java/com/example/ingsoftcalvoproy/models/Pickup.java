package com.example.ingsoftcalvoproy.models;

public class Pickup {
    private int id;
    private String address;
    private double weight;
    private double volume;
    private String status;

    public Pickup(int id, String address, double weight, double volume, String status) {
        this.id = id;
        this.address = address;
        this.weight = weight;
        this.volume = volume;
        this.status = status;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getAddress() { return address; }
    public double getWeight() { return weight; }
    public double getVolume() { return volume; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
