package code.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityBankListBinding;

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

public class BankListActivity extends BaseActivity implements View.OnClickListener {

    ActivityBankListBinding b;

    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    private Adapter adapter;

    private String pageFrom="", bankId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityBankListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.tvHeader.setText(getString(R.string.bankList));
        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.fabAdd.setOnClickListener(this);
        b.tvWithdraw.setOnClickListener(this);

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

       // pageFrom = getIntent().getStringExtra("pageFrom");
        pageFrom= "2";

    }

    private void hitGetBankListApi() {

        WebServices.getApi(mActivity, AppUrls.BankList, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseBankList(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }
    private void parseBankList(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonObject1.getString("_id"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("bankName", jsonObject1.getString("bankName"));
                    hashMap.put("accountNumber", jsonObject1.getString("accountNumber"));
                    hashMap.put("ifsc", jsonObject1.getString("ifsc"));
                    hashMap.put("accountType", jsonObject1.getString("accountType"));
                    hashMap.put("status","0");

                    arrayList.add(hashMap);
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.bankList),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.fabAdd:

                startActivity(new Intent(mActivity, AddBankActivity.class));

                break;

            case R.id.tvWithdraw:


                if (AppSettings.getString(AppSettings.isWithdrawPinCreated).equals("0")) {

                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseCreateWithdrawPin));
                    startActivity(new Intent(mActivity, CreateWithdrawalPinActivity.class).putExtra("pageFrom","1"));

                } else {

                    Intent intent = new Intent(mActivity, WithdrawalPinActivity.class);
                    intent.putExtra("pageFrom", "2");
                    intent.putExtra("bankId", bankId);
                    intent.putExtra("amount", getIntent().getStringExtra("amount"));
                    startActivity(intent);
                }

                break;
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> data;


        private Adapter(ArrayList<HashMap<String, String>> arrayList) {

            data = arrayList;
        }

        @NonNull
        @Override
        public Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_bank, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            if (data.get(position).get("status").equals("0")){
                holder.llMain.setBackgroundResource(0);
            }
            else{
                holder.llMain.setBackgroundResource(R.drawable.rectangular_border_color);
            }

            holder.tvBankName.setText(data.get(position).get("bankName"));
            holder.tvName.setText(data.get(position).get("name"));
            holder.tvAccountNumber.setText(data.get(position).get("accountNumber"));

            holder.ivDelete.setOnClickListener(view -> {
                showAlert(data.get(position).get("_id"));

            });


            holder.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, AddBankActivity.class);
                    intent.putExtra("_id", data.get(position).get("_id"));
                    intent.putExtra("name", data.get(position).get("name"));
                    intent.putExtra("bankName", data.get(position).get("bankName"));
                    intent.putExtra("accountNumber", data.get(position).get("accountNumber"));
                    intent.putExtra("ifsc", data.get(position).get("ifsc"));
                    intent.putExtra("accountType", data.get(position).get("accountType"));
                    startActivity(intent);
                }
            });

            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (pageFrom.equals("2")){

                        for (int i = 0; i <data.size() ; i++) {

                            HashMap<String, String> hashMap = data.get(i);
                            hashMap.put("status","0");
                            data.set(i, hashMap);

                        }

                        HashMap<String, String> hashMap = data.get(position);
                        hashMap.put("status","1");
                        data.set(position, hashMap);

                        notifyDataSetChanged();


                        bankId = data.get(position).get("_id");
                        b.tvWithdraw.setVisibility(View.VISIBLE);
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvAccountNumber, tvBankName;

            ImageView ivEdit, ivDelete;

            LinearLayout llMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);
                tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
                tvBankName = itemView.findViewById(R.id.tvBankName);

                ivDelete = itemView.findViewById(R.id.ivDelete);
                ivEdit = itemView.findViewById(R.id.ivEdit);

                llMain = itemView.findViewById(R.id.llMain);
            }
        }
    }

    private void showAlert(String id) {
        new AlertDialog.Builder(mActivity)
                .setTitle(getString(R.string.deleteBank))
                .setMessage(getString(R.string.areYouSure))

                .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                    hitDeleteBankApi(id);

                })


                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void hitDeleteBankApi(String id) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("bankId", id);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.DeleteBank, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseDeleteJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseDeleteJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitGetBankListApi();

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hitGetBankListApi();

    }
}