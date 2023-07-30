package com.example.phonepe.service.interfaces;

import com.example.phonepe.models.Event;
import com.example.phonepe.models.Slot;
import com.example.phonepe.models.User;

import java.util.List;

public interface EventInterface {

    Event getEvent(String eventId);

    Event createEvent(User organizer, String eventName, Slot slot, List<User> users);

    void deleteEvent(Event event);
}
