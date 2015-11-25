/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
    public static final String CALENDAR_PARAMS_KEY_ID = "id";

    public static final String CALENDAR_PARAMS_KEY_DATE = "date";
    public static final String CALENDAR_PARAMS_KEY_YEAR = "year";
    public static final String CALENDAR_PARAMS_KEY_MOUTH = "month";
    public static final String CALENDAR_PARAMS_KEY_DAY = "day";

    public static String Json2Str(String[] params) {
        try {
            JSONObject obj = new JSONObject(params[0]);
            JSONObject data = obj.getJSONObject(CALENDAR_PARAMS_KEY_DATE);
            int year = Integer.parseInt(data.getString(CALENDAR_PARAMS_KEY_YEAR));
            int month = Integer.parseInt(data.getString(CALENDAR_PARAMS_KEY_MOUTH));
            int day = Integer.parseInt(data.getString(CALENDAR_PARAMS_KEY_DAY));
            return year + "-" + month + "-" + day;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
