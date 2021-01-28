package com.debasish.odiacalendararchiveadmin.model;

import java.util.HashMap;

//convert months to their respective numbers
public class MapMonth {
    public String getMonth(String month) {
        HashMap<String, String> convertMonth = new HashMap<>();
        convertMonth.put("January", "01");
        convertMonth.put("February", "02");
        convertMonth.put("March", "03");
        convertMonth.put("April", "04");
        convertMonth.put("May", "05");
        convertMonth.put("June", "06");
        convertMonth.put("July", "07");
        convertMonth.put("August", "08");
        convertMonth.put("September", "09");
        convertMonth.put("October", "10");
        convertMonth.put("November", "11");
        convertMonth.put("December", "12");

        return convertMonth.get(month);
    }
}
