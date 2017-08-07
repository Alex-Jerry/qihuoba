package com.yjjr.yjfutures.ui.trade;


import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.contants.Constants;
import com.yjjr.yjfutures.event.OneMinuteEvent;
import com.yjjr.yjfutures.model.HisData;
import com.yjjr.yjfutures.model.Quote;
import com.yjjr.yjfutures.store.StaticStore;
import com.yjjr.yjfutures.ui.BaseFragment;
import com.yjjr.yjfutures.utils.DateUtils;
import com.yjjr.yjfutures.utils.LogUtils;
import com.yjjr.yjfutures.utils.RxUtils;
import com.yjjr.yjfutures.utils.StringUtils;
import com.yjjr.yjfutures.utils.http.HttpManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * A simple {@link Fragment} subclass.
 */
public class CandleStickChartFragment extends BaseFragment {


    public static final String MIN = "min";
    public static final String MIN5 = "min5";
    public static final String MIN15 = "min15";
    public static final String HOUR = "hour";
    public static final String DAY = "day";

    private CandleStickChart mChart;
    private String mSymbol;
    /**
     * 数据类型  day=日线 hour=小时图 min15=15分钟图 min5=5分钟图 min=1分钟图
     */
    private String mType = MIN;
    private List<HisData> mList;

    public CandleStickChartFragment() {
        // Required empty public constructor
    }

    public static CandleStickChartFragment newInstance(String symbol) {
        CandleStickChartFragment fragment = new CandleStickChartFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.CONTENT_PARAMETER, symbol);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setType(String type) {
        mType = type;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            mSymbol = getArguments().getString(Constants.CONTENT_PARAMETER);
        }
    }

    @Override
    protected View initViews(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mChart = new CandleStickChart(mContext);
        mChart.setBackgroundColor(ContextCompat.getColor(mContext, R.color.chart_background));
        mChart.getDescription().setEnabled(false);
        mChart.setNoDataText(mContext.getString(R.string.loading));
        mChart.setNoDataTextColor(ContextCompat.getColor(mContext, R.color.third_text_color));

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(100);

        mChart.setAutoScaleMinMaxEnabled(false);
        mChart.setScaleYEnabled(true);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        mChart.setDrawGridBackground(false);
        int whiteColor = ContextCompat.getColor(mContext, R.color.main_text_color);
        int dividerColor = ContextCompat.getColor(mContext, R.color.divider_color);
        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(whiteColor);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (mList != null && value < mList.size()) {
                    DateTime dateTime = new DateTime(mList.get((int) value).getsDate());
                    if (mType.equals(DAY)) {
                        return android.text.format.DateUtils.formatDateTime(mContext, dateTime.getMillis(), android.text.format.DateUtils.FORMAT_ABBREV_ALL);
                    }
                    return DateUtils.formatTime(dateTime.getMillis());
                }
                return "";
            }
        });

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(whiteColor);
        rightAxis.setLabelCount(7, false);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGridColor(dividerColor);
        rightAxis.setGridLineWidth(0.5f);

        rightAxis.enableGridDashedLine(20, 5, 0);
        final Quote quote = StaticStore.sQuoteMap.get(mSymbol);
        rightAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return StringUtils.getStringByTick(value, quote.getTick());
            }
        });
//        rightAxis.setDrawAxisLine(false);

        YAxis left = mChart.getAxisLeft();
        left.setEnabled(false);


        mChart.getLegend().setEnabled(false);
//        mChart.setMarker(new RealPriceMarkerView(mContext, 2));
        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                if (lastPerformedGesture.equals(ChartTouchListener.ChartGesture.DRAG) || lastPerformedGesture.equals(ChartTouchListener.ChartGesture.FLING)) {
                    float lowestVisibleX = mChart.getLowestVisibleX();
                    if (lowestVisibleX <= 0) {
                    }
                }
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
//                LogUtils.d("onChartFling--------------X is %f, Y is %f", velocityX, velocityY);
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
            }
        });
        return mChart;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OneMinuteEvent event) {
        if (mList != null && TextUtils.equals(mType, MIN)) {
            HisData hisData = mList.get(mList.size() - 1);
            Quote quote = StaticStore.sQuoteMap.get(mSymbol);
            HttpManager.getHttpService().getHistoryData(quote.getSymbol(), quote.getExchange(), hisData.getsDate(), mType)
                    .filter(new Predicate<List<HisData>>() {
                        @Override
                        public boolean test(@NonNull List<HisData> hisDatas) throws Exception {
                            return hisDatas != null && hisDatas.size() > 0;
                        }
                    })
                    .compose(RxUtils.<List<HisData>>applySchedulers())
                    .compose(this.<List<HisData>>bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(new Consumer<List<HisData>>() {
                        @Override
                        public void accept(@NonNull List<HisData> hisDatas) throws Exception {
                            HisData data = hisDatas.get(hisDatas.size() - 1);
                            mList.add(data);
                            mChart.notifyDataSetChanged();
                            mChart.moveViewToX(mChart.getCandleData().getEntryCount());
                        }
                    }, RxUtils.commonErrorConsumer());
        }
    }

    public void loadDataByType(String type) {
        mType = type;
        Quote quote = StaticStore.sQuoteMap.get(mSymbol);
        DateTime dateTime;
        if (quote.isRest()) { //未开盘，数据加载前一天的
            dateTime = new DateTime();
            if (dateTime.getDayOfWeek() == 1 || dateTime.getDayOfWeek() == 7) { //星期一、星期天前一天还是没数据，要加载星期五的
                dateTime = dateTime.minusDays(1).withDayOfWeek(5).withHourOfDay(6).withMinuteOfHour(0).withSecondOfMinute(0);
            } else {
                dateTime = dateTime.minusDays(1).withHourOfDay(6).withMinuteOfHour(0).withSecondOfMinute(0);
            }
        } else {
            dateTime = new DateTime().withHourOfDay(6).withMinuteOfHour(0).withSecondOfMinute(0);
        }
        if (DAY.equals(type)) {
            dateTime = dateTime.minusYears(1);
        } else if (MIN15.equals(type) || MIN5.equals(type)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (HOUR.equals(type)) {
            dateTime = dateTime.minusMonths(1);
        }
        HttpManager.getHttpService().getHistoryData(quote.getSymbol(), quote.getExchange(), DateUtils.formatData(dateTime.getMillis()), mType)
                .map(new Function<List<HisData>, List<HisData>>() {
                    @Override
                    public List<HisData> apply(@NonNull List<HisData> hisDatas) throws Exception {
                        if (hisDatas == null || hisDatas.isEmpty()) {
                            throw new RuntimeException("数据为空");
                        }
                        return hisDatas;
                    }
                })
                .compose(RxUtils.<List<HisData>>applySchedulers())
                .compose(this.<List<HisData>>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Consumer<List<HisData>>() {
                    @Override
                    public void accept(@NonNull List<HisData> list) throws Exception {
                        mList = list;
                        fullData(list);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        LogUtils.e(throwable);
                        mChart.setNoDataText(getString(R.string.data_load_fail));
                    }
                });
    }

    private void fullData(List<HisData> datas) {
        if (datas == null || datas.size() == 0) {
            return;
        }
        ArrayList<CandleEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            HisData data = datas.get(i);
            yVals1.add(new CandleEntry(
                    i, (float) data.getHigh(),
                    (float) data.getLow(),
                    (float) data.getOpen(),
                    (float) data.getClose()
            ));
        }

        CandleDataSet set1 = new CandleDataSet(yVals1, "日期");

        set1.setDrawIcons(false);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setShadowColor(Color.DKGRAY);
        set1.setShadowWidth(0.7f);
        set1.setDecreasingColor(ContextCompat.getColor(getContext(), R.color.main_color_green));
        set1.setDecreasingPaintStyle(Paint.Style.FILL);
        set1.setShadowColorSameAsCandle(true);
        set1.setIncreasingColor(ContextCompat.getColor(getContext(), R.color.main_color_red));
        set1.setIncreasingPaintStyle(Paint.Style.STROKE);
        set1.setIncreasingPaintStyle(Paint.Style.FILL);
        set1.setNeutralColor(ContextCompat.getColor(getContext(), R.color.main_color_red));
        //set1.setHighlightLineWidth(1f);
        set1.setDrawValues(false);
        CandleData data = new CandleData(set1);

        mChart.setData(data);
        mChart.notifyDataSetChanged();
        mChart.setVisibleXRange(100, 20); // allow 20 values to be displayed at once on the x-axis, not more
        mChart.moveViewToX(mChart.getCandleData().getEntryCount());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
