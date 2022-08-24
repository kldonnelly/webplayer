package com.isb.webplayer;

import java.util.Calendar;


import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;




class OleAutomationDateUtil {
    
    private long ONE_DAY = 24L * 60 * 60 * 1000;

    
    //days since days since midnight 30 December 1899
    public Date fromOADate(double d) {

        long wholeDays = (long) d;
        double fracDays = Math.abs(d - wholeDays);

        long offset = (ONE_DAY * wholeDays) + (long) (fracDays * ONE_DAY);
        
        Date base = baseFor();
        return new Date( base.getTime() + offset ); //number of milliseconds since Jan. 1, 1970, midnight GMT 
    }

    private Date baseFor() {

        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(1899,11,30,0,0,0);//year month day hour minute second
        return c.getTime();
    }
}

//OLE Automation Date, days since midnight 30 December 1899