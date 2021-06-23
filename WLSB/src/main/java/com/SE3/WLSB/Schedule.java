package com.SE3.WLSB;

import java.time.Duration;


/**
 * Class to model a schedule, objects cannot be changed
 * <br>
 * Build object through constructor {@link #Schedule(AppProperties properties, boolean nap, int age, boolean breakfast, String wakeUpTime, String morningRoutine, String workingHours)}
 * <br>
 * a validation and determination of the schedule can be triggered by {@link #determineSchedule()}
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

    /**
     * Adds a Duration with an identifier as key in JSON at the end of a given String 
     * @param schedule String to be extended
     * @param identifier Identifier for json key
     * @param duration Duration to be added
     * @return schedule-String extended with the identifier and Duration
     */
    private String addDurationToSchedule(String schedule, String identifier, Duration duration){
        if(duration != null){
            schedule += String.format("{\"%s\":\"%s\"},", identifier, TimeParser.durationToString(duration));
        }
        return schedule;
    }

    /**
    * determines a schedule, if never called before, and sets the attributes of the object to correct times and status
    */
    public int determineSchedule(){
        if(status != 0){
            return status;
        }
        status = 1;
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
     * determines if working in the morning is needed and sets {@link #morningWork} or {@link #morningFreetime}
     * @param remainingWorkingHours working hours that have not be scheduled yet
     */
    private void determineMorningWork(Duration remainingWorkingHours) {
        if(remainingWorkingHours.isZero()){
            morningFreetime = wakeUpTime.plus(morningRoutine);
        }
        else {
            morningWork = wakeUpTime.plus(morningRoutine);
        }
    }

    /**
     * Determines when lunch is scheduled, dependent on {@link #breakfast}, lunch is always between 11:00 and 16:00 
     * <br>
     * Normally lunch is scheduled a fix amount of time (as specified from properties{@link #properties}) after the morningRoutine 
     */
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

    /**
     * Determines if freetime before lunch is possible and if yes sets {@link #morningFreetime}
     * <br>
     * @param remainingWorkingHours working hours that have not be scheduled yet
     * @return new amount of working hours that have not be scheduled yet
     */
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

    /**
     * Determines if a nap hast to be scheduled and schedules it if necessary {@link #napTime}
     * @param afternoon marks the beginning of the afternoon, after lunchbreak
     * @return beginning of afternoon, if nap is scheduled, afternoon is later
     */
    private Duration determineNap(Duration afternoon) {
        if (nap){
            napTime = Duration.ofSeconds(afternoon.getSeconds());
	    	afternoon = afternoon.plus(napDuration);
		}
        return afternoon;
    }

    /**
     * Determines {@link #dinner} a fix amount of time (as specified by {@link #properties}) after lunckbreak
     * @param afternoon marks the beginning of the afternoon, after lunchbreak
     */
    private void determineDinner(Duration afternoon) {
        dinner = afternoon.plus(Duration.ofHours(properties.durationBetweenLunchAndDinnerInHours));
    }

    /**
     * Determines which activities during afternoon are scheduled ({@link #afternoonFreetime} and {@link #afternoonWork})
     * @param remainingWorkingHours working hours that have not be scheduled yet
     * @param afternoon marks the beginning of the afternoon, after lunchbreak
     * @return new amount of working hours that have not be scheduled yet
     */
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

    /**
     * Determines which activities during evening are scheduled and when to go to bed ({@link #eveningFreetime}, {@link #eveningWork} and {@link #goToBed})
     * @param remainingWorkingHours working hours that have not be scheduled yet
     * @param sleeptime Duration how long sleep has to be scheduled
     */
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

    /**
     * @return the status {@link #status} of the object (1: ok, 2: not enough sleep, 3: not possible to determine plan)
     */
    public int getStatus(){
        return this.status;
    }
}
