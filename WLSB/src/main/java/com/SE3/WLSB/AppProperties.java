package com.SE3.WLSB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppProperties {
    @Value("${schedule.input.breakDurationInMinutes}")
    public int breakDurationInMinutes;
    @Value("${schedule.input.napDurationInMinutes}")
    public int napDurationInMinutes;
    @Value("${schedule.input.hoursOfSleepOver21}")
    public int hoursOfSleepOver21;
    @Value("${schedule.input.hoursOfSleepUnder21}")
    public int hoursOfSleepUnder21;
    @Value("${schedule.input.morningWorkWithBreakfastInHours}")
    public int morningWorkWithBreakfastInHours;
    @Value("${schedule.input.morningWorkWithoutBreakfastInHours}")
    public int morningWorkWithoutBreakfastInHours;
    @Value("${schedule.input.durationBetweenLunchAndDinnerInHours}")
    public int durationBetweenLunchAndDinnerInHours;

    @Value("${schedule.output.wakeUp}")
    public String wakeUpString;
    @Value("${schedule.output.morningWork}")
    public String morningWorkString;
    @Value("${schedule.output.afternoonWork}")
    public String afternoonWorkString;
    @Value("${schedule.output.eveningWork}")
    public String eveningWorkString;
    @Value("${schedule.output.morningFreetime}")
    public String morningFreetimeString;
    @Value("${schedule.output.afternoonFreetime}")
    public String afternoonFreetimeString;
    @Value("${schedule.output.eveningFreetime}")
    public String eveningFreetimeString;
    @Value("${schedule.output.lunch}")
    public String lunchString;
    @Value("${schedule.output.dinner}")
    public String dinnerString;
    @Value("${schedule.output.nap}")
    public String napString;
    @Value("${schedule.output.sleep}")
    public String sleepString;
}
