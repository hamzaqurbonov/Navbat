package com.example.sartarosh.profil;

import com.google.firebase.Timestamp;

import java.util.List;

public class BarberProfile {
    private String name, phone1, phone2, province, region, аddress,  userID;

    public BarberProfile() {
        // Firestore учун бўш конструктор
    }

    public BarberProfile(String name, String phone1, String phone2, String province, String region, String аddress, String userID) {
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.province = province;
        this.region = region;
        this.аddress = аddress;
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public String getPhone1() {
        return phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public String getProvince() {
        return province;
    }

    public String getRegion() {
        return region;
    }

    public String getАddress() {
        return аddress;
    }

    public String getUserID() {
        return userID;
    }
}

