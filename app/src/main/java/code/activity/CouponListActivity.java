package code.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityCouponListBinding;

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

public class CouponListActivity extends BaseActivity implements View.OnClickListener {

    ActivityCouponListBinding b;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    Adapter adapter;
    double amount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCouponListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.couponList));
        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);
        amount = Double.parseDouble(getIntent().getStringExtra("totalAmount"));
        hitGetCouponListApi();
    }

    private void hitGetCouponListApi() {

        WebServices.getApi(mActivity, AppUrls.getCoupon, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCoupon(response);

            }

            @Override
            public void OnFail(String response) {
             AppUtils.showToastSort(mActivity,response);
            }
        });
    }

    private void parseCoupon(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("couponCode", jsonObject1.getString("couponCode"));
                    hashMap.put("amount", jsonObject1.getString("amount"));
                    hashMap.put("type", jsonObject1.getString("type"));
                    hashMap.put("minPriceForApplyCoupon", jsonObject1.getString("minPriceForApplyCoupon"));
                    hashMap.put("maxDiscount", jsonObject1.getString("maxDiscount"));

                    arrayList.add(hashMap);
                }

            }/* else
                AppUtils.showMessageDialog(mActivity, getString(R.string.couponList),
                        jsonObject.getString(AppConstants.resMsg), 2);*/

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View view) {

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_coupon, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {


            holder.tvCouponCode.setText(data.get(position).get("couponCode"));

            //Percentage
            if (data.get(position).get("type").equals("1")) {
                holder.tvDiscountValue.setText(getString(R.string.Get) + " " + data.get(position).get("amount") + "% " + getString(R.string.off));

                holder.tvDescription.setText(getString(R.string.useCode) + " " + data.get(position).get("couponCode") + " & " +
                        getString(R.string.get) + " " + data.get(position).get("amount") + "% " + getString(R.string.off) + " " +
                        getString(R.string.onOrdersAbove) + " " + getString(R.string.rupeeSymbol) +
                        data.get(position).get("minPriceForApplyCoupon") + ". " + getString(R.string.maxDiscount) + " " +
                        getString(R.string.rupeeSymbol) + data.get(position).get("maxDiscount"));
            }
            //Flat
            else {
                holder.tvDiscountValue.setText(getString(R.string.Get) + " " + getString(R.string.rupeeSymbol) +
                        data.get(position).get("amount") + " " + getString(R.string.discount));

                holder.tvDescription.setText(getString(R.string.useCode) + " " + data.get(position).get("couponCode") + " & " +
                        getString(R.string.get) + " " + getString(R.string.rupeeSymbol) + data.get(position).get("amount")
                        + getString(R.string.discount) + " " +
                        getString(R.string.onOrdersAbove) + " " + getString(R.string.rupeeSymbol) +
                        data.get(position).get("minPriceForApplyCoupon") + ". " + getString(R.string.maxDiscount) + " " +
                        getString(R.string.rupeeSymbol) + data.get(position).get("maxDiscount"));
            }

            holder.tvApply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (amount>=AppUtils.returnDouble(data.get(position).get("minPriceForApplyCoupon")))
                    {
                        hitApplyCouponApi(data.get(position).get("couponCode"));
                    }
                    else
                    {
                      AppUtils.showMessageDialog(mActivity,getString(R.string.app_name),getString(R.string.couponApplyMsg),5);
                    }


                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvCouponCode, tvApply, tvDiscountValue, tvDescription;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvCouponCode = itemView.findViewById(R.id.tvCouponCode);
                tvApply = itemView.findViewById(R.id.tvApply);
                tvDiscountValue = itemView.findViewById(R.id.tvDiscountValue);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }
        }
    }

    private void hitApplyCouponApi(String id) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("couponCode", id);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.applyCoupon, json, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseApplyCoupon(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseApplyCoupon(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppUtils.showToastSort(mActivity, getString(R.string.couponAppliedSuucessfully));
                onBackPressed();

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}