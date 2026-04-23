package com.hotel.model;

import java.util.UUID;

public class Sensor {
    private String id;
    private String type;
    private String roomId;
    private String status;
    private double currentValue;

    public Sensor() {}

    public Sensor(String type, String roomId) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.roomId = roomId;
        this.status = "ACTIVE";
        this.currentValue = 0.0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
}
