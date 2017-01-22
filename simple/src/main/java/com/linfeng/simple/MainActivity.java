package com.linfeng.simple;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baozi.picktimeview.SimpleDateFormatUtils;
import com.baozi.picktimeview.view.TimePickerView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private int canvas_type = TimePickerView.LINES;
    private TextView mTv_set_canvas_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickDialog(TimePickerView.Type.ALL);
            }
        });
        findViewById(R.id.tv_month_day_hour_min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickDialog(TimePickerView.Type.MONTH_DAY_HOUR_MIN);
            }
        });
        findViewById(R.id.tv_year_month_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickDialog(TimePickerView.Type.YEAR_MONTH_DAY);
            }
        });
        findViewById(R.id.tv_hour_min).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickDialog(TimePickerView.Type.HOURS_MINS);
            }
        });
        findViewById(R.id.tv_year_month).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickDialog(TimePickerView.Type.YEAR_MONTH);
            }
        });
        mTv_set_canvas_type = (TextView) findViewById(R.id.tv_set_canvas_type);
        mTv_set_canvas_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (canvas_type){
                    case TimePickerView.CIRClE:
                        canvas_type=TimePickerView.LINES;
                        mTv_set_canvas_type.setText("切换为圆形");
                        break;
                    case TimePickerView.LINES:
                        canvas_type=TimePickerView.CIRClE;
                        mTv_set_canvas_type.setText("切换为线条");
                        break;
                }
            }
        });
    }

    private void TimePickDialog(TimePickerView.Type type) {
        new TimePickerView.Builder(MainActivity.this)
                .setCyclic(true)//是否循环
                .setDate(new Date())//设置时间
                .setType(type)//显示的样式
                .setCanvasType(canvas_type)//圆圈还是直线
                .setPositiveButton("确定", new TimePickerView.OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date) {
                        Toast.makeText(MainActivity.this, SimpleDateFormatUtils.safeFormatDate(date)
                                , Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setRange(2010, 2020)//选择年限范围
                .show();
    }
}
