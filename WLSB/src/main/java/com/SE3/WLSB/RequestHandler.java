package com.SE3.WLSB;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * class to handle requests to API
 */
@RestController
public class RequestHandler {
    /**
     * API entry point
     * @return links to navigate through api
     */
    @GetMapping("/api")
	public @ResponseBody String api() {
		return "{\"_links\": {\"self\": \"/api\",\"schedule\": \"/api/schedule\"}}";
	}
    /**
	 * Handles Get-Requests to /api/schedule
     * <br>
	 * Determines a schedule through a schedule {@Link #Schedule.Schedule(boolean, int, boolean, String, String, String)} object for a typical Homeoffice day with the given parameters
	 * 
	 * @param nap boolean, true if a nap should be scheduled
	 * @param age int, age of the person of the 
	 * @param breakfast boolean, true if the person eats breakfast
	 * @param wakeUpTime String (Format: "HH:mm"), Time when the person plans to wake up
	 * @param getReadyDuration String (Format: "HH:mm"), Duration how long the persons needs for morning routine (Breakfast, hygiene, clothing etc.)
	 * @param workingHours String (Format: "HH:mm"), Duration how long the person has to work on an average day
	 * @return the schedule in JSON-Format and a status code (1: ok, 2: not enough sleep, 3: not possible to determine plan)
	 */
	@GetMapping("/api/schedule")
	public @ResponseBody String schedule (@RequestParam(value = "nap") boolean nap, 
							@RequestParam(value = "age") int age,
							@RequestParam(value = "breakfast") boolean breakfast, 
							@RequestParam(value = "wakeUpTime") String wakeUpTime,
							@RequestParam(value = "getReadyDuration") String getReadyDuration,
							@RequestParam(value = "workingHours") String workingHours) {
		
		Schedule schedule = new Schedule(nap, age, breakfast, wakeUpTime, getReadyDuration, workingHours);
		
		return String.format("{\"status\":\"%s\",\"schedule\":[%s],\"_links\":{\"self\":\"/api/schedule\"}}", ""+schedule.getStatus(), schedule.toString());
	}
}
