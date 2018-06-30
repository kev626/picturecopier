package org.homenet.raneri;

import java.util.Date;
import java.util.HashMap;

public class DuplicateDateCounter {

    private HashMap<Date, Integer> dates;

    public DuplicateDateCounter() {
        dates = new HashMap<>();
    }

    public int getDateNumber(Date date) {
        if (dates.containsKey(date)) {
            dates.replace(date, dates.get(date) + 1);
            return dates.get(date);
        } else {
            dates.put(date, 0);
            return 0;
        }
    }

}
