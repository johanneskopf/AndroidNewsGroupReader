package com.freeteam01.androidnewsgroupreader.Models;

import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostDate {
    private String date_input_string;
    private String timezone;
    private String date_string;
    private String weekday;
    private int day;
    private String month_string;
    private int month;
    private int year;
    private int hours;
    private int minutes;
    private int seconds;
    private GregorianCalendar date;

    public PostDate(String date_input){
        date_input_string = date_input;
        splitAndSetDateAndTimezone();
        splitAndSetDateAttributes();
        this.date = new GregorianCalendar(year, month, day, hours, minutes, seconds);
    }

    public String getDateString(){
        return date_string;
    }

    public GregorianCalendar getDate(){
        return date;
    }

    private void splitAndSetDateAndTimezone(){
        Pattern p = Pattern.compile("[^(+)]*");
        Matcher m = p.matcher(date_input_string);
        if(m.find())
            date_string = m.group(0).trim();
        else
            date_string = "Anon";
        timezone = date_string.replace(m.group(0), "");
    }

    private void splitAndSetDateAttributes(){
        String[] tokens = date_string.split(" ");
        weekday = tokens[0].substring(0, tokens[0].length()-2);
        day = Integer.valueOf(tokens[1]);
        month_string = tokens[2];
        month = getMonthByName(month_string);
        year = Integer.valueOf(tokens[3]);
        String[] time_tokens = tokens[4].split(":");
        hours = Integer.valueOf(time_tokens[0]);
        minutes = Integer.valueOf(time_tokens[1]);
        seconds = Integer.valueOf(time_tokens[2]);
    }

    private int getMonthByName(String month){
        switch (month){
            case "Jan": return 1;
            case "Feb": return 2;
            case "Mar": return 3;
            case "Apr": return 4;
            case "May": return 5;
            case "Jun": return 6;
            case "Jul": return 7;
            case "Aug": return 8;
            case "Sep": return 9;
            case "Oct": return 10;
            case "Nov": return 11;
            case "Dec": return 12;
        }
        return -1;
    }

}
