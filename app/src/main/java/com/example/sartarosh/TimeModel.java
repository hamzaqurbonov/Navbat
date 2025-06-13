package com.example.sartarosh;

public class TimeModel {
//    private int docid;
    String docid, first;

    public TimeModel() {

    }

    public TimeModel(String docid, String first) {
        this.docid = docid;
        this.first = first;
    }

    public String getDocid() {
        return docid;
    }

    public String getFirst() {
        return first;
    }
}
