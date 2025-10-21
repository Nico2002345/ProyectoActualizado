package com.example.ingsoftcalvoproy.models;

/**
 * Modelo de Solicitud de Recogida.
 */
public class Pickup {
    private int id;
    private int userId;
    private int collectorId;
    private String address;
    private String scheduledAt;
    private String status;
    private String createdAt;

    public Pickup() {}

    public Pickup(int id, int userId, int collectorId, String address,
                  String scheduledAt, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.collectorId = collectorId;
        this.address = address;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCollectorId() { return collectorId; }
    public void setCollectorId(int collectorId) { this.collectorId = collectorId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Recogida en " + address + " (" + status + ")";
    }
}
