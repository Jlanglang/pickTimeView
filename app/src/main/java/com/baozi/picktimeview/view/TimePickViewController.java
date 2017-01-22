/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baozi.picktimeview.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.baozi.picktimeview.R;
import com.baozi.picktimeview.SimpleDateFormatUtils;
import com.baozi.picktimeview.adapter.NumericWheelAdapter;
import com.baozi.picktimeview.lib.WheelView;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


class TimePickViewController implements WheelView.WheelScrollChangeListener {
    private final Context mContext;
    private final TimePickerView mDialog;
    private final Window mWindow;
    private CharSequence mTitle;
    private View mView;
    private int mViewLayoutResId;

    private TextView mButtonPositive;
    private CharSequence mButtonPositiveText;
    private Message mButtonPositiveMessage;

    private TextView mButtonNegative;
    private CharSequence mButtonNegativeText;
    private Message mButtonNegativeMessage;

    private TextView mTitleView;
    private Handler mHandler;
    private boolean cyclic;
    private Date date;
    private TimePickerView.OnTimeSelectListener mTimeSelectListener;
    private int p;
    private int canvasType;
    private View contentView;
    private TimePickerView.Type mType;

    private static final int DEFULT_START_YEAR = 1990;
    private static final int DEFULT_END_YEAR = 2100;
    private int startYear = DEFULT_START_YEAR;
    private int endYear = DEFULT_END_YEAR;
    private WheelView wv_year;
    private WheelView wv_month;
    private WheelView wv_day;
    private WheelView wv_hours;
    private WheelView wv_mins;
    private View mTv_hour_mins;


    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Message m;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else {
                m = null;
            }
            if (m != null) {
                m.sendToTarget();
            }
            // Post a message so we dismiss after the above handlers are executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialog)
                    .sendToTarget();
        }
    };

    private static final class ButtonHandler extends Handler {
        // Button clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;

        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;

                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    public TimePickViewController(Context context, TimePickerView di, Window window) {
        mContext = context;
        mDialog = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);
        di.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (window != null) {
//            int measuredWidth = window.getDecorView().getMeasuredWidth();
//            window.getDecorView().setPadding(measuredWidth/20, 0, measuredWidth/20, 0);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }


    /**
     * Set the view resource to display in the dialog.
     */
    public void setView(int layoutResId) {
        mView = null;
        mViewLayoutResId = layoutResId;
    }

    /**
     * Set the view to display in the dialog.
     */
    public void setView(View view) {
        mView = view;
        mViewLayoutResId = 0;
    }

    /**
     * Sets a click listener or a message to be sent when the button is clicked.
     * You only need to pass one of {@code listener} or {@code msg}.
     *
     * @param whichButton Which button, can be one of
     *                    {@link DialogInterface#BUTTON_POSITIVE},
     *                    {@link DialogInterface#BUTTON_NEGATIVE}, or
     *                    {@link DialogInterface#BUTTON_NEUTRAL}
     * @param text        The text to display in positive button.
     * @param listener    The {@link DialogInterface.OnClickListener} to use.
     * @param msg         The {@link Message} to be sent when clicked.
     */
    public void setButton(int whichButton, CharSequence text,
                          DialogInterface.OnClickListener listener, Message msg) {

        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }

        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                break;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }


    public TextView getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            default:
                return null;
        }
    }

    public void setP(int p) {
        this.p = p;
    }

    public void setType(TimePickerView.Type type) {
        mType = type;
    }

    public void setCanvasType(int canvasType) {
        this.canvasType = canvasType;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public void setContentView(View contentView) {
        this.contentView = contentView;
    }

    public int getCanvasType() {
        return canvasType;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setTimeSelectListener(CharSequence buttonNegativeText, TimePickerView.OnTimeSelectListener timeSelectListener) {
        mTimeSelectListener = timeSelectListener;
        if (buttonNegativeText != null && timeSelectListener != null) {
            setButton(DialogInterface.BUTTON_POSITIVE, mButtonPositiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mTimeSelectListener.onTimeSelect(getTime());
                            dialog.dismiss();
                        }
                    }, null);
        }
    }

    public void installContent() {
        mDialog.setContentView(contentView);
        setupButtons();
        setupTitle();
        setupPicker();

    }

    /**
     * 初始化button
     */
    private void setupButtons() {
        mButtonNegative = (TextView) contentView.findViewById(R.id.btnCancel);
        mButtonNegative.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setText(R.string.pickerview_cancel);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
        }

        mButtonPositive = (TextView) contentView.findViewById(R.id.btnSubmit);
        mButtonPositive.setOnClickListener(mButtonHandler);
        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setText(R.string.pickerview_submit);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
        }
    }

    private void setupTitle() {
        mTitleView = (TextView) contentView.findViewById(R.id.tv_time_title);
        if (!TextUtils.isEmpty(mTitle)) {
            mTitleView.setText(mTitle);
        }
    }

    private void setupPicker() {
        //默认选中当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] months_big = {"1", "3", "5", "7", "8", "10", "12"};
        String[] months_little = {"4", "6", "9", "11"};

        final List<String> list_big = Arrays.asList(months_big);
        final List<String> list_little = Arrays.asList(months_little);

        // 年
        wv_year = (WheelView) contentView.findViewById(R.id.year);
        wv_year.setAdapter(new NumericWheelAdapter(startYear, endYear));// 设置"年"的显示数据
        if (year < startYear) {//如果手机时间不对,则可能小于startYear,将其等于startYear.使其在选取范围内.
            year = startYear;
        } else if (year > endYear) {
            year = endYear;
        }
        wv_year.setCurrentItem(year - startYear);// 初始化时显示的数据
        wv_year.setCanvasType(canvasType);

        // 月
        wv_month = (WheelView) contentView.findViewById(R.id.month);
        wv_month.setAdapter(new NumericWheelAdapter(1, 12));
        wv_month.setCurrentItem(month);
        wv_month.setCanvasType(canvasType);

        // 日
        wv_day = (WheelView) contentView.findViewById(R.id.day);
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (list_big.contains(String.valueOf(month + 1))) {
            wv_day.setAdapter(new NumericWheelAdapter(1, 31));
        } else if (list_little.contains(String.valueOf(month + 1))) {
            wv_day.setAdapter(new NumericWheelAdapter(1, 30));
        } else {
            // 闰年
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
                wv_day.setAdapter(new NumericWheelAdapter(1, 29));
            else
                wv_day.setAdapter(new NumericWheelAdapter(1, 28));
        }
        wv_day.setCurrentItem(day - 1);
        wv_day.setCanvasType(canvasType);
        //时
        wv_hours = (WheelView) contentView.findViewById(R.id.hour);
        wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
        wv_hours.setCurrentItem(h);
        wv_hours.setCanvasType(canvasType);

        //分
        wv_mins = (WheelView) contentView.findViewById(R.id.min);
        wv_mins.setAdapter(new NumericWheelAdapter(0, 59));
        wv_mins.setCurrentItem(m);
        wv_mins.setCanvasType(canvasType);

        mTv_hour_mins = contentView.findViewById(R.id.tv_hour_mins);
        // 添加"年"监听
        OnItemSelectedListener wheelListener_year = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int year_num = index + startYear;
                // 判断大小月及是否闰年,用来确定"日"的数据
                int maxItem = 30;
                if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                    maxItem = 31;
                } else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                    maxItem = 30;
                } else {
                    if ((year_num % 4 == 0 && year_num % 100 != 0)
                            || year_num % 400 == 0) {
                        wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                        maxItem = 29;
                    } else {
                        wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                        maxItem = 28;
                    }
                }
                if (wv_day.getCurrentItem() > maxItem - 1) {
                    wv_day.setCurrentItem(maxItem - 1);
                }
            }
        };
        // 添加"月"监听
        OnItemSelectedListener wheelListener_month = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                int month_num = index + 1;
                int maxItem = 30;
                // 判断大小月及是否闰年,用来确定"日"的数据
                if (list_big.contains(String.valueOf(month_num))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                    maxItem = 31;
                } else if (list_little.contains(String.valueOf(month_num))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                    maxItem = 30;
                } else {
                    if (((wv_year.getCurrentItem() + startYear) % 4 == 0 && (wv_year.getCurrentItem() + startYear) % 100 != 0)
                            || (wv_year.getCurrentItem() + startYear) % 400 == 0) {
                        wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                        maxItem = 29;
                    } else {
                        wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                        maxItem = 28;
                    }
                }
                if (wv_day.getCurrentItem() > maxItem - 1) {
                    wv_day.setCurrentItem(maxItem - 1);
                }

            }
        };

        wv_year.setOnItemSelectedListener(wheelListener_year);
        wv_month.setOnItemSelectedListener(wheelListener_month);
        wv_year.setWheelScrollChangeListener(this);
        wv_month.setWheelScrollChangeListener(this);
        wv_day.setWheelScrollChangeListener(this);
        wv_hours.setWheelScrollChangeListener(this);
        wv_mins.setWheelScrollChangeListener(this);

        // 根据屏幕密度来指定选择器字体的大小(不同屏幕可能不同)
        int textSize = 6;
        switch (mType) {
            case ALL:
                textSize = textSize * 3;
                break;
            case YEAR_MONTH_DAY:
                textSize = textSize * 4;
                wv_hours.setVisibility(View.GONE);
                wv_mins.setVisibility(View.GONE);
                mTv_hour_mins.setVisibility(View.GONE);
                break;
            case HOURS_MINS:
                textSize = textSize * 4;
                wv_year.setVisibility(View.GONE);
                wv_month.setVisibility(View.GONE);
                wv_day.setVisibility(View.GONE);
                break;
            case MONTH_DAY_HOUR_MIN:
                textSize = textSize * 3;
                wv_year.setVisibility(View.GONE);
                break;
            case YEAR_MONTH:
                textSize = textSize * 4;
                wv_day.setVisibility(View.GONE);
                wv_hours.setVisibility(View.GONE);
                wv_mins.setVisibility(View.GONE);
                mTv_hour_mins.setVisibility(View.GONE);
        }
        //设置字体大小
        wv_day.setTextSize(textSize);
        wv_month.setTextSize(textSize);
        wv_year.setTextSize(textSize);
        wv_hours.setTextSize(textSize);
        wv_mins.setTextSize(textSize);

        //设置是否循环滚动
        wv_year.setCyclic(false);//不让年循环
        wv_month.setCyclic(cyclic);
        wv_day.setCyclic(cyclic);
        wv_hours.setCyclic(cyclic);
        wv_mins.setCyclic(cyclic);
    }

    @Override
    public void onScrollChange(int position) {
        mTitleView.setText(SimpleDateFormatUtils.safeFormatDate(getTime(),mType.getPattern()));
    }


    /**
     * 设置是否循环滚动
     *
     * @param cyclic
     */
    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public Date getTime() {
        StringBuffer sb = new StringBuffer();
        sb.append((wv_year.getCurrentItem() + startYear))
                .append("-")
                .append(wv_month.getCurrentItem() + 1 > 9 ? "" : 0)
                .append(wv_month.getCurrentItem() + 1)
                .append("-")
                .append(wv_day.getCurrentItem() + 1 > 9 ? "" : 0)
                .append(wv_day.getCurrentItem() + 1)
                .append(" ")
                .append(wv_hours.getCurrentItem() + 1 > 9 ? "" : 0)
                .append(wv_hours.getCurrentItem() + 1)
                .append(":")
                .append(wv_mins.getCurrentItem() + 1 > 9 ? "" : 0)
                .append(wv_mins.getCurrentItem() + 1);
        return SimpleDateFormatUtils.safeParseDate(sb.toString());
    }

    public static class TimePickViewParmas {
        public final Context mContext;
        public CharSequence mTitle;
        public int mViewLayoutResId;
        public View contentView;

        public TextView mButtonPositive;
        public CharSequence mButtonPositiveText;
        public Message mButtonPositiveMessage;

        public TextView mButtonNegative;
        public CharSequence mButtonNegativeText;
        public Message mButtonNegativeMessage;
        public DialogInterface.OnClickListener mButtonNegativeListener;

        public Date date;
        public boolean cyclic;
        public int startYear;
        public int endYear;
        public TimePickerView.OnTimeSelectListener timeSelectListener;
        public int p;
        public int canvasType;
        public TimePickerView.Type type;

        public TimePickViewParmas(Context context) {
            mContext = context;
        }

        public void apply(TimePickViewController dialog) {
            if (contentView != null) {
                dialog.setView(contentView);
            } else if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId);
            } else {
                throw new IllegalArgumentException("contentView不能为null");
            }
            if (mTitle != null) {
                dialog.setTitle(mTitle);
            }
            if (date != null) {
                dialog.setDate(date);
            }
            if (timeSelectListener != null) {
                dialog.setTimeSelectListener(mButtonPositiveText, timeSelectListener);
            }
            if (mButtonNegativeText != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mButtonNegativeText,
                        mButtonNegativeListener, null);
            }
            if (type == null) {
                dialog.setType(TimePickerView.Type.ALL);
            } else {
                dialog.setType(type);
            }
            dialog.setEndYear(endYear);
            dialog.setStartYear(startYear);
            dialog.setCanvasType(canvasType);
            dialog.setCyclic(cyclic);
            dialog.setP(p);
            dialog.setContentView(contentView);
        }
    }
}
