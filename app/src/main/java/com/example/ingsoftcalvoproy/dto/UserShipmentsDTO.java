package com.example.ingsoftcalvoproy.dto;

import java.util.List;

public class UserShipmentsDTO {

    private int id;
    private String name;
    private String email;
    private List<ShipmentDTO> shipments;

    // Constructor
    public UserShipmentsDTO(int id, String name, String email, List<ShipmentDTO> shipments) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.shipments = shipments;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<ShipmentDTO> getShipments() {
        return shipments;
    }

    // Setters (opcional, si quieres permitir modificar los valores)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setShipments(List<ShipmentDTO> shipments) {
        this.shipments = shipments;
    }
}
