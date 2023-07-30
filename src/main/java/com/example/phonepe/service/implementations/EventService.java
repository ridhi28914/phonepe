package com.example.phonepe.service.implementations;

import com.example.phonepe.models.Event;
import com.example.phonepe.models.Slot;
import com.example.phonepe.models.User;
import com.example.phonepe.service.interfaces.EventInterface;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventService implements EventInterface {

    Map<String, Event> events = new HashMap<>();


    @Override
    public Event getEvent(String eventId) {
        Event event = events.get(eventId);
        if(event == null)
            throw new RuntimeException("Event not found");

        return event;
    }

    @Override
    public Event createEvent(User organizer, String eventName, Slot slot, List<User> users) {

        String id = UUID.randomUUID().toString();

        Event event = new Event(id, eventName, slot, organizer, users);
        events.put(event.getEventId(), event);
        return event;
    }

    @Override
    public void deleteEvent(Event event) {

        events.remove(event.getEventId());

        event.getUsers().forEach(user -> {
            List<Event> events = user.getEvents();
            if (events == null)
                events = new ArrayList<>();
            events.remove(event);
        });
    }

}
