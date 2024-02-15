package code.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityNeftBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.GlobalData;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class NeftActivity extends BaseActivity implements View.OnClickListener {
    String amount = "";
    ActivityNeftBinding b;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neft);
        b = ActivityNeftBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    @SuppressLint("SetTextI18n")
    private void inits() {

        b.header.ivBack.setOnClickListener(this);
        b.btnSubmit.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        amount = extras.getString("amount");

        b.header.tvHeader.setText(getString(R.string.depositing) + " " + getString(R.string.rupeeSymbol) + "" + amount);
        hitGetBankListApi();


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
                if (jsonArray.length() > 0) {
                    b.tvAccountName.setText(arrayList.get(0).get("name"));
                    b.tvBankName.setText(arrayList.get(0).get("bankName"));
                    b.tvAccountNo.setText(arrayList.get(0).get("accountNumber"));
                    b.tvIfscCode.setText(arrayList.get(0).get("ifsc"));
                }
                else
                {
                    b.llName.setVisibility(View.GONE);
                    b.llBankName.setVisibility(View.GONE);
                    b.llBankAccount.setVisibility(View.GONE);
                    b.llBankIfsc.setVisibility(View.GONE);
                }
            } else {
                b.tvAccountName.setVisibility(View.GONE);
                b.tvBankName.setVisibility(View.GONE);
                b.tvAccountNo.setVisibility(View.GONE);
                b.tvIfscCode.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }




    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ivBack:

                onBackPressed();

                break;

            case R.id.btnSubmit:

                if (b.etReferenceNumber.getText().toString().trim().isEmpty()) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterReferenceNumber));
                } else if (AppUtils.isNetworkAvailable(mActivity)) {
                    hitDepositApi();
                } else {
                    AppUtils.showToastSort(mActivity, getString(R.string.noInternetConnection));
                }

                break;
        }
    }

    private void hitDepositApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("amount", amount);
            jsonObject.put("modeOfPayment", "2");
            jsonObject.put("referenceUtrNumber", b.etReferenceNumber.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = AppUrls.depositAmount;

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 1);


            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }



}