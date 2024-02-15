package code.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityCategoriesBinding;

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

public class CategoriesActivity extends BaseActivity implements View.OnClickListener {

    ActivityCategoriesBinding b;

    Adapter adapter;

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(view -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.allCategories));

        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));

        adapter = new Adapter(arrayList);
        b.rvList.setAdapter(adapter);

        hitGetCategoryApi();

        b.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                hitGetCategoryApi();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


    @Override
    public void onClick(View view) {

    }

    private void hitGetCategoryApi() {

        String url = "";
        boolean loader = true;

        if (!b.etSearch.getText().toString().trim().isEmpty()) {
            url = AppUrls.searchCategory + b.etSearch.getText().toString().trim();
            loader = false;
        } else {
            url = AppUrls.category;
            loader = true;
        }

        WebServices.getApi(mActivity, url, loader, false, new WebServicesCallback() {

            @Override
            public void OnJsonSuccess(JSONObject response) {

                parseCategoryJson(response);

            }

            @Override
            public void OnFail(String response) {

            }
        });
    }

    private void parseCategoryJson(JSONObject response) {

        arrayList.clear();

        try {
            JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

            if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonCategory = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("_id", jsonCategory.getString("_id"));
                    hashMap.put("image", jsonCategory.getString("image"));
                    hashMap.put("name", jsonCategory.getString("name"));

                    arrayList.add(hashMap);


                }


            } else {
                /*AppUtils.showMessageDialog(mActivity, getString(R.string.allCategories),
                        jsonObject.getString(AppConstants.resMsg), 2);*/

            }

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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_category, viewGroup, false);
            return new Adapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.MyViewHolder holder, final int position) {

            holder.tvName.setText(data.get(position).get("name"));

            AppUtils.loadPicassoImage(data.get(position).get("image"), holder.ivImage);

            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(mActivity, StoreListActivity.class).putExtra("categoryId", data.get(position).get("_id")));

                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout llMain;

            TextView tvName;

            ImageView ivImage;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                llMain = itemView.findViewById(R.id.llMain);

                ivImage = itemView.findViewById(R.id.ivImage);

                tvName = itemView.findViewById(R.id.tvName);
            }
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