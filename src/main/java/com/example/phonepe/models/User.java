package com.example.phonepe.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class User {

    String userId;

    String name;

    String email;

    List<Event> events;

    List<Slot> shifts;

    public User(String name, String email) {
        this.userId = UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.events = new ArrayList<>();
        this.shifts = new ArrayList<>();
    }
}
