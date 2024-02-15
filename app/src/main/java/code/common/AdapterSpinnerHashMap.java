package code.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.hathme.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterSpinnerHashMap extends ArrayAdapter<HashMap<String, String>> {

    ArrayList<HashMap<String, String>> data;

    public AdapterSpinnerHashMap(Context context, int resource, ArrayList<HashMap<String, String>> processName) {

        super(context, resource, processName);

        this.data = processName;


    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.adapter_spinner, parent, false);

        TextView tvName = row.findViewById(R.id.tvName);

        tvName.setText(data.get(position).get("name"));


        return row;
    }
}