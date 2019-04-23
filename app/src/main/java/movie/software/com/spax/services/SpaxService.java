package movie.software.com.spax.services;

/**
 * Created by John Muya on 14/06/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import movie.software.com.spax.MainActivity;
import movie.software.com.spax.R;
import movie.software.com.spax.custom.HttpPostClass;


public class SpaxService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    SharedPreferences preferences;
    String strResult = "",StatusCode, StallID;
    private PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Toast.makeText(this, "SpaxAdmin Service created!", Toast.LENGTH_LONG).show();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                //Toast.makeText(context, "SpaxAdmin Service is still running", Toast.LENGTH_LONG).show();
                new PrefetchData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                handler.postDelayed(runnable, 600000);//10 Minutes
            }
        };

        handler.postDelayed(runnable, 15000); //15 Seconds
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        Toast.makeText(this, "Spax Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        //Toast.makeText(this, "SpaxAdmin Service started by user.", Toast.LENGTH_LONG).show();
    }

    public void ShowNotification() {
        Context ctx = this;

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra("isFromNotification", "True");
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.moviedb_icon)
                .setTicker("Hearty365")
                .setContentTitle("Spax")
                .setContentText("Your order is now ready.")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, b.build());

        /*SharedPreferences.Editor editor = preferences.edit();
        editor.putString("IsNotified", "True");
        editor.apply();*/
    }

    /*
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {
        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                // before making http calls
                //Log.e("JSON", "Pre execute");
                //This part ensures that background task is running even when screen is turned off
                PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
                wakeLock.acquire();
            }catch(Exception e){

            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            StallID = preferences.getString("StallID", "");
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("Request", "22"));
            nameValuePair.add(new BasicNameValuePair("UserID", preferences.getString("UserID","")));
            strResult = HttpPostClass.PostAsIs(nameValuePair);
            try {
                JSONObject jsonRootObject = new JSONObject(strResult);
                //Get the instance of JSONArray that contains JSONObjects
                JSONArray jsonArray = jsonRootObject.optJSONArray("data");
                //Iterate the jsonArray and print the info of JSONObjects
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    StatusCode = jsonObject.optString("responsecode").toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ServiceOrderID", jsonObject.optString("OrderID").toString());
                    editor.apply();
                }
            } catch (JSONException e) {
                //fa.showToastFromBackground("An error occurred during the operation!");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // After completing http call
            // will close this activity and lauch main activity
            //String isNotified = preferences.getString("IsNotified", "False");
            try {
                if (StatusCode.equals("000")) {
                    ShowNotification();
                    wakeLock.release();//release the wake lock
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
