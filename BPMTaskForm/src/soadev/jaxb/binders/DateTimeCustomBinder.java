package soadev.jaxb.binders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeCustomBinder {
    public static Date parseDateTime(String s) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            return formatter.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }

    // crazy hack because the 'Z' formatter produces an output incompatible with the xsd:dateTime
    public static String printDateTime(Date dt) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat tzFormatter = new SimpleDateFormat("Z");
        String timezone = tzFormatter.format(dt);
        return formatter.format(dt) + timezone.substring(0, 3) + ":"
                + timezone.substring(3);
    }
}