package code.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityAddressListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class AddressListActivity extends BaseActivity implements View.OnClickListener {

    private ActivityAddressListBinding b;

    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    Adapter adapter;
    String requestOrderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddressListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.addressList));
        if (getIntent().getExtras() != null && getIntent().hasExtra("requestOrderId")) {
            requestOrderId = getIntent().getStringExtra("requestOrderId");
        }
        b.tvAddAddress.setOnClickListener(this);

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        turnOnGps();
        AppUtils.checkAndRequestPermissions(mActivity);

        hitGetAddressListApi();

    }

    private void hitGetAddressListApi() {

        WebServices.getApi(mActivity, AppUrls.GetAddress, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAddressList(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAddressList(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();

                    hashMap.put("addressId", jsonObject1.getString("addressId"));
                    hashMap.put("areaName", jsonObject1.getString("areaName"));
                    hashMap.put("addressType", jsonObject1.getString("addressType"));
                    hashMap.put("fullAddress", jsonObject1.getString("fullAddress"));
                    hashMap.put("completeAddress", jsonObject1.getString("completeAddress"));
                    hashMap.put("latitude", jsonObject1.getString("latitude"));
                    hashMap.put("longitude", jsonObject1.getString("longitude"));
                    hashMap.put("floor", jsonObject1.getString("floor"));
                    hashMap.put("landmark", jsonObject1.getString("landmark"));
                    hashMap.put("status", jsonObject1.getString("status"));
                    hashMap.put("defaultAddress", jsonObject1.getString("defaultAddress"));
                    arrayList.add(hashMap);

                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.myAddresses),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_address, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            // holder.llMain.setVisibility(View.VISIBLE);

            if (data.get(position).get("defaultAddress").equals("1")) {
                holder.tvSetDefault.setVisibility(View.GONE);
            } else {
                holder.tvSetDefault.setVisibility(View.VISIBLE);

            }

            holder.llMain.setVisibility(View.VISIBLE);
            holder.tvAddressType.setVisibility(View.VISIBLE);
            holder.tvAddress.setVisibility(View.VISIBLE);
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);
            String completeAddress = data.get(position).get("completeAddress")
                    + " " + data.get(position).get("floor") + " " + data.get(position).get("landmark");
            holder.tvAddress.setText(completeAddress);

            holder.ivDelete.setOnClickListener(view -> hitDeleteAddressApi(data.get(position).get("addressId")));

            holder.ivEdit.setOnClickListener(view -> {

                Intent intent = new Intent(mActivity, AddressActivity.class);
                intent.putExtra("addressId", data.get(position).get("addressId"));
                intent.putExtra("areaName", data.get(position).get("areaName"));
                intent.putExtra("addressType", data.get(position).get("addressType"));
                intent.putExtra("fullAddress", data.get(position).get("fullAddress"));
                intent.putExtra("completeAddress", data.get(position).get("completeAddress"));
                intent.putExtra("latitude", data.get(position).get("latitude"));
                intent.putExtra("longitude", data.get(position).get("longitude"));
                intent.putExtra("floor", data.get(position).get("floor"));
                intent.putExtra("requestOrderId", requestOrderId);
                intent.putExtra("landmark", data.get(position).get("landmark"));
                intent.putExtra("type", getIntent().hasExtra("type") ? "3" : "2");
                startActivity(intent);

            });

            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppSettings.getString(AppSettings.isFromBucket).equalsIgnoreCase("1")) {
                        AppSettings.putString(AppSettings.isFromBucket, "0");
                        Intent intent = new Intent(mActivity, BucketCartActivity.class);
                        intent.putExtra("addressId", data.get(position).get("addressId"));
                        intent.putExtra("requestOrderId", requestOrderId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransitionExit();
                        finish();
                    } else {
                        if (AppSettings.getString(AppSettings.isFromPage).equalsIgnoreCase("Profile")) {
                        } else {

                            hitSetDefaultApi(data.get(position).get("addressId"),"2");

                            /*Intent intent = new Intent(mActivity, CartActivity.class);
                            intent.putExtra("addressId", data.get(position).get("addressId"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransitionExit();
                            finish();*/
                        }
                    }

                }
            });

            switch (data.get(position).get("addressType")) {

                case "1":

                    holder.tvAddressType.setText(getString(R.string.home));

                    break;

                case "2":

                    holder.tvAddressType.setText(getString(R.string.work));

                    break;

                case "3":

                    holder.tvAddressType.setText(getString(R.string.hotel));
                    break;

                case "4":

                    holder.tvAddressType.setText(getString(R.string.other));
                    break;

            }

            holder.tvSetDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hitSetDefaultApi(data.get(position).get("addressId"),"1");
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvAddressType, tvAddress, tvSetDefault;

            ImageView ivEdit, ivDelete;

            LinearLayout llMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvAddressType = itemView.findViewById(R.id.tvAddressType);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvSetDefault = itemView.findViewById(R.id.tvSetDefault);

                ivEdit = itemView.findViewById(R.id.ivEdit);
                ivDelete = itemView.findViewById(R.id.ivDelete);

                llMain = itemView.findViewById(R.id.llMain);
            }
        }
    }

    private void hitSetDefaultApi(String addressId, String type) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("addressId", addressId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.defaultAddress, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDefaultAddressJson(response, type);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseDefaultAddressJson(JSONObject response, String type) {
        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                if (type.equals("1")) {

                    hitGetAddressListApi();
                } else {
                    Intent intent = new Intent(mActivity, CartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransitionExit();
                    finish();
                }
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.friends),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void hitDeleteAddressApi(String addressId) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("addressId", addressId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.deleteApi(mActivity, AppUrls.deleteAddress, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDeleteAddressJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseDeleteAddressJson(JSONObject response) {


        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetAddressListApi();
            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.friends),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvAddAddress:

                startActivity(new Intent(mActivity, AddressActivity.class).putExtra("type", "1").putExtra("requestOrderId", requestOrderId));

                break;

        }

    }

    private void turnOnGps() {

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(this, (LocationSettingsResponse response) -> {
                    // startUpdatingLocation(...);
                })
                .addOnFailureListener(this, ex -> {
                    if (ex instanceof ResolvableApiException) {
                        // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) ex;
                            resolvable.startResolutionForResult(mActivity, 2);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }

}


