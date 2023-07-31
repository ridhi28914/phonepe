package com.example.phonepe.service.interfaces;

import com.example.phonepe.models.Event;
import com.example.phonepe.models.Slot;
import com.example.phonepe.models.User;

import java.util.List;
import java.util.Set;

public interface UserInterface {

    Event createEvent(User organizer, String eventName, Slot slot, List<User> users);

    User createUser(String name, String email);

    void addUserShifts(String userId, List<Slot> slots);

    void deleteEvent(String userId, String eventId);

    List<Event> getEvents(String userId);

    Slot getCommonFreeSlot(List<User> users, Integer durationInMins);

    Set<Event> fetchConflictingEvents(String userId);

}
