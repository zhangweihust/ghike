package zonesdk.in.android.games.in.utils;


import java.util.Calendar;
import java.util.Date;

/*
 *  create by wei.zhang@opi-corp.com
 *  2013-01-21
 */
public class DateUtils {

	public static int  compareDay(Date d1,Date d2){
        Calendar c1 = Calendar.getInstance();  
        Calendar c2 = Calendar.getInstance();  
        c1.setTime(d1);  
        c2.setTime(d2);  
        
        if((c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR))!=0){
        	return (c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR));
        }
        
        if((c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH)) !=0){
        	return (c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH));
        }
        
        if((c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH)) !=0){
        	return (c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH));
        }
        

        return 0;
	}

}
