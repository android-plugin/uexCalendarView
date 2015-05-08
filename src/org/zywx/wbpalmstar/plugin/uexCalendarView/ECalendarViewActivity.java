package org.zywx.wbpalmstar.plugin.uexCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexCalendarView.CalendarView.OnDateChangeListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
@SuppressLint("NewApi")
public class ECalendarViewActivity extends Activity {

	private final java.text.DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private CalendarView myCalendarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(EUExUtil.getResLayoutID("plugin_uexcalendarview_main"));
		EUExCalendarView eCalendarView = (EUExCalendarView) getIntent().getSerializableExtra(ECalendarViewUtils.CALENDAR_PARAMS_KEY_OBJ);
		initView(eCalendarView);
	}

	private void initView(final EUExCalendarView eCalendarView) {
		myCalendarView = (CalendarView) findViewById(EUExUtil.getResIdID("myCalendarView"));
		myCalendarView.setOnDateChangeListener(new OnDateChangeListener() {

			@Override
			public void onSelectedDayChange(CalendarView calendarView,
					int year, int month, int day) {
				
				if (eCalendarView != null) {
					eCalendarView.callBack(year, month + 1, day);
				}
			}
		});
	}

	public void setSelectDate(String dateStr) {
		try {
			Date date = mDateFormat.parse(dateStr);
			if (date != null) {
				myCalendarView.setDate(date.getTime());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
