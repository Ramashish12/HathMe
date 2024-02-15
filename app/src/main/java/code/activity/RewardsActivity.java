package code.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anupkumarpanwar.scratchview.ScratchView;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityRewardsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class RewardsActivity extends BaseActivity implements View.OnClickListener {

    private ActivityRewardsBinding b;

    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRewardsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    @SuppressLint("SetTextI18n")
    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());

        b.tvPoints.setText(getString(R.string.points)+" 0");

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetScratchCardApi();
    }

    private void hitGetScratchCardApi() {

        WebServices.getApi(mActivity, AppUrls.getAllScratchCard, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseScratchListJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseScratchListJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                b.tvPoints.setText(getString(R.string.points) + " " + jsonObject.getJSONObject("data").getString("totalPoints"));

                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("scratchCards");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("scratchCardId", jsonObject1.getString("_id"));
                    hashMap.put("amount", jsonObject1.getString("amount"));
                    hashMap.put("status", jsonObject1.getString("status"));

                    arrayList.add(hashMap);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        public Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_rewards, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {


            if (data.get(position).get("status").equals("1")) {

                holder.ivImage.setVisibility(View.GONE);
                holder.rlMain.setVisibility(View.VISIBLE);

                holder.tvAmount.setText(AppUtils.roundOff2Digit(data.get(position).get("amount")));

            } else {
                holder.ivImage.setVisibility(View.VISIBLE);
                holder.rlMain.setVisibility(View.GONE);
            }

            holder.ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showScratchDialog(data.get(position));

                }
            });

        }

        @Override
        public int getItemCount() {

            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView ivImage;

            RelativeLayout rlMain;

            TextView tvAmount;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);

                tvAmount = itemView.findViewById(R.id.tvAmount);

                rlMain = itemView.findViewById(R.id.rlMain);
            }
        }
    }

    private void showScratchDialog(HashMap<String, String> hashMap) {

        Dialog dialog = new Dialog(mActivity);
        dialog.setContentView(R.layout.dialog_rewards);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ScratchView scratchView = dialog.findViewById(R.id.scratchView);
        TextView tvAmount = dialog.findViewById(R.id.tvAmount);
        TextView tvType = dialog.findViewById(R.id.tvType);
        TextView tvClaimed = dialog.findViewById(R.id.tvClaimed);

        tvClaimed.setOnClickListener(v -> dialog.dismiss());

        tvAmount.setText(AppUtils.roundOff2Digit(hashMap.get("amount")));

        tvType.setText(getString(R.string.unlocked));

        scratchView.setRevealListener(new ScratchView.IRevealListener() {
            @Override
            public void onRevealed(ScratchView scratchView) {

                scratchView.reveal();
                hitUseScratchApi(hashMap.get("scratchCardId"));
                new Handler(Looper.myLooper()).postDelayed(dialog::dismiss, 1500);

            }

            @Override
            public void onRevealPercentChangedListener(ScratchView scratchView, float percent) {

            }
        });
    }

    private void hitUseScratchApi(String id) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("scratchCardId", id);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.cardScratch, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseUSeScratchJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseUSeScratchJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetScratchCardApi();

            } else
                AppUtils.showMessageDialog(mActivity, mActivity.getString(R.string.rewards), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {

    }
}