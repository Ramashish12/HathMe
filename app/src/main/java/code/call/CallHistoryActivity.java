package code.call;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hathme.android.R;
import com.hathme.android.databinding.ActivityCallHistoryBinding;
import com.sendbird.calls.DirectCallLog;
import com.sendbird.calls.DirectCallLogListQuery;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.handler.DirectCallLogListQueryResultHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import code.utils.AppConstants;
import code.utils.AppUrls;
import code.utils.AppUtils;
import code.utils.WebServices;
import code.utils.WebServicesCallback;
import code.view.BaseActivity;

public class CallHistoryActivity extends BaseActivity {

    private ActivityCallHistoryBinding b;

    private Adapter adapter;
    private ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> arrayUserCallList = new ArrayList<>();


    private ProgressBar mProgressBar;

    private HistoryRecyclerViewAdapter mRecyclerViewHistoryAdapter;

    private DirectCallLogListQuery mDirectCallLogListQuery;
    DirectCallLog callLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCallHistoryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        inits();

    }

    private void inits() {

        b.header.ivBack.setOnClickListener(v -> onBackPressed());
        b.header.tvHeader.setText(getString(R.string.callHistory));
//        b.rvList.setLayoutManager(new GridLayoutManager(mActivity, 1));
//        adapter = new Adapter(arrayList);
//        b.rvList.setAdapter(adapter);
        mRecyclerViewHistoryAdapter = new HistoryRecyclerViewAdapter(mActivity);
        b.rvCallHistory.setAdapter(mRecyclerViewHistoryAdapter);

        b.rvCallHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            }
        });
        b.rvCallHistory.setVisibility(View.GONE);
        b.linearLayoutEmpty.setVisibility(View.GONE);
        b.progressBar.setVisibility(View.VISIBLE);

        mDirectCallLogListQuery = SendBirdCall.createDirectCallLogListQuery(new DirectCallLogListQuery.Params().setLimit(20));
        mDirectCallLogListQuery.next((list, e) -> {
            b.progressBar.setVisibility(View.GONE);

            if (e != null) {
                AppUtils.showToastSort(mActivity,e.getMessage());
                return;
            }

            if (list.size() > 0) {
                b.rvCallHistory.setVisibility(View.VISIBLE);
                b.linearLayoutEmpty.setVisibility(View.GONE);

                mRecyclerViewHistoryAdapter.setCallLogs(list);
                mRecyclerViewHistoryAdapter.notifyDataSetChanged();
            } else {
                b.rvCallHistory.setVisibility(View.GONE);
                b.linearLayoutEmpty.setVisibility(View.VISIBLE);
            }
        });
        if (mRecyclerViewHistoryAdapter != null) {
            mRecyclerViewHistoryAdapter.addLatestCallLog(callLog);
            mRecyclerViewHistoryAdapter.notifyDataSetChanged();
        }
    }


        //getCall history
        private void hitCallHistoryListApi () {

            WebServices.getApi(mActivity, AppUrls.callHistory, true, true,
                    new WebServicesCallback() {

                        @Override
                        public void OnJsonSuccess(JSONObject response) {

                            parseCallHistoryList(response);

                        }

                        @Override
                        public void OnFail(String response) {

                        }
                    });
        }

        private void parseCallHistoryList (JSONObject response){

            arrayList.clear();

            try {
                JSONObject jsonObject = response.getJSONObject(AppConstants.projectName);

                if (jsonObject.getString(AppConstants.resCode).equals("1")) {

                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObjects = jsonArray.getJSONObject(i);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("callId", jsonObjects.getString("callId"));
                        hashMap.put("status", jsonObjects.getString("status"));
                        hashMap.put("callDuration", jsonObjects.getString("callDuration"));
                        hashMap.put("callStartDateTIme", jsonObjects.getString("callStartDateTIme"));
                        hashMap.put("userCall", jsonObjects.getString("userCall"));
                        if (jsonObjects.has("call")) {
                            hashMap.put("call", jsonObjects.getString("call"));
                        } else {
                            hashMap.put("call", "");
                        }

                        arrayList.add(hashMap);
                    }

                } else {
                    AppUtils.showToastSort(mActivity, jsonObject.getString("resMsg"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();

        }


        private void getCallHistory () {

            DirectCallLogListQuery.Params params = new DirectCallLogListQuery.Params();
            DirectCallLogListQuery listQuery = SendBirdCall.createDirectCallLogListQuery(params);

            listQuery.next(new DirectCallLogListQueryResultHandler() {
                @Override
                public void onResult(List<DirectCallLog> callLogs, SendBirdException e) {
                    if (e == null) {

                        if (adapter == null) {
                            // adapter = new Adapter(callLogs);
                            b.rvList.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        if (listQuery.hasNext() && !listQuery.isLoading()) {


                            // The listQuery.next() can be called once more to fetch more call logs.
                        }
                    } else {
                        Log.v("sbqqs", e.getMessage());
                    }
                }
            });
        }

        private class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

            // List<DirectCallLog> callLogs;

            ArrayList<HashMap<String, String>> data;

            public Adapter(ArrayList<HashMap<String, String>> arrayList) {

                data = arrayList;
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inflate_call_history, viewGroup, false);
                return new MyViewHolder(view);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

                if (data.get(position).get("call").equalsIgnoreCase("") || data.get(position).get("call").equalsIgnoreCase(null) || data.get(position).get("call").equalsIgnoreCase("null")) {

                } else {
                    if (data.get(position).get("call").equalsIgnoreCase("outgoing")) {
                        holder.ivCallType.setImageResource(R.drawable.ic_call_outgoing);
                    } else {
                        holder.ivCallType.setImageResource(R.drawable.ic_call_incoming);
                    }
                }
                holder.tvDateTime.setText(AppUtils.changeDateTimeFormat(data.get(position).get("callStartDateTIme")));

                holder.tvDuration.setText(data.get(position).get("callDuration"));
                getUserData(data.get(position).get("userCall"), holder.tvName, position, holder.tvDateTime, holder.ivImage);
            }

            @Override
            public int getItemCount() {
                return data.size();
            }

            public class MyViewHolder extends RecyclerView.ViewHolder {

                TextView tvName, tvDuration, tvDateTime;

                ImageView ivCallType, ivImage;

                public MyViewHolder(@NonNull View itemView) {
                    super(itemView);

                    ivCallType = itemView.findViewById(R.id.ivCallType);
                    ivImage = itemView.findViewById(R.id.ivImage);

                    tvName = itemView.findViewById(R.id.tvName);
                    tvDuration = itemView.findViewById(R.id.tvDuration);
                    tvDateTime = itemView.findViewById(R.id.tvDateTime);
                }
            }
        }

        private void getUserData (String strUserCall, TextView tvName,int position, TextView
        tvDateTime, ImageView ivImage){
            arrayUserCallList.clear();
            try {
                JSONArray jsonArray = new JSONArray(strUserCall);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObjects = jsonArray.getJSONObject(i);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("userId", jsonObjects.getString("userId"));
                    hashMap.put("name", jsonObjects.getString("name"));
                    hashMap.put("profileImage", jsonObjects.getString("profileImage"));
                    hashMap.put("joinCallDateTime", jsonObjects.getString("joinCallDateTime"));
                    arrayUserCallList.add(hashMap);
                }

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            tvName.setText(arrayUserCallList.get(0).get("name"));


        }

        @Override
        protected void onResume () {
           // hitCallHistoryListApi();
            super.onResume();
        }

}