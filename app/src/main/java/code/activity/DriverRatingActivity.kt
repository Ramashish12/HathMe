package code.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.hathme.android.R
import com.hathme.android.databinding.ActivityDriverRatingBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DriverRatingActivity : BaseActivity(),View.OnClickListener {
    private lateinit var b: ActivityDriverRatingBinding
    var orderId = ""
    var driverId = ""
    var driverName = ""
    var jsonArray = JSONArray()
    var arrayList = ArrayList<HashMap<String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDriverRatingBinding.inflate(layoutInflater)
        setContentView(b.root)
        initi()
    }

    private fun initi() {
        orderId = intent.getStringExtra("orderId")!!
        b.ivBack.setOnClickListener(this)
        b.ratingBar.setOnClickListener(this)
        if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
        {
            b.ivImage.setImageDrawable(getDrawable(R.drawable.driver))
            b.tvStoreName.text = getString(R.string.rateYourDelivery)
            b.tvRateDeliveryTitle.text = getString(R.string.rateYourDelivery)
        }
        else
        {
            b.ivImage.setImageDrawable(getDrawable(R.drawable.product))
            b.tvStoreName.text = getString(R.string.rateYourItem)
            b.tvRateDeliveryTitle.text = getString(R.string.rateYourItem)
        }
        b.ratingBar.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                startActivity(Intent(mActivity, RateNowActivity::class.java).putExtra("orderId", orderId).putExtra("driverName",driverName).putExtra("rating",rating.toString()).putExtra("driverId",driverId))
            }
    }

    override fun onClick(view: View?) {
      when(view)
      {
          b.ivBack->{
              onBackPressed()
          }

      }
    }

    private fun hitGetOrderDetailApi() {
        val jsonObject = JSONObject()
        val json = JSONObject()
        try {
            jsonObject.put("orderId", orderId)
            json.put(AppConstants.projectName, jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        WebServices.postApi(
            mActivity,
            AppUrls.orderDetail,
            json,
            true,
            true,
            object : WebServicesCallback {
                override fun OnJsonSuccess(response: JSONObject) {
                    parseOrderDetail(response)
                }

                override fun OnFail(response: String) {}
            })
    }

    @SuppressLint("SetTextI18n")
    private fun parseOrderDetail(response: JSONObject) {
        arrayList.clear()
        try {
            val jsonObject = response.getJSONObject(AppConstants.projectName)
            if (jsonObject.getString(AppConstants.resCode) == "1") {
                val jsonData = jsonObject.getJSONObject("data")
                val driverData = jsonData.getJSONObject("driverData")
                driverId = driverData.getString("_id")

                if (AppSettings.getString(AppSettings.isFromActivity).equals("1"))
                {
                    driverName = driverData.getString("name")
                    b.tvDeliveryBoyName.text = driverName
                }
                else
                {
                    driverName = jsonData.getString("merchantName")
                    b.tvDeliveryBoyName.text = driverName
                }

                jsonArray = jsonData.getJSONArray("products")
                if (jsonData.getString("orderType").equals("2")) {
                    for (i in 0 until jsonArray.length()) {
                        val jsonProduct: JSONObject = jsonArray.getJSONObject(i)
                        val hashMap = HashMap<String, String>()
                        hashMap["_id"] = jsonProduct.getString("_id")
                        hashMap["requestOrderId"] = jsonProduct.getString("requestOrderId")
                        hashMap["name"] = jsonProduct.getString("productName")
                        hashMap["price"] = jsonProduct.getString("productAmount")
                        hashMap["sellingPrice"] = jsonProduct.getString("productAmount")
                        hashMap["priceWithQuantity"] = jsonProduct.getString("productAmount")
                        hashMap["orderType"] = "2"
                        hashMap["quantity"] = jsonProduct.getString("quantity")
                        hashMap["createdAt"] = jsonProduct.getString("createdAt")
                        hashMap["modifiedAt"] = jsonProduct.getString("modifiedAt")
                        hashMap["__v"] = jsonProduct.getString("__v")
                        arrayList.add(hashMap)
                    }

                }

                else {
                    for (i in 0 until jsonArray.length()) {
                        val jsonProduct: JSONObject = jsonArray.getJSONObject(i)
                        val hashMap = HashMap<String, String>()
                        hashMap["productId"] = jsonProduct.getString("productId")
                        hashMap["name"] = jsonProduct.getString("name")
                        hashMap["quantity"] = jsonProduct.getString("quantity")
                        hashMap["priceOfEachItem"] = jsonProduct.getString("priceOfEachItem")
                        hashMap["priceWithQuantity"] = jsonProduct.getString("priceWithQuantity")
                        hashMap["weight"] = jsonProduct.getString("weight")
                        hashMap["bestBefore"] = jsonProduct.getString("bestBefore")
                        hashMap["description"] = jsonProduct.getString("description")
                        hashMap["price"] = jsonProduct.getString("price")
                        hashMap["category"] = jsonProduct.getString("category")
                        hashMap["subCategory"] = jsonProduct.getString("subCategory")
                        hashMap["status"] = jsonProduct.getString("status")
                        hashMap["sellingPrice"] = jsonProduct.getString("sellingPrice")
                        hashMap["offerPrice"] = jsonProduct.getString("offerPrice")
                        hashMap["specialFeature"] = jsonProduct.getString("specialFeature")
                        hashMap["brand"] = jsonProduct.getString("brand")
                        hashMap["color"] = jsonProduct.getString("color")
                        hashMap["size"] = jsonProduct.getString("size")
                        hashMap["isVeg"] = jsonProduct.getString("isVeg")
                        hashMap["unit"] = jsonProduct.getString("unit")
                        hashMap["unitType"] = jsonProduct.getString("unitType")
                        hashMap["mrp"] = jsonProduct.getString("mrp")
                        hashMap["packedType"] = jsonProduct.getString("packedType")
                        hashMap["expiryDate"] = jsonProduct.getString("expiryDate")
                        hashMap["batchNumber"] = jsonProduct.getString("batchNumber")
                        hashMap["distributorName"] = jsonProduct.getString("distributorName")
                        hashMap["materialType"] = jsonProduct.getString("materialType")
                        hashMap["aboutThisItem"] = jsonProduct.getString("aboutThisItem")
                        hashMap["manufacturer"] = jsonProduct.getString("manufacturer")
                        hashMap["disclaimer"] = jsonProduct.getString("disclaimer")
                        hashMap["shelfLife"] = jsonProduct.getString("shelfLife")
                        hashMap["fssaiLicense"] = jsonProduct.getString("fssaiLicense")
                        hashMap["countryOfOrigin"] = jsonProduct.getString("countryOfOrigin")
                        hashMap["seller"] = jsonProduct.getString("seller")
                        hashMap["ingredients"] = jsonProduct.getString("ingredients")
                        hashMap["content"] = jsonProduct.getString("content")
                        hashMap["isProductApproved"] = jsonProduct.getString("isProductApproved")
                        hashMap["recommended"] = jsonProduct.getString("recommended")
                        hashMap["rating"] = jsonProduct.getString("rating")
                        hashMap["imageOne"] = jsonProduct.getString("imageOne")
                        hashMap["orderType"] = "1"
                        arrayList.add(hashMap)
                    }

                }
                val totalAmt = AppUtils.returnDouble(jsonData.getString("finalAmount"))
                b.tvItemCount.text = arrayList.size.toString()+" "+getString(R.string.items)+" "+getString(R.string.rupeeSymbol)+" "+totalAmt

            }
            else {AppUtils.showMessageDialog(
                mActivity,
                getString(R.string.orderDetails),
                jsonObject.getString(AppConstants.resMsg),
                2
            )}
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        hitGetOrderDetailApi()
        super.onResume()
    }
}