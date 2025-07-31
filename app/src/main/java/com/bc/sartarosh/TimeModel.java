package com.bc.sartarosh;

public class TimeModel {
//    private int docid;
    String barbersId, customerId, docId, first, name, phone1;

    public TimeModel() {

    }

    public TimeModel(String barbersId, String customerId, String docId, String first, String name, String phone1) {
        this.barbersId = barbersId;
        this.customerId = customerId;
        this.docId = docId;
        this.first = first;
        this.name = name;
        this.phone1 = phone1;
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
}
