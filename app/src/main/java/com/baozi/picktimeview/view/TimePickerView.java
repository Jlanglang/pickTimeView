package com.baozi.picktimeview.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;

import com.baozi.picktimeview.R;

import java.util.Date;

/**
 */
public class TimePickerView extends Dialog implements DialogInterface {
    public static final int LINES = 0;
    public static final int CIRClE = 2;


    public enum Type {
        ALL("yyyy年MM月dd日 HH:mm E a"), YEAR_MONTH_DAY("yyyy年MM月dd日 E"),
        HOURS_MINS("a HH:mm"), MONTH_DAY_HOUR_MIN("MM月dd日 HH:mm E a"), YEAR_MONTH("yyyy年MM月");

        // 成员变量
        private String pattern;

        // 构造方法
        Type(String pattern) {
            this.pattern = pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }

    }// 四种选择模式，年月日时分，年月日，时分，月日时分

    private TimePickViewController mTimePick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimePick.installContent();
    }

    protected TimePickerView(Context context) {
        super(context);
        mTimePick = new TimePickViewController(context, this, getWindow());
    }

    @Override
    public void setTitle(CharSequence title) {
        mTimePick.setTitle(title);
    }

    public void setP(int p) {
        mTimePick.setP(p);
    }

    public interface OnTimeSelectListener {
        void onTimeSelect(Date date);
    }

    public static class Builder {
        private TimePickViewController.TimePickViewParmas params;

        public Builder(Context context) {
            params = new TimePickViewController.TimePickViewParmas(context);
        }

        public Builder setDate(Date date) {
            params.date = date;
            return this;
        }

        public Builder setType(TimePickerView.Type type) {
            params.type = type;
            return this;
        }

        public Builder setCyclic(boolean cyclic) {
            params.cyclic = cyclic;
            return this;
        }

        public Builder setRange(int startYear, int endYear) {
            params.startYear = startYear;
            params.endYear = endYear;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnTimeSelectListener listener) {
            params.mButtonPositiveText = text;
            params.timeSelectListener = listener;
            return this;
        }

        public Builder setPositiveButton(@StringRes int textId, OnTimeSelectListener listener) {
            params.mButtonPositiveText = params.mContext.getText(textId);
            params.timeSelectListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
            params.mButtonNegativeText = text;
            params.mButtonNegativeListener = listener;
            return this;
        }

        public Builder setNegativeButton(@StringRes int textId, DialogInterface.OnClickListener listener) {
            params.mButtonNegativeText = params.mContext.getText(textId);
            params.mButtonNegativeListener = listener;
            return this;
        }

        public Builder setP(int p) {
            params.p = p;
            return this;
        }

        public Builder setCanvasType(int canvasType) {
            View inflate;
            switch (canvasType) {
                case LINES:
                    inflate = View.inflate(params.mContext, R.layout.pickerview_time_line, null);
                    break;
                case CIRClE:
                    inflate = View.inflate(params.mContext, R.layout.pickerview_time_circle, null);
                    break;
                default:
                    inflate = View.inflate(params.mContext, R.layout.pickerview_time_circle, null);
                    break;
            }
            params.canvasType = canvasType;
            params.contentView = inflate;
            return this;
        }

        public TimePickerView show() {
            TimePickerView timePickerView = creat();
            timePickerView.show();
            return timePickerView;
        }

        private TimePickerView creat() {
            TimePickerView timePickerView = new TimePickerView(params.mContext);
            params.apply(timePickerView.mTimePick);
            return timePickerView;
        }
    }
}
