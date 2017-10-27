package com.yjjr.yjfutures.ui.trade;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.model.HisData;
import com.yjjr.yjfutures.model.Quote;
import com.yjjr.yjfutures.ui.BaseFragment;
import com.yjjr.yjfutures.utils.DateUtils;
import com.yjjr.yjfutures.widget.chart.AppCombinedChart;
import com.yjjr.yjfutures.widget.chart.CoupleChartGestureListener;
import com.yjjr.yjfutures.widget.chart.LineChartXMarkerView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2017/10/26.
 */

public class BaseFullScreenChartFragment extends BaseFragment {

    public static final int MAX_COUNT = 480;
    public static final int MIN_COUNT = 60;


    protected AppCombinedChart mChartPrice;
    protected AppCombinedChart mChartVolume;

    protected XAxis xAxisPrice;
    protected YAxis axisRightPrice;
    protected YAxis axisLeftPrice;

    protected XAxis xAxisVolume;
    protected YAxis axisRightVolume;
    protected YAxis axisLeftVolume;
    protected List<HisData> mData = new ArrayList<>(300);

    private int textColor;


    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_screen_line_chart, container, false);
        mChartPrice = (AppCombinedChart) v.findViewById(R.id.line_chart);
        mChartVolume = (AppCombinedChart) v.findViewById(R.id.bar_chart);
        textColor = ContextCompat.getColor(mContext, R.color.main_text_color);
        initChartPrice();
        initChartVolume();
        initChartListener();

        return v;
    }

    private void initChartPrice() {
        mChartPrice.setScaleEnabled(true);//启用图表缩放事件
        mChartPrice.setDrawBorders(false);//是否绘制边线
        mChartPrice.setBorderWidth(1);//边线宽度，单位dp
        mChartPrice.setDragEnabled(true);//启用图表拖拽事件
        mChartPrice.setScaleYEnabled(false);//启用Y轴上的缩放
//        mChartPrice.setBorderColor(getResources().getColor(R.color.border_color));//边线颜色
        mChartPrice.getDescription().setEnabled(false);//右下角对图表的描述信息
        mChartPrice.setAutoScaleMinMaxEnabled(true);
        LineChartXMarkerView mvx = new LineChartXMarkerView(mContext, mData);
        mvx.setChartView(mChartPrice);
        mChartPrice.setXMarker(mvx);
        Legend lineChartLegend = mChartPrice.getLegend();//主要控制左下方的图例的
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例

        //x轴
        xAxisPrice = mChartPrice.getXAxis();//控制X轴的
        xAxisPrice.setDrawLabels(false);//是否显示X坐标轴上的刻度，默认是true
        xAxisPrice.setDrawAxisLine(false);//是否绘制坐标轴的线，即含有坐标的那条线，默认是true
        xAxisPrice.setDrawGridLines(false);//是否显示X坐标轴上的刻度竖线，默认是true
        xAxisPrice.enableGridDashedLine(10f, 10f, 0f);//绘制成虚线，只有在关闭硬件加速的情况下才能使用

        //左边y
        axisLeftPrice = mChartPrice.getAxisLeft();
        axisLeftPrice.setLabelCount(5, true); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布
        axisLeftPrice.setDrawLabels(true);//是否显示Y坐标轴上的刻度，默认是true
        axisLeftPrice.setDrawGridLines(false);//是否显示Y坐标轴上的刻度竖线，默认是true
        /*轴不显示 避免和border冲突*/
        axisLeftPrice.setDrawAxisLine(false);//是否绘制坐标轴的线，即含有坐标的那条线，默认是true
        axisLeftPrice.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART); //参数是INSIDE_CHART(Y轴坐标在内部) 或 OUTSIDE_CHART(在外部（默认是这个）)
        axisLeftPrice.setTextColor(textColor);


        //右边y
        axisRightPrice = mChartPrice.getAxisRight();
        axisRightPrice.setLabelCount(5, true);//参考上面
        axisRightPrice.setDrawLabels(true);//参考上面
        axisRightPrice.setDrawGridLines(false);//参考上面
        axisRightPrice.setDrawAxisLine(false);//参考上面
        axisRightPrice.setTextColor(textColor);
        axisRightPrice.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART); //参数是INSIDE_CHART(Y轴坐标在内部) 或 OUTSIDE_CHART(在外部（默认是这个）)

    }


    private void initChartVolume() {
        mChartVolume.setScaleEnabled(true);//启用图表缩放事件
        mChartVolume.setDrawBorders(true);//是否绘制边线
        mChartVolume.setBorderWidth(1);//边线宽度，单位dp
        mChartVolume.setDragEnabled(true);//启用图表拖拽事件
        mChartVolume.setScaleYEnabled(false);//启用Y轴上的缩放
//        mChartVolume.setBorderColor(getResources().getColor(R.color.border_color));//边线颜色
        mChartVolume.getDescription().setEnabled(false);//右下角对图表的描述信息

        Legend lineChartLegend = mChartVolume.getLegend();
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例

        //x轴
        xAxisVolume = mChartVolume.getXAxis();
        xAxisVolume.setDrawLabels(true);
        xAxisVolume.setDrawAxisLine(false);
        xAxisVolume.setDrawGridLines(false);
        xAxisVolume.setTextColor(textColor);
        xAxisVolume.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisVolume.setLabelCount(5,true);


        //左边y
        axisLeftVolume = mChartVolume.getAxisLeft();
        axisLeftVolume.setDrawLabels(true);//参考上面
        axisLeftVolume.setDrawGridLines(false);//参考上面
        /*轴不显示 避免和border冲突*/
        axisLeftVolume.setDrawAxisLine(false);//参考上面
        axisLeftVolume.setTextColor(textColor);
        axisLeftVolume.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);


        //右边y
        axisRightVolume = mChartVolume.getAxisRight();
        axisRightVolume.setDrawLabels(false);//参考上面
        axisRightVolume.setDrawGridLines(false);//参考上面
        axisRightVolume.setDrawAxisLine(false);//参考上面


    }

    private void initChartListener() {
        // 将K线控的滑动事件传递给交易量控件
        mChartPrice.setOnChartGestureListener(new CoupleChartGestureListener(mChartPrice, mChartVolume));
//        // 将交易量控件的滑动事件传递给K线控件
        mChartVolume.setOnChartGestureListener(new CoupleChartGestureListener(mChartVolume, mChartPrice));
        mChartPrice.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mChartVolume.highlightValues(new Highlight[]{h});
            }


            @Override
            public void onNothingSelected() {
                mChartVolume.highlightValue(null);
            }
        });

        mChartVolume.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                mChartPrice.highlightValues(new Highlight[]{h});
            }

            @Override
            public void onNothingSelected() {
                mChartPrice.highlightValue(null);
            }
        });

    }

    protected void initChartKData(AppCombinedChart combinedChartX) {

        ArrayList<CandleEntry> lineCJEntries = new ArrayList<>();
        ArrayList<Entry> lineJJEntries = new ArrayList<>();

        for (int i = 0, j = 0; i < mData.size(); i++, j++) {
            HisData hisData = mData.get(i);
            lineCJEntries.add(new CandleEntry(i, (float) hisData.getHigh(), (float) hisData.getLow(), (float) hisData.getOpen(), (float) hisData.getClose()));
            lineJJEntries.add(new Entry(i, (float) hisData.getAvePrice()));

        }

        /*注老版本LineData参数可以为空，最新版本会报错，修改进入ChartData加入if判断*/
        LineData lineData = new LineData(setLine(1, lineJJEntries));
        CandleData candleData = new CandleData(setKLine(0, lineCJEntries));
        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);
        combinedData.setData(candleData);
        combinedChartX.setData(combinedData);

        combinedChartX.notifyDataSetChanged();
        combinedChartX.invalidate();
    }

    protected void initChartPriceData(AppCombinedChart combinedChartX) {

        ArrayList<Entry> lineCJEntries = new ArrayList<>(MAX_COUNT);
        ArrayList<Entry> lineJJEntries = new ArrayList<>(MAX_COUNT);

        for (int i = 0, j = 0; i < mData.size(); i++, j++) {
            HisData t = mData.get(j);

            if (t == null) {
                lineCJEntries.add(new Entry(Float.NaN, i));
                lineJJEntries.add(new Entry(Float.NaN, i));
                continue;
            }
            lineCJEntries.add(new Entry(i, (float) mData.get(i).getClose()));
            lineJJEntries.add(new Entry(i, (float) mData.get(i).getAvePrice()));

        }

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setLine(0, lineCJEntries));
        sets.add(setLine(1, lineJJEntries));
        /*注老版本LineData参数可以为空，最新版本会报错，修改进入ChartData加入if判断*/
        LineData lineData = new LineData(sets);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);
        combinedChartX.setData(combinedData);

