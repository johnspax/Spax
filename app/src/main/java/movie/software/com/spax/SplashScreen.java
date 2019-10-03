package movie.software.com.spax;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import movie.software.com.spax.custom.HttpPostClass;
import movie.software.com.spax.loginregistration.LoginActivity;

public class SplashScreen extends Activity {

    String now_playing, earned;
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private String deviceid = null, strResult, strStatus;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

		/*
         * Showing splashscreen while making network calls to download necessary
		 * data before launching the app Will use AsyncTask to make http call
		 */
        new PrefetchData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /*
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            Log.e("JSON", "Pre execute");

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
                String uname = preferences.getString("UserID", "");
                if (uname.equals("")) {
                    strStatus = "99";
                } else {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SplashScreen.this);
                    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                    nameValuePair.add(new BasicNameValuePair("Request", "1"));
                    nameValuePair.add(new BasicNameValuePair("UserID", sp.getString("UserID", "")));
                    nameValuePair.add(new BasicNameValuePair("DeviceIMEI", deviceid));
                    nameValuePair.add(new BasicNameValuePair("Password", sp.getString("Password", "")));
                    strResult = HttpPostClass.PostAsIs(nameValuePair);
                    String[] Split = strResult.split(":");
                    strStatus = Split[0];
                    if(strStatus.equals("00")) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("FirstName", Split[1]);
                        editor.putString("LastName", Split[2]);
                        editor.putString("UserID", Split[3]);
                        editor.putString("Email", Split[3]);
                        editor.putString("Phone", Split[5]);
                        editor.putString("DeviceIMEI", deviceid);
                        if(Split.length > 6) {
                            String profPath = Split[8].replace('-', ':');
                            editor.putString("HomeLong", Split[6]);
                            editor.putString("HomeLat", Split[7]);
                            editor.putString("ProfilePath", Split[8].replace('-', ':'));
                        } else {
                            editor.putString("HomeLong", null);
                            editor.putString("HomeLat", null);
                            editor.putString("ProfilePath", null);
                        }
                        editor.apply();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
                showToastFromBackground("An error has occurred in the application!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                // After completing http call
                // will close this activity and lauch main activity
                if (strStatus.equals("00")) {
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else if (strStatus.equals("98")) {

                    builder = new AlertDialog.Builder(SplashScreen.this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Network Error");
                    builder.setMessage("Connection is very slow, please check your data/wifi connectivity!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new PrefetchData().execute();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                    //i.putExtra("DeviceIMEI", deviceid);
                    startActivity(i);
                    finish();
                }
            } catch (Exception e){
                e.printStackTrace();
                showToastFromBackground("An error has  occurred!");
            }
        }
    }

    //Code to show Toast message
    public void showToastFromBackground(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
