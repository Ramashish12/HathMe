package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityWalletBinding;

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

public class WalletActivity extends BaseActivity implements View.OnClickListener {

    ActivityWalletBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private boolean isExpanded = false;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {
        AppUtils.checkAppUpdate(mActivity);

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.wallet));

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        b.tvAddMoney.setOnClickListener(this);
        b.tvWithdraw.setOnClickListener(this);
//        b.tvSendReceiveMoney.setOnClickListener(this);

    }

    private void hitGetWalletDataApi() {

        WebServices.getApi(mActivity, AppUrls.transactionHistory, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetDetail(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseGetDetail(JSONObject response) {
        arrayList.clear();
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                b.tvBalance.setText(getString(R.string.rupeeSymbol) + " " + jsonObject.getJSONObject("data").getString("totalWalletBalance"));

                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("transactions");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("transactionId", jsonObject1.getString("transactionId"));
                    hashMap.put("amount", jsonObject1.getString("amount"));
                    hashMap.put("createdAt", jsonObject1.getString("createdAt"));
                    hashMap.put("type", jsonObject1.getString("type"));
                    hashMap.put("status", jsonObject1.getString("status"));
                    hashMap.put("action", jsonObject1.getString("action"));
                    hashMap.put("moreLessStatus", "0");

                    arrayList.add(hashMap);

                }

            } /*else
                AppUtils.showMessageDialog(mActivity, getString(R.string.wallet),
                        jsonObject.getString(AppConstants.resMsg), 2);*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
        arrayList.addAll(arrayList);
        arrayList.addAll(arrayList);

        arrayList.addAll(arrayList);

        arrayList.addAll(arrayList);

        arrayList.addAll(arrayList);


        adapter.notifyDataSetChanged();

        if (arrayList.size() == 0) {
            b.tvNoDataFound.setVisibility(View.VISIBLE);
        } else {
            b.tvNoDataFound.setVisibility(View.GONE);
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_wallet, viewGroup, false);
            return new MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {


            //status 0=Pending, 1=Approved, 2=Rejected
            holder.tvAmount.setText(getString(R.string.rupeeSymbol) + " " + data.get(position).get("amount"));
            holder.tvTime.setText(AppUtils.parseDateTime(data.get(position).get("createdAt")));
            String truncatedText = truncateLongText("#" + data.get(position).get("transactionId"), 8);
            holder.tvPaymentId.setText(truncatedText);
            holder.tvShowMoreLess.setText(getString(R.string.more));

            if (data.get(position).get("moreLessStatus").equals("0")) {
                truncatedText = truncateLongText("#" + data.get(position).get("transactionId"), 8);
                holder.tvPaymentId.setText(truncatedText);
                holder.tvShowMoreLess.setText(getString(R.string.more));
            } else {
                truncatedText = truncateLongText("#" + data.get(position).get("transactionId"), Integer.MAX_VALUE);
                holder.tvPaymentId.setText(truncatedText);
                holder.tvShowMoreLess.setText(getString(R.string.less));
            }

            holder.tvShowMoreLess.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (data.get(position).get("moreLessStatus").equals("0")) {

                        HashMap<String, String> hashMap = data.get(position);
                        hashMap.put("moreLessStatus", "1");
                        arrayList.set(position, hashMap);
                        notifyItemChanged(position);

                    } else {

                        HashMap<String, String> hashMap = data.get(position);
                        hashMap.put("moreLessStatus", "0");
                        arrayList.set(position, hashMap);
                        notifyItemChanged(position);
                    }

                    isExpanded = !isExpanded;
                }
            });
            switch (data.get(position).get("status")) {

                case "0":

                    holder.tvStatus.setText(getString(R.string.pending));
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case "1":

                    holder.tvStatus.setText(getString(R.string.approved));

                    if (data.get(position).get("action").equals("7")) {
                        holder.tvStatus.setText(getString(R.string.gpSold));

                    } else if (data.get(position).get("action").equals("3")) {
                        holder.tvStatus.setText(getString(R.string.refund));

                    }

                    holder.tvStatus.setTextColor(getResources().getColor(R.color.green));

                    break;
                case "2":

                    holder.tvStatus.setText(getString(R.string.rejected));
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.red));
                    break;

            }
            switch (data.get(position).get("type")) {

                case "1":

                    holder.tvType.setText(getString(R.string.credit));
                    break;
                case "2":

                    holder.tvType.setText(getString(R.string.debit));

            }

            switch (data.get(position).get("action")) {

                case "1":
                    holder.tvAction.setText(getString(R.string.walletTopUp));
                    break;
                case "2":
                    holder.tvAction.setText(getString(R.string.placeOrder));
                    break;
                case "3":
                    holder.tvAction.setText(getString(R.string.refundOrderAmount));
                    break;
                case "7":
                    holder.tvAction.setText(getString(R.string.greenPointRedeem));
                    break;
            }

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvPaymentId, tvTime, tvAmount, tvStatus, tvType, tvAction, tvShowMoreLess;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvPaymentId = itemView.findViewById(R.id.tvPaymentId);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvType = itemView.findViewById(R.id.tvType);
                tvAction = itemView.findViewById(R.id.tvAction);
                tvShowMoreLess = itemView.findViewById(R.id.tvShowMoreLess);
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvAddMoney:

                startActivity(new Intent(mActivity, DepositActivity.class));

                break;

            case R.id.tvWithdraw:

                startActivity(new Intent(mActivity, WithdrawalAmountActivity.class));

                break;

            case R.id.tvSendReceiveMoney:

                startActivity(new Intent(mActivity, SendReceiveMoneyActivity.class));

                break;

        }

    }

    private String truncateLongText(String text, int maxLength) {
        if (text.length() > maxLength) {
            // If the text is longer than maxLength, truncate it
            return text.substring(0, maxLength);
        } else {
            // If the text is already shorter, return the original text
            return text;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hitGetWalletDataApi();

    }
}