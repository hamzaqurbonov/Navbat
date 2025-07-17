package com.example.sartarosh;

public class TimeModel {
//    private int docid;
    String barbersId, customerId, docId, first;

    public TimeModel() {

    }

    public TimeModel(String barbersId, String customerId, String docId, String first) {
        this.barbersId = barbersId;
        this.customerId = customerId;
        this.docId = docId;
        this.first = first;
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
}
