package com.SE3;

import java.time.Duration;

public class TimeParser {

    public static Duration stringToDuration(String input){
        try {
            
            Duration d = Duration.ofHours(Long.parseLong(input.subSequence(0, 2).toString()));
            return d.plus(Duration.ofMinutes(Long.parseLong(input.subSequence(3, 5).toString())));
                
        } catch (Exception e) {
            return null;
        }
       
    }
    public static String durationToString(Duration input){
        try {
            long minutes = input.getSeconds()/60;
            long hours = minutes/60;
            minutes = minutes%60;
            String output = "";
            if (hours < 10)
                output += "0";
            output += hours +":";
            if (minutes < 10)  
                output += "0";
            output += minutes;
            return output;

        } catch (Exception e) {
            return null;
        }
       
    }
}

