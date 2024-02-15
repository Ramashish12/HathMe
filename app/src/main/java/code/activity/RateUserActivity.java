package code.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityRateUserBinding;

import org.json.JSONException;
import org.json.JSONObject;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class RateUserActivity extends BaseActivity implements View.OnClickListener {

    private ActivityRateUserBinding b;

    private String toUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRateUserBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    @SuppressLint("SetTextI18n")
    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());

        b.tvContinue.setOnClickListener(this);

        toUserId = getIntent().getStringExtra("userId");
        b.tvUserName.setText(getIntent().getStringExtra("name"));
        AppUtils.loadPicassoImage(getIntent().getStringExtra("profileImage"), b.ivProfile);
        b.ratingBar.setRating(AppUtils.returnFloat(getIntent().getStringExtra("selfRating")));
        b.etDescription.setText(getIntent().getStringExtra("remark"));
        b.tvTotalReviews.setText(getIntent().getStringExtra("totalRating") + " " + getString(R.string.reviews));

        if (!getIntent().getStringExtra("selfRating").equals("0")) {
            b.tvContinue.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tvContinue:

                validate();


                break;
        }
    }

    private void validate() {

        if (b.ratingBar.getRating() == 0) {
            AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectRating));
        } else {

            hitRateUserApi();
        }

    }

    private void hitRateUserApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("rateToUserId", getIntent().getStringExtra("userId"));
            jsonObject.put("ratingAmount", String.valueOf(b.ratingBar.getRating()));
            jsonObject.put("remarks", b.etDescription.getText().toString().trim());

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.rateUser, json, true, true, new WebServicesCallback() {

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

                onBackPressed();
                AppUtils.showResMsgToastSort(mActivity, jsonObject);

            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}