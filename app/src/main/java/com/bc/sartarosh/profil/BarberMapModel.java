package com.bc.sartarosh.profil;



public class BarberMapModel {
    private String endHour, startHour, date;

    public BarberMapModel() {}

    public BarberMapModel(String endHour, String startHour, String date ) {
        this.endHour = endHour;
        this.startHour = startHour;
        this.date = date;
    }

    public String getEndHour() {
        return endHour;
    }

    public String getStartHour() {
        return startHour;
    }

    public String getDate() {
        return date;
    }
}