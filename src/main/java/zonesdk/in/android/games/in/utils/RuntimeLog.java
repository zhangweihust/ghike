/*
 * 
 *      Created by wei.zhang for debug usage
 *      2012-12-13
 * 
 */
package zonesdk.in.android.games.in.utils;

import android.util.*;

import java.io.IOException;
import java.util.logging.*;

public class RuntimeLog {

	public static boolean DEBUG_ON = true;

	// Change this to LOG_ANDROID to false for in-house debugging,
	// or true for logcat debugging
	static boolean LOG_ANDROID = true;

	static final String logfileName = "/sdcard/hike_runtime.log";
	static final String TAG = "UA-XYZ";

	// //////////////////////////////////////////////////

	public static final int ERROR = 3;
	public static final int WARNING = 4;
	public static final int INFO = 5;
	public static final int DEBUG = 6;
	public static final int VERBOSE = 7;

	static Logger logger;
	static LogRecord record;

	// initiate the log on first use of the class and

	// create your custom formatter
	static {
		try {
			FileHandler fh = new FileHandler(logfileName, true);
			fh.setFormatter(new Formatter() {
				public String format(LogRecord rec) {
					StringBuffer buf = new StringBuffer(1000);
					buf.append(new java.util.Date().getDate());
					buf.append('/');
					buf.append(new java.util.Date().getMonth());
					buf.append('/');
					buf.append((new java.util.Date().getYear()) % 100);
					buf.append(' ');
					buf.append(new java.util.Date().getHours());
					buf.append(':');
					buf.append(new java.util.Date().getMinutes());
					buf.append(':');
					buf.append(new java.util.Date().getSeconds());
					buf.append('\n');

					return buf.toString();
				}
			});
			logger = Logger.getLogger(logfileName);
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String msg) {
		if (!DEBUG_ON) {
			return;
		}

		try {
			// use java runtime log
			if (LOG_ANDROID) {

				Log.d(TAG, msg);

			} else {

				record = new LogRecord(Level.ALL, msg);
				record.setLoggerName(logfileName);
				record.setLevel(Level.INFO);
				logger.log(record);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	// the log method
	public static void log(int logLevel, String msg) {
		// don't log DEBUG in release mode
		if (DEBUG_ON) {
			return;
		}

		try {
			if (LOG_ANDROID) {
				// use android logcat
				switch (logLevel) {
				case ERROR:
					Log.e(TAG, msg);
					break;
				case WARNING:
					Log.w(TAG, msg);
					break;
				case INFO:
					Log.i(TAG, msg);
					break;
				case DEBUG:
					Log.d(TAG, msg);
					break;
				case VERBOSE:
					Log.v(TAG, msg);
					break;
				}

			} else {
				// use java runtime log
				record = new LogRecord(Level.ALL, msg);

				// //
				record.setLoggerName(logfileName);

				switch (logLevel) {
				case ERROR:
					record.setLevel(Level.SEVERE);
					logger.log(record);
					break;
				case WARNING:
					record.setLevel(Level.WARNING);
					logger.log(record);
					break;
				case INFO:
					record.setLevel(Level.INFO);
					logger.log(record);
					break;
				// FINE and FINEST levels may not work on some API versions
				// use INFO instead
				case DEBUG:
					record.setLevel(Level.INFO);
					logger.log(record);
					break;
				case VERBOSE:
					record.setLevel(Level.INFO);
					logger.log(record);
					break;
				}
				// /

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

}
