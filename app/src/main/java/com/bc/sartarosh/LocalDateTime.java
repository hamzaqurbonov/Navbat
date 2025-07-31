package com.bc.sartarosh;

import java.time.format.DateTimeFormatter;

public class LocalDateTime {

    public static String dateDD () {
        java.time.LocalDateTime DateObj = java.time.LocalDateTime.now();
        DateTimeFormatter FormatObj = DateTimeFormatter.ofPattern("dd");
        String dd = DateObj.format(FormatObj);
        return dd;
    }

    public static String dateDDMMYY () {
        java.time.LocalDateTime DateObj = java.time.LocalDateTime.now();
        DateTimeFormatter Format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String ddmmyy = DateObj.format(Format);
        return ddmmyy;
    }

    public static String dateMM () {
        java.time.LocalDateTime DateObj = java.time.LocalDateTime.now();
        DateTimeFormatter FormatObj = DateTimeFormatter.ofPattern("MM");
        String mm = DateObj.format(FormatObj);
        return mm;
    }

    public static String dateYYYY () {
        java.time.LocalDateTime DateObj = java.time.LocalDateTime.now();
        DateTimeFormatter FormatObj = DateTimeFormatter.ofPattern("yy");
        String yy = DateObj.format(FormatObj);
        return yy;
    }
}
