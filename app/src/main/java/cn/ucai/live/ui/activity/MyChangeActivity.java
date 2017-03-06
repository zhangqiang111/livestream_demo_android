package cn.ucai.live.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.ucai.live.R;
import cn.ucai.live.bean.Result;
import cn.ucai.live.data.model.Wallet;
import cn.ucai.live.net.NetDao;
import cn.ucai.live.net.OnCompleteListener;
import cn.ucai.live.utils.PreferenceManager;
import cn.ucai.live.utils.ResultUtils;

/**
 * Created by Administrator on 2017/3/6 0006.
 */

public class MyChangeActivity extends BaseActivity {
    int change;
    @BindView(R.id.tv_change_balance)
    TextView mTvChangeBalance;
    View loadView;
    @BindView(R.id.target_layout)
    LinearLayout mTargetLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rp_fragment_change);
        ButterKnife.bind(this);
        loadView = LayoutInflater.from(this).inflate(R.layout.rp_loading, mTargetLayout, false);
        mTargetLayout.addView(loadView);
        initData();
    }

    private void initData() {
        NetDao.loadChange(this, EMClient.getInstance().getCurrentUser(), new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e(">>>>", s.toString());
                boolean success = false;
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, Wallet.class);
                    if (result != null && result.isRetMsg()) {
                        //保存用户信息到数据库
                        Wallet wallet = (Wallet) result.getRetData();
                        if (wallet != null) {
                            success = true;
                            PreferenceManager.getInstance().setUserChange(wallet.getBalance());
                            change = wallet.getBalance();
                            setChange();
                        }
                    }
                }
                if (!success) {
                    PreferenceManager.getInstance().setUserChange(0);
                }
                loadView.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                loadView.setVisibility(View.GONE);
                PreferenceManager.getInstance().setUserChange(0);
            }
        });
    }

    private void setChange() {
        change = PreferenceManager.getInstance().getUserChange();
        mTvChangeBalance.setText("￥" + Float.valueOf(String.valueOf(change)));
    }
}
