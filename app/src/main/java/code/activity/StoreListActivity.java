package code.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.recyclerview.widget.GridLayoutManager;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityStoreListBinding;
import com.zego.ve.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import code.adapters.AdapterStore;
import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class StoreListActivity extends BaseActivity implements View.OnClickListener {

    private ActivityStoreListBinding b;

    private AdapterStore adapter;

    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityStoreListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    @SuppressLint("SetTextI18n")
    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));

        adapter = new AdapterStore(arrayList, mActivity);
        b.rvList.setAdapter(adapter);

        b.tvApply.setOnClickListener(this);
        b.tvFilter.setText(getString(R.string.filterByDistance)+" - "+b.seekBar.getProgress()+" Km");

        hitGetMerchantListApi();

        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean bool) {

                b.tvFilter.setText(getString(R.string.filterByDistance)+" - "+i+" Km");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void hitGetMerchantListApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("kilometre",String.valueOf(b.seekBar.getProgress()));
            jsonObject.put("categoryId", getIntent().getStringExtra("categoryId"));

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.merchantListByCategory, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseMerchantListJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseMerchantListJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("merchantId", jsonObject1.getString("merchantId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("profileImage", jsonObject1.getString("profileImage"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    arrayList.add(hashMap);
                }

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.tvApply:

                hitGetMerchantListApi();

                break;
        }

    }


}