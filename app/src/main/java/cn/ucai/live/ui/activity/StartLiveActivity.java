package cn.ucai.live.ui.activity;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.live.data.model.LiveRoom;
import cn.ucai.live.data.model.LiveSettings;
import cn.ucai.live.utils.Log2FileUtil;

import cn.ucai.live.R;
import cn.ucai.live.data.TestDataRepository;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.ucloud.common.util.DeviceUtils;
import com.ucloud.live.UEasyStreaming;
import com.ucloud.live.UStreamingProfile;
import com.ucloud.live.widget.UAspectFrameLayout;
import java.util.List;
import java.util.Random;

public class StartLiveActivity extends LiveBaseActivity
    implements UEasyStreaming.UStreamingStateListener {
  private static final String TAG = StartLiveActivity.class.getSimpleName();
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.container) UAspectFrameLayout mPreviewContainer;
  @BindView(R.id.start_container) RelativeLayout startContainer;
  @BindView(R.id.countdown_txtv) TextView countdownView;
  @BindView(R.id.tv_username) TextView usernameView;
  @BindView(R.id.btn_start) Button startBtn;
  @BindView(R.id.finish_frame) ViewStub liveEndLayout;
  @BindView(R.id.cover_image) ImageView coverImage;
  @BindView(R.id.img_bt_switch_light) ImageButton lightSwitch;
  @BindView(R.id.img_bt_switch_voice) ImageButton voiceSwitch;

  protected UEasyStreaming mEasyStreaming;
  protected String rtmpPushStreamDomain = "publish3.cdn.ucloud.com.cn";
  public static final int MSG_UPDATE_COUNTDOWN = 1;

  public static final int COUNTDOWN_DELAY = 1000;

  public static final int COUNTDOWN_START_INDEX = 3;
  public static final int COUNTDOWN_END_INDEX = 1;
  protected boolean isShutDownCountdown = false;
  private LiveSettings mSettings;
  private UStreamingProfile mStreamingProfile;
  UEasyStreaming.UEncodingType encodingType;

  boolean isStarted;

  private Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_UPDATE_COUNTDOWN:
          handleUpdateCountdown(msg.arg1);
          break;
      }
    }
  };

  //203138620012364216
  @Override protected void onActivityCreate(@Nullable Bundle savedInstanceState) {
    setContentView(R.layout.activity_start_live);
    ButterKnife.bind(this);

    liveId = TestDataRepository.getLiveRoomId(EMClient.getInstance().getCurrentUser());
    chatroomId = TestDataRepository.getChatRoomId(EMClient.getInstance().getCurrentUser());
    anchorId = EMClient.getInstance().getCurrentUser();
    usernameView.setText(anchorId);
    initEnv();
  }

  public void initEnv() {
    mSettings = new LiveSettings(this);
    if (mSettings.isOpenLogRecoder()) {
      Log2FileUtil.getInstance().setLogCacheDir(mSettings.getLogCacheDir());
      Log2FileUtil.getInstance().startLog(); //
    }

    //        UStreamingProfile.Stream stream = new UStreamingProfile.Stream(rtmpPushStreamDomain, "ucloud/" + mSettings.getPusblishStreamId());
    //hardcode
    UStreamingProfile.Stream stream =
        new UStreamingProfile.Stream(rtmpPushStreamDomain, "ucloud/" + liveId);

    mStreamingProfile =
        new UStreamingProfile.Builder().setVideoCaptureWidth(mSettings.getVideoCaptureWidth())
            .setVideoCaptureHeight(mSettings.getVideoCaptureHeight())
            .setVideoEncodingBitrate(
                mSettings.getVideoEncodingBitRate()) //UStreamingProfile.VIDEO_BITRATE_NORMAL
            .setVideoEncodingFrameRate(mSettings.getVideoFrameRate())
            .setStream(stream)
            .build();

    encodingType = UEasyStreaming.UEncodingType.MEDIA_X264;
    if (DeviceUtils.hasJellyBeanMr2()) {
      encodingType = UEasyStreaming.UEncodingType.MEDIA_CODEC;
    }
    mEasyStreaming = new UEasyStreaming(this, encodingType);
    mEasyStreaming.setStreamingStateListener(this);
    mEasyStreaming.setAspectWithStreamingProfile(mPreviewContainer, mStreamingProfile);
  }

  @Override public void onStateChanged(int type, Object event) {
    switch (type) {
      case UEasyStreaming.State.MEDIA_INFO_SIGNATRUE_FAILED:
        Toast.makeText(this, event.toString(), Toast.LENGTH_LONG).show();
        break;
      case UEasyStreaming.State.START_RECORDING:
        new Thread(new Runnable() {
          @Override public void run() {
            while (!isFinishing()) {
              runOnUiThread(new Runnable() {
                @Override public void run() {
                  periscopeLayout.addHeart();
                }
              });
              try {
                Thread.sleep(new Random().nextInt(400) + 200);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          }
        }).start();
        break;
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }

  @Override public void onBackPressed() {
    mEasyStreaming.stopRecording();
    super.onBackPressed();
  }

  /**
   * 切换摄像头
   */
  @OnClick(R.id.img_bt_switch_camera) void switchCamera() {
    mEasyStreaming.switchCamera();
  }

  /**
   * 开始直播
   */
  @OnClick(R.id.btn_start) void startLive() {
    //demo为了测试方便，只有指定的账号才能开启直播
    if (liveId == null) {
      String[] anchorIds = TestDataRepository.anchorIds;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < anchorIds.length; i++) {
        sb.append(anchorIds[i]);
        if (i != (anchorIds.length - 1)) sb.append(",");
      }
      new EaseAlertDialog(this, "demo中只有" + sb.toString() + "这几个账户才能开启直播").show();
      return;
    }

    startContainer.setVisibility(View.INVISIBLE);
    //Utils.hideKeyboard(titleEdit);
    new Thread() {
      public void run() {
        int i = COUNTDOWN_START_INDEX;
        do {
          Message msg = Message.obtain();
          msg.what = MSG_UPDATE_COUNTDOWN;
          msg.arg1 = i;
          handler.sendMessage(msg);
          i--;
          try {
            Thread.sleep(COUNTDOWN_DELAY);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } while (i >= COUNTDOWN_END_INDEX);
      }
    }.start();
  }

  /**
   * 关闭直播显示直播成果
   */
  @OnClick(R.id.img_bt_close) void closeLive() {
    mEasyStreaming.stopRecording();
    if (!isStarted) {
      finish();
      return;
    }
    showConfirmCloseLayout();
  }

  @OnClick(R.id.img_bt_switch_voice) void toggleMicrophone(){
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    if(audioManager.isMicrophoneMute()){
      audioManager.setMicrophoneMute(false);
      voiceSwitch.setSelected(false);
    }else{
      audioManager.setMicrophoneMute(true);
      voiceSwitch.setSelected(true);
    }
  }

  private void showConfirmCloseLayout() {
    //显示封面
    coverImage.setVisibility(View.VISIBLE);
    List<LiveRoom> liveRoomList = TestDataRepository.getLiveRoomList();
    for (LiveRoom liveRoom : liveRoomList) {
      if (liveRoom.getId().equals(liveId)) {
        coverImage.setImageResource(liveRoom.getCover());
      }
    }
    View view = liveEndLayout.inflate();
    Button closeConfirmBtn = (Button) view.findViewById(R.id.live_close_confirm);
    TextView usernameView = (TextView) view.findViewById(R.id.tv_username);
    usernameView.setText(EMClient.getInstance().getCurrentUser());
    closeConfirmBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        finish();
      }
    });
  }

  private void confirmClose() {

  }

  /**
   * 打开或关闭闪关灯
   */
  @OnClick(R.id.img_bt_switch_light) void switchLight() {
    boolean succeed = mEasyStreaming.toggleFlashMode();
    if(succeed){
      if(lightSwitch.isSelected()){
        lightSwitch.setSelected(false);
      }else{
        lightSwitch.setSelected(true);
      }
    }
  }

  @Override void onChatImageClick() {
    ConversationListFragment fragment = ConversationListFragment.newInstance(null, false);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.message_container, fragment)
        .commit();
  }

  protected void setListItemClickListener() {
  }

  public void handleUpdateCountdown(final int count) {
    if (countdownView != null) {
      countdownView.setVisibility(View.VISIBLE);
      countdownView.setText(String.format("%d", count));
      ScaleAnimation scaleAnimation =
          new ScaleAnimation(1.0f, 0f, 1.0f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
              Animation.RELATIVE_TO_SELF, 0.5f);
      scaleAnimation.setDuration(COUNTDOWN_DELAY);
      scaleAnimation.setFillAfter(false);
      scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
        @Override public void onAnimationStart(Animation animation) {
        }

        @Override public void onAnimationEnd(Animation animation) {
          countdownView.setVisibility(View.GONE);
          EMClient.getInstance()
              .chatroomManager()
              .joinChatRoom(chatroomId, new EMValueCallBack<EMChatRoom>() {
                @Override public void onSuccess(EMChatRoom emChatRoom) {
                  chatroom = emChatRoom;
                  addChatRoomChangeListenr();
                  onMessageListInit();
                }

                @Override public void onError(int i, String s) {
                  showToast("加入聊天室失败");
                }
              });

          if (count == COUNTDOWN_END_INDEX && mEasyStreaming != null && !isShutDownCountdown) {
            showToast("直播开始！");
            mEasyStreaming.startRecording();
            isStarted = true;
          }
        }

        @Override public void onAnimationRepeat(Animation animation) {

        }
      });
      if (!isShutDownCountdown) {
        countdownView.startAnimation(scaleAnimation);
      } else {
        countdownView.setVisibility(View.GONE);
      }
    }
  }

  @Override protected void onPause() {
    super.onPause();
    mEasyStreaming.onPause();
  }

  @Override protected void onResume() {
    super.onResume();
    mEasyStreaming.onResume();
    if (isMessageListInited) messageView.refresh();
    EaseUI.getInstance().pushActivity(this);
    // register the event listener when enter the foreground
    EMClient.getInstance().chatManager().addMessageListener(msgListener);
  }

  @Override public void onStop() {
    super.onStop();
    // unregister this event listener when this activity enters the
    // background
    EMClient.getInstance().chatManager().removeMessageListener(msgListener);

    // 把此activity 从foreground activity 列表里移除
    EaseUI.getInstance().popActivity(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (mSettings.isOpenLogRecoder()) {
      Log2FileUtil.getInstance().stopLog();
    }
    mEasyStreaming.onDestroy();

    EMClient.getInstance().chatroomManager().leaveChatRoom(chatroomId);

    if (chatRoomChangeListener != null) {
      EMClient.getInstance().chatroomManager().removeChatRoomChangeListener(chatRoomChangeListener);
    }
  }
}
