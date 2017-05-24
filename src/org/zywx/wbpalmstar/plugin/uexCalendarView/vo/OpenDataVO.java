package org.zywx.wbpalmstar.plugin.uexCalendarView.vo;

import org.zywx.wbpalmstar.plugin.uexCalendarView.EUExCalendarView;

import java.io.Serializable;


public class OpenDataVO implements Serializable {
    private static final long serialVersionUID = -2494597734930975094L;

    private String id;
    private double x;
    private double y;
    private double w;
    private double h;

    private boolean isScrollWithWebView = false;

    public int getX() {
        return (int) x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getY() {
        return (int) y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getW() {
        return (int) w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public int getH() {
        return (int) h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public boolean isScrollWithWebView() {
        return isScrollWithWebView;
    }

    public void setScrollWithWebView(boolean scrollWithWebView) {
        isScrollWithWebView = scrollWithWebView;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
