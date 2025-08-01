package com.bc.sartarosh;

public class TimeModel {
//    private int docid;
    String barbersId, customerId, docId, first, name, phone1, customerUserID;

    public TimeModel() {

    }

    public TimeModel(String barbersId, String customerId, String docId, String first, String name, String phone1, String customerUserID) {
        this.barbersId = barbersId;
        this.customerId = customerId;
        this.docId = docId;
        this.first = first;
        this.name = name;
        this.phone1 = phone1;
        this.customerUserID = customerUserID;
    }

    public String getBarbersId() {
        return barbersId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDocId() {
        return docId;
    }

    public String getFirst() {
        return first;
    }

    public String getName() {
        return name;
    }

    public String getPhone1() {
        return phone1;
    }

    public String getCustomerUserID() {
        return customerUserID;
    }
}
