package code.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    protected BaseActivity mActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivity = (BaseActivity) getActivity();
    }
}
