package movie.software.com.spax.custom;

/**
 * Created by fadhili on 6/23/2015.
 */

import android.annotation.TargetApi;
import android.os.Build;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CustomHttpClient {
    String strResult = "999";

    // constructor
    public CustomHttpClient() {

    }

    // function get json from url
    // HttpURLConnection
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String makeHttpRequest(List<NameValuePair> nameValuePair, String APIURL) throws Exception {

        String url = APIURL;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        String urlParameters = "";
        for (NameValuePair nvp : nameValuePair) {
            urlParameters += nvp.getName() + "=" + nvp.getValue() + "&";
            //strToHash += nvp.getValue();
        }
        urlParameters = urlParameters.substring(0, urlParameters.length() - 1);
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        //System.out.println(response.toString());

        return response.toString();

    }

    //Http Post
    public String HttpPostRequest(List<NameValuePair> nameValuePair, String APIURL) {
        //Post Data

        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used.
        int timeoutConnection = 30000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT)
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 35000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(APIURL);

        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
        }
        //making POST request.
        try {
            HttpResponse response = httpClient.execute(httpPost);
            strResult = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            // Log exception
            strResult = "97";
            e.printStackTrace();
        } catch (IOException e) {
            // Log exception
            strResult = "97";
            e.printStackTrace();
            //statusView.setText("A problem occurred during connection!!!");
        }
        return strResult;
    }
}