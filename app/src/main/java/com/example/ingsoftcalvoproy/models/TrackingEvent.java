package com.example.ingsoftcalvoproy.models;

/**
 * Modelo de Evento de Seguimiento (Tracking).
 */
public class TrackingEvent {
    private int id;
    private int shipmentId;
    private String status;
    private String location;
    private String eventTime;

    public TrackingEvent() {}

    public TrackingEvent(int id, int shipmentId, String status, String location, String eventTime) {
        this.id = id;
        this.shipmentId = shipmentId;
        this.status = status;
        this.location = location;
        this.eventTime = eventTime;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getShipmentId() { return shipmentId; }
    public void setShipmentId(int shipmentId) { this.shipmentId = shipmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    @Override
    public String toString() {
        return status + " en " + location + " (" + eventTime + ")";
    }
}
