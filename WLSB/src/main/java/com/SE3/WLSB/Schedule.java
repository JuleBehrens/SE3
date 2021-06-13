package com.SE3.WLSB;

import java.time.Duration;

public class Schedule {
    private final Duration breaks = Duration.ofMinutes(45);
    private final Duration napDuration = Duration.ofMinutes(20);
    private final int hoursOfSleepOver21 = 7;
    private final int hoursOfSleepUnder21 = 8;
    
    private boolean nap;
    private int age;
    private boolean breakfast;

    private Duration wakeUpTime;
    private Duration workingHours;
    private Duration morningRoutine;
    private Duration sleep;
    private Duration lunchBreak;
    private Duration ready;
    private Duration napTime;
    private Duration dinner;
    private Duration afternoonWork;
    private Duration afternoonFreetime;
    private Duration morningFreeTime;
    private Duration eveningWork;
    private Duration goToBed;
    private Duration afterWorkFreetime;

    int status;

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

    private void checkIfActivitiesLongerThan24h() {
        Duration wholeDuration = morningRoutine.plus(workingHours).plus(sleep).plus(breaks).plus(breaks);
        if (nap){
			wholeDuration = wholeDuration.plus(napDuration);
		}
        if (wholeDuration.compareTo(Duration.ofHours(24)) > 0 ){
			status = 3;			// geht nicht
		}
    }

    private void determineSleepDuration() {
		if (age > 21) {
			sleep = Duration.ofHours(hoursOfSleepOver21);
		}
		else {
			sleep = Duration.ofHours(hoursOfSleepUnder21);
		}
	}	

    public String toString(){
        String schedule = "";
        if(status == 1){
            schedule += String.format("{\"wakeup\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(wakeUpTime)); 
            schedule += String.format("{\"work\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(ready));
            if (morningFreeTime != null){
                schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(morningFreeTime));
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
            if(afterWorkFreetime != null){
                schedule += String.format("{\"freetime\": \"%s\"}​​​​​​​​​​​,", TimeParser.durationToString(afterWorkFreetime));
            }
            schedule += String.format("{\"sleep\": \"%s\"}​​​​​​​​​​​", TimeParser.durationToString(goToBed));
            return schedule;
        }else {
            return "Incorrect Status";
        }
 
    }

    public void determineSchedule(){
            Duration remainingWorkingHours = workingHours;

            ready = wakeUpTime.plus(morningRoutine);

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

			if (lunchBreak.minus(ready).compareTo(remainingWorkingHours) > 0){
                morningFreeTime = ready.plus(remainingWorkingHours);
				remainingWorkingHours = Duration.ZERO;
			}
			else {
                morningFreeTime = null;
				remainingWorkingHours = remainingWorkingHours.minus(lunchBreak.minus(ready));
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
            afterWorkFreetime = null;
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
                afterWorkFreetime = dinner.plus(breaks);
				
				if(sleeptime.compareTo(Duration.ofHours(24)) >= 0){
					sleeptime = sleeptime.minus(Duration.ofHours(24));
				}

                goToBed = sleeptime;
			}
    }

    public int getStatus(){
        return this.status;
    }
}
