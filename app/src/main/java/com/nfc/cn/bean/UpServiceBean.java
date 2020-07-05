package com.nfc.cn.bean;

public class UpServiceBean {
    /**
     * {"Longitude":"104.43243","Latitude":30.321432","StartTime":13:25,"SerialNumber":32435325,"Temperature":"30℃","ElectricQuantity":"3.4V","Frequency ":1323}
     */
    /**
     * Longitude : 104.43243
     * Latitude : 30.321432
     * StartTime : 1570711116000
     * SerialNumber : 32435325 // mac地址
     * Temperature : 30℃
     * ElectricQuantity : 3.4V
     * Frequency  : 1323
     */
    private String Longitude;
    private String Latitude;
    private long StartTime;
    private String SerialNumber;
    private String Temperature;
    private String ElectricQuantity;
    private String Frequency;

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String Longitude) {
        this.Longitude = Longitude;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String Latitude) {
        this.Latitude = Latitude;
    }

    public long getStartTime() {
        return StartTime;
    }

    public void setStartTime(long StartTime) {
        this.StartTime = StartTime;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String SerialNumber) {
        this.SerialNumber = SerialNumber;
    }

    public String getTemperature() {
        return Temperature;
    }

    public void setTemperature(String Temperature) {
        this.Temperature = Temperature;
    }

    public String getElectricQuantity() {
        return ElectricQuantity;
    }

    public void setElectricQuantity(String ElectricQuantity) {
        this.ElectricQuantity = ElectricQuantity;
    }

    public String getFrequency() {
        return Frequency;
    }

    public void setFrequency(String Frequency) {
        this.Frequency = Frequency;
    }
}
