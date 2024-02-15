package code.utils;

import org.json.JSONObject;

/**
 * Created by Mohammad Faiz on 2/2/2020.
 */

public interface WebServicesCallback {

    void OnJsonSuccess(JSONObject response);

    void OnFail(String response);


}
