package code.activity;

import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_TITLE;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_CHANNEL_URL;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_RECEIVER_ID;
import static com.sendbird.chat.module.utils.Constants.INTENT_KEY_RECEIVER_NAME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityStoreBinding;
import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.sendbird.android.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import code.adapters.AdapterItem;
import code.call.CallService;
import code.chat.ChatActivity;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class StoreActivity extends BaseActivity implements View.OnClickListener {

    ActivityStoreBinding b;
    public static TextView tvCartQty;
    private AdapterItem adapterRecommend;
    private AdapterItem adapterAllProduct;
    int totalQuantity = 0;
    private final ArrayList<HashMap<String, String>> arrayListRecommend = new ArrayList<>();
    private final ArrayList<HashMap<String, String>> arrayListAllProduct = new ArrayList<>();
    ArrayList<HashMap<String, String>> countArrayList = new ArrayList<>();
    private boolean isFav = false;

    private String channelName = "", channelUrl = "", merchantId = "", merchantName = "", coverPhoto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityStoreBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        tvCartQty = findViewById(R.id.tvCartQty);
        b.header.ivCart.setOnClickListener(this);
        b.tvUpload.setOnClickListener(this);
        b.header.ivShare.setVisibility(View.VISIBLE);
        b.header.ivSearch.setVisibility(View.VISIBLE);

        merchantId = getIntent().getStringExtra("merchantId");

        adapterRecommend = new AdapterItem(arrayListRecommend, mActivity, (productId, quantity, productQuantity) -> {
            if (Integer.parseInt(quantity) > Integer.parseInt(productQuantity)) {
                AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantity), 4);
            } else {
                hitAddToCartApi(productId, quantity);
            }

        });

        adapterAllProduct = new AdapterItem(arrayListAllProduct, mActivity, (productId, quantity, productQuantity) -> {
            if (Integer.parseInt(quantity) > Integer.parseInt(productQuantity)) {
                AppUtils.showMessageDialog(mActivity, getString(R.string.quantity), getString(R.string.productQuantity), 4);
            } else {
                hitAddToCartApi(productId, quantity);
            }
        });

        b.rvRecommend.setAdapter(adapterRecommend);
        b.rvAllProduct.setAdapter(adapterAllProduct);

        b.rlRecommend.setOnClickListener(this);
        b.rlAllProduct.setOnClickListener(this);
        b.ivVideoCall.setOnClickListener(this);
        b.ivAudioCall.setOnClickListener(this);
        b.ivChat.setOnClickListener(this);

        b.header.ivFavorite.setOnClickListener(this);
        b.header.ivShare.setOnClickListener(this);
        b.header.ivSearch.setOnClickListener(this);

        hitGetMerchantProductApi();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.rlRecommend:

                if (b.rvRecommend.getVisibility() == View.VISIBLE) {
                    b.rvRecommend.setVisibility(View.GONE);
                    b.ivRecommendArrow.setRotation(180);
                } else {
                    b.rvRecommend.setVisibility(View.VISIBLE);
                    b.ivRecommendArrow.setRotation(0);
                }

                break;
            case R.id.rlAllProduct:

                if (b.rvAllProduct.getVisibility() == View.VISIBLE) {
                    b.rvAllProduct.setVisibility(View.GONE);
                    b.ivAllProductArrow.setRotation(180);
                } else {
                    b.rvAllProduct.setVisibility(View.VISIBLE);
                    b.ivAllProductArrow.setRotation(0);
                }

                break;

            case R.id.ivVideoCall:
                videoCall(merchantId, merchantName, coverPhoto);
                break;

            case R.id.ivAudioCall:
                // String userid = AppSettings.getString(AppSettings.userId);
                voiceCall(merchantId, merchantName, coverPhoto);
                break;

            case R.id.ivChat:

                if (channelName.isEmpty()) {
                    createChannel();
                } else {
                    gotoChatActivity();
                }

                break;

            case R.id.ivFavorite:

                hitFavApi();

                break;

            case R.id.ivShare:

                share();

                break;
            case R.id.ivSearch:
                // startActivity(new Intent(mActivity, SearchAllProductActivity.class));
                startActivity(new Intent(mActivity, ProductSearchActivity.class).putExtra("merchantId", merchantId));
                break;
            case R.id.tvUpload:
                startActivity(new Intent(mActivity, UploadOrderImageActivity.class).putExtra("merchantId", merchantId));
                break;

            case R.id.ivCart:

                startActivity(new Intent(mActivity, CartActivity.class));

                break;

        }

    }

    private void share() {

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage = merchantName + " from " + getString(R.string.app_name) +
                    "\n\n" + AppUrls.shareUrl + merchantId;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createChannel() {

        OpenChannelCreateParams params = new OpenChannelCreateParams();
        params.setName(merchantId + AppSettings.getString(AppSettings.userId));
        User currentUser = SendbirdChat.getCurrentUser();
        if (currentUser != null) {
            List<User> list = new ArrayList<>();
            list.add(currentUser);
            params.setOperators(list);

            OpenChannel.createChannel(params, (openChannel, e) -> {

                if (openChannel != null) {

                    channelName = openChannel.getName();
                    channelUrl = openChannel.getUrl();
                    hitCreateChannelApi();

                } else {
                    AppUtils.showToastSort(mActivity, getString(R.string.retry));
                }

            });
        } else {
            AppUtils.showToastSort(mActivity, getString(R.string.retry));
        }


    }

    private void hitCreateChannelApi() {
        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("merchantId", merchantId);
            jsonObject.put("channelName", channelName);
            jsonObject.put("channelUrl", channelUrl);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.createMerchantChannel, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCreateChannelJson(response);

            }

            @Override
            public void OnFail(String response) {
                AppUtils.showToastSort(mActivity, response);
            }
        });
    }

    private void parseCreateChannelJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                gotoChatActivity();
            } else {
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void gotoChatActivity() {
        Intent intent = new Intent(mActivity, ChatActivity.class);
        intent.putExtra("receiverId", merchantId);
        //  intent.putExtra(INTENT_KEY_RECEIVER_ID, merchantId);
        if (AppSettings.getString(AppSettings.userId).equalsIgnoreCase(merchantId)) {
            intent.putExtra(INTENT_KEY_RECEIVER_ID, AppSettings.getString(AppSettings.userId));
        } else {
            intent.putExtra(INTENT_KEY_RECEIVER_ID, merchantId);
        }
        intent.putExtra(INTENT_KEY_CHANNEL_URL, channelUrl);
        intent.putExtra(INTENT_KEY_CHANNEL_TITLE, channelName);
        intent.putExtra(INTENT_KEY_RECEIVER_NAME, merchantName);
        startActivity(intent);
    }

    private void hitFavApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("merchantId", merchantId);

            json.put(AppConstants.projectName, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "";

        if (isFav) {
            url = AppUrls.DeleteFavoriteMerchant;

        } else {
            url = AppUrls.setMerchantFavorite;

        }

        WebServices.postApi(mActivity, url, json, true, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseFavJson(response);

            }

            @Override
            public void OnFail(String response) {


            }
        });
    }

    private void parseFavJson(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                isFav = !isFav;

                setFavorite();


            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setFavorite() {


        if (isFav) {
            b.header.ivFavorite.setImageResource(R.drawable.ic_heart_colored);
        } else {
            b.header.ivFavorite.setImageResource(R.drawable.ic_heart_grey);

        }
    }

    private void hitGetMerchantProductApi() {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {
            jsonObject.put("merchantId", merchantId);
            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WebServices.postApi(mActivity, AppUrls.productListByMerchantId, json, false, true, new WebServicesCallback() {

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

        arrayListRecommend.clear();
        arrayListAllProduct.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                merchantName = jsonData.getString("merchantName");
                channelUrl = jsonData.getString("channelUrl");
                //channelName = merchantName;
                coverPhoto = jsonData.getString("coverPhoto");
                if (jsonData.getString("isDocument").equalsIgnoreCase("0") || jsonData.getString("isDocument").equalsIgnoreCase("")) {
                    b.tvUpload.setVisibility(View.GONE);
                } else {
                    b.tvUpload.setVisibility(View.VISIBLE);
                }
                b.tvName.setText(jsonData.getString("merchantName"));
                b.tvAddress.setText(jsonData.getString("MerchantAddress"));
                b.tvRating.setText(AppUtils.roundOff2Digit(jsonData.getString("rating")));

                isFav = jsonData.getString("favoriteStatus").equals("1");
                setFavorite();

                channelName = jsonData.getString("channelName");

                JSONArray jsonArrayRecommend = jsonData.getJSONArray("recommendedProducts");
                JSONArray jsonArrayAllProduct = jsonData.getJSONArray("allProducts");

                for (int i = 0; i < jsonArrayRecommend.length(); i++) {

                    JSONObject jsonObject1 = jsonArrayRecommend.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonObject1.getString("productId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("description", jsonObject1.getString("description"));
                    hashMap.put("price", jsonObject1.getString("price"));
                    hashMap.put("productQuantity", jsonObject1.getString("productQuantity"));
                    hashMap.put("sellingPrice", jsonObject1.getString("sellingPrice"));
                    hashMap.put("offerPrice", jsonObject1.getString("offerPrice"));
                    hashMap.put("productImageOne", jsonObject1.getString("productImageOne"));
                    hashMap.put("productImageTwo", jsonObject1.getString("productImageTwo"));
                    hashMap.put("productImageThree", jsonObject1.getString("productImageThree"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    hashMap.put("merchantId", merchantId);

                    if (jsonObject1.has("quantity"))
                        hashMap.put("quantity", jsonObject1.getString("quantity"));
                    else
                        hashMap.put("quantity", "");

                    arrayListRecommend.add(hashMap);

                }

                for (int i = 0; i < jsonArrayAllProduct.length(); i++) {

                    JSONObject jsonObject1 = jsonArrayAllProduct.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("productId", jsonObject1.getString("productId"));
                    hashMap.put("name", jsonObject1.getString("name"));
                    hashMap.put("description", jsonObject1.getString("description"));
                    hashMap.put("price", jsonObject1.getString("price"));
                    hashMap.put("sellingPrice", jsonObject1.getString("sellingPrice"));
                    hashMap.put("offerPrice", jsonObject1.getString("offerPrice"));
                    hashMap.put("productQuantity", jsonObject1.getString("productQuantity"));
                    hashMap.put("productImageOne", jsonObject1.getString("productImageOne"));
                    hashMap.put("productImageTwo", jsonObject1.getString("productImageTwo"));
                    hashMap.put("productImageThree", jsonObject1.getString("productImageThree"));
                    hashMap.put("rating", jsonObject1.getString("rating"));
                    hashMap.put("merchantId", merchantId);
                    if (jsonObject1.has("quantity"))
                        hashMap.put("quantity", jsonObject1.getString("quantity"));
                    else
                        hashMap.put("quantity", "");

                    arrayListAllProduct.add(hashMap);

                }
                hitGetCartApi();
            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {

            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterRecommend.notifyDataSetChanged();
        adapterAllProduct.notifyDataSetChanged();

    }

    private void hitGetCartApi() {

        WebServices.getApi(mActivity, AppUrls.getProductFromCart, false, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseGetCart(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseGetCart(JSONObject response) {

        countArrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONObject jsonData = jsonObject.getJSONObject("data");

                JSONArray jsonArray = jsonData.getJSONArray("items");
                totalQuantity = 0;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    String productQuantityStr = json.getString("quantity");
                    int productQuantity = Integer.parseInt(productQuantityStr);
                    totalQuantity += productQuantity;

                    countArrayList.add(hashMap);
                }
                b.header.tvCartQty.setText("" + totalQuantity);


            } else {
                b.header.tvCartQty.setText("0");
//                AppUtils.showMessageDialog(mActivity, getString(R.string.cart),
//                        jsonObject.getString(AppConstants.resMsg), 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private String getPrice(String price, String sellingPrice, String offerPrice) {

        String value = "";

        if (sellingPrice.isEmpty() && offerPrice.isEmpty()) {
            value = price;
        } else if (offerPrice.isEmpty()) {
            value = sellingPrice;
        } else {
            value = offerPrice;
        }

        return price;

    }

    private void hitAddToCartApi(String productId, String quantity) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            jsonObject.put("quantity", quantity);
            jsonObject.put("productId", productId);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WebServices.postApi(mActivity, AppUrls.addToCart, json, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseAddToCartJson(response, quantity, productId);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseAddToCartJson(JSONObject response, String quantity, String productId) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {
                hitGetMerchantProductApi();
            } else if (jsonObject.getString(AppConstants.resCode).equals("2")) {

                showAlreadyCartDialog(jsonObject.getString(AppConstants.resMsg), quantity, productId);
            } else {
                // hitGetCartApi();
                b.header.tvCartQty.setText("0");
                hitGetMerchantProductApi();
                hitClearCartApi(quantity, productId);
                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
                        jsonObject.getString(AppConstants.resMsg), 2);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showAlreadyCartDialog(String message, String quantity, String productId) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_already_added);
        bottomSheetDialog.setCancelable(false);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvCancel, tvMessage, tvContinue;

        tvCancel = bottomSheetDialog.findViewById(R.id.tvCancel);
        tvMessage = bottomSheetDialog.findViewById(R.id.tvMessage);
        tvContinue = bottomSheetDialog.findViewById(R.id.tvContinue);

        tvMessage.setText(message);

        tvCancel.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            hitGetMerchantProductApi();
        });

        tvContinue.setOnClickListener(v -> {

            bottomSheetDialog.dismiss();

            hitClearCartApi(quantity, productId);
        });

    }

    private void hitClearCartApi(String quantity, String productId) {

        WebServices.getApi(mActivity, AppUrls.RemoveProductsFromCart, false, true, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseClearCart(response, quantity, productId);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseClearCart(JSONObject response, String quantity, String productId) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                hitAddToCartApi(productId, quantity);

            }

//            else
//                AppUtils.showMessageDialog(mActivity, getString(R.string.app_name),
//                        jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void voiceCall(String userId, String name, String profileUrl) {
        //  hitStartCallApi(userId, name, profileUrl);
        CallService.dial(mActivity, userId, name, profileUrl, false);
    }

    private void videoCall(String userId, String name, String profileUrl) {

        CallService.dial(mActivity, userId, name, profileUrl, true);

    }

    @Override
    protected void onResume() {
        hitGetCartApi();
        hitGetMerchantProductApi();
        super.onResume();
    }
}