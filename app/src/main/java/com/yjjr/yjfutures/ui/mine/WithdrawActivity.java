package com.yjjr.yjfutures.ui.mine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trello.rxlifecycle2.android.ActivityEvent;
import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.event.FinishEvent;
import com.yjjr.yjfutures.event.PollRefreshEvent;
import com.yjjr.yjfutures.model.biz.BizResponse;
import com.yjjr.yjfutures.model.biz.Funds;
import com.yjjr.yjfutures.store.StaticStore;
import com.yjjr.yjfutures.ui.BaseActivity;
import com.yjjr.yjfutures.utils.DoubleUtil;
import com.yjjr.yjfutures.utils.LogUtils;
import com.yjjr.yjfutures.utils.RxUtils;
import com.yjjr.yjfutures.utils.SpannableUtil;
import com.yjjr.yjfutures.utils.ToastUtils;
import com.yjjr.yjfutures.utils.http.HttpManager;
import com.yjjr.yjfutures.widget.HeaderView;
import com.yjjr.yjfutures.widget.RegisterInput;
import com.yjjr.yjfutures.widget.listener.TextWatcherAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.functions.Consumer;

public class WithdrawActivity extends BaseActivity {

    private Button mBtnConfirm;
    private TextView mTvYue;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, WithdrawActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        HeaderView headerView = (HeaderView) findViewById(R.id.header_view);
        mTvYue = (TextView) findViewById(R.id.tv_yue);
        headerView.bindActivity(mContext);
        final RegisterInput riMoney = (RegisterInput) findViewById(R.id.ri_money);
        EditText etMoney = riMoney.getEtInput();
        etMoney.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etMoney.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                Funds funds = StaticStore.getFunds(false);
                mBtnConfirm.setSelected(!TextUtils.isEmpty(s) && Double.parseDouble(s.toString()) != 0 && funds.getAvailableFunds() >= Double.parseDouble(s.toString()));
            }
        });
        mBtnConfirm = (Button) findViewById(R.id.btn_confirm);
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBtnConfirm.isSelected()) {
                    HttpManager.getBizService().whetherApply()
                            .compose(RxUtils.applyBizSchedulers())
                            .compose(mContext.<BizResponse>bindUntilEvent(ActivityEvent.DESTROY))
                            .subscribe(new Consumer<BizResponse>() {
                                @Override
                                public void accept(BizResponse bizResponse) throws Exception {
                                    InputPayPwdActivity.startActivity(mContext, riMoney.getValue());
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    LogUtils.e(throwable);
                                    ToastUtils.show(mContext, throwable.getMessage());
                                }
                            });
                }
            }
        });


        TextView tvInfo = (TextView) findViewById(R.id.tv_info);
        tvInfo.setText(TextUtils.concat("每日提现次数限",
                SpannableUtil.getStringByColor(mContext, "1次", R.color.third_text_color),
                "\n提现处理时间为",
                SpannableUtil.getStringByColor(mContext, "工作日8:30-17:00", R.color.third_text_color),
                "\n到账时间以银行为准，节假日延后处理"));

        headerView.setOperateClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CashRecordActivity.startActivity(mContext, CashRecordActivity.WITHDRAW);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FinishEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PollRefreshEvent event) {
        Funds funds = StaticStore.getFunds(false);
        mTvYue.setText(getString(R.string.rmb_symbol) + DoubleUtil.format2Decimal(funds.getAvailableFunds()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
