package cn.ucai.live.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.ucai.live.LiveHelper;
import cn.ucai.live.R;
import cn.ucai.live.data.model.Gift;

/**
 * Created by wei on 2016/7/25.
 */
public class RoomGiftListDialog extends DialogFragment {

    Unbinder unbinder;
    @BindView(R.id.rv_gift)
    RecyclerView mRvGift;

    private String username;
    GridLayoutManager mManager;
    List<Gift> mGiftList;
    GiftAdapter mAdapter;
    public static RoomGiftListDialog newInstance() {
        RoomGiftListDialog dialog = new RoomGiftListDialog();
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_gift_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mManager = new GridLayoutManager(getContext(), 4);
        mRvGift.setLayoutManager(mManager);
        initData();
        mAdapter = new GiftAdapter(getContext(), mGiftList);
        mRvGift.setAdapter(mAdapter);

    }
    private void initData() {
        mGiftList = new ArrayList<>();
        Map<Integer, Gift> map = LiveHelper.getInstance().getAppGiftList();
        Iterator<Map.Entry<Integer, Gift>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            mGiftList.add(iterator.next().getValue());
        }
        Collections.sort(mGiftList, new Comparator<Gift>() {
            @Override
            public int compare(Gift lhs, Gift rhs) {
                return lhs.getId()-rhs.getId();
            }
        });
    }

    private View.OnClickListener mListener;

    public void setGiftOnClickListener(View.OnClickListener dialogListener) {
        this.mListener = dialogListener;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 使用不带theme的构造器，获得的dialog边框距离屏幕仍有几毫米的缝隙。
        // Dialog dialog = new Dialog(getActivity());
        Dialog dialog = new Dialog(getActivity(), R.style.room_user_details_dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // must be called before set content
        dialog.setContentView(R.layout.fragment_room_user_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);

        // 设置宽度为屏宽、靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
    class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {
        Context mContext;
        List<Gift> mGiftList;

        public GiftAdapter(Context context, List<Gift> giftList) {
            mContext = context;
            mGiftList = giftList;
        }

        @Override
        public GiftViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_gift, null);
            return new GiftViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GiftViewHolder holder, int position) {
            Gift gift = mGiftList.get(position);
            holder.mTvGiftDes.setText(gift.getGname());
            holder.mTvGiftPrice.setText(String.valueOf(gift.getGprice()));
            holder.mLayout.setTag(gift.getId());
            EaseUserUtils.setAppUserAvatarByPath(getContext(), gift.getGurl(), holder.mIvGiftIcon, "cn.ucai.live.gift");
        }

        @Override
        public int getItemCount() {
            return mGiftList == null ? 0 : mGiftList.size();
        }

        class GiftViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.iv_gift_icon)
            ImageView mIvGiftIcon;
            @BindView(R.id.tv_gift_des)
            TextView mTvGiftDes;
            @BindView(R.id.tv_gift_price)
            TextView mTvGiftPrice;
            @BindView(R.id.ll_gift)
            LinearLayout mLayout;

            GiftViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                mLayout.setOnClickListener(mListener);
            }
        }
    }
}
