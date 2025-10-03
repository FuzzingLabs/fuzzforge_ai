package com.google.android.material.datepicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import com.google.android.material.C0690R;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.resources.MaterialResources;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class CalendarStyle {
    final CalendarItemStyle day;
    final CalendarItemStyle invalidDay;
    final Paint rangeFill;
    final CalendarItemStyle selectedDay;
    final CalendarItemStyle selectedYear;
    final CalendarItemStyle todayDay;
    final CalendarItemStyle todayYear;
    final CalendarItemStyle year;

    /* JADX INFO: Access modifiers changed from: package-private */
    public CalendarStyle(Context context) {
        int calendarStyle = MaterialAttributes.resolveOrThrow(context, C0690R.attr.materialCalendarStyle, MaterialCalendar.class.getCanonicalName());
        TypedArray calendarAttributes = context.obtainStyledAttributes(calendarStyle, C0690R.styleable.MaterialCalendar);
        this.day = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_dayStyle, 0));
        this.invalidDay = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_dayInvalidStyle, 0));
        this.selectedDay = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_daySelectedStyle, 0));
        this.todayDay = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_dayTodayStyle, 0));
        ColorStateList rangeFillColorList = MaterialResources.getColorStateList(context, calendarAttributes, C0690R.styleable.MaterialCalendar_rangeFillColor);
        this.year = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_yearStyle, 0));
        this.selectedYear = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_yearSelectedStyle, 0));
        this.todayYear = CalendarItemStyle.create(context, calendarAttributes.getResourceId(C0690R.styleable.MaterialCalendar_yearTodayStyle, 0));
        Paint paint = new Paint();
        this.rangeFill = paint;
        paint.setColor(rangeFillColorList.getDefaultColor());
        calendarAttributes.recycle();
    }
}
