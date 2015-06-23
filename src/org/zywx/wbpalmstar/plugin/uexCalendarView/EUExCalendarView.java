package org.zywx.wbpalmstar.plugin.uexCalendarView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

@SuppressWarnings({ "deprecation", "serial" })
public class EUExCalendarView extends EUExBase implements Serializable {

    private final java.text.DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private CalendarView myCalendarView;

	private String activityId;
	public static final String SCRIPT_HEADER = "javascript:";
	public static final String CALLBACK_ONITEMCLICK = "uexCalendarView.onItemClick";
    private static final String DEFAULT_VIEW_ID="plugin_calendar_view_id";
	public EUExCalendarView(Context context, EBrowserView inParent) {
		super(context, inParent);
		activityId = ECalendarViewUtils.CALENDAR_PARAMS_KEY_ACTIVITYID + this.hashCode();
	}

	@Override
	protected boolean clean() {
		close(null);
		return false;
	}
	public void open(String[] params) {
		sendMessageInProgress(ECalendarViewUtils.CALENDAR_MSG_CODE_OPEN, params);
	}

	public void setSelectedDate(final String[] params) {
		sendMessageInProgress(ECalendarViewUtils.CALENDAR_MSG_CODE_SETSELECTEDDATE, params);
	}

	public void close(String[] params) {
		sendMessageInProgress(ECalendarViewUtils.CALENDAR_MSG_CODE_CLOSE, params);
	}
	
	private void sendMessageInProgress(int msgType, String[] params) {
		if(mHandler == null) {
			return;
		}
		Message msg = Message.obtain();
		msg.what = msgType;
		msg.obj = this;
		Bundle b = new Bundle();
		b.putStringArray(ECalendarViewUtils.CALENDAR_PARAMS_KEY_FUNCTION, params);
		msg.setData(b);
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onHandleMessage(Message msg) {
		if(msg.what == ECalendarViewUtils.CALENDAR_MSG_CODE_OPEN) {
			handleOpen(msg);
		}else {
			handleInCalendar(msg);
		}
	}

	private void handleInCalendar(Message msg) {
			String[] params = msg.getData().getStringArray(ECalendarViewUtils.CALENDAR_PARAMS_KEY_FUNCTION);
			switch (msg.what) {
			case ECalendarViewUtils.CALENDAR_MSG_CODE_SETSELECTEDDATE:
				handleSetSelectedDate(params);
				break;
			case ECalendarViewUtils.CALENDAR_MSG_CODE_CLOSE:
				handleClose(params);
				break;
			}
	}

    public void setSelectDate(String dateStr) {
        try {
            Date date = mDateFormat.parse(dateStr);
            if (date != null) {
                myCalendarView.setDate(date.getTime());
            }
        } catch (ParseException e) {
        }
    }

	private void handleClose(String[] params) {
        if (params==null||params.length==0){
            removeViewFromWebView(DEFAULT_VIEW_ID);
            return;
        }
        String jsonData=params[0];
        try {

            JSONObject jsonObject=new JSONObject(jsonData);
            removeViewFromWebView(jsonObject.optString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_ID,DEFAULT_VIEW_ID));
        } catch (JSONException e) {
        }
	}

	private void handleSetSelectedDate(String[] params) {
		if (params == null || params.length != 1) {
			return;
		}
		String str = ECalendarViewUtils.Json2Str(params);
		if(str == null) {
			return;
		}
		setSelectDate(str);
	}

	private void handleOpen(Message msg) {
		String[] params = msg.getData().getStringArray(ECalendarViewUtils.CALENDAR_PARAMS_KEY_FUNCTION);
		if(params == null || params.length != 1) {
			return;
		}
		try {
			JSONObject json = new JSONObject(params[0]);
			int x = Integer.parseInt(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_X));
            int y = Integer.parseInt(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_Y));
            int w = Integer.parseInt(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_W));
            int h = Integer.parseInt(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_H));
            String id=json.optString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_ID,DEFAULT_VIEW_ID);
            myCalendarView=new CalendarView(mContext);
            myCalendarView.setBackgroundColor(Color.WHITE);
            myCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

                @Override
                public void onSelectedDayChange(CalendarView calendarView,
                                                int year, int month, int day) {
                    callBack(year, month + 1, day);
                }

            });
            android.widget.AbsoluteLayout.LayoutParams layoutParams=
                    new android.widget.AbsoluteLayout.LayoutParams(w,h,x,y);
            addViewToWebView(myCalendarView,layoutParams,id);
        } catch (Exception e) {
		}
	}
	
	private void addView2CurrentWindow(View child,
			RelativeLayout.LayoutParams parms) {
		int l = (int) (parms.leftMargin);
		int t = (int) (parms.topMargin);
		int w = parms.width;
		int h = parms.height;
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.gravity = Gravity.NO_GRAVITY;
		lp.leftMargin = l;
		lp.topMargin = t;
		adptLayoutParams(parms, lp);
		mBrwView.addViewToCurrentWindow(child, lp);
	}
	
	public void callBack(int year, int month, int day) {
		String json = "{date:{year:" + year + ",month:" + month + ",day:" + day + "}}";
		String js = SCRIPT_HEADER + "if(" + CALLBACK_ONITEMCLICK + "){" + CALLBACK_ONITEMCLICK + "('" + json + "');}";
		onCallback(js);
	}
	
}
