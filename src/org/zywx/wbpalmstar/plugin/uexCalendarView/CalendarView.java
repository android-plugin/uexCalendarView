package org.zywx.wbpalmstar.plugin.uexCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
@SuppressLint("NewApi")
public class CalendarView extends FrameLayout implements OnClickListener {
	private static final String LOG_TAG = CalendarView.class.getSimpleName();
	private static final boolean DEFAULT_SHOW_WEEK_NUMBER = false;
	private static final long MILLIS_IN_DAY = 86400000L;
	private static final int DAYS_PER_WEEK = 7;
	private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;
	private static final int SCROLL_HYST_WEEKS = 2;
	private static final int GOTO_SCROLL_DURATION = 1000;
	private static final int ADJUSTMENT_SCROLL_DURATION = 500;
	private static final int SCROLL_CHANGE_DELAY = 40;
	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final String DEFAULT_MIN_DATE = "01/01/1900";
	private static final String DEFAULT_MAX_DATE = "01/01/2100";
	private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;
	private static final int DEFAULT_DATE_TEXT_SIZE = 22;
	private static final int UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH = 6;
	private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;
	private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 2;
	private static final int UNSCALED_BOTTOM_BUFFER = 20;
	private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 10;
	private static final int DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID = -1;
	private final int mWeekSeperatorLineWidth;
	private int mDateTextSize;
	private Drawable mSelectedDateVerticalBar;
	private final int mSelectedDateVerticalBarWidth;
	private int mSelectedWeekBackgroundColor;
	private int mFocusedMonthDateColor;
	private int mUnfocusedMonthDateColor;
	private int mWeekSeparatorLineColor;
	private int mWeekNumberColor;
	private int mWeekDayTextAppearanceResId;
	private int mDateTextAppearanceResId;
	private ImageView sdtv;
	private int mListScrollTopOffset = 2;
	private int mWeekMinVisibleHeight = 12;
	private int mBottomBuffer = 20;
	private int mShownWeekCount;
	private boolean mShowWeekNumber;
	private int mDaysPerWeek = 7;
	private float mFriction = .05f;
	private float mVelocityScale = 0.333f;
	private WeeksAdapter mAdapter;
	private ListView mListView;
	private TextView mMonthName;
	private ViewGroup mDayNamesHeader;
	private String[] mDayLabels;
	private int mFirstDayOfWeek;
	private int mCurrentMonthDisplayed;
	private int mCurrentYearDisplayed;
	private long mPreviousScrollPosition;
	private boolean mIsScrollingUp = false;
	private int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	private int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	private OnDateChangeListener mOnDateChangeListener;
	private ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();
	private Calendar mTempDate;
	private Calendar mFirstDayOfMonth;
	private Calendar mMinDate;
	private Calendar mMaxDate;
	private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
	private Locale mCurrentLocale;
	private Calendar nowCalendar = Calendar.getInstance(Locale.getDefault());
	public interface OnDateChangeListener {
		public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth);
	}

	public CalendarView(Context context) {
		this(context, null);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			try {
				Date date = mDateFormat.parse((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
				setDate(date.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	};


	public CalendarView(final Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, 0);
		setCurrentLocale(Locale.getDefault());

		mShowWeekNumber = DEFAULT_SHOW_WEEK_NUMBER;
		mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
		String minDate = "";
		if (TextUtils.isEmpty(minDate) || !parseDate(minDate, mMinDate)) {
			parseDate(DEFAULT_MIN_DATE, mMinDate);
		}
		String maxDate = "";
		if (TextUtils.isEmpty(maxDate) || !parseDate(maxDate, mMaxDate)) {
			parseDate(DEFAULT_MAX_DATE, mMaxDate);
		}
		if (mMaxDate.before(mMinDate)) {
			throw new IllegalArgumentException("Max date cannot be before min date.");
		}
		mShownWeekCount = DEFAULT_SHOWN_WEEK_COUNT;
		mSelectedWeekBackgroundColor = getResources().getColor(EUExUtil.getResColorID("transparent"));
		mFocusedMonthDateColor = getResources().getColor(EUExUtil.getResColorID("black"));
		mUnfocusedMonthDateColor = getResources().getColor(EUExUtil.getResColorID("plugin_uexCalendarView_cv_light_gray_color"));
		mWeekSeparatorLineColor = getResources().getColor(EUExUtil.getResColorID("transparent"));
		mWeekNumberColor = getResources().getColor(EUExUtil.getResColorID("plugin_uexCalendarView_cv_weekNumberColor"));
		mSelectedDateVerticalBar = getResources().getDrawable(EUExUtil.getResDrawableID("plugin_uexcalendarview_line_bottom_red"));
		mDateTextAppearanceResId = EUExUtil.getResStyleID("TextAppearance_Small");
		updateDateTextSize();
		mWeekDayTextAppearanceResId = DEFAULT_WEEK_DAY_TEXT_APPEARANCE_RES_ID;
		final Drawable horizontalDivider = getResources().getDrawable(EUExUtil.getResDrawableID("plugin_uexcalendarview_list_divider_holo_dark"));

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		mWeekMinVisibleHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_WEEK_MIN_VISIBLE_HEIGHT, displayMetrics);
		mListScrollTopOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_LIST_SCROLL_TOP_OFFSET, displayMetrics);
		mBottomBuffer = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_BOTTOM_BUFFER, displayMetrics);
		mSelectedDateVerticalBarWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_SELECTED_DATE_VERTICAL_BAR_WIDTH, displayMetrics);
		mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, displayMetrics);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		View content = layoutInflater.inflate(EUExUtil.getResLayoutID("plugin_uexcalendarview_calendar_view"), null, false);
		addView(content);

		mListView = (ListView) content.findViewById(EUExUtil.getResIdID("cv_list"));
		mDayNamesHeader = (ViewGroup) content.findViewById(EUExUtil.getResIdID("cv_day_names"));
		mMonthName = (TextView) content.findViewById(EUExUtil.getResIdID("cv_month_name"));
		sdtv = (ImageView) content.findViewById(EUExUtil.getResIdID("selectDateImageView"));
		mMonthName.setOnClickListener(this);
		sdtv.setOnClickListener(this);

		TextView cv_right_name = (TextView) content.findViewById(EUExUtil.getResIdID("cv_right_name"));
		cv_right_name.setOnClickListener(this);
		((ImageView) findViewById(EUExUtil.getResIdID("cv_divider"))).setImageDrawable(horizontalDivider);
		setUpHeader();
		setUpListView();
		setUpAdapter();

		mTempDate.setTimeInMillis(System.currentTimeMillis());
		if (mTempDate.before(mMinDate)) {
			goTo(mMinDate, false, true, true);
		} else if (mMaxDate.before(mTempDate)) {
			goTo(mMaxDate, false, true, true);
		} else {
			goTo(mTempDate, false, true, true);
		}

		invalidate();
	}

	public void setShownWeekCount(int count) {
		if (mShownWeekCount != count) {
			mShownWeekCount = count;
			invalidate();
		}
	}

	public int getShownWeekCount() {
		return mShownWeekCount;
	}

	public void setSelectedWeekBackgroundColor(int color) {
		if (mSelectedWeekBackgroundColor != color) {
			mSelectedWeekBackgroundColor = color;
			final int childCount = mListView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				WeekView weekView = (WeekView) mListView.getChildAt(i);
				if (weekView.mHasSelectedDay) {
					weekView.invalidate();
				}
			}
		}
	}

	public int getSelectedWeekBackgroundColor() {
		return mSelectedWeekBackgroundColor;
	}

	public void setFocusedMonthDateColor(int color) {
		if (mFocusedMonthDateColor != color) {
			mFocusedMonthDateColor = color;
			final int childCount = mListView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				WeekView weekView = (WeekView) mListView.getChildAt(i);
				if (weekView.mHasFocusedDay) {
					weekView.invalidate();
				}
			}
		}
	}

	public int getFocusedMonthDateColor() {
		return mFocusedMonthDateColor;
	}

	public void setUnfocusedMonthDateColor(int color) {
		if (mUnfocusedMonthDateColor != color) {
			mUnfocusedMonthDateColor = color;
			final int childCount = mListView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				WeekView weekView = (WeekView) mListView.getChildAt(i);
				if (weekView.mHasUnfocusedDay) {
					weekView.invalidate();
				}
			}
		}
	}

	public int getUnfocusedMonthDateColor() {
		return mFocusedMonthDateColor;
	}

	public void setWeekNumberColor(int color) {
		if (mWeekNumberColor != color) {
			mWeekNumberColor = color;
			if (mShowWeekNumber) {
				invalidateAllWeekViews();
			}
		}
	}

	public int getWeekNumberColor() {
		return mWeekNumberColor;
	}

	public void setWeekSeparatorLineColor(int color) {
		if (mWeekSeparatorLineColor != color) {
			mWeekSeparatorLineColor = color;
			invalidateAllWeekViews();
		}
	}

	public int getWeekSeparatorLineColor() {
		return mWeekSeparatorLineColor;
	}

	public void setSelectedDateVerticalBar(int resourceId) {
		Drawable drawable = getResources().getDrawable(resourceId);
		setSelectedDateVerticalBar(drawable);
	}

	public void setSelectedDateVerticalBar(Drawable drawable) {
		if (mSelectedDateVerticalBar != drawable) {
			mSelectedDateVerticalBar = drawable;
			final int childCount = mListView.getChildCount();
			for (int i = 0; i < childCount; i++) {
				WeekView weekView = (WeekView) mListView.getChildAt(i);
				if (weekView.mHasSelectedDay) {
					weekView.invalidate();
				}
			}
		}
	}

	public Drawable getSelectedDateVerticalBar() {
		return mSelectedDateVerticalBar;
	}

	public void setWeekDayTextAppearance(int resourceId) {
		if (mWeekDayTextAppearanceResId != resourceId) {
			mWeekDayTextAppearanceResId = resourceId;
			setUpHeader();
		}
	}

	public int getWeekDayTextAppearance() {
		return mWeekDayTextAppearanceResId;
	}

	public void setDateTextAppearance(int resourceId) {
		if (mDateTextAppearanceResId != resourceId) {
			mDateTextAppearanceResId = resourceId;
			updateDateTextSize();
			invalidateAllWeekViews();
		}
	}

	public int getDateTextAppearance() {
		return mDateTextAppearanceResId;
	}

	@Override
	public void setEnabled(boolean enabled) {
		mListView.setEnabled(enabled);
	}

	@Override
	public boolean isEnabled() {
		return mListView.isEnabled();
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setCurrentLocale(newConfig.locale);
	}

	@Override
	public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		event.setClassName(CalendarView.class.getName());
	}

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		info.setClassName(CalendarView.class.getName());
	}

	public long getMinDate() {
		return mMinDate.getTimeInMillis();
	}

	public void setMinDate(long minDate) {
		mTempDate.setTimeInMillis(minDate);
		if (isSameDate(mTempDate, mMinDate)) {
			return;
		}
		mMinDate.setTimeInMillis(minDate);
		Calendar date = mAdapter.mSelectedDate;
		if (date.before(mMinDate)) {
			mAdapter.setSelectedDay(mMinDate);
		}
		mAdapter.init();
		if (date.before(mMinDate)) {
			setDate(mTempDate.getTimeInMillis());
		} else {
			goTo(date, false, true, false);
		}
	}

	public long getMaxDate() {
		return mMaxDate.getTimeInMillis();
	}

	public void setMaxDate(long maxDate) {
		mTempDate.setTimeInMillis(maxDate);
		if (isSameDate(mTempDate, mMaxDate)) {
			return;
		}
		mMaxDate.setTimeInMillis(maxDate);
		mAdapter.init();
		Calendar date = mAdapter.mSelectedDate;
		if (date.after(mMaxDate)) {
			setDate(mMaxDate.getTimeInMillis());
		} else {
			goTo(date, false, true, false);
		}
	}

	public void setShowWeekNumber(boolean showWeekNumber) {
		if (mShowWeekNumber == showWeekNumber) {
			return;
		}
		mShowWeekNumber = showWeekNumber;
		mAdapter.notifyDataSetChanged();
		setUpHeader();
	}

	public boolean getShowWeekNumber() {
		return mShowWeekNumber;
	}

	public int getFirstDayOfWeek() {
		return mFirstDayOfWeek;
	}

	public void setFirstDayOfWeek(int firstDayOfWeek) {
		if (mFirstDayOfWeek == firstDayOfWeek) {
			return;
		}
		mFirstDayOfWeek = firstDayOfWeek;
		mAdapter.init();
		mAdapter.notifyDataSetChanged();
		setUpHeader();
	}

	public void setOnDateChangeListener(OnDateChangeListener listener) {
		mOnDateChangeListener = listener;
	}

	public long getDate() {
		return mAdapter.mSelectedDate.getTimeInMillis();
	}

	public void setDate(long date) {
		setDate(date, false, false);
	}

	public void setDate(long date, boolean animate, boolean center) {
		mTempDate.setTimeInMillis(date);
		// if (isSameDate(mTempDate, mAdapter.mSelectedDate)) {
		// return;
		// }
		goTo(mTempDate, animate, true, center);
	}

	private void updateDateTextSize() {
		TypedArray dateTextAppearance = getContext().obtainStyledAttributes(mDateTextAppearanceResId, new int[0]);
		mDateTextSize = (int) getResources().getDimension(EUExUtil.getResDimenID("plugin_uexCalendarView_date_size"));
		dateTextAppearance.recycle();
	}

	private void invalidateAllWeekViews() {
		final int childCount = mListView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = mListView.getChildAt(i);
			view.invalidate();
		}
	}

	private void setCurrentLocale(Locale locale) {
		if (locale.equals(mCurrentLocale)) {
			return;
		}

		mCurrentLocale = locale;

		mTempDate = getCalendarForLocale(mTempDate, locale);
		mFirstDayOfMonth = getCalendarForLocale(mFirstDayOfMonth, locale);
		mMinDate = getCalendarForLocale(mMinDate, locale);
		mMaxDate = getCalendarForLocale(mMaxDate, locale);
	}

	private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
		if (oldCalendar == null) {
			return Calendar.getInstance(locale);
		} else {
			final long currentTimeMillis = oldCalendar.getTimeInMillis();
			Calendar newCalendar = Calendar.getInstance(locale);
			newCalendar.setTimeInMillis(currentTimeMillis);
			return newCalendar;
		}
	}

	private boolean isSameDate(Calendar firstDate, Calendar secondDate) {
		return (firstDate.get(Calendar.DAY_OF_YEAR) == secondDate
				.get(Calendar.DAY_OF_YEAR) && firstDate.get(Calendar.YEAR) == secondDate
				.get(Calendar.YEAR));
	}

	private void setUpAdapter() {
		if (mAdapter == null) {
			mAdapter = new WeeksAdapter(getContext());
			mAdapter.registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
				}
			});
			mListView.setAdapter(mAdapter);
		}

		mAdapter.notifyDataSetChanged();
	}

	private void setUpHeader() {
		mDayLabels = new String[mDaysPerWeek];
		for (int i = mFirstDayOfWeek, count = mFirstDayOfWeek + mDaysPerWeek; i < count; i++) {
			int calendarDay = (i > Calendar.SATURDAY) ? i - Calendar.SATURDAY : i;
			mDayLabels[i - mFirstDayOfWeek] = DateUtils.getDayOfWeekString(calendarDay, DateUtils.LENGTH_SHORTEST);
		}

		TextView label = (TextView) mDayNamesHeader.getChildAt(0);
		if (mShowWeekNumber) {
			label.setVisibility(View.VISIBLE);
		} else {
			label.setVisibility(View.GONE);
		}
		for (int i = 1, count = mDayNamesHeader.getChildCount(); i < count; i++) {
			label = (TextView) mDayNamesHeader.getChildAt(i);
			if (mWeekDayTextAppearanceResId > -1) {
				label.setTextAppearance(getContext(), mWeekDayTextAppearanceResId);
			}
			if (i < mDaysPerWeek + 1) {
				label.setText(mDayLabels[i - 1]);
				label.setVisibility(View.VISIBLE);
			} else {
				label.setVisibility(View.GONE);
			}
		}
		mDayNamesHeader.invalidate();
	}

	private void setUpListView() {
		mListView.setDivider(null);
		mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				CalendarView.this.onScrollStateChanged(view, scrollState);
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				CalendarView.this.onScroll(view, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mListView.setFriction(mFriction);
			mListView.setVelocityScale(mVelocityScale);
		}
	}

	private void goTo(Calendar date, boolean animate, boolean setSelected, boolean forceScroll) {
		if (date.before(mMinDate) || date.after(mMaxDate)) {
			throw new IllegalArgumentException("Time not between " + mMinDate.getTime() + " and " + mMaxDate.getTime());
		}
		int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
		View firstChild = mListView.getChildAt(0);
		if (firstChild != null && firstChild.getTop() < 0) {
			firstFullyVisiblePosition++;
		}
		int lastFullyVisiblePosition = firstFullyVisiblePosition + mShownWeekCount - 1;
		if (firstChild != null && firstChild.getTop() > mBottomBuffer) {
			lastFullyVisiblePosition--;
		}
		if (setSelected) {
			mAdapter.setSelectedDay(date);
		}
		int position = getWeeksSinceMinDate(date);

		if (position < firstFullyVisiblePosition
				|| position > lastFullyVisiblePosition || forceScroll) {
			mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
			mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

			setMonthDisplayed(mFirstDayOfMonth);

			if (mFirstDayOfMonth.before(mMinDate)) {
				position = 0;
			} else {
				position = getWeeksSinceMinDate(mFirstDayOfMonth);
			}

			mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;
			if (animate) {
				mListView.smoothScrollToPositionFromTop(position, mListScrollTopOffset, GOTO_SCROLL_DURATION);
			} else {
				mListView.setSelectionFromTop(position, mListScrollTopOffset);
				onScrollStateChanged(mListView, OnScrollListener.SCROLL_STATE_IDLE);
			}
		} else if (setSelected) {
			setMonthDisplayed(date);
		}
	}

	private boolean parseDate(String date, Calendar outDate) {
		try {
			outDate.setTime(mDateFormat.parse(date));
			return true;
		} catch (ParseException e) {
			Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
			return false;
		}
	}

	private void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
	}

	private void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		WeekView child = (WeekView) view.getChildAt(0);
		if (child == null) {
			return;
		}

		long currScroll = view.getFirstVisiblePosition() * child.getHeight() - child.getBottom();

		if (currScroll < mPreviousScrollPosition) {
			mIsScrollingUp = true;
		} else if (currScroll > mPreviousScrollPosition) {
			mIsScrollingUp = false;
		} else {
			return;
		}

		int offset = child.getBottom() < mWeekMinVisibleHeight ? 1 : 0;
		if (mIsScrollingUp) {
			child = (WeekView) view.getChildAt(SCROLL_HYST_WEEKS + offset);
		} else if (offset != 0) {
			child = (WeekView) view.getChildAt(offset);
		}

		int month;
		if (mIsScrollingUp) {
			month = child.getMonthOfFirstWeekDay();
		} else {
			month = child.getMonthOfLastWeekDay();
		}

		int monthDiff;
		if (mCurrentMonthDisplayed == 11 && month == 0) {
			monthDiff = 1;
		} else if (mCurrentMonthDisplayed == 0 && month == 11) {
			monthDiff = -1;
		} else {
			monthDiff = month - mCurrentMonthDisplayed;
		}

		if ((!mIsScrollingUp && monthDiff > 0) || (mIsScrollingUp && monthDiff < 0)) {
			Calendar firstDay = child.getFirstDay();
			if (mIsScrollingUp) {
				firstDay.add(Calendar.DAY_OF_MONTH, -DAYS_PER_WEEK);
			} else {
				firstDay.add(Calendar.DAY_OF_MONTH, DAYS_PER_WEEK);
			}
			setMonthDisplayed(firstDay);
		}
		mPreviousScrollPosition = currScroll;
		mPreviousScrollState = mCurrentScrollState;
	}

	private void setMonthDisplayed(Calendar calendar) {
		final int newMonthDisplayed = calendar.get(Calendar.MONTH);
		if (mCurrentMonthDisplayed != newMonthDisplayed) {
			mCurrentMonthDisplayed = newMonthDisplayed;
		}
		mCurrentYearDisplayed = calendar.get(Calendar.YEAR);
		
		mAdapter.setFocusMonth(mCurrentMonthDisplayed);
		final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
		final long millis = calendar.getTimeInMillis();
		String newMonthName = DateUtils.formatDateRange(getContext(), millis, millis, flags);
		mMonthName.setText(newMonthName);
		mMonthName.invalidate();
	}

	private int getWeeksSinceMinDate(Calendar date) {
		if (date.before(mMinDate)) {
			throw new IllegalArgumentException("fromDate: "
					+ mMinDate.getTime() + " does not precede toDate: "
					+ date.getTime());
		}
		long endTimeMillis = date.getTimeInMillis()
				+ date.getTimeZone().getOffset(date.getTimeInMillis());
		long startTimeMillis = mMinDate.getTimeInMillis()
				+ mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());
		long dayOffsetMillis = (mMinDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
				* MILLIS_IN_DAY;
		return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
	}

	private class ScrollStateRunnable implements Runnable {
		private AbsListView mView;

		private int mNewState;

		public void doScrollStateChange(AbsListView view, int scrollState) {
			mView = view;
			mNewState = scrollState;
			removeCallbacks(this);
			postDelayed(this, SCROLL_CHANGE_DELAY);
		}

		public void run() {
			mCurrentScrollState = mNewState;
			if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
					&& mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				View child = mView.getChildAt(0);
				if (child == null) {
					return;
				}
				int dist = child.getBottom() - mListScrollTopOffset;
				if (dist > mListScrollTopOffset) {
					if (mIsScrollingUp) {
						mView.smoothScrollBy(dist - child.getHeight(),
								ADJUSTMENT_SCROLL_DURATION);
					} else {
						mView.smoothScrollBy(dist, ADJUSTMENT_SCROLL_DURATION);
					}
				}
			}
			mPreviousScrollState = mNewState;
		}
	}

	private class WeeksAdapter extends BaseAdapter implements OnTouchListener {

		private int mSelectedWeek;

		private GestureDetector mGestureDetector;

		private int mFocusedMonth;

		private final Calendar mSelectedDate = Calendar.getInstance();

		private int mTotalWeekCount;

		public WeeksAdapter(Context context) {
			mGestureDetector = new GestureDetector(getContext(),
					new CalendarGestureListener());
			init();
		}

		private void init() {
			mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
			mTotalWeekCount = getWeeksSinceMinDate(mMaxDate);
			if (mMinDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek
					|| mMaxDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
				mTotalWeekCount++;
			}
		}

		public void setSelectedDay(Calendar selectedDay) {
			if (selectedDay.get(Calendar.DAY_OF_YEAR) == mSelectedDate.get(Calendar.DAY_OF_YEAR)
					&& selectedDay.get(Calendar.YEAR) == mSelectedDate.get(Calendar.YEAR)) {
				return;
			}
			mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
			mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
			mFocusedMonth = mSelectedDate.get(Calendar.MONTH);
			notifyDataSetChanged();
		}

		public Calendar getSelectedDay() {
			return mSelectedDate;
		}

		@Override
		public int getCount() {
			return mTotalWeekCount;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			WeekView weekView = null;
			if (convertView != null) {
				weekView = (WeekView) convertView;
			} else {
				weekView = new WeekView(getContext());
				android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				weekView.setLayoutParams(params);
				weekView.setClickable(true);
				weekView.setOnTouchListener(this);
			}

			int selectedWeekDay = (mSelectedWeek == position) ? mSelectedDate.get(Calendar.DAY_OF_WEEK) : -1;
			weekView.init(position, selectedWeekDay, mFocusedMonth);

			return weekView;
		}

		public void setFocusMonth(int month) {
			if (mFocusedMonth == month) {
				return;
			}
			mFocusedMonth = month;
			notifyDataSetChanged();
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mListView.isEnabled() && mGestureDetector.onTouchEvent(event)) {
				WeekView weekView = (WeekView) v;
				if (!weekView.getDayFromLocation(event.getX(), mTempDate)) {
					return true;
				}
				if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
					return true;
				}
				onDateTapped(mTempDate);
				return true;
			}
			return false;
		}

		private void onDateTapped(Calendar day) {
			setSelectedDay(day);
			setMonthDisplayed(day);
			if (mOnDateChangeListener != null) {
				Calendar selectedDay = mAdapter.getSelectedDay();
				mOnDateChangeListener.onSelectedDayChange(CalendarView.this,
						selectedDay.get(Calendar.YEAR),
						selectedDay.get(Calendar.MONTH),
						selectedDay.get(Calendar.DAY_OF_MONTH));
			}
		}

		class CalendarGestureListener extends
				GestureDetector.SimpleOnGestureListener {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}
		}
	}

	private class WeekView extends View {

		private final Rect mTempRect = new Rect();

		private final Paint mDrawPaint = new Paint();

		private final Paint mMonthNumDrawPaint = new Paint();

		private String[] mDayNumbers;

		private boolean[] mFocusDay;

		private boolean mHasFocusedDay;

		private boolean mHasUnfocusedDay;

		private Calendar mFirstDay;

		private int mMonthOfFirstWeekDay = -1;

		private int mLastWeekDayMonth = -1;

		private int mWeek = -1;

		private int mWidth;

		private int mHeight;

		private boolean mHasSelectedDay = false;

		private int mSelectedDay = -1;

		private int mNumCells;

		private int mSelectedLeft = -1;

		private int mSelectedRight = -1;

		public WeekView(Context context) {
			super(context);

			initilaizePaints();
		}

		public void init(int weekNumber, int selectedWeekDay, int focusedMonth) {
			mSelectedDay = selectedWeekDay;
			mHasSelectedDay = mSelectedDay != -1;
			mNumCells = mShowWeekNumber ? mDaysPerWeek + 1 : mDaysPerWeek;
			mWeek = weekNumber;
			mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());

			mTempDate.add(Calendar.WEEK_OF_YEAR, mWeek);
			mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);

			mDayNumbers = new String[mNumCells];
			mFocusDay = new boolean[mNumCells];

			int i = 0;
			if (mShowWeekNumber) {
				mDayNumbers[0] = String.format(Locale.getDefault(), "%d", mTempDate.get(Calendar.WEEK_OF_YEAR));
				i++;
			}

			int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
			mTempDate.add(Calendar.DAY_OF_MONTH, diff);

			mFirstDay = (Calendar) mTempDate.clone();
			mMonthOfFirstWeekDay = mTempDate.get(Calendar.MONTH);

			mHasUnfocusedDay = true;
			for (; i < mNumCells; i++) {
				final boolean isFocusedDay = (mTempDate.get(Calendar.MONTH) == focusedMonth);
				mFocusDay[i] = isFocusedDay;
				mHasFocusedDay |= isFocusedDay;
				mHasUnfocusedDay &= !isFocusedDay;
				if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
					mDayNumbers[i] = "";
				} else {
					mDayNumbers[i] = String.format(Locale.getDefault(), "%d",
							mTempDate.get(Calendar.DAY_OF_MONTH));
				}
				mTempDate.add(Calendar.DAY_OF_MONTH, 1);
			}
			if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
				mTempDate.add(Calendar.DAY_OF_MONTH, -1);
			}
			mLastWeekDayMonth = mTempDate.get(Calendar.MONTH);

			updateSelectionPositions();
		}

		private void initilaizePaints() {
			mDrawPaint.setFakeBoldText(false);
			mDrawPaint.setAntiAlias(true);
			mDrawPaint.setStyle(Style.FILL);

			mMonthNumDrawPaint.setFakeBoldText(true);
			mMonthNumDrawPaint.setAntiAlias(true);
			mMonthNumDrawPaint.setStyle(Style.FILL);
			mMonthNumDrawPaint.setTextAlign(Align.CENTER);
			mMonthNumDrawPaint.setTextSize(mDateTextSize);
		}

		public int getMonthOfFirstWeekDay() {
			return mMonthOfFirstWeekDay;
		}

		public int getMonthOfLastWeekDay() {
			return mLastWeekDayMonth;
		}

		public Calendar getFirstDay() {
			return mFirstDay;
		}

		public boolean getDayFromLocation(float x, Calendar outCalendar) {
			final boolean isLayoutRtl = isLayoutRtl();

			int start;
			int end;

			if (isLayoutRtl) {
				start = 0;
				end = mShowWeekNumber ? mWidth - mWidth / mNumCells : mWidth;
			} else {
				start = mShowWeekNumber ? mWidth / mNumCells : 0;
				end = mWidth;
			}

			if (x < start || x > end) {
				outCalendar.clear();
				return false;
			}

			int dayPosition = (int) ((x - start) * mDaysPerWeek / (end - start));

			if (isLayoutRtl) {
				dayPosition = mDaysPerWeek - 1 - dayPosition;
			}

			outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
			outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);

			return true;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			drawBackground(canvas);
			drawWeekNumbersAndDates(canvas);
			drawWeekSeparators(canvas);
			drawSelectedDateVerticalBars(canvas);
		}

		private void drawBackground(Canvas canvas) {
			if (!mHasSelectedDay) {
				return;
			}
			mDrawPaint.setColor(mSelectedWeekBackgroundColor);

			mTempRect.top = mWeekSeperatorLineWidth;
			mTempRect.bottom = mHeight;

			final boolean isLayoutRtl = isLayoutRtl();

			if (isLayoutRtl) {
				mTempRect.left = 0;
				mTempRect.right = mSelectedLeft - 2;
			} else {
				mTempRect.left = mShowWeekNumber ? mWidth / mNumCells : 0;
				mTempRect.right = mSelectedLeft - 2;
			}
			canvas.drawRect(mTempRect, mDrawPaint);

			if (isLayoutRtl) {
				mTempRect.left = mSelectedRight + 3;
				mTempRect.right = mShowWeekNumber ? mWidth - mWidth / mNumCells : mWidth;
			} else {
				mTempRect.left = mSelectedRight + 3;
				mTempRect.right = mWidth;
			}
			canvas.drawRect(mTempRect, mDrawPaint);
		}

		private void drawWeekNumbersAndDates(Canvas canvas) {
			final float textHeight = mDrawPaint.getTextSize();
			final int y = (int) ((mHeight + textHeight) / 2);
			final int nDays = mNumCells;
			final int divisor = 2 * nDays;
			
			mDrawPaint.setTextAlign(Align.CENTER);
			int i = 0;

			if (isLayoutRtl()) {
				for (; i < nDays - 1; i++) {
					mMonthNumDrawPaint.setColor(mFocusDay[i] ? mFocusedMonthDateColor : mUnfocusedMonthDateColor);
					int x = (2 * i + 1) * mWidth / divisor;
					canvas.drawText(mDayNumbers[nDays - 1 - i], x, y, mMonthNumDrawPaint);
				}
				if (mShowWeekNumber) {
					mDrawPaint.setColor(mWeekNumberColor);
					int x = mWidth - mWidth / divisor;
					canvas.drawText(mDayNumbers[0], x, y, mDrawPaint);
				}
			} else {
				if (mShowWeekNumber) {
					mDrawPaint.setColor(mWeekNumberColor);
					int x = mWidth / divisor;
					canvas.drawText(mDayNumbers[0], x, y, mDrawPaint);
					i++;
				}
				for (; i < nDays; i++) {
					mMonthNumDrawPaint.setColor(mFocusDay[i] ? mFocusedMonthDateColor : mUnfocusedMonthDateColor);
					int x = (2 * i + 1) * mWidth / divisor;
					try {
						String nowCellDateStr = mCurrentMonthDisplayed + "/" + mDayNumbers[i] + "/" + mCurrentYearDisplayed;
						Date nowCellDate = mDateFormat.parse(nowCellDateStr);
						int yearNow = nowCalendar.get(Calendar.YEAR);
						int monthNow = nowCalendar.get(Calendar.MONTH);
						int dayNow = nowCalendar.get(Calendar.DAY_OF_MONTH);
						String cellDateStr = monthNow + "/" + dayNow + "/" + yearNow;
						Date nowDate = mDateFormat.parse(cellDateStr);
						if (nowCellDate.equals(nowDate) && mFocusDay[i]) {
							mMonthNumDrawPaint.setColor(getResources().getColor(android.R.color.holo_red_light));
							canvas.drawText(mDayNumbers[i], x, y, mMonthNumDrawPaint);
						} else {
							canvas.drawText(mDayNumbers[i], x, y, mMonthNumDrawPaint);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}

				}
			}
		}

		private void drawWeekSeparators(Canvas canvas) {
			int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
			if (mListView.getChildAt(0).getTop() < 0) {
				firstFullyVisiblePosition++;
			}
			if (firstFullyVisiblePosition == mWeek) {
				return;
			}
			mDrawPaint.setColor(mWeekSeparatorLineColor);
			mDrawPaint.setStrokeWidth(mWeekSeperatorLineWidth);
			float startX;
			float stopX;
			if (isLayoutRtl()) {
				startX = 0;
				stopX = mShowWeekNumber ? mWidth - mWidth / mNumCells : mWidth;
			} else {
				startX = mShowWeekNumber ? mWidth / mNumCells : 0;
				stopX = mWidth;
			}
			canvas.drawLine(startX, 0, stopX, 0, mDrawPaint);
		}

		private void drawSelectedDateVerticalBars(Canvas canvas) {
			if (!mHasSelectedDay) {
				return;
			}
			mSelectedDateVerticalBar.setBounds(
					mSelectedLeft + mSelectedDateVerticalBarWidth,
					mHeight - mSelectedDateVerticalBarWidth,
					mSelectedRight - mSelectedDateVerticalBarWidth, 
					mHeight - mSelectedDateVerticalBarWidth / 3);
			mSelectedDateVerticalBar.draw(canvas);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mWidth = w;
			updateSelectionPositions();
		}

		private void updateSelectionPositions() {
			if (mHasSelectedDay) {
				final boolean isLayoutRtl = isLayoutRtl();
				int selectedPosition = mSelectedDay - mFirstDayOfWeek;
				if (selectedPosition < 0) {
					selectedPosition += 7;
				}
				if (mShowWeekNumber && !isLayoutRtl) {
					selectedPosition++;
				}
				if (isLayoutRtl) {
					mSelectedLeft = (mDaysPerWeek - 1 - selectedPosition) * mWidth / mNumCells;

				} else {
					mSelectedLeft = selectedPosition * mWidth / mNumCells;
				}
				mSelectedRight = mSelectedLeft + mWidth / mNumCells;
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView.getPaddingBottom()) / mShownWeekCount;
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
		}
	}

	private boolean isLayoutRtl() {

		return false;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == EUExUtil.getResIdID("cv_month_name") || v.getId() == EUExUtil.getResIdID("selectDateImageView")) {
			if (mAdapter != null) {
				Calendar selectedDay = mAdapter.getSelectedDay();
				if (selectedDay != null) {
					int year = selectedDay.get(Calendar.YEAR);
					int month = selectedDay.get(Calendar.MONTH);
					int day = selectedDay.get(Calendar.DAY_OF_MONTH);
					DatePickerDialog datePicker = new DatePickerDialog(getContext(), onDateSetListener, year, month, day);
					datePicker.show();
				}
			}

		}
		if(v.getId() == EUExUtil.getResIdID("cv_right_name")) {
			setDate(Calendar.getInstance(Locale.getDefault()).getTimeInMillis());
		}
	}
}
