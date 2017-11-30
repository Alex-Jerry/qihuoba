package com.yjjr.yjfutures.widget.chart;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.model.HisData;
import com.yjjr.yjfutures.utils.DateUtils;
import com.yjjr.yjfutures.utils.DoubleUtil;

import org.joda.time.DateTime;

import java.util.Locale;

/**
 * Created by dell on 2017/9/25.
 */

public class LineChartInfoView extends ChartInfoView {

    private TextView mTvPrice;
    private TextView mTvChangeRate;
    private TextView mTvVol;
    private TextView mTvTime;

    public LineChartInfoView(Context context) {
        this(context, null);
    }

    public LineChartInfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_line_chart_info, this);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvPrice = (TextView) findViewById(R.id.tv_price);
        mTvChangeRate = (TextView) findViewById(R.id.tv_change_rate);
        mTvVol = (TextView) findViewById(R.id.tv_vol);
    }

    @Override
    public void setData(double lastClose, HisData data) {
        mTvTime.setText(DateUtils.formatData(new DateTime(data.getsDate()).getMillis()));
        mTvPrice.setText(DoubleUtil.formatDecimal(data.getClose()));
//        mTvChangeRate.setText(String.format(Locale.getDefault(), "%.2f%%", (data.getClose()- data.getOpen()) / data.getOpen() * 100));
        mTvChangeRate.setText(String.format(Locale.getDefault(), "%.2f%%", (data.getClose() - lastClose) / lastClose * 100));
        mTvVol.setText(data.getVol() + "");
        removeCallbacks(mRunnable);
        postDelayed(mRunnable, 2000);
    }

}
