package com.bc.sartarosh.customer;

public class CustomerBarbesModel {

    String name, phone, barbesId, userID;
    public CustomerBarbesModel () {
    }
    public CustomerBarbesModel(String name, String phone, String barbesId, String userID) {
        this.name = name;
        this.phone = phone;
        this.barbesId = barbesId;
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getBarbesId() {
        return barbesId;
    }

    public String getUserID() {
        return userID;
    }

}
