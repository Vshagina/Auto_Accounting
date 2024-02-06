package com.example.auto_accounting;

import java.util.List;


public class RefillData {
    private long vehicleId;
    private long refillId;
    private String fuelType;
    private double quantity;
    private String date;
    private String updateTime;
    private long id;
    public RefillData(long refillId, long vehicleId, String fuelType, double quantity, String date) {
        this.refillId = refillId;
        this.vehicleId = vehicleId;
        this.fuelType = fuelType;
        this.quantity = quantity;
        this.date = date;
        this.updateTime = "";
    }
    public long getVehicleId() {
        return vehicleId;
    }

    public String getFuelType() {
        return fuelType;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getRefillId() {
        return refillId;
    }
}