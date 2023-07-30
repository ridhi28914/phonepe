package com.example.phonepe;

import com.example.phonepe.models.Event;
import com.example.phonepe.models.Slot;
import com.example.phonepe.models.User;
import com.example.phonepe.service.implementations.EventService;
import com.example.phonepe.service.implementations.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class PhonepeApplicationTests {

	EventService eventService = new EventService();

	UserService userService = new UserService(eventService);

	//	Test for creteEvent
	@Test
	void testCreateEvent() {

		User user1 = userService.createUser("ridhi", "ridhikumari@gmal.com");

		User user2 = userService.createUser("simran", "simranbindra@gmal.com");

		User user3 = userService.createUser("ravu", "ravu@gmal.com");

		List<User> userList = new ArrayList<>();
		userList.add(user2);
		userList.add(user3);

		userService.addUserShifts(user1.getUserId(), Arrays.asList(new Slot(
				LocalDateTime.of(2023, 7, 30, 0, 0,0),
				LocalDateTime.of(2023, 7, 30, 23, 59, 59))));

		userService.addUserShifts(user2.getUserId(), Arrays.asList(new Slot(
				LocalDateTime.of(2023, 7, 30, 0, 0,0),
				LocalDateTime.of(2023, 7, 30, 23, 59, 59))));

		userService.addUserShifts(user3.getUserId(), Arrays.asList(new Slot(
				LocalDateTime.of(2023, 7, 30, 0, 0,0),
				LocalDateTime.of(2023, 7, 30, 23, 59, 59))));

		Event event1 = userService.createEvent(user1, "event1", new Slot(
						LocalDateTime.of(2023, 7, 30, 9, 0, 0),
						LocalDateTime.of(2023, 7, 30, 10, 59, 59)),
				userList);


		userService.getEvents(user1.getUserId()).forEach(event -> {
			System.out.println(event.getEventName());
		});

		userService.deleteEvent(user1.getUserId(), event1.getEventId());

		userService.getEvents(user1.getUserId()).forEach(event -> {
			System.out.println(event.getEventName());
		});

//		create conflicting event

		List<User> userList2 = new ArrayList<>();
		userList2.add(user2);
		userList2.add(user3);

		Event event2 = userService.createEvent(user1, "event2", new Slot(
						LocalDateTime.of(2023, 7, 30, 9, 30, 0),
						LocalDateTime.of(2023, 7, 30, 10, 00, 59)),
				userList);

		List<Event> events = userService.fetchConflictingEvents(user1.getUserId());


	}

}
