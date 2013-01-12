package soadev.jaxb.adapters;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import soadev.jaxb.binders.DateTimeCustomBinder;

public class DateAdapter extends  XmlAdapter<String, Date>{
    
    public Date unmarshal(String s) {
        if(s == null){
            return null;
        }
        return DateTimeCustomBinder.parseDateTime(s);
    }

    public String marshal(Date d) {
        if (d == null){
            return null;
        }
        return DateTimeCustomBinder.printDateTime(d);
    }  
}