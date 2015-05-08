package org.zywx.wbpalmstar.plugin.uexCalendarView;

import org.json.JSONException;
import org.json.JSONObject;


public class ECalendarViewUtils {

	public static final int CALENDAR_MSG_CODE_OPEN = 0;
	public static final int CALENDAR_MSG_CODE_SETSELECTEDDATE = 1;
	public static final int CALENDAR_MSG_CODE_CLOSE = 2;
	
	public static final String CALENDAR_PARAMS_KEY_FUNCTION = "function";
	public static final String CALENDAR_PARAMS_KEY_ACTIVITYID = "calendarview";
	public static final String CALENDAR_PARAMS_KEY_OBJ = "obj";
	
	public static final String CALENDAR_PARAMS_KEY_X = "x";
	public static final String CALENDAR_PARAMS_KEY_Y = "y";
	public static final String CALENDAR_PARAMS_KEY_W = "w";
	public static final String CALENDAR_PARAMS_KEY_H = "h";
	
	public static final String CALENDAR_PARAMS_KEY_DATE = "date";
	public static final String CALENDAR_PARAMS_KEY_YEAR = "year";
	public static final String CALENDAR_PARAMS_KEY_MOUTH = "mouth";
	public static final String CALENDAR_PARAMS_KEY_DAY = "day";
	
	public static String Json2Str(String[] params) {
		try {
			JSONObject obj = new JSONObject(params[0]);
			JSONObject data = obj.getJSONObject(CALENDAR_PARAMS_KEY_DATE);
			int year = Integer.parseInt(data.getString(CALENDAR_PARAMS_KEY_YEAR));
			int mouth = Integer.parseInt(data.getString(CALENDAR_PARAMS_KEY_MOUTH));
			int day = Integer.parseInt(data.getString(CALENDAR_PARAMS_KEY_DAY));
			return year+"-"+mouth+"-"+day;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
