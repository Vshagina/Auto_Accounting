package com.example.auto_accounting;

public class ServiceData {
    private long serviceId;
    private long vehicleId;
    private String serviceType;
    private double amount;
    private String date;
    private long id;
    public ServiceData(long serviceId, long vehicleId, String serviceType, double amount, String date) {
        this.serviceId = serviceId;
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.amount = amount;
        this.date = date;
    }
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getServiceId() {
        return serviceId;
    }
    public long getVehicleId() {
        return vehicleId;
    }
    public String getServiceType() {
        return serviceType;
    }
    public double getAmount() {
        return amount;
    }
    public String getDate() {
        return date;
    }
}
