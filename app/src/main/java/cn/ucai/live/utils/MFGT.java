package cn.ucai.live.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;

import cn.ucai.live.I;
import cn.ucai.live.R;
import cn.ucai.live.ui.activity.LoginActivity;
import cn.ucai.live.ui.activity.MainActivity;
import cn.ucai.live.ui.activity.MyChangeActivity;


/**
 * Created by Administrator on 2017/1/10 0010.
 */

public class MFGT {
    public static void startActivity(Activity context, Class<?> cla) {
        context.startActivity(new Intent(context, cla));
        context.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public static void finish(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public static void startActivity(Activity context, Intent intent) {
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }



    public static void gotoMain(Activity activity) {
        startActivity(activity, new Intent(activity, MainActivity.class)
                .putExtra(I.BACK_MAIN_FROM_CHAT, 1));

    }
    public static void gotoLoginActivityClear(Activity activity) {
        startActivity(activity, new Intent(activity, LoginActivity.class).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    public static void gotoChange(Activity activity) {
        startActivity(activity,MyChangeActivity.class);
    }
}

