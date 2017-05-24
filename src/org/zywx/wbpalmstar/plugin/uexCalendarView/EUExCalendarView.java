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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexCalendarView.vo.DataItemVO;
import org.zywx.wbpalmstar.plugin.uexCalendarView.vo.OpenDataVO;
import org.zywx.wbpalmstar.plugin.uexCalendarView.vo.SelectedDateVO;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@SuppressWarnings({"deprecation", "serial"})
public class EUExCalendarView extends EUExBase implements Serializable {

    private final java.text.DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private CalendarView myCalendarView;

    private String activityId;
    public static final String SCRIPT_HEADER = "javascript:";
    public static final String CALLBACK_ONITEMCLICK = "uexCalendarView.onItemClick";
    private static final String DEFAULT_VIEW_ID = "plugin_calendar_view_id";

    private HashMap<String, DataItemVO> mData = new HashMap<String, DataItemVO>();

    public EUExCalendarView(Context context, EBrowserView inParent) {
        super(context, inParent);
        activityId = ECalendarViewUtils.CALENDAR_PARAMS_KEY_ACTIVITYID + this.hashCode();
    }

    @Override
    protected boolean clean() {
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
        if (mHandler == null) {
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
        if (msg.what == ECalendarViewUtils.CALENDAR_MSG_CODE_OPEN) {
            handleOpen(msg);
        } else {
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
        if (mBrwView == null) {
            return;
        }
        if (params == null || params.length == 0) {
            closeView(null);
            return;
        }
        String jsonData = params[0];
        try {

            JSONObject jsonObject = new JSONObject(jsonData);
            closeView(jsonObject.optString(ECalendarViewUtils.CALENDAR_PARAMS_KEY_ID, DEFAULT_VIEW_ID));
        } catch (JSONException e) {
        }
    }

    private void handleSetSelectedDate(String[] params) {
        if (params == null || params.length != 1) {
            return;
        }
        SelectedDateVO selectedDateVO = DataHelper.gson.fromJson(params[0], SelectedDateVO.class);
        String str = selectedDateVO.getDate().getSelectedDataString();
        if (str == null) {
            return;
        }
        setSelectDate(str);
    }

    private void handleOpen(Message msg) {
        String[] params = msg.getData().getStringArray(ECalendarViewUtils.CALENDAR_PARAMS_KEY_FUNCTION);
        if (params == null || params.length != 1) {
            return;
        }
        OpenDataVO data = DataHelper.gson.fromJson(params[0], OpenDataVO.class);
        String id = data.getId();
        if(TextUtils.isEmpty(data.getId())){
            id = DEFAULT_VIEW_ID;
        }

        if(mData.containsKey(id)){
            closeView(id);
        }
        myCalendarView = new CalendarView(mContext);
        myCalendarView.setBackgroundColor(Color.WHITE);
        myCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView calendarView,
                                            int year, int month, int day) {
                callBack(year, month + 1, day);
            }

        });
        if(data.isScrollWithWebView()){
            android.widget.AbsoluteLayout.LayoutParams layoutParams =
                    new android.widget.AbsoluteLayout.LayoutParams(data.getW(), data.getH(), data.getX(), data.getY());
            addViewToWebView(myCalendarView, layoutParams, id);
        }else{
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(data.getW(), data.getH());
            layoutParams.topMargin = data.getY();
            layoutParams.leftMargin = data.getX();

            addViewToCurrentWindow(myCalendarView, layoutParams);
        }
        mData.put(id, new DataItemVO(data, myCalendarView));
    }

    private void closeView(String id) {
        String viewId = id;
        if(TextUtils.isEmpty(id)){
            viewId = DEFAULT_VIEW_ID;
        }
        OpenDataVO data = mData.get(viewId).getDataVO();
        if(data.isScrollWithWebView()){
            removeViewFromWebView(viewId);
        }else{
            removeViewFromCurrentWindow(mData.get(viewId).getView());
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
