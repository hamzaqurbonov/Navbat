package com.bc.sartarosh.customer;

public class CustomerModel {

String name, phone1, phone2, fcmToken, barbesId, userID;
    public CustomerModel () {
    }

    public CustomerModel(String name, String phone1, String phone2, String fcmToken, String barbesId, String userID) {
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.fcmToken = fcmToken;
        this.barbesId = barbesId;
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

    public String getFcmToken() {
        return fcmToken;
    }

    public String getBarbesId() {
        return barbesId;
    }

    public String getUserID() {
        return userID;
    }
}
