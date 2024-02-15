package code.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hathme.android.R;
import com.hathme.android.databinding.ActivityAddressBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import code.common.SingleShotLocationProvider;
import code.utils.AppConstants;
import code.utils.AppSettings;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class AddressActivity extends BaseActivity implements View.OnClickListener, OnMapReadyCallback {

    ActivityAddressBinding b;

    private GoogleMap mMap;

    double currentLat = 0, currentLong = 0;

    private String addressId = "", type = "1",requestOrderId = "";//1=Add,2=Edit,3=Goto Cart

    private PlacesClient placesClient;

    private List<AutocompletePrediction> predictionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();
    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.chooseDeliveryLocation));
        b.tvCurrentLocation.setOnClickListener(this);
        b.tvEnterAddress.setOnClickListener(this);
        if (getIntent().getExtras() != null && getIntent().hasExtra("requestOrderId")) {
            requestOrderId = getIntent().getStringExtra("requestOrderId");
        }
        b.tvChange.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    private void getIntentValues() {

        if (getIntent().getExtras() != null) {

            Intent intent = getIntent();

            type = intent.getStringExtra("type");

            if (intent.hasExtra("addressId")) {


                addressId = intent.getStringExtra("addressId");
                moveMapOnLocation(AppUtils.returnDouble(intent.getStringExtra("latitude")), AppUtils.returnDouble(intent.getStringExtra("longitude")));

            }
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.tvCurrentLocation:

                goToCurrentLocation();

                break;

            case R.id.tvEnterAddress:

                if (currentLat == 0) {
                    AppUtils.showToastSort(mActivity, getString(R.string.pleaseSelectLocation));
                    goToCurrentLocation();
                } else {

                    showCompleteAddressApi();
                }

                break;

            case R.id.tvChange:

                showSearchLocationDialog();

                break;

        }

    }

    private void showSearchLocationDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_auto_complete);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        EditText etSearch = bottomSheetDialog.findViewById(R.id.etSearch);

        RecyclerView rvList = bottomSheetDialog.findViewById(R.id.rvList);
        rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
        rvList.addItemDecoration(new DividerItemDecoration(rvList.getContext(), DividerItemDecoration.VERTICAL));

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

        Places.initialize(mActivity, "AIzaSyDTAyrOge4-B9kR32N5dfAQoNBJrD5bUeg");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                arrayList.clear();

                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        // .setCountry("sa")
                        // .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token).setQuery(s.toString()).build();

                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {

                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {


                            }
                        });

                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                //List<String> suggestionsList = new ArrayList<>();


                                for (int i = 0; i < predictionList.size(); i++) {

                                    AutocompletePrediction prediction = predictionList.get(i);
                                    HashMap<String, String> hashMap = new HashMap<>();
                                    hashMap.put("fullAdd", prediction.getFullText(null).toString());
                                    hashMap.put("primaryAdd", prediction.getPrimaryText(null).toString());
                                    arrayList.add(hashMap);

                                }

                                rvList.setAdapter(new AutocompleteAdapter(mActivity, arrayList, bottomSheetDialog));


                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().isEmpty()) {
                    arrayList.clear();
                    rvList.setAdapter(null);
                }

            }
        });

    }

    private void showCompleteAddressApi() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mActivity, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.dialog_complete_address);
        bottomSheetDialog.setCancelable(true);
        //   bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();

        TextView tvAddress, tvSubAddress, tvHome, tvWork, tvHotel, tvOther, tvSave;

        EditText etCompleteAddress, etFloor, etLandmark;

        etCompleteAddress = bottomSheetDialog.findViewById(R.id.etCompleteAddress);
        etFloor = bottomSheetDialog.findViewById(R.id.etFloor);
        etLandmark = bottomSheetDialog.findViewById(R.id.etLandmark);

        tvAddress = bottomSheetDialog.findViewById(R.id.tvAddress);
        tvSubAddress = bottomSheetDialog.findViewById(R.id.tvSubAddress);
        tvHome = bottomSheetDialog.findViewById(R.id.tvHome);
        tvWork = bottomSheetDialog.findViewById(R.id.tvWork);
        tvHotel = bottomSheetDialog.findViewById(R.id.tvHotel);
        tvOther = bottomSheetDialog.findViewById(R.id.tvOther);
        tvSave = bottomSheetDialog.findViewById(R.id.tvSave);

        tvAddress.setText(b.tvAddress.getText().toString().trim());
        tvSubAddress.setText(b.tvSubAddress.getText().toString().trim());

        final int[] type = {1};

        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type[0] = 1;

                tvHome.setBackgroundResource(R.drawable.rectangular_border_less_radius);
                tvWork.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvHotel.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvOther.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);

                tvHome.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvWork.setTextColor(getResources().getColor(R.color.textGrey));
                tvHotel.setTextColor(getResources().getColor(R.color.textGrey));
                tvOther.setTextColor(getResources().getColor(R.color.textGrey));
            }
        });

        tvWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type[0] = 2;

                tvHome.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvWork.setBackgroundResource(R.drawable.rectangular_border_less_radius);
                tvHotel.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvOther.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);

                tvHome.setTextColor(getResources().getColor(R.color.textGrey));
                tvWork.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvHotel.setTextColor(getResources().getColor(R.color.textGrey));
                tvOther.setTextColor(getResources().getColor(R.color.textGrey));
            }
        });

        tvHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type[0] = 3;
                tvHome.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvWork.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvHotel.setBackgroundResource(R.drawable.rectangular_border_less_radius);
                tvOther.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);

                tvHome.setTextColor(getResources().getColor(R.color.textGrey));
                tvWork.setTextColor(getResources().getColor(R.color.textGrey));
                tvHotel.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvOther.setTextColor(getResources().getColor(R.color.textGrey));
            }
        });

        tvOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                type[0] = 4;

                tvHome.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvWork.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvHotel.setBackgroundResource(R.drawable.rectangular_grey_border_less_radius);
                tvOther.setBackgroundResource(R.drawable.rectangular_border_less_radius);

                tvHome.setTextColor(getResources().getColor(R.color.textGrey));
                tvWork.setTextColor(getResources().getColor(R.color.textGrey));
                tvHotel.setTextColor(getResources().getColor(R.color.textGrey));
                tvOther.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        if (!addressId.isEmpty()) {

            etCompleteAddress.setText(getIntent().getStringExtra("completeAddress"));
            etFloor.setText(getIntent().getStringExtra("floor"));
            etLandmark.setText(getIntent().getStringExtra("landmark"));

            switch (getIntent().getStringExtra("addressType")) {
                case "1":
                    tvHome.performClick();
                    break;
                case "2":
                    tvWork.performClick();
                    break;
                case "3":
                    tvHotel.performClick();
                    break;
                case "4":
                    tvOther.performClick();
                    break;
            }

        }

        tvSave.setOnClickListener(view -> {

            if (etCompleteAddress.getText().toString().trim().isEmpty()) {
                AppUtils.showToastSort(mActivity, getString(R.string.pleaseEnterCompleteAddress));
            } else {
                hitSaveOrUpdateAddressApi(etCompleteAddress.getText().toString().trim(), etFloor.getText().toString().trim(), etLandmark.getText().toString().trim(), type[0]);
            }

        });

    }

    private void hitSaveOrUpdateAddressApi(String completeAddress, String floor, String landmark, int type) {

        JSONObject jsonObject = new JSONObject();
        JSONObject json = new JSONObject();

        try {

            if (!addressId.isEmpty()) jsonObject.put("addressId", addressId);

            jsonObject.put("areaName", b.tvAddress.getText().toString().trim());
            jsonObject.put("fullAddress", b.tvSubAddress.getText().toString().trim());
            jsonObject.put("addressType", String.valueOf(type));
            jsonObject.put("completeAddress", completeAddress);
            jsonObject.put("latitude", String.valueOf(currentLat));
            jsonObject.put("longitude", String.valueOf(currentLong));
            jsonObject.put("floor", floor);
            jsonObject.put("landmark", landmark);

            json.put(AppConstants.projectName, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (addressId.isEmpty()) {

            WebServices.postApi(mActivity, AppUrls.address, json, true, true, new WebServicesCallback() {

                @Override
                public void OnJsonSuccess(JSONObject response) {

                    parseAddAddress(response);

                }

                @Override
                public void OnFail(String response) {

                }
            });
        }
        else {

            WebServices.putApi(mActivity, AppUrls.updateAddress, json, true, true, new WebServicesCallback() {

                @Override
                public void OnJsonSuccess(JSONObject response) {

                    parseAddAddress(response);

                }

                @Override
                public void OnFail(String response) {

                }
            });
        }

    }

    private void parseAddAddress(JSONObject response) {

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                if (type.equals("3")) {
                    if (AppSettings.getString(AppSettings.isFromBucket).equalsIgnoreCase("1"))
                    {
                        AppSettings.putString(AppSettings.isFromBucket,"0");
                        startActivity(new Intent(mActivity, BucketCartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("requestOrderId",requestOrderId));
                    }
                    else
                    {
                        startActivity(new Intent(mActivity, CartActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }

                } else {

                    startActivity(new Intent(mActivity, AddressListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("requestOrderId",requestOrderId));
                    overridePendingTransitionExit();
                    finish();
                }


            } else
                AppUtils.showMessageDialog(mActivity, getString(R.string.address), jsonObject.getString(AppConstants.resMsg), 2);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style));

        getIntentValues();

        if (!type.equals("2")) goToCurrentLocation();

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                currentLat = mMap.getCameraPosition().target.latitude;
                currentLong = mMap.getCameraPosition().target.longitude;

                b.llBottom.setVisibility(View.VISIBLE);
                b.tvCurrentLocation.setVisibility(View.VISIBLE);

                getMarkerAddress();
                // b.included.tvPickup.setText(AppUtils.getMarkerAddress(mActivity, currentLat, currentLong));

               /* if (isAutoCompleteClicked) {

                    isAutoCompleteClicked = false;

                    if (AppSettings.getString(AppSettings.autoCompleteType).equals("2"))
                        getMarkerAddress();

                } else {

                    getMarkerAddress();
                }*/
            }
        });
    }

    private void goToCurrentLocation() {

        AppUtils.showRequestDialog(mActivity, getString(R.string.gettingLocation));
        SingleShotLocationProvider.requestSingleUpdate(mActivity, (SingleShotLocationProvider.GPSCoordinates location) -> {

            AppUtils.hideDialog();
            currentLat = location.latitude;
            currentLong = location.longitude;

            moveMapOnLocation(currentLat, currentLong);

        });
    }

    private void moveMapOnLocation(double latitude, double longitude) {

        if (mMap != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);

            LatLng gps = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 15));

        }
    }

    private void getMarkerAddress() {

        try {

            String address = getString(R.string.na);
            String premises = getString(R.string.na);

            Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());

            List<Address> addresses = geocoder.getFromLocation(currentLat, currentLong, 1);

            if (addresses.get(0).getAddressLine(0) != null) {
                address = addresses.get(0).getAddressLine(0);
            }

            if (addresses.get(0).getSubLocality() != null) {
                premises = addresses.get(0).getSubLocality();
            }

            b.tvAddress.setText(premises);
            b.tvSubAddress.setText(address);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AutocompleteAdapter extends RecyclerView.Adapter<AutocompleteAdapter.MyViewHolder> {

        Activity activity;
        ArrayList<HashMap<String, String>> resultList;
        BottomSheetDialog bottomSheetDialog;

        public AutocompleteAdapter(Activity mActivity, ArrayList<HashMap<String, String>> suggestionsList, BottomSheetDialog bottomSheetDialog) {

            activity = mActivity;
            resultList = suggestionsList;
            this.bottomSheetDialog = bottomSheetDialog;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_autocpmplete, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AutocompleteAdapter.MyViewHolder holder, final int i) {

            holder.tvAddress.setText(resultList.get(i).get("fullAdd"));
            holder.tvSubAddress.setText(resultList.get(i).get("primaryAdd"));

            holder.llMain.setOnClickListener(v -> {

                AutocompletePrediction selectedPrediction = predictionList.get(i);
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {

                    Place place = fetchPlaceResponse.getPlace();
                    LatLng latLngOfPlace = place.getLatLng();

                    assert latLngOfPlace != null;
                    currentLat = latLngOfPlace.latitude;
                    currentLong = latLngOfPlace.longitude;

                    bottomSheetDialog.dismiss();
                    moveMapOnLocation(currentLat, currentLong);

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if (e instanceof ApiException) {

                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });

            });


        }

        @Override
        public int getItemCount() {
            return resultList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvAddress, tvSubAddress;
            LinearLayout llMain;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvSubAddress = itemView.findViewById(R.id.tvSubAddress);

                llMain = itemView.findViewById(R.id.llMain);
            }
        }
    }

}