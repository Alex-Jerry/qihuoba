package com.yjjr.yjfutures.ui.trade;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.contants.Constants;
import com.yjjr.yjfutures.event.ChartTouchEvent;
import com.yjjr.yjfutures.event.PollRefreshEvent;
import com.yjjr.yjfutures.event.PriceRefreshEvent;
import com.yjjr.yjfutures.event.SendOrderEvent;
import com.yjjr.yjfutures.model.CommonResponse;
import com.yjjr.yjfutures.model.FastTakeOrderConfig;
import com.yjjr.yjfutures.model.MarketDepth;
import com.yjjr.yjfutures.model.Quote;
import com.yjjr.yjfutures.model.biz.BizResponse;
import com.yjjr.yjfutures.model.biz.Funds;
import com.yjjr.yjfutures.model.biz.HistoricalTicks;
import com.yjjr.yjfutures.model.biz.Holds;
import com.yjjr.yjfutures.store.FastOrderSharePrefernce;
import com.yjjr.yjfutures.store.StaticStore;
import com.yjjr.yjfutures.ui.BaseApplication;
import com.yjjr.yjfutures.ui.BaseFragment;
import com.yjjr.yjfutures.ui.SimpleFragmentPagerAdapter;
import com.yjjr.yjfutures.ui.WebActivity;
import com.yjjr.yjfutures.utils.ActivityTools;
import com.yjjr.yjfutures.utils.BizSocketUtils;
import com.yjjr.yjfutures.utils.DialogUtils;
import com.yjjr.yjfutures.utils.DisplayUtils;
import com.yjjr.yjfutures.utils.DoubleUtil;
import com.yjjr.yjfutures.utils.LogUtils;
import com.yjjr.yjfutures.utils.RxUtils;
import com.yjjr.yjfutures.utils.SocketUtils;
import com.yjjr.yjfutures.utils.SpannableUtil;
import com.yjjr.yjfutures.utils.StringUtils;
import com.yjjr.yjfutures.utils.ToastUtils;
import com.yjjr.yjfutures.utils.http.HttpConfig;
import com.yjjr.yjfutures.utils.http.HttpManager;
import com.yjjr.yjfutures.widget.CustomPromptDialog;
import com.yjjr.yjfutures.widget.HeaderView;
import com.yjjr.yjfutures.widget.MarketDepthView;
import com.yjjr.yjfutures.widget.NestRadioGroup;
import com.yjjr.yjfutures.widget.NoTouchScrollViewpager;
import com.yjjr.yjfutures.widget.dropdownmenu.MenuItem;
import com.yjjr.yjfutures.widget.dropdownmenu.TopRightMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.socket.emitter.Emitter;

public class TradeFragment extends BaseFragment implements View.OnClickListener {

    private boolean mIsDemo;
    private TextView tvLeft;
    private TextView tvRight;
    private String leftText = "看涨";
    private String rightText = "看跌";
    private ProgressDialog mProgressDialog;
    private NoTouchScrollViewpager mViewpager;
    private NestRadioGroup rgNav;
    private TextView mTvKchart;
    private TopRightMenu mTopRightMenu;
    private String mSymbol;
    private KLineChartFragment mCandleStickChartFragment;
    private View vgOrder;
    private View vgSettlement;
    private TextView tvDirection;
    private TextView tvYueValue;
    private TextView tvTotal;

    private TextView tvOpen;
    private TextView tvHigh;
    private TextView tvLow;
    private TextView tvVol;
    private TextView tvLastClose;

    private View colorView;
    private List<Holds> mHoldsList;
    private TextView tvPrice;
    private TextView tvChange;
    private TextView tvChangeRate;
    private CustomPromptDialog mCloseSuccessDialog;
    private CustomPromptDialog mCloseAllDialog;
    private CustomPromptDialog mCloseDialog;
    private HeaderView mHeaderView;
    /**
     * 休市信息，休市时显示出来
     */
    private TextView tvRest;
    private MarketDepthView marketDepthView;
    private MarketHisAdapter mMarketHisAdapter;
    private RecyclerView mRvMarketHis;
    private ArrayList<HistoricalTicks> mHisList = new ArrayList<HistoricalTicks>(20) {{
        for (int i = 0; i < 20; i++) {
            add(null);
        }
    }};
    private NestedScrollView mScrollView;
    private TextView mTvTradeToast;


