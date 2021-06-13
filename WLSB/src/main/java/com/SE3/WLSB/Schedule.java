package com.SE3.WLSB;

import java.time.Duration;
/**
 * Class to model a schedule, objects cannot be changed
 * <p>
 * Through constructor a validation and determination of the schedule is triggered
 * <p>
 * Schedule can be accessed via a method {@link #toString()} 
 * <p>
 * Status can be accessed via method {@link #getStatus()} (1: ok, 2: not enough sleep, 3: no determination possible)
 * 
 */

public class Schedule {
    /**Constants to determine schedule*/
    private final Duration breaks = Duration.ofMinutes(45);
    private final Duration napDuration = Duration.ofMinutes(20);
    private final int hoursOfSleepOver21 = 7;
    private final int hoursOfSleepUnder21 = 8;
    
    /**Status of schedule object*/
    int status;

    /**Input Params*/
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
     * Only constructor, triggers validation of input {@link #checkIfActivitiesLongerThan24h()} and determination {@link #determineSchedule()} of schedule and sleeptime {@link #determineSleepDuration()}
     * @param nap boolean, true if a nap should be scheduled
	 * @param age int, age of the person of the 
	 * @param breakfast boolean, true if the person eats breakfast
	 * @param wakeUpTime String (Format: "HH:mm"), Time when the person plans to wake up
	 * @param getReadyDuration String (Format: "HH:mm"), Duration how long the persons needs for morning routine (Breakfast, hygiene, clothing etc.)
	 * @param workingHours String (Format: "HH:mm"), Duration how long the person has to work on an average day
     */
    public Schedule(boolean nap, int age, boolean breakfast, String wakeUpTime, String morningRoutine, String workingHours){
		this.status = 1;
        this.nap = nap;
        this.age = age;
        this.breakfast = breakfast;
        this.wakeUpTime = TimeParser.stringToDuration(wakeUpTime);
		this.morningRoutine = TimeParser.stringToDuration(morningRoutine);
	    this.workingHours = TimeParser.stringToDuration(workingHours);
		determineSleepDuration();
        checkIfActivitiesLongerThan24h();
		determineSchedule();
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
			sleep = Duration.ofHours(hoursOfSleepOver21);
		}
		else {
			sleep = Duration.ofHours(hoursOfSleepUnder21);
		}
	}	

    /**
     * @return returns a String representation of the object in JSON-Format
     */
    public String toString(){
        String schedule = "";
        if(status < 3){
            schedule += String.format("{\"wakeup\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(wakeUpTime)); 
            schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(morningWork));
            if (morningFreetime != null){
                schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(morningFreetime));
            }
			schedule += String.format("{\"lunch\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(lunchBreak));
            if (nap){
				schedule += String.format("{\"nap\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(napTime));
			}
            if(afternoonWork != null){
                schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afternoonWork));
            }
            if(afternoonFreetime != null){
                schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afternoonFreetime));
            }
            schedule += String.format("{\"dinner\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(dinner));
            if(eveningWork != null){
                schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(eveningWork));
            }
            if(eveningFreetime != null){
                schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(eveningFreetime));
            }
            schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(goToBed));
            return schedule;
        }else {
            return  "Schedule not possible";
        }
 
    }

    /**
    * determines a schedule and sets the attributes of the object to correct times
    */
    private void determineSchedule(){
        
        Duration remainingWorkingHours = workingHours;

        morningWork = wakeUpTime.plus(morningRoutine);

		if (breakfast){
			lunchBreak = morningWork.plus(Duration.ofHours(4));
		}
		else{
			lunchBreak = morningWork.plus(Duration.ofHours(3));
		}

		if (lunchBreak.compareTo(Duration.ofHours(11)) <= 0){	// essen zu früh, dann frühestens 11:30 Mittag
			lunchBreak = Duration.ofHours(11);
		}
		else if (lunchBreak.compareTo(Duration.ofHours(16)) >= 0){	// essen  nach 15:30  dann spätestens 15:30
			lunchBreak = Duration.ofHours(16);
		}

		if (lunchBreak.minus(morningWork).compareTo(remainingWorkingHours) > 0){
            morningFreetime = morningWork.plus(remainingWorkingHours);
			remainingWorkingHours = Duration.ZERO;
		}
		else {
            morningFreetime = null;
			remainingWorkingHours = remainingWorkingHours.minus(lunchBreak.minus(morningWork));
		}

		Duration afternoon = lunchBreak.plus(breaks);
	    dinner = afternoon.plus(Duration.ofHours(5));

		if (nap){
            napTime = Duration.ofSeconds(afternoon.getSeconds());
	    	afternoon = afternoon.plus(napDuration);
		}

		if (remainingWorkingHours.isZero()){
            afternoonFreetime = Duration.ofSeconds(afternoon.getSeconds());
            afternoonWork = null;
		}
		else {
            afternoonWork = Duration.ofSeconds(afternoon.getSeconds());
            afternoonFreetime = null;
		    if (remainingWorkingHours.compareTo(Duration.ofHours(5)) < 0){
                afternoonFreetime = afternoon.plus(remainingWorkingHours);
			    remainingWorkingHours = Duration.ZERO;
			}
		}

		Duration sleeptime = wakeUpTime.minus(sleep).plus(Duration.ofHours(24));
        eveningWork = null;
        eveningFreetime = null;
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
