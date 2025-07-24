package com.example.sartarosh.profil;

import com.google.firebase.Timestamp;

import java.util.List;

public class BarberProfile {
    private String name, phone, userID;
    private String province;
    private String region;
    private String destination;

    private String backupPhone ;

    public BarberProfile() {
        // Firestore учун бўш конструктор
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getUserID() { return userID; }
    public String getProvince() { return province; }
    public String getRegion() { return region; }
    public String getDestination() { return destination; }

    public String getBackupPhone() { return backupPhone; }


}

