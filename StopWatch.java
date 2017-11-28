//****************************************************************************
// Info for build  found at https://goo.gl/9w8ZdV, https://goo.gl/EvBL7F
// Tyler Matthews U#09879383, Navin Ramkishun U#58568482
// Date: 11/23/2017
// Simple StopWatch class used for timing file transfers
//
//***************************************************************************

import java.util.Calendar;
import java.util.GregorianCalendar;

class StopWatch
{

    private final double startMillSec;

    StopWatch() 
    {
        //set the start time
        Calendar cal = new GregorianCalendar();
        int sec = cal.get(Calendar.SECOND);
        int min = cal.get(Calendar.MINUTE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int millSec = cal.get(Calendar.MILLISECOND);
        startMillSec = millSec + (sec * 1000) + (min * 60000) + (hour * 3600000);
     }

    double getElapsedTime() 
    {
        //get the elapsed time which = current time - start time.
        Calendar cal = new GregorianCalendar();
        double millSecElapsed = cal.get(Calendar.MILLISECOND);
        double secElapsed = cal.get(Calendar.SECOND);
        double minElapsed = cal.get(Calendar.MINUTE);
        double hourElapsed = cal.get(Calendar.HOUR_OF_DAY);
        double currMillSec = millSecElapsed + (secElapsed * 1000) + (minElapsed * 60000) + (hourElapsed * 3600000);
        return currMillSec - startMillSec;
    }
}