package com.example.sartarosh.customer;

public class CustomerModel {

String name, phone, barbesId;
    public CustomerModel () {
    }

    public CustomerModel(String name, String phone, String barbesId) {
        this.name = name;
        this.phone = phone;
        this.barbesId = barbesId;
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
}
