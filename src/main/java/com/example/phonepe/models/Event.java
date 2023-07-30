package com.example.phonepe.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Event {

    String eventId;

    String eventName;

    Slot slot;

    User organizer;

    List<User> users;
}