//        combinedChartX.setVisibleXRange(MIN_COUNT, MAX_COUNT);

        combinedChartX.notifyDataSetChanged();
        combinedChartX.invalidate();
    }

    @android.support.annotation.NonNull
    private LineDataSet setLine(int type, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setDrawValues(false);
        if (type == 0) {
//            lineDataSetMa.setDrawFilled(true);
            lineDataSetMa.setColor(getResources().getColor(R.color.minute_blue));
        } else if (type == 1) {
            lineDataSetMa.setColor(getResources().getColor(R.color.minute_yellow));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.transparent));
        }
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetMa.setLineWidth(1f);

        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setCircleColors(getResources().getColor(R.color.transparent));

        return lineDataSetMa;
    }

    @android.support.annotation.NonNull
    private CandleDataSet setKLine(int type, ArrayList<CandleEntry> lineEntries) {
        CandleDataSet set1 = new CandleDataSet(lineEntries, "ma" + type);
        set1.setDrawIcons(false);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setShadowColor(Color.DKGRAY);
        set1.setShadowWidth(0.7f);
        set1.setDecreasingColor(ContextCompat.getColor(getContext(), R.color.main_color_green));
        set1.setDecreasingPaintStyle(Paint.Style.FILL);
        set1.setShadowColorSameAsCandle(true);
        set1.setIncreasingColor(ContextCompat.getColor(getContext(), R.color.main_color_red));
        set1.setIncreasingPaintStyle(Paint.Style.FILL);
        set1.setNeutralColor(ContextCompat.getColor(getContext(), R.color.main_color_red));
        //set1.setHighlightLineWidth(1f);
        set1.setDrawValues(false);

        return set1;
    }

    protected void initChartVolumeData(CombinedChart combinedChartX) {

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            HisData t = mData.get(i);

            if (t == null) {
                barEntries.add(new BarEntry(Float.NaN, i));
                continue;
            }
            barEntries.add(new BarEntry(i, t.getVol()));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "成交量");
