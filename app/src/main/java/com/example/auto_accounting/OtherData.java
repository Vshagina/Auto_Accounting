package com.example.auto_accounting;

public class OtherData {

    private long vehicleId;
    private String otherType;
    private double amountOther;
    private String dateOther;
    public OtherData(long otherId, long vehicleId, String otherType, double amountOther, String dateOther) {
        this.otherId = otherId;
        this.vehicleId = vehicleId;
        this.otherType = otherType;
        this.amountOther = amountOther;
        this.dateOther = dateOther;
    }
    private long otherId;
    public long getOtherId() {
        return otherId;
    }
    public long getVehicleId() {
        return vehicleId;
    }
    public String getOtherType() {
        return otherType;
    }
    public double getAmountOther() {
        return amountOther;
    }
    public String getDateOther() {
        return dateOther;
    }
}

