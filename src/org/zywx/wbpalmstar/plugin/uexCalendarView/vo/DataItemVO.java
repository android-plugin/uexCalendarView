package org.zywx.wbpalmstar.plugin.uexCalendarView.vo;

import android.view.View;

import java.io.Serializable;


public class DataItemVO implements Serializable {
    private static final long serialVersionUID = -448007350434229176L;
    private OpenDataVO dataVO;
    private View view;

    public DataItemVO(OpenDataVO dataVO, View view) {
        this.dataVO = dataVO;
        this.view = view;
    }

    public OpenDataVO getDataVO() {
        return dataVO;
    }

    public void setDataVO(OpenDataVO dataVO) {
        this.dataVO = dataVO;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}
