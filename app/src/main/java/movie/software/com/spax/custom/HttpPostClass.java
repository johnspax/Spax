package movie.software.com.spax.custom;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static movie.software.com.spax.Global.GlobalClass.APIURL;

/**
 * Created by John Muya on 11/4/2015.
 */
public class HttpPostClass extends AsyncTask<String, Void, String> {
    public static String POST(List<NameValuePair> nameValuePair) {
        String strResult = "A problem occurred during connection! Please check internet connection.";
        //Post Data
        //making POST request.
        try {
            String responseStr = "";
            CustomHttpClient chc = new CustomHttpClient();
            try {
                responseStr = chc.makeHttpRequest(nameValuePair, APIURL);
            } catch (Exception e) {
                e.printStackTrace();
                responseStr = "<!";
            }
            String resp = responseStr.subSequence(0, 2).toString();
            // write response to log
            Log.d("Http Post Response:", responseStr.toString());
            if (resp.equals("<!")) {
                strResult = "A problem occurred during the operation! Please contact your nearest branch for more assistance!";
            } else if (resp.equals("00") || resp.equals("01")) {
                strResult = responseStr;
            } else if (resp.equals("98")) {
                strResult = "Your account has been temporarily disabled, Please contact your bank!";
            } else if (resp.equals("97")) {
                strResult = "A problem occurred during connection! Please check internet connection.";
            } else {
                strResult = "Invalid username or password!";
            }
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            strResult = "A problem occurred during connection! Please check internet connection.";
        }
        return strResult;
    }

    public static String PostAsIs(List<NameValuePair> nameValuePair) {
        String strResult = "";
        //Post Data
        //making POST request.
        try {
            String responseStr = "";
            CustomHttpClient chc = new CustomHttpClient();
            try {
                responseStr = chc.makeHttpRequest(nameValuePair, APIURL);
            } catch (Exception e) {
                e.printStackTrace();
                responseStr = "98";
            }
            // write response to log
            Log.d("Http Post Response:", responseStr.toString());
            strResult = responseStr;
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            strResult = "98";
        }
        return strResult;
    }

    public static String POSTJSON(List<NameValuePair> nameValuePair) {
        String responsecode = "000", strMessage;
        String strResult = "";
        strResult = "{\\\"data\\\" :[{\\\"responsecode\\\":\\\"999\\\",\\\"description\\\":\\\"A problem occurred during connection!\\\"}]}";
        //Post Data
        //making POST request.
        try {
            String responseStr = "";
            CustomHttpClient chc = new CustomHttpClient();
            try {
                strResult = chc.makeHttpRequest(nameValuePair, APIURL);
                JSONObject jsonRootObject = new JSONObject(strResult);
                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                //Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    responsecode = jsonObject.optString("responsecode").toString();
                    strMessage = jsonObject.optString("Message").toString();
                    if (responsecode.equals("000")) {

                    } else {

                    }
                }
            } catch (Exception e) {
                responsecode = "999";
                e.printStackTrace();
            }
            // write response to log
            Log.d("Http Post Response:", strResult.toString());

        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
        }
        return responsecode;
    }

    @Override
    protected String doInBackground(String... params) {
        return null;
    }
}
