package com.example.sartarosh.profil;

import com.google.firebase.Timestamp;

import java.util.List;

public class BarberProfile {
    private String userId;
    private String name;
    private String location;
    private String description;
    private String workHours;
    private List<String> services;
    private Timestamp createdAt;

    public BarberProfile() {}

    public BarberProfile(String userId, String name, String location, String description,
                         String workHours, List<String> services, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.location = location;
        this.description = description;
        this.workHours = workHours;
        this.services = services;
        this.createdAt = createdAt;
    }

    // Getters and setters (auto-generate in Android Studio)
}
