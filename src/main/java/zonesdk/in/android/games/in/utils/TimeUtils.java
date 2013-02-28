package zonesdk.in.android.games.in.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeUtils {

	public static Date StrToDate(String timeString) {
		// 2012-03-14 11:41:52
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return format.parse(timeString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
