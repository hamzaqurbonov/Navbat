package com.example.sartarosh.profil;
public class BarberProfile {
    private String name;
    private String phone1;
    private String phone2;
    private String province;
    private String region;
    private String address;
    private String userID;

    // Bo'sh konstruktor (Firestore uchun zarur)
    public BarberProfile() {}

    public BarberProfile(String name, String phone1, String phone2, String province, String region, String address, String userID) {
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.province = province;
        this.region = region;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public String getUserID() {
        return userID;
    }

    // ⚙️ Agar kerak bo‘lsa, setterlar ham qo‘shing:
    public void setName(String name) {
        this.name = name;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
