package com.example.phonepe.service.implementations;

import com.example.phonepe.models.Event;
import com.example.phonepe.models.Slot;
import com.example.phonepe.models.User;
import com.example.phonepe.service.interfaces.UserInterface;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
    public Set<Event> fetchConflictingEvents(String userId) {

        User user = userIdToUserMap.get(userId);
        List<Event> events = user.getEvents();

//       sort all events
        events.sort(Comparator.comparing(event -> event.getSlot().getStartTime()));

        Set<Event> overlappingEvents = new HashSet<>();

//        check if events are overlapping
        for (int i = 1; i < events.size(); i++) {
            if (events.get(i - 1).getSlot().getEndTime().isAfter(events.get(i).getSlot().getStartTime())) {
                overlappingEvents.add(events.get(i - 1));
                overlappingEvents.add(events.get(i));
            }
        }

        return overlappingEvents;
    }

    @Override
    public Slot getCommonFreeSlot(List<User> users, Integer durationInMins) {

        List<List<Slot>> userFreeSlot = new ArrayList<>();
        for (User user : users) {
            List<Slot> shifts = user.getShifts();
            List<Slot> occupiedSlots = user.getEvents().stream().map(event -> event.getSlot()).collect(Collectors.toList());
            shifts.sort(Comparator.comparing(Slot::getStartTime));
            occupiedSlots.sort(Comparator.comparing(Slot::getStartTime));

            List<Slot> freeSlots = new ArrayList<>();

            for (int i = 0; i < shifts.size(); i++) {
                Slot currentShift = shifts.get(i);
                LocalDateTime lastFreeTime = currentShift.getStartTime();
                for (int j = 0; j < occupiedSlots.size(); j++) {
                    if (occupiedSlots.get(j).getStartTime().isAfter(lastFreeTime)) {
                        Slot freeSlot = new Slot(lastFreeTime, occupiedSlots.get(j).getStartTime());
                        freeSlots.add(freeSlot);
                    }
                    lastFreeTime = occupiedSlots.get(j).getEndTime();
                }


                if (lastFreeTime.isBefore(currentShift.getEndTime())) {
                    Slot freeSlot = new Slot(lastFreeTime, currentShift.getEndTime());
                    freeSlots.add(freeSlot);
                }
            }

            userFreeSlot.add(freeSlots);
        }

        List<Slot> commonFreeSlots = userFreeSlot.get(0);

        for (int i = 1; i < userFreeSlot.size(); i++) {

            commonFreeSlots = findCommonSlotsBetweenTwoUsers(commonFreeSlots, userFreeSlot.get(i), durationInMins);
        }

        if (commonFreeSlots.isEmpty())
            throw new RuntimeException("Free slot not found");

        return commonFreeSlots.get(0);
    }



        private List<Slot> findCommonSlotsBetweenTwoUsers(List<Slot> slot1, List<Slot> slot2, int durationInMins) {

        int i=0;
        int j=0;
        List<Slot> commonSlots = new ArrayList<>();

        while(i< slot1.size() && j<slot2.size()) {

            Slot firstSlot= slot1.get(i);
            Slot secondSlot = slot2.get(j);

            LocalDateTime startTime = firstSlot.getStartTime().isAfter(secondSlot.getStartTime()) ? firstSlot.getStartTime() : secondSlot.getStartTime();
            LocalDateTime endTime = firstSlot.getEndTime().isBefore(secondSlot.getEndTime()) ? firstSlot.getEndTime() : secondSlot.getEndTime();



            if(startTime.isBefore(endTime) && startTime.plusMinutes(durationInMins).isBefore(endTime)){
                LocalDateTime slotStartTime = startTime;
                LocalDateTime slotEndTime = startTime.plusMinutes(durationInMins);
                while (slotEndTime.isBefore(endTime)) {

                    slotEndTime = slotEndTime.plusMinutes(durationInMins);
                }

                commonSlots.add(new Slot(slotStartTime, slotEndTime));
            }

            if (firstSlot.getEndTime().isBefore(secondSlot.getEndTime())) {
                i++;
            } else {
                j++;
            }

        }
        return commonSlots;
    }

}
