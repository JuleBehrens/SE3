package com.SE3.WLSB;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Class to model a schedule, objects cannot be changed
 * <br>
 * Through constructor a validation and determination of the schedule is triggered
 * <br>
 * Schedule can be accessed via a method {@link #toString()} 
 * <br>
 * Status can be accessed via method {@link #getStatus()} (1: ok, 2: not enough sleep, 3: no determination possible)
 */

public class Schedule {
    /**Constants to determine schedule*/
    AppProperties properties;

    private Duration breaks;
    private Duration napDuration;
    
    /**Status of schedule object*/
    int status;

    /**Input Params from user*/
    private boolean nap;
    private int age;
    private boolean breakfast;
    private Duration workingHours;
    private Duration morningRoutine;
    private Duration sleep;

    /**Timestamps of possible events through the day*/
    private Duration wakeUpTime;
    private Duration morningWork;
    private Duration morningFreetime;
    private Duration lunchBreak;
    private Duration napTime;
    private Duration afternoonWork;
    private Duration afternoonFreetime;
    private Duration dinner;
    private Duration eveningWork;
    private Duration eveningFreetime;
    private Duration goToBed;


    /**
     * Only constructor
     * @param nap boolean, true if a nap should be scheduled
	 * @param age int, age of the person of the 
	 * @param breakfast boolean, true if the person eats breakfast
	 * @param wakeUpTime String (Format: "HH:mm"), Time when the person plans to wake up
	 * @param getReadyDuration String (Format: "HH:mm"), Duration how long the persons needs for morning routine (Breakfast, hygiene, clothing etc.)
	 * @param workingHours String (Format: "HH:mm"), Duration how long the person has to work on an average day
     */
    public Schedule(AppProperties properties, boolean nap, int age, boolean breakfast, String wakeUpTime, String morningRoutine, String workingHours){
		this.status = 1;
        this.properties = properties;
        this.nap = nap;
        this.age = age;
        this.breakfast = breakfast;
        this.wakeUpTime = TimeParser.stringToDuration(wakeUpTime);
		this.morningRoutine = TimeParser.stringToDuration(morningRoutine);
	    this.workingHours = TimeParser.stringToDuration(workingHours);
        this.breaks = Duration.ofMinutes(properties.breakDurationInMinutes);
        this.napDuration = Duration.ofMinutes(properties.napDurationInMinutes); 
    }

    /**
     * sets status {@link #status} to 3: Determination not possible, if the sum of Durations of all events is longer than a day
     */
    private void checkIfActivitiesLongerThan24h() {
        Duration wholeDuration = morningRoutine.plus(workingHours).plus(sleep).plus(breaks).plus(breaks);
        if (nap){
			wholeDuration = wholeDuration.plus(napDuration);
		}
        if (wholeDuration.compareTo(Duration.ofHours(24)) > 0 ){
			status = 3;			// geht nicht
		}
    }

    /**
     * sets sleep {@link #sleep} dependent on age
     */
    private void determineSleepDuration() {
		if (age > 21) {
			sleep = Duration.ofHours(properties.hoursOfSleepOver21);
		}
		else {
			sleep = Duration.ofHours(properties.hoursOfSleepUnder21);
		}
	}	

    /**
     * @return returns a String representation of the object in JSON-Format
     */
    public String toString(){
        String schedule = "";
        if(status < 3){
            schedule = addDurationToSchedule(schedule, properties.wakeUpString, wakeUpTime);
            schedule = addDurationToSchedule(schedule, properties.morningWorkString, morningWork);
            schedule = addDurationToSchedule(schedule, properties.morningFreetimeString, morningFreetime);
            schedule = addDurationToSchedule(schedule, properties.lunchString, lunchBreak);
			schedule = addDurationToSchedule(schedule, properties.napString, napTime);
            schedule = addDurationToSchedule(schedule, properties.afternoonWorkString, afternoonWork);
            schedule = addDurationToSchedule(schedule, properties.afternoonFreetimeString, afternoonFreetime);
            schedule = addDurationToSchedule(schedule, properties.dinnerString, dinner);
            schedule = addDurationToSchedule(schedule, properties.eveningWorkString, eveningWork);
            schedule = addDurationToSchedule(schedule, properties.eveningFreetimeString, eveningFreetime);
            schedule = addDurationToSchedule(schedule, properties.sleepString, goToBed);
            return schedule.substring(0, schedule.length()-1);
        }else {
            return "Schedule not possible";
        }
 
    }

    private String addDurationToSchedule(String schedule, String identifier, Duration duration){
        if(duration != null){
            schedule += String.format("{\"%s\":\"%s\"},", identifier, TimeParser.durationToString(duration));
        }
        return schedule;
    }

    /**
    * determines a schedule and sets the attributes of the object to correct times
    */
    public int determineSchedule(){
        determineSleepDuration();
        checkIfActivitiesLongerThan24h();
        if(status == 3){
            return status;
        }
        Duration remainingWorkingHours = workingHours;
        determineMorningWork(remainingWorkingHours);
		determineLunchBreak();
		remainingWorkingHours = determineMorningFreetime(remainingWorkingHours);
		Duration afternoon = lunchBreak.plus(breaks);  
		afternoon = determineNap(afternoon);
        determineDinner(afternoon);
		remainingWorkingHours = determineAfternoonActivities(remainingWorkingHours, afternoon);
		Duration sleeptime = wakeUpTime.minus(sleep).plus(Duration.ofHours(24));
        determineEveningActivities(remainingWorkingHours, sleeptime);
        return status;
    }

    private Duration determineMorningFreetime(Duration remainingWorkingHours) {
        if (lunchBreak.minus(wakeUpTime.plus(morningRoutine)).compareTo(remainingWorkingHours) > 0){
            if(morningFreetime == null){
                morningFreetime = morningWork.plus(remainingWorkingHours);
            }
            remainingWorkingHours = Duration.ZERO;
		}
		else {
            morningFreetime = null;
			remainingWorkingHours = remainingWorkingHours.minus(lunchBreak.minus(wakeUpTime.plus(morningRoutine)));
		}
        return remainingWorkingHours;
    }

    private void determineEveningActivities(Duration remainingWorkingHours, Duration sleeptime) {
		if (!remainingWorkingHours.isZero()){
            eveningWork = dinner.plus(breaks);
			Duration afterwork = dinner.plus(breaks).plus(remainingWorkingHours);

            if (afterwork.compareTo(sleeptime) >= 0){
                goToBed = Duration.ofSeconds(afterwork.getSeconds());
				status = 2;
			}
			else {
				if(sleeptime.compareTo(Duration.ofHours(24)) >= 0){
					sleeptime = sleeptime.minus(Duration.ofHours(24));
				}

                eveningWork = Duration.ofSeconds(afterwork.getSeconds());
                goToBed = Duration.ofSeconds(sleeptime.getSeconds());
			}
		}
		else if (sleeptime.compareTo(dinner.plus(breaks)) <= 0){
            goToBed = dinner.plus(breaks);
		}
		else {
            eveningFreetime = dinner.plus(breaks);
				
			if(sleeptime.compareTo(Duration.ofHours(24)) >= 0){
				sleeptime = sleeptime.minus(Duration.ofHours(24));
			}

            goToBed = sleeptime;
		}
    }

    private Duration determineAfternoonActivities(Duration remainingWorkingHours, Duration afternoon) {
        if (remainingWorkingHours.isZero()){
            afternoonFreetime = Duration.ofSeconds(afternoon.getSeconds());
		}
		else {
            afternoonWork = Duration.ofSeconds(afternoon.getSeconds());
		    if (remainingWorkingHours.compareTo(Duration.ofHours(properties.durationBetweenLunchAndDinnerInHours)) <= 0){
                if (remainingWorkingHours.compareTo(Duration.ofHours(properties.durationBetweenLunchAndDinnerInHours)) < 0){
                    afternoonFreetime = afternoon.plus(remainingWorkingHours);
                }
			    remainingWorkingHours = Duration.ZERO;
			}else {
                remainingWorkingHours = remainingWorkingHours.minus(Duration.ofHours(properties.durationBetweenLunchAndDinnerInHours));
            }
		}
        return remainingWorkingHours;
    }

    private Duration determineNap(Duration afternoon) {
        if (nap){
            napTime = Duration.ofSeconds(afternoon.getSeconds());
	    	afternoon = afternoon.plus(napDuration);
		}
        return afternoon;
    }

    private void determineDinner(Duration afternoon) {
        dinner = afternoon.plus(Duration.ofHours(properties.durationBetweenLunchAndDinnerInHours));
    }

    private void determineLunchBreak() {
        Duration morning = wakeUpTime.plus(morningRoutine);
        if (breakfast){
			lunchBreak = morning.plus(Duration.ofHours(properties.morningWorkWithBreakfastInHours));
		}
		else{
			lunchBreak = morning.plus(Duration.ofHours(properties.morningWorkWithoutBreakfastInHours));
		}

		if (lunchBreak.compareTo(Duration.ofHours(11)) <= 0){	// essen zu früh, dann frühestens 11:30 Mittag
			lunchBreak = Duration.ofHours(11);
		}
		else if (lunchBreak.compareTo(Duration.ofHours(16)) >= 0){	// essen  nach 15:30  dann spätestens 15:30
			lunchBreak = Duration.ofHours(16);
		}
    }

    private void determineMorningWork(Duration remainingWorkingHours) {
        if(remainingWorkingHours.isZero()){
            morningFreetime = wakeUpTime.plus(morningRoutine);
        }
        else {
            morningWork = wakeUpTime.plus(morningRoutine);
        }
    }

    /**
     * @return the status {@link #status} of the object (1: ok, 2: not enough sleep, 3: not possible to determine plan)
     */
    public int getStatus(){
        return this.status;
    }
}
