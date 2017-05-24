package org.zywx.wbpalmstar.plugin.uexCalendarView.vo;

import java.io.Serializable;


public class SelectedDateVO implements Serializable {
    private static final long serialVersionUID = 6519488534396890743L;
    private DateVO date;

    public DateVO getDate() {
        return date;
    }

    public void setDate(DateVO date) {
        this.date = date;
    }
}
