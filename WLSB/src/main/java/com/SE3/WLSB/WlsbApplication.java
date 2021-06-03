package com.SE3.WLSB;


import java.time.Duration;
import com.SE3.TimeParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class WlsbApplication {

	public static void main(String[] args) {
		SpringApplication.run(WlsbApplication.class, args);
	}

	@GetMapping("/api")
	public String api() {
		return "{\"_links\": {\"self\": \"/api\",\"schedule\": \"/api/schedule\"}}";
	}
	@GetMapping("/api/schedule")
	public String schedule (@RequestParam(value = "nap") boolean nap, 
							@RequestParam(value = "age") int age,
							@RequestParam(value = "breakfast") boolean breakfast, 
							@RequestParam(value = "wakeUpTime") String wakeUpTime,
							@RequestParam(value = "getReadyDuration") String getReadyDuration,
							@RequestParam(value = "workingHours") String workingHours) {
		String schedule = "";
		String status = "1";
		
		Duration getReady = TimeParser.stringToDuration(getReadyDuration);
		Duration working = TimeParser.stringToDuration(workingHours);
		Duration sleep;
		Duration breakDuration = Duration.ofMinutes(45);
		Duration napTime = Duration.ofMinutes(20);

		if (age > 21) {
			sleep = Duration.ofHours(7);
		}
		else {
			sleep = Duration.ofHours(8);
		}
		Duration duration = getReady.plus(working).plus(sleep).plus(breakDuration.plus(breakDuration));

		if (nap){
			duration = duration.plus(napTime);
		}

		if (duration.compareTo(Duration.ofHours(24)) > 0 ){
			status = "3";			// geht nicht
		}
		else{
			Duration wakeUp = TimeParser.stringToDuration(wakeUpTime);
			schedule += String.format("{\"wakeup\": \"%s\"}​​​​​​​​​​​,", wakeUpTime); // aufstehen

			Duration ready = wakeUp.plus(getReady);	// zur Arbeit fahren
			String work = TimeParser.durationToString(ready);
			schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", work);

			Duration lunchBreak;	// Mittagspause
			if (breakfast){
				lunchBreak = ready.plus(Duration.ofHours(4));
			}
			else{
				lunchBreak = ready.plus(Duration.ofHours(3));
			}

			if (lunchBreak.compareTo(Duration.ofHours(11)) <= 0){	// essen zu früh, dann frühestens 11:30 Mittag
				lunchBreak = Duration.ofHours(11);
			}
			else if (lunchBreak.compareTo(Duration.ofHours(16)) >= 0){	// essen  nach 15:30  dann spätestens 15:30
				lunchBreak = Duration.ofHours(16);
			}

			if (lunchBreak.minus(ready).compareTo(working) > 0){
				schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(ready.plus(working)));
				working = Duration.ZERO;
			}
			else {
				Duration dg = lunchBreak.minus(ready);
				working = working.minus(dg);
			}
			schedule += String.format("{\"lunch\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(lunchBreak));

			Duration afternoon = lunchBreak.plus(breakDuration);
			Duration dinner = afternoon.plus(Duration.ofHours(5));

			if (nap){
				schedule += String.format("{\"nap\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afternoon));
				afternoon = afternoon.plus(napTime);
			}
			if (working.isZero()){
				schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afternoon));
			}
			else {
				schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afternoon));
				if (working.compareTo(Duration.ofHours(5)) < 0){
					schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afternoon.plus(working)));
					working = Duration.ZERO;
				}
			}
			schedule += String.format("{\"dinner\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(dinner));

			Duration sleeptime = wakeUp.minus(sleep).plus(Duration.ofHours(24));
			

			if (!working.isZero()){
				schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(dinner.plus(breakDuration)));

				Duration afterwork = dinner.plus(breakDuration).plus(working);
				
				if (afterwork.compareTo(sleeptime) > 0){
					schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(afterwork));
					status = "2";
				}
				else if (afterwork.compareTo(sleeptime) == 0){
					schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(afterwork));
				}
				else {
					if(sleeptime.compareTo(Duration.ofHours(24)) >= 0){
						sleeptime = sleeptime.minus(Duration.ofHours(24));
					}
					schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afterwork));
					schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(sleeptime));
				}
			}
			else if (sleeptime.compareTo(dinner.plus(breakDuration)) <= 0){
				schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(dinner.plus(breakDuration)));
			}
			else {
				schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(dinner.plus(breakDuration)));
				if(sleeptime.compareTo(Duration.ofHours(24)) >= 0){
					sleeptime = sleeptime.minus(Duration.ofHours(24));
				}
				schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(sleeptime));
			}
		}
		
		return String.format("{\"status\":\"%s\",\"schedule\":[%s],\"_links\":{\"self\":\"/api/schedule\"}}", status, schedule);
	}	
}