//        barDataSet.setBarSpacePercent(20); //bar空隙，可以控制树状图的大小，空隙越大，树状图越窄
        barDataSet.setDrawValues(false);//是否在线上绘制数值
        barDataSet.setColor(getResources().getColor(R.color.increasing_color));//设置树状图颜色
        List<Integer> list = new ArrayList<>();
        list.add(getResources().getColor(R.color.increasing_color));
        list.add(getResources().getColor(R.color.decreasing_color));
        barDataSet.setColors(list);//可以给树状图设置多个颜色，判断条件在BarChartRenderer 类的140行以下修改了判断条件
        BarData barData = new BarData(barDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedChartX.setData(combinedData);

        setOffset();
        combinedChartX.notifyDataSetChanged();
        combinedChartX.invalidate();
    }

    protected void setOffset() {
        float lineLeft = mChartPrice.getViewPortHandler().offsetLeft();
        float barLeft = mChartVolume.getViewPortHandler().offsetLeft();
        float lineRight = mChartPrice.getViewPortHandler().offsetRight();
        float barRight = mChartVolume.getViewPortHandler().offsetRight();
        float offsetLeft, offsetRight;
 /*注：setExtraLeft...函数是针对图表相对位置计算，比如A表offLeftA=20dp,B表offLeftB=30dp,则A.setExtraLeftOffset(10),并不是30，还有注意单位转换*/
        if (barLeft < lineLeft) {
            offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
            mChartVolume.setExtraLeftOffset(offsetLeft);
        } else {
            offsetLeft = Utils.convertPixelsToDp(barLeft - lineLeft);
            mChartPrice.setExtraLeftOffset(offsetLeft);
        }
  /*注：setExtra...函数是针对图表绝对位置计算，比如A表offRightA=20dp,B表offRightB=30dp,则A.setExtraLeftOffset(30),并不是10，还有注意单位转换*/
        if (barRight < lineRight) {
            offsetRight = Utils.convertPixelsToDp(lineRight);
            mChartVolume.setExtraRightOffset(offsetRight);
        } else {
            offsetRight = Utils.convertPixelsToDp(barRight);
            mChartPrice.setExtraRightOffset(offsetRight);
        }

    }


    protected void setLimitLine(Quote quote) {
        LimitLine limitLine = new LimitLine((float) quote.getLastclose());
        limitLine.enableDashedLine(5, 10, 0);
        axisLeftPrice.addLimitLine(limitLine);
    }
}