    public TradeFragment() {
        // Required empty public constructor
    }

    public static TradeFragment newInstance(boolean isDemo, String symbol) {
        TradeFragment fragment = new TradeFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.CONTENT_PARAMETER, isDemo);
        args.putString(Constants.CONTENT_PARAMETER_2, symbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (getArguments() != null) {
            mIsDemo = getArguments().getBoolean(Constants.CONTENT_PARAMETER);
            mSymbol = getArguments().getString(Constants.CONTENT_PARAMETER_2);
        }
        mCloseSuccessDialog = DialogUtils.createCommonDialog(mContext, "卖出委托成交完毕");
        mCloseAllDialog = new CustomPromptDialog.Builder(mContext)
                .setMessage("您确定要卖出全部持仓么？")
                .isShowClose(true)
                .setMessageDrawableId(R.drawable.ic_info)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        closeAllOrder();
                    }
                })
                .isShowClose(true)
                .create();
        mCloseDialog = new CustomPromptDialog.Builder(mContext)
                .setMessage("您确定要平仓么？")
                .isShowClose(true)
                .setMessageDrawableId(R.drawable.ic_info)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        closeOrder();
                    }
                })
                .isShowClose(true)
                .create();
    }

    @Override
    protected View initViews(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trade, container, false);
        final Quote quote = StaticStore.getQuote(mSymbol, mIsDemo);
        findViews(v, quote);
        setRestView(quote);
        mHeaderView.setOperateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebActivity.startActivity(mContext, String.format(HttpConfig.URL_RULE, StringUtils.getRuleName(quote)));
            }
        });
        mCandleStickChartFragment = KLineChartFragment.newInstance(mSymbol, mIsDemo, HttpConfig.MIN);
        Fragment[] fragments = {
                LineChartFragment.newInstance(mSymbol, mIsDemo),
                mCandleStickChartFragment,
                KLineChartFragment.newInstance(mSymbol, mIsDemo, HttpConfig.DAY),
                KLineChartFragment.newInstance(mSymbol, mIsDemo, HttpConfig.WEEK),
                KLineChartFragment.newInstance(mSymbol, mIsDemo, HttpConfig.MONTH)};
        SimpleFragmentPagerAdapter KLineAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragments);
        mViewpager.setAdapter(KLineAdapter);
//        mViewpager.setOffscreenPageLimit(fragments.length);
        rgNav.setOnCheckedChangeListener(new NestRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(NestRadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_chart1:
                        mViewpager.setCurrentItem(0, false);
                        break;
                    case R.id.rb_chart2:
                        mViewpager.setCurrentItem(2, false);
                        break;
                    case R.id.rb_chart3:
                        mViewpager.setCurrentItem(3, false);
                        break;
                    case R.id.rb_chart4:
                        mViewpager.setCurrentItem(4, false);
                        break;
                    case R.id.rb_chart5:
                        mViewpager.setCurrentItem(5, false);
                        break;
                }
                mTvKchart.setTextColor(ContextCompat.getColor(mContext, R.color.main_text_color));
                mTvKchart.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(mContext, R.drawable.ic_down_arrow_white), null);
            }
        });
        ((RadioButton) rgNav.getChildAt(0)).setChecked(true);
        mTopRightMenu = new TopRightMenu(getActivity());

//添加菜单项
        final List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.transport, "1分钟"));
        menuItems.add(new MenuItem(R.drawable.transport, "5分钟"));
        menuItems.add(new MenuItem(R.drawable.transport, "15分钟"));
        menuItems.add(new MenuItem(R.drawable.transport, "1小时"));
        mTopRightMenu
                .setWidth(DisplayUtils.getWidthHeight(mContext)[0] / fragments.length)      //默认宽度wrap_content
                .setHeight(DisplayUtils.dip2px(mContext, 30 * menuItems.size()))
                .showIcon(false)     //显示菜单图标，默认为true
                .dimBackground(false)        //背景变暗，默认为true
                .needAnimationStyle(false)   //显示动画，默认为true
                .addMenuList(menuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        rgNav.clearCheck();
                        mTvKchart.setText(menuItems.get(position).getText());
                        mTvKchart.setTextColor(ContextCompat.getColor(mContext, R.color.third_text_color));
//                        mTvKchart.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(mContext, R.drawable.ic_down_arrow), null);
                        mViewpager.setCurrentItem(1, false);
                        String type = HttpConfig.MIN;
                        switch (position) {
                            case 0:
                                type = HttpConfig.MIN;
                                break;
                            case 1:
                                type = HttpConfig.MIN5;
                                break;
                            case 2:
                                type = HttpConfig.MIN15;
                                break;
                            case 3:
                                type = HttpConfig.HOUR;
                                break;
                        }
                        mCandleStickChartFragment.loadDataByType(type);
                    }
                });
        fillViews(quote);
        mHeaderView.setMainTitle(TextUtils.concat(quote.getSymbolname(), "\n", SpannableUtil.getStringBySize(quote.getSymbol(), .6f)));
        tvLeft.setOnClickListener(this);
        tvRight.setOnClickListener(this);
        v.findViewById(R.id.ll_trade).setVisibility(HttpConfig.IS_OPEN_TRADE ? View.VISIBLE : View.GONE);
        v.findViewById(R.id.tv_deposit).setOnClickListener(this);
        v.findViewById(R.id.tv_position).setOnClickListener(this);
        v.findViewById(R.id.tv_close_order).setOnClickListener(this);
        v.findViewById(R.id.tv_kchart).setOnClickListener(this);
        getMarketDepth(quote);
        return v;
    }

    private void getMarketDepth(final Quote quote) {
        if (SocketUtils.getSocket() != null && quote != null) {
            final Gson gson = new Gson();
            Emitter.Listener fn = new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        final List<MarketDepth> list = gson.fromJson(args[0].toString(), new TypeToken<List<MarketDepth>>() {
                        }.getType());
                        marketDepthView.post(new Runnable() {
                            @Override
                            public void run() {
                                marketDepthView.setData(list, quote.getTick());
                            }
                        });
                    } catch (Exception e) {
                        LogUtils.e(e);
                    }
                }
            };
            SocketUtils.getSocket().on("topMarketDepth" + quote.getSort(), fn);
            SocketUtils.getSocket().once("getMktDepthList", fn);
            SocketUtils.getSocket().emit("getMktDepthList", quote.getSort());
            SocketUtils.getSocket().on(SocketUtils.HIS_TICKS + quote.getSort(), new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        final List<HistoricalTicks> list = gson.fromJson(args[0].toString(), new TypeToken<List<HistoricalTicks>>() {
                        }.getType());
                        marketDepthView.post(new Runnable() {
                            @Override
                            public void run() {
                                mMarketHisAdapter.replaceData(list);
                            }
                        });
                    } catch (Exception e) {
                        LogUtils.e(e);
                    }
                }
            });
            SocketUtils.getSocket().emit(SocketUtils.HIS_TICKS, mSymbol);
        }
        if (BizSocketUtils.getSocket() != null && HttpConfig.IS_OPEN_TRADE) {
            BizSocketUtils.getSocket().on(BizSocketUtils.TRADE_RECORD, new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    if (mTvTradeToast == null) return;
                    Runnable hide = new Runnable() {
                        @Override
                        public void run() {
                            mTvTradeToast.setVisibility(View.INVISIBLE);
                        }
                    };
                    mTvTradeToast.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvTradeToast.setText(args[0].toString());
                            mTvTradeToast.setVisibility(View.VISIBLE);
                            ObjectAnimator animator = ObjectAnimator.ofFloat(mTvTradeToast, "x", -mTvTradeToast.getMeasuredWidth(), 0);
                            animator.start();
                        }
                    });
                    mTvTradeToast.removeCallbacks(hide);
                    mTvTradeToast.postDelayed(hide, 3000);
                }
            });
        }
    }

    /**
     * 设置休市状态
     *
     * @param quote
     */
    private void setRestView(Quote quote) {
        if (quote == null || quote.isRest()) { // 休市的状态
//            tvRest.setText(String.format("休市中\t下一个交易时间段：\n%s", quote.getTradingTime()));
            tvRest.setText("休市中    " + quote.getLastTime());
        } else {
            tvRest.setText("交易中    " + quote.getLastTime());
        }
    }

    private void fillViews(Quote quote) {
        if (quote == null) return;
        tvLeft.setText(leftText);
        tvRight.setText(rightText);

        int profitColor = StringUtils.getProfitColor(mContext, quote.getChange());
        tvPrice.setTextColor(profitColor);
        tvChange.setTextColor(profitColor);
        tvChangeRate.setTextColor(profitColor);
        tvPrice.setText(StringUtils.getStringByTick(quote.getLastPrice(), quote.getTick()));
        tvChange.setText(String.format(Locale.getDefault(), "%+.2f", quote.getChange()));
        tvChangeRate.setText(String.format(Locale.getDefault(), "(%+.2f%%)", quote.getChangeRate()));

        tvOpen.setText(StringUtils.getStringByTick(quote.getOpen(), quote.getTick()));
        tvHigh.setText(StringUtils.getStringByTick(quote.getHigh(), quote.getTick()));
        tvLow.setText(StringUtils.getStringByTick(quote.getLow(), quote.getTick()));
        tvVol.setText(quote.getVol() + "");
        tvLastClose.setText(StringUtils.getStringByTick(quote.getLastclose(), quote.getTick()));
    }


    private void findViews(View v, Quote quote) {
        mHeaderView = (HeaderView) v.findViewById(R.id.header_view);
        mHeaderView.bindActivity(getActivity());
        mScrollView = (NestedScrollView) v.findViewById(R.id.scrollView);
        rgNav = (NestRadioGroup) v.findViewById(R.id.rg_nav);
        tvLeft = (TextView) v.findViewById(R.id.tv_left);
        tvRight = (TextView) v.findViewById(R.id.tv_right);
        mViewpager = (NoTouchScrollViewpager) v.findViewById(R.id.viewpager);
        mTvKchart = (TextView) v.findViewById(R.id.tv_kchart);
        vgOrder = v.findViewById(R.id.vg_order);
        vgSettlement = v.findViewById(R.id.vg_settlement);
        tvDirection = (TextView) v.findViewById(R.id.tv_direction);
        tvYueValue = (TextView) v.findViewById(R.id.tv_yue_value);
        mRvMarketHis = (RecyclerView) v.findViewById(R.id.rv_list);
        mTvTradeToast = (TextView) v.findViewById(R.id.tv_trade_toast);
        RecyclerView.ItemAnimator animator = mRvMarketHis.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mRvMarketHis.setLayoutManager(new LinearLayoutManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mMarketHisAdapter = new MarketHisAdapter(mHisList, quote.getTick());
        mMarketHisAdapter.bindToRecyclerView(mRvMarketHis);
        if (mIsDemo) {
            TextView tvYue = (TextView) v.findViewById(R.id.tv_yue);
            tvYue.setText("可用金币");
            tvYue.setVisibility(HttpConfig.IS_OPEN_TRADE ? View.VISIBLE : View.GONE);
            tvYueValue.setVisibility(HttpConfig.IS_OPEN_TRADE ? View.VISIBLE : View.GONE);
        }
        tvTotal = (TextView) v.findViewById(R.id.tv_total);
        colorView = v.findViewById(R.id.view_color);
        tvPrice = (TextView) v.findViewById(R.id.tv_price);
        tvChange = (TextView) v.findViewById(R.id.tv_change);
        tvChangeRate = (TextView) v.findViewById(R.id.tv_change_rate);

        tvOpen = (TextView) v.findViewById(R.id.tv_open);
        tvHigh = (TextView) v.findViewById(R.id.tv_high);
        tvLow = (TextView) v.findViewById(R.id.tv_low);
        tvVol = (TextView) v.findViewById(R.id.tv_vol);
        tvLastClose = (TextView) v.findViewById(R.id.tv_last_close);

        tvRest = (TextView) v.findViewById(R.id.tv_rest);
        marketDepthView = (MarketDepthView) v.findViewById(R.id.market_depth_view);
        TextView tvType = (TextView) v.findViewById(R.id.tv_type);
        tvType.setText(quote.getCurrency());
        v.findViewById(R.id.iv_more).setOnClickListener(this);
        v.findViewById(R.id.tv_pankou).setOnClickListener(this);

        View reset = v.findViewById(R.id.tv_reset);
        reset.setVisibility(mIsDemo && HttpConfig.IS_OPEN_TRADE ? View.VISIBLE : View.GONE);
        reset.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(getString(R.string.operaing));
        mProgressDialog.setCancelable(false);
    }

    @Override
    protected void initData() {
        super.initData();
        getHolding();
    }

    private void getHolding() {
//        HttpManager.getBizService(mIsDemo).getHolding()
        BizSocketUtils.getHolding(mIsDemo)
                .map(new Function<BizResponse<List<Holds>>, List<Holds>>() {
                    @Override
                    public List<Holds> apply(@NonNull BizResponse<List<Holds>> listBizResponse) throws Exception {
                        List<Holds> holdsList = listBizResponse.getResult();
                        Iterator<Holds> it = holdsList.iterator();
                        while (it.hasNext()) {
                            Holds holds = it.next();
                            if (!TextUtils.equals(holds.getSymbol(), mSymbol)) {
                                it.remove();
                            }
                        }
                        return holdsList;
                    }
                })
                .compose(RxUtils.<List<Holds>>applySchedulers())
                .compose(this.<List<Holds>>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Consumer<List<Holds>>() {
                    @Override
                    public void accept(@NonNull List<Holds> holdses) throws Exception {
                        mHoldsList = holdses;
                        if (!holdses.isEmpty()) {
                            Holds holding = holdses.get(0);
                            // 总手数
                            int sumQty = 0;
                            double sumUnrealizedPL = 0; // 总浮动盈亏
                            // 遍历将这些参数累加
                            for (Holds h : holdses) {
                                sumQty += Math.abs(h.getQty());
                                sumUnrealizedPL += h.getUnrealizedPL();
                            }
                            vgSettlement.setVisibility(View.GONE);
                            vgOrder.setVisibility(View.VISIBLE);
                            tvDirection.setText((TextUtils.equals(holding.getBuySell(), "买入") ? "看涨" : "看跌") + Math.abs(sumQty) + "手");
                            tvTotal.setText(TextUtils.concat(StringUtils.formatUnrealizePL(mContext, sumUnrealizedPL), "\n持仓盈亏"));
                            if (TextUtils.equals(holding.getBuySell(), "买入")) {
                                leftText = "追加";
                                rightText = "平仓";
                                tvDirection.setTextColor(ContextCompat.getColor(mContext, R.color.main_color_red));
                                colorView.setBackgroundResource(R.drawable.shape_online_tx_red);
                            } else {
                                leftText = "平仓";
                                rightText = "追加";
                                tvDirection.setTextColor(ContextCompat.getColor(mContext, R.color.main_color_green));
                                colorView.setBackgroundResource(R.drawable.shape_online_tx_green);
                            }
                        } else {
                            vgOrder.setVisibility(View.GONE);
                            vgSettlement.setVisibility(View.VISIBLE);
                            leftText = "看涨";
                            rightText = "看跌";
                        }
                        // 成功后刷新UI
                        Quote quote = StaticStore.getQuote(mSymbol, mIsDemo);
                        fillViews(quote);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        vgOrder.setVisibility(View.GONE);
                        vgSettlement.setVisibility(View.VISIBLE);
                        leftText = "看涨";
                        rightText = "看跌";
                    }
                });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PollRefreshEvent event) {
        if (getActivity() instanceof TradeActivity && ((TradeActivity) getActivity()).mIndex == 0) {
            getHolding();
            Funds result = StaticStore.getFunds(mIsDemo);
            tvYueValue.setText(getString(R.string.rmb_symbol) + DoubleUtil.format2Decimal(result.getAvailableFunds()));
            if (SocketUtils.getSocket() != null) {
                SocketUtils.getSocket().emit(SocketUtils.HIS_TICKS, mSymbol);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PriceRefreshEvent event) {
        if (TextUtils.equals(event.getSymbol(), mSymbol)) {
            Quote quote = StaticStore.getQuote(mSymbol, mIsDemo);
//            if (mHisList.size() > 20) {
//                mHisList.remove(0);
//            }
//            mHisList.add(new Quote(quote));
//            mMarketHisAdapter.notifyDataSetChanged();
//            mRvMarketHis.scrollToPosition(mHisList.size() - 1);
            fillViews(quote);
            setRestView(quote);
            Funds result = StaticStore.getFunds(mIsDemo);
            tvYueValue.setText(getString(R.string.rmb_symbol) + DoubleUtil.format2Decimal(result.getAvailableFunds()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChartTouchEvent event) {
        if (mScrollView != null) {
            mScrollView.requestDisallowInterceptTouchEvent(event.isStatues());
        }
    }

    @Override
    public void onClick(View v) {
        FastTakeOrderConfig fastTakeOrder = FastOrderSharePrefernce.getFastTakeOrder(mContext, mSymbol);
        Quote quote = StaticStore.getQuote(mSymbol, mIsDemo);
        if (quote == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.tv_close_order:
                mCloseAllDialog.show();
                break;
            case R.id.tv_left:
                if (quote.isRest()) {
                    ToastUtils.show(mContext, "休市中，暂时不能下单");
                    return;
                }
                if (TextUtils.equals("平仓", leftText)) {
                    if (fastTakeOrder != null) {
                        closeOrder();
                    } else {
                        mCloseDialog.show();
                    }
                } else {
                    if (fastTakeOrder != null) {
                        //快速下单
                        takeOrder(fastTakeOrder, "买入");
                    } else {
                        TakeOrderActivity.startActivity(mContext, mSymbol, TakeOrderActivity.TYPE_BUY, mIsDemo);
                    }
                }
                break;
            case R.id.tv_right:
                if (quote.isRest()) {
                    ToastUtils.show(mContext, "休市中，暂时不能下单");
                    return;
                }
                if (TextUtils.equals("平仓", rightText)) {
                    if (fastTakeOrder != null) {
                        closeOrder();
                    } else {
                        mCloseDialog.show();
                    }
                } else {
                    if (fastTakeOrder != null) {
                        //快速下单
                        takeOrder(fastTakeOrder, "卖出");
                    } else {
                        TakeOrderActivity.startActivity(mContext, mSymbol, TakeOrderActivity.TYPE_SELL, mIsDemo);
                    }
                }
                break;
            case R.id.tv_position:
                PositionActivity.startActivity(mContext, mIsDemo);
                break;
            case R.id.tv_deposit:
                if (mIsDemo) {
                    HttpManager.getBizService(true).resetCapitalAccount(BaseApplication.getInstance().getTradeToken(mIsDemo))
                            .compose(RxUtils.applyBizSchedulers())
                            .compose(this.<BizResponse>bindUntilEvent(FragmentEvent.DESTROY))
                            .subscribe(new Consumer<BizResponse>() {
                                @Override
                                public void accept(@NonNull BizResponse response) throws Exception {
                                    ToastUtils.show(mContext, response.getRmsg());
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable throwable) throws Exception {
                                    LogUtils.e(throwable);
                                    ToastUtils.show(mContext, throwable.getMessage());
                                }
                            });
                } else {
                    ActivityTools.toDeposit(mContext);
                }
                break;
            case R.id.iv_more:
            case R.id.tv_pankou:
                MarketDetailActivity.startActivity(mContext, mSymbol);
                break;
            case R.id.tv_kchart:
                mTopRightMenu.showAsDropDown(mTvKchart, 0, 0);
                break;
            case R.id.tv_reset:
                HttpManager.getBizService(true).resetSimualAccount()
                        .compose(RxUtils.applyBizSchedulers())
                        .compose(this.<BizResponse>bindUntilEvent(FragmentEvent.DESTROY))
                        .subscribe(new Consumer<BizResponse>() {
                            @Override
                            public void accept(@NonNull BizResponse response) throws Exception {
                                ToastUtils.show(mContext, response.getRmsg());
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                LogUtils.e(throwable);
                                ToastUtils.show(mContext, throwable.getMessage());
                            }
                        });
                break;
        }
    }

    private void closeOrder() {
        if (mHoldsList == null || mHoldsList.isEmpty()) return;
        mProgressDialog.show();
        RxUtils.createCloseObservable(mIsDemo, mHoldsList.get(0))
                .delay(1, TimeUnit.SECONDS)
                .compose(RxUtils.<CommonResponse>applySchedulers())
                .compose(this.<CommonResponse>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Consumer<CommonResponse>() {
                    @Override
                    public void accept(@NonNull CommonResponse response) throws Exception {
                        mCloseSuccessDialog.show();
                        mProgressDialog.dismiss();
                        EventBus.getDefault().post(new SendOrderEvent());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        LogUtils.e(throwable);
                        mProgressDialog.dismiss();
                        ToastUtils.show(mContext, throwable.getMessage());
                    }
                });
    }

    private void closeAllOrder() {
        if (mHoldsList == null || mHoldsList.isEmpty()) return;
        mProgressDialog.show();
        HttpManager.getBizService(mIsDemo).closeAllOrder(BaseApplication.getInstance().getTradeToken(mIsDemo), mSymbol)
                .delay(1, TimeUnit.SECONDS)
                .compose(RxUtils.<BizResponse>applyBizSchedulers())
                .compose(this.<BizResponse>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Consumer<BizResponse>() {
                    @Override
                    public void accept(@NonNull BizResponse response) throws Exception {
                        mCloseSuccessDialog.show();
                        mProgressDialog.dismiss();
                        EventBus.getDefault().post(new SendOrderEvent());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        LogUtils.e(throwable);
                        mProgressDialog.dismiss();
                        ToastUtils.show(mContext, throwable.getMessage());
                    }
                });
    }

    private void takeOrder(FastTakeOrderConfig order, final String type) {
        mProgressDialog.show();
        RxUtils.createSendOrderObservable(mIsDemo, mSymbol, type, Math.abs(order.getQty()),
                order.getStopLose(), order.getStopWin(), order.getFee(), order.getMarginYJ())
                .delay(1, TimeUnit.SECONDS)
                .compose(RxUtils.<BizResponse<CommonResponse>>applyBizSchedulers())
                .compose(this.<BizResponse<CommonResponse>>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Consumer<BizResponse<CommonResponse>>() {
                    @Override
                    public void accept(@NonNull BizResponse<CommonResponse> commonResponse) throws Exception {
                        DialogUtils.createTakeOrderSuccessDialog(mContext, type).show();
                        mProgressDialog.dismiss();
                        EventBus.getDefault().post(new SendOrderEvent());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        LogUtils.e(throwable);
                        mProgressDialog.dismiss();
                        ToastUtils.show(mContext, throwable.getMessage());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Quote quote = StaticStore.getQuote(mSymbol, mIsDemo);
        if (SocketUtils.getSocket() != null && quote != null) {
            SocketUtils.getSocket().off("topMarketDepth" + quote.getSort());
            SocketUtils.getSocket().off(SocketUtils.HIS_TICKS + quote.getSort());
        }
        if (BizSocketUtils.getSocket() != null) {
            BizSocketUtils.getSocket().off(BizSocketUtils.TRADE_RECORD);
        }
    }
}
