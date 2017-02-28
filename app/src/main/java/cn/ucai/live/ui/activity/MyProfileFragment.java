package cn.ucai.live.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.ucai.live.LiveHelper;
import cn.ucai.live.R;
import cn.ucai.live.data.model.LiveSettings;
import cn.ucai.live.utils.MFGT;

public class MyProfileFragment extends Fragment {
    Unbinder unbinder;
    //@BindView(R.id.spinner)
    //Spinner spinner;
    //@BindView(R.id.frame_rate)
    //TextView frameRateText;
    @BindView(R.id.tv_username)
    TextView usernameView;

    LiveSettings liveSettings;
    @BindView(R.id.iv_avatar)
    EaseImageView mIvAvatar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EaseUserUtils.setAppUserAvatar(getContext(), EMClient.getInstance().getCurrentUser(),mIvAvatar );
        EaseUserUtils.setUserNick(EMClient.getInstance().getCurrentUser(),usernameView);

        //liveSettings = new LiveSettings(getContext());
        //final String[] bitrateArr = getResources().getStringArray(R.array.bitrate_types);
        //String curBitrate = String.valueOf(liveSettings.getVideoEncodingBitRate());
        //for(int i = 0; i < bitrateArr.length; i++){
        //    if(curBitrate.equals(bitrateArr[i]))
        //        spinner.setSelection(i);
        //}
        //
        //spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //    @Override
        //    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //        liveSettings.setVideoEncodingBitRate(Integer.parseInt(bitrateArr[position]));
        //    }
        //
        //    @Override
        //    public void onNothingSelected(AdapterView<?> parent) {
        //
        //    }
        //});

    }

    @OnClick(R.id.btn_logout)
    void onLogout() {
        LiveHelper.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                getActivity().finish();
                MFGT.gotoLoginActivityClear(getActivity());
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }

            @Override
            public void onError(int i, String s) {

            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

    //@OnClick(R.id.frame_rate_container) void onFramrateClick(){
    //    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    //    final EditText editText = new EditText(getContext());
    //    editText.setText(frameRateText.getText());
    //    editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
    //    builder.setTitle("修改直播帧率").setView(editText)
    //            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
    //                @Override
    //                public void onClick(DialogInterface dialog, int which) {
    //                    frameRateText.setText(editText.getText());
    //                    liveSettings.setVideoFrameRate(Integer.parseInt(editText.getText().toString()));
    //                }
    //            })
    //            .setNegativeButton("取消", null)
    //            .show();
    //}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.iv_avatar)
    public void onClick() {
    }
}
