package code.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityRateNowBinding
import org.json.JSONException
import org.json.JSONObject

class RateNowActivity : BaseActivity(), View.OnClickListener {
    private lateinit var b: ActivityRateNowBinding
    var orderId = ""
    var driverId = ""
    var driverName = ""
    var ratingCount = ""
    var isBehaviourYes = ""
    var isOnTimeYes = ""
    var isNoContactYes = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRateNowBinding.inflate(layoutInflater)
        setContentView(b.root)
        initi()
    }

    private fun initi() {
        orderId = intent.getStringExtra("orderId")!!
        driverId = intent.getStringExtra("driverId")!!
        driverName = intent.getStringExtra("driverName")!!
        ratingCount = intent.getStringExtra("rating")!!
        b.tvTitle.text = getString(R.string.rate) + " " + driverName

        b.ivBack.setOnClickListener(this)
        b.tvBehaviourYes.setOnClickListener(this)
        b.tvBehaviourNo.setOnClickListener(this)
        b.tvOnTimeYes.setOnClickListener(this)
        b.tvOnTimeNo.setOnClickListener(this)
        b.tvNoContactYes.setOnClickListener(this)
        b.tvNoContactNo.setOnClickListener(this)
        b.submitFeedback.setOnClickListener(this)
        b.ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                ratingCount = rating.toString()
                setRating()
            }
        setRating()
        setQuestions()
    }

    private fun setRating() {
        b.ratingBar.rating = AppUtils.returnFloat(ratingCount)
        b.tvRated.text = getString(R.string.youHaveRated) + " " + ratingCount
        if (ratingCount == "1.0")
        {
          b.tvRateDeliveryBehavior.text = getString(R.string.hatedIt)
        }
        else if (ratingCount=="2.0")
        {
            b.tvRateDeliveryBehavior.text = getString(R.string.didNotLike)
        }
        else if (ratingCount=="3.0")
        {
            b.tvRateDeliveryBehavior.text = getString(R.string.wasOk)
        }
        else if (ratingCount=="4.0")
        {
            b.tvRateDeliveryBehavior.text = getString(R.string.good)
        }
        else if (ratingCount=="5.0")
        {
            b.tvRateDeliveryBehavior.text = getString(R.string.awesome)
        }
        else
        {

        }

    }
    private fun setQuestions() {
        if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
        {
            b.tvBehaviourTitles.text = getString(R.string.waExecutive)
            b.tvWasOnTimeTitle.text = getString(R.string.waOnTime)
            b.tvNoContactTitles.text = getString(R.string.noContact)
        }
        else
        {
            b.tvBehaviourTitles.text = getString(R.string.wasPacking)
            b.tvWasOnTimeTitle.text = getString(R.string.wasPolite)
            b.tvNoContactTitles.text = getString(R.string.wasDamages)
        }
        
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onClick(view: View?) {
        when (view) {
            b.ivBack -> {
                onBackPressed()
            }
            b.tvBehaviourYes->{
                b.tvBehaviourYes.setBackgroundResource(R.drawable.et_rectangular_selected)
                b.tvBehaviourNo.setBackgroundResource(R.drawable.et_rectangular_border)
                isBehaviourYes = "1"
            }
            b.tvBehaviourNo->{
                b.tvBehaviourYes.setBackgroundResource(R.drawable.et_rectangular_border)
                b.tvBehaviourNo.setBackgroundResource(R.drawable.et_rectangular_selected)
                isBehaviourYes = "0"
            }
            b.tvOnTimeYes->{
                b.tvOnTimeYes.setBackgroundResource(R.drawable.et_rectangular_selected)
                b.tvOnTimeNo.setBackgroundResource(R.drawable.et_rectangular_border)
                isOnTimeYes = "1"
            }
            b.tvOnTimeNo->{
                b.tvOnTimeYes.setBackgroundResource(R.drawable.et_rectangular_border)
                b.tvOnTimeNo.setBackgroundResource(R.drawable.et_rectangular_selected)
                isOnTimeYes = "0"
            }
            b.tvNoContactYes->{
                b.tvNoContactYes.setBackgroundResource(R.drawable.et_rectangular_selected)
                b.tvNoContactNo.setBackgroundResource(R.drawable.et_rectangular_border)
                isNoContactYes = "0"
            }
            b.tvNoContactNo->{
                b.tvNoContactYes.setBackgroundResource(R.drawable.et_rectangular_border)
                b.tvNoContactNo.setBackgroundResource(R.drawable.et_rectangular_selected)
                isNoContactYes = "0"
            }
            b.submitFeedback->{
                validates()
            }
        }
    }

    private fun validates() {
        if (b.ratingBar.rating == 0f) {
            AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseRate),8)
        }
        else  if (isBehaviourYes == "") {
            if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
            {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseEnterDriverBehavior),8)
            }
            else
            {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseSelectMerchantPacking),8)
            }

        }
        else  if (isOnTimeYes == "") {
            if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
            {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseEnterDeliveryOnTime),8)
            }
            else
            {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseSelectMerchantPolite),8)
            }

        }
        else  if (isNoContactYes == "") {
            if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
            {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseEnterDriverContact),8)
            }
            else
            {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseSelectProductDamages),8)
            }
        }
        else  if (b.etDescription.text.toString() == "") {
            AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),getString(R.string.pleaseEnterDescription),8)
        }
        else {
            if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
            {
                hitRatingDriverApi(b.ratingBar.rating, b.etDescription.text.toString().trim())
            }
            else
            {
                hitItemRatingApi(b.ratingBar.rating, b.etDescription.text.toString().trim())
            }

        }
    }

    private fun hitRatingDriverApi(rating: Float, description: String) {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("orderId", orderId)
            jsonObject.put("driverId", driverId)
            jsonObject.put("remark", description)
            jsonObject.put("executiveBehaviourGood", isBehaviourYes)
            jsonObject.put("deliveryOnTime", isOnTimeYes)
            jsonObject.put("deliveryNoContact", isNoContactYes)
            jsonObject.put("rating", rating.toDouble())
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        WebServices.postApi(
            mActivity,
            AppUrls.driverOrderRating,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseJson(response)
                }
                override fun OnFail(response: String) {}
            })
    }
    private fun hitItemRatingApi(rating: Float, description: String) {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("orderId", orderId)
            jsonObject.put("productId", AppSettings.getString(AppSettings.productId))
            jsonObject.put("remark", description)
            jsonObject.put("executiveBehaviourGood", isBehaviourYes)
            jsonObject.put("deliveryOnTime", isOnTimeYes)
            jsonObject.put("deliveryNoContact", isNoContactYes)
            jsonObject.put("rating", rating.toDouble())
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        WebServices.postApi(
            mActivity,
            AppUrls.orderReview,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseJson(response)
                }

                override fun OnFail(response: String) {}
            })
    }
    private fun parseJson(response: JSONObject) {
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                val intent = Intent(
                    mActivity,
                    RateAndReviewActivity::class.java
                )
                intent.putExtra("orderId", orderId)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                overridePendingTransitionExit()
                finish()
                AppUtils.showResMsgToastSort(mActivity, jsonObject)
            } else {
                AppUtils.showMessageDialog(
                    mActivity,
                    getString(R.string.app_name),
                    jsonObject.getString(AppConstants.resMsg),
                    2
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}