package com.yjjr.yjfutures.widget.chart;

/**
 * Created by Administrator on 2016/2/1.
 */

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.model.HisData;
import com.yjjr.yjfutures.utils.DateUtils;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
public class LineChartXMarkerView extends MarkerView {

    private List<HisData> mList;
    private TextView tvContent;

    public LineChartXMarkerView(Context context, List<HisData> list) {
        super(context, R.layout.view_mp_real_price_marker);
        mList = list;
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int value = (int) e.getX();
        if (mList != null && value < mList.size()) {
            DateTime dateTime = new DateTime(mList.get(value).getsDate());
            tvContent.setText(DateUtils.formatTime(dateTime.getMillis()));
        }
        super.refreshContent(e, highlight);
    }
}
