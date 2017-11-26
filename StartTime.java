//****************************************************************************
// Based on program found at https://goo.gl/9w8ZdV , bottom of the page
// Tyler Matthews U#09879383, Navin Ramkishun U#
// 11/23/2017
//Simple timer class used for timing file transfers
//
//***************************************************************************



import java.util.Calendar;
import java.util.GregorianCalendar;

class StartTime 
{

    private final double startMilSeconds;

    StartTime() 
    {
        //set the start time
        Calendar cal = new GregorianCalendar();
        int sec = cal.get(Calendar.SECOND);
        int min = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int milliSec = cal.get(Calendar.MILLISECOND);
        startMilSeconds = milliSec + (sec * 1000) + (min * 60000) + (hour * 3600000);
     }

    double getElapsedTime() 
    {
        //get the elapsed time which = current time - start time.
        Calendar cal = new GregorianCalendar();
        double secElapsed = cal.get(Calendar.SECOND);
        double minElapsed = cal.get(Calendar.MINUTE);
        double hourElapsed = cal.get(Calendar.HOUR_OF_DAY);
        double milliSecElapsed = cal.get(Calendar.MILLISECOND);
        double currentMseconds = milliSecElapsed + (secElapsed * 1000) + (minElapsed * 60000) + (hourElapsed * 3600000);
        return currentMseconds - startMilSeconds;
    }
}