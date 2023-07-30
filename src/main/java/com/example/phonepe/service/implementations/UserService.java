package com.example.phonepe.service.implementations;

import com.example.phonepe.models.Event;
import com.example.phonepe.models.Slot;
import com.example.phonepe.models.User;
import com.example.phonepe.service.interfaces.UserInterface;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class UserService implements UserInterface {

    Map<String, List<Event>> organizerEvents = new HashMap<>();   // userOrganizer events
    Map<String, User> userIdToUserMap = new HashMap<>();

    private final EventService eventService;

    public UserService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public Event createEvent(User organizer, String eventName, Slot slot, List<User> users) {

//      check if this event is in user shift
        List<Slot> shifts = organizer.getShifts();
        boolean eventInShift = false;

        for (Slot shift : shifts) {
            if((shift.getStartTime().isBefore(slot.getStartTime()) && shift.getEndTime().isAfter(slot.getEndTime())))
                eventInShift = true;
        }

        if(eventInShift == false)
            throw new RuntimeException("Event not in shift");

        Event event = eventService.createEvent(organizer, eventName, slot, users);

        List<Event> organizerEventList = organizerEvents.getOrDefault(organizer.getUserId(), new ArrayList<>());
        organizerEventList.add(event);
        organizerEvents.put(organizer.getUserId(), organizerEventList);
        organizer.getEvents().add(event);
        users.forEach(user -> {
            List<Event> events = user.getEvents();
            if (events == null)
                events = new ArrayList<>();
            events.add(event);
        });

        return event;
    }

    @Override
    public User createUser(String name, String email) {
        User user = new User(name, email);
        userIdToUserMap.put(user.getUserId(), user);
        return user;
    }

    @Override
    public void addUserShifts(String userId, List<Slot> slots) {
        User user = userIdToUserMap.get(userId);
        List<Slot> userShifts = user.getShifts();
        if (userShifts == null)
            userShifts = new ArrayList<>();
        userShifts.addAll(slots);
    }

    @Override
    public void deleteEvent(String userId, String eventId) {
        User user = userIdToUserMap.get(userId);
        Event event = eventService.getEvent(eventId);
        organizerEvents.getOrDefault(userId, new ArrayList<>()).remove(event);
        user.getEvents().remove(event);
        eventService.deleteEvent(event);
    }


    @Override
    public List<Event> getEvents(String userId) {
        return userIdToUserMap.get(userId).getEvents();
    }

    @Override
    public Slot getUpcomingEmptySlot(List<User> users, Integer durationInMins) {

        for(User user: users) {
            List<Slot> shifts = user.getShifts();
            if (shifts == null)
                continue;
            for (Slot shift : shifts) {
                if (shift.getEndTime().isBefore(shift.getStartTime().plusMinutes(durationInMins)))
                    continue;
                AtomicBoolean isSlotFree = new AtomicBoolean(true);
                for (User user1 : users) {
                    if (user1.getEvents() == null)
                        continue;
                    List<Event> events =
                            user1.getEvents().stream()
                                    .filter(event -> event.getSlot().getStartTime().isBefore(shift.getEndTime()) && event.getSlot().getEndTime()
                                            .isAfter(shift.getStartTime())).collect(Collectors.toList());
                    
                    if (events.size() > 0) {
                        isSlotFree.set(false);
                        break;
                    }
                }
                if (isSlotFree.get())
                    return shift;
            }
        }

        throw new RuntimeException("No slot found");
    }

    @Override
    public List<Event> fetchConflictingEvents(String userId) {

        User user = userIdToUserMap.get(userId);
        List<Event> events = user.getEvents();

//       sort all events
        events.sort((event1, event2) -> event1.getSlot().getStartTime().compareTo(event2.getSlot().getStartTime()));

        List<Event> overlappingEvents = new ArrayList<>();

//        check if events are overlapping
        for (Event event : events) {
           for (Event event1 : events) {
               if (event != event1) {
                   if (event.getSlot().getStartTime().isBefore(event1.getSlot().getEndTime()) && event.getSlot().getEndTime().isAfter(event1.getSlot().getStartTime())) {
                       overlappingEvents.add(event);
                       break;
                   }
               }
           }
        }

        return overlappingEvents;
    }

}
