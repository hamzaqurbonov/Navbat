package com.bc.sartarosh.profil;

import java.util.List;

public class BarberProfile {
    private String name, phone1, phone2, province, region, address, userID, fcmToken, hairTime, beardTime, strictEndHour, strictStartHour, childrenTime;
    private List<BarberMapModel> key;


    public BarberProfile() {
    }

    public BarberProfile(String name, String phone1, String phone2, String province, String region, String address, String userID, String fcmToken, String hairTime, String beardTime, String strictEndHour, String strictStartHour,String childrenTime) {
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.province = province;
        this.region = region;
        this.address = address;
        this.userID = userID;
        this.fcmToken = fcmToken;
        this.hairTime = hairTime;
        this.beardTime = beardTime;
        this.strictStartHour = strictStartHour;
        this.strictEndHour = strictEndHour;
        this.childrenTime = childrenTime;
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

    public String getAddress() {
        return address;
    }

    public String getUserID() {
        return userID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getHairTime() {
        return hairTime;
    }

    public String getBeardTime() {
        return beardTime;
    }

    public String getStrictEndHour() {
        return strictEndHour;
    }

    public String getStrictStartHour() {
        return strictStartHour;
    }
    public String getChildrenTime() {
        return childrenTime;
    }
    public List<BarberMapModel> getKey() {
        return key;
    }

    public void setKey(List<BarberMapModel> key) {
        this.key = key;
    }
}
