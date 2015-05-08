package org.zywx.wbpalmstar.plugin.uexCalendarView;

import java.io.Serializable;

import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

@SuppressWarnings({ "deprecation", "serial" })
public class EUExCalendarView extends EUExBase implements Serializable {
	
	private LocalActivityManager mgr;
	private String activityId;
	public static final String SCRIPT_HEADER = "javascript:";
	public static final String CALLBACK_ONITEMCLICK = "uexCalendarView.onItemClick";

	public EUExCalendarView(Context context, EBrowserView inParent) {
		super(context, inParent);
		mgr = ((ActivityGroup)mContext).getLocalActivityManager();
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
		Activity activity = mgr.getActivity(activityId);
		if (activity != null && activity instanceof ECalendarViewActivity) {
			ECalendarViewActivity eCalendarViewActivity = ((ECalendarViewActivity) activity);
			String[] params = msg.getData().getStringArray(ECalendarViewUtils.CALENDAR_PARAMS_KEY_FUNCTION);
			switch (msg.what) {
			case ECalendarViewUtils.CALENDAR_MSG_CODE_SETSELECTEDDATE:
				handleSetSelectedDate(params, eCalendarViewActivity);
				break;
			case ECalendarViewUtils.CALENDAR_MSG_CODE_CLOSE:
				handleClose(params, eCalendarViewActivity);
				break;
			}
		}
	}

	private void handleClose(String[] params,
			ECalendarViewActivity eCalendarViewActivity) {
		View decorView = eCalendarViewActivity.getWindow().getDecorView();
		removeViewFromCurrentWindow(decorView);
		mgr.destroyActivity(activityId, true);
	}

	private void handleSetSelectedDate(String[] params,
			ECalendarViewActivity eCalendarViewActivity) {
		if (params == null || params.length != 1) {
			return;
		}
		String str = ECalendarViewUtils.Json2Str(params);
		if(str == null) {
			return;
		}
		eCalendarViewActivity.setSelectDate(str);
	}

	private void handleOpen(Message msg) {
		String[] params = msg.getData().getStringArray(ECalendarViewUtils.CALENDAR_PARAMS_KEY_FUNCTION);
		if(params == null || params.length != 1) {
			return;
		}
		try {
			JSONObject json = new JSONObject(params[0]);
			float x = Float.parseFloat(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_X));
			float y = Float.parseFloat(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_Y));
			float w = Float.parseFloat(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_W));
			float h = Float.parseFloat(json.getString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_H));
			Intent intent = new Intent(mContext, ECalendarViewActivity.class);
			ECalendarViewActivity eCalendarViewActivity = (ECalendarViewActivity) mgr.getActivity(activityId);
			if (eCalendarViewActivity != null) {
				View view = eCalendarViewActivity.getWindow().getDecorView();
				removeViewFromCurrentWindow(view);
				mgr.destroyActivity(activityId, true);
				view = null;
			}
			intent.putExtra(ECalendarViewUtils.CALENDAR_PARAMS_KEY_OBJ, this);
			Window window = mgr.startActivity(activityId, intent);
			View decorView = window.getDecorView();
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) w, (int) h);
			lp.topMargin = (int) y;
			lp.leftMargin = (int) x;
			addView2CurrentWindow(decorView, lp);

		} catch (Exception e) {
			e.printStackTrace();
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
