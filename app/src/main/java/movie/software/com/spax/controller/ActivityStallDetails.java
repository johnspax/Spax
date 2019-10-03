package movie.software.com.spax.controller;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

import movie.software.com.spax.Global.GlobalClass;
import movie.software.com.spax.MainActivity;
import movie.software.com.spax.R;
import movie.software.com.spax.adapter.GoogleDirectionAPI;
import movie.software.com.spax.custom.HttpPostClass;

import static android.view.View.INVISIBLE;
import static movie.software.com.spax.MainActivity.fa;

/**
 * Created by John Muya on 30/03/2017.
 */

public class ActivityStallDetails extends AppCompatActivity {
    String StallName, StallMobile, StallEmail, StallID, jsonMarkers, iLat, iLon, iName, MovieCartJson, strResult, ProfilePath = "";
    Double StallLat, StallLong;
    TextView _stallName, _stallMobile, _stallEmail;
    Button _btnGo, _placeOrder;
    ImageView _imgProfile;
    private boolean Status = false;
    private SharedPreferences prefs;
    private CoordinatorLayout coordinatorLayout;

    private MainActivity mActivity;
    private ActivityStallDetails activity;
    private CircledImageView moreIcon;
    private CircledImageView navigationIcon;
    private CircledImageView callIcon;
    private CircledImageView smsIcon;
    private int moreIconCheck;
    private int navigationIconCheck;
    private int callIconCheck;
    private int smsIconCheck;
    private int iconDirection;
    private int iconMarginConstant;
    private int iconMarginLandscape;
    private int iconConstantSpecialCase;
    private float scale;

    private onMoreIconClick onMoreIconClick;
    private onNavigationIconClick onNavigationIconClick;
    private onCallIconClick onCallIconClick;
    private onSmsIconClick onSmsIconClick;

    private TranslateAnimation iconUpAnimation;
    private IconUpAnimationListener iconUpAnimationListener;
    private TranslateAnimation iconDownAnimation;
    private IconDownAnimationListener iconDownAnimationListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stalldetails);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Fab Stuff Here
        mActivity = fa;
        activity = (ActivityStallDetails.this);
        onCallIconClick = new onCallIconClick();
        onSmsIconClick = new onSmsIconClick();
        onMoreIconClick = new onMoreIconClick();
        onNavigationIconClick = new onNavigationIconClick();
        iconUpAnimationListener = new IconUpAnimationListener();
        iconDownAnimationListener = new IconDownAnimationListener();
        scale = getResources().getDisplayMetrics().density;

        navigationIcon = (CircledImageView) findViewById(R.id.navigateIcon);
        navigationIcon.bringToFront();
        navigationIcon.setVisibility(INVISIBLE);
        callIcon = (CircledImageView) findViewById(R.id.callIcon);
        callIcon.bringToFront();
        callIcon.setVisibility(INVISIBLE);
        smsIcon = (CircledImageView) findViewById(R.id.smsIcon);
        smsIcon.bringToFront();
        smsIcon.setVisibility(INVISIBLE);

        // Highest Z-index has to be declared last
        moreIcon = (CircledImageView) findViewById(R.id.moreIcon);
        moreIcon.bringToFront();
        moreIcon.setOnClickListener(onMoreIconClick);
        navigationIcon.setOnClickListener(onNavigationIconClick);
        callIcon.setOnClickListener(onCallIconClick);
        smsIcon.setOnClickListener(onSmsIconClick);

        iconMarginConstant = mActivity.getIconMarginConstant();
        iconMarginLandscape = mActivity.getIconMarginLandscape();
        iconConstantSpecialCase = mActivity.getIconConstantSpecialCase();

        Intent intent = getIntent();
        iLat = intent.getStringExtra("Lat");
        iLon = intent.getStringExtra("Lon");
        iName = intent.getStringExtra("Title");

        _stallName = (TextView) findViewById(R.id.titleName);
        _stallMobile = (TextView) findViewById(R.id.stallMobile);
        _stallEmail = (TextView) findViewById(R.id.stallEmail);
        _imgProfile = (ImageView) findViewById(R.id.thumbnail);
        _btnGo = (Button) findViewById(R.id.btnGo);
        _placeOrder = (Button) findViewById(R.id.btnPlaceOrder);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        //Extract the details of the stall
        GetStallDetails();
        _stallName.setText(StallName);
        _stallEmail.setText(StallEmail);
        _stallMobile.setText(StallMobile);
        if (!ProfilePath.equals(""))
            Picasso.with(this)
                    .load(ProfilePath)
                    .fit()
                    .into(_imgProfile);
        //_imgProfile.setBackgroundResource(ProfilePath);

        //transform(getBitmapFromURL(ProfilePath));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        MovieCartJson = prefs.getString("MovieCart", "");
        if (MovieCartJson.equals("")) {
            _placeOrder.setVisibility(View.GONE);
        } else {
            _placeOrder.setVisibility(View.VISIBLE);
        }

        _btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNavigationData(StallLat, StallLong);
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });

        _placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Place order at: " + StallName, 5000)
                        .setAction("ORDER", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    PlaceOrder();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Your order is successful!", Snackbar.LENGTH_SHORT);
                                snackbar1.show();
                            }
                        });
                snackbar.show();
            }
        });
    }

    public void PlaceOrder() {
        Log.d("StallDetails", "Login");

        final ProgressDialog progressDialog = new ProgressDialog(ActivityStallDetails.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        // TODO: Implement your own authentication logic here.

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;
                while (jumpTime < totalProgressTime) {
                    try {
                        // On complete call either onLoginSuccess or onLoginFailed
                        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                        nameValuePair.add(new BasicNameValuePair("Request", "6"));
                        nameValuePair.add(new BasicNameValuePair("UserID", prefs.getString("UserID", "")));
                        nameValuePair.add(new BasicNameValuePair("Phone", prefs.getString("Phone", "")));
                        nameValuePair.add(new BasicNameValuePair("StallID", StallID));
                        nameValuePair.add(new BasicNameValuePair("OrderJsonList", MovieCartJson.replace("\\", "")));
                        strResult = HttpPostClass.PostAsIs(nameValuePair);
                        String[] Split = strResult.split("-");
                        if (Split[0].equals("00")) {
                            SharedPreferences.Editor ed = prefs.edit();
                            ed.putString("MovieCart", "");
                            //ed.clear();
                            ed.apply();
                            onSuccess();
                            showToastFromBackground("Success!");
                        } else if (Split[0].equals("99")) {
                            onFailed();
                            //showToastFromBackground("A problem occurred during the operation!");
                        } else {
                            showToastFromBackground("A problem occurred during the operation!");
                        }
                        // onLoginFailed();
                        jumpTime += 100;
                        progressDialog.setProgress(jumpTime);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
            }
        };
        t.start();
    }

    public void onSuccess() {
        _placeOrder.setEnabled(true);
        runOnUiThread(new Runnable() {
            public void run() {
                //Do something on UiThread
                _placeOrder.setVisibility(View.INVISIBLE);
            }
        });

    }

    public void onFailed() {
        showToastFromBackground("A problem occurred during the operation!");
        _placeOrder.setEnabled(true);
    }

    /**
     * Class which listens when the user has tapped on More icon button.
     */
    public class onMoreIconClick implements View.OnClickListener {
        private boolean key;

        public onMoreIconClick() {
            // keep references for your onClick logic
        }

        public boolean getKey() {
            return key;
        }

        public void setKey(boolean key) {
            this.key = key;
        }

        @Override
        public void onClick(View v) {
            if (!key) {
                iconDirection = 1;
                moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_close_white_36dp));
                showHideImages(View.VISIBLE, navigationIcon, smsIcon, callIcon);
                key = true;
            } else {

                moreIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_more_vert_white_36dp));
                showHideImages(INVISIBLE, navigationIcon, smsIcon, callIcon);
                key = false;
            }
        }
    }

    /**
     * Class which listens when the user has tapped on Home icon button.
     */
    public class onNavigationIconClick implements View.OnClickListener {
        public onNavigationIconClick() {
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            setNavigationData(StallLat, StallLong);
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    /**
     * Class which listens when the user has tapped on Call icon button.
     */
    public class onCallIconClick implements View.OnClickListener {
        public onCallIconClick() {
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            //Do Something here
            try {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(ActivityStallDetails.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // Asking user if explanation is needed
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ActivityStallDetails.this,
                                Manifest.permission.CALL_PHONE)) {

                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(ActivityStallDetails.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    fa.MY_PERMISSIONS_REQUEST_LOCATION);


                        } else {
                            // No explanation needed, we can request the permission.
                            ActivityCompat.requestPermissions(ActivityStallDetails.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    fa.MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                        //return;
                    } else {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + _stallMobile.getText().toString()));
                        startActivity(callIntent);
                    }
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + _stallMobile.getText().toString()));
                    startActivity(callIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) {

                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + _stallMobile.getText().toString()));
                        startActivity(callIntent);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    /**
     * Class which listens when the user has tapped on Trailer icon button.
     */
    public class onSmsIconClick implements View.OnClickListener {
        public onSmsIconClick() {
            // keep references for your onClick logic
        }


        @Override
        public void onClick(View v) {
            //Do Something here
            String uri = "smsto:" + _stallMobile.getText().toString();
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
            intent.putExtra("sms_body", "");
            intent.putExtra("compose_mode", true);
            startActivity(intent);
            //finish();
        }
    }

    /**
     * Fired from the on More Icon click listeners. Updates the visibility of the gallery and homePage icon.
     * And creates animation for them also.
     *
     * @param visibility     visibility value
     * @param navigationIcon first icon
     * @param callIcon       second icon
     */
    public void showHideImages(int visibility, CircledImageView navigationIcon, CircledImageView smsIcon, CircledImageView callIcon) {
        //float dy[] = {0.7f, 56.7f, 112.5f};
        float dy[] = {0.0f, 0.0f, 0.0f};
        float infoTabDy[] = {-2.4f, 53.5f, 109.25f};
        int currDy = 0;
        int delay = 100;
        int iconCount[] = {navigationIconCheck, smsIconCheck, callIconCheck};
        ArrayList<CircledImageView> circledImageViews = new ArrayList<>();
        circledImageViews.add(navigationIcon);
        circledImageViews.add(smsIcon);
        circledImageViews.add(callIcon);

        for (int i = 0; i < iconCount.length; i++) {
            if (iconCount[i] == 1)
                circledImageViews.get(circledImageViews.size() - 1).setVisibility(INVISIBLE);
            else {
                CircledImageView temp = circledImageViews.get(0);
                if (visibility == View.VISIBLE) {
                    createIconUpAnimation(dy[currDy], delay);
                    temp.startAnimation(iconUpAnimation);
                } else {
                    createIconDownAnimation(dy[currDy]);
                    temp.startAnimation(iconDownAnimation);
                }
                currDy++;
                delay -= 50;
                temp.setVisibility(visibility);
                circledImageViews.remove(0);
            }

        }

    }

    /**
     * Creates animation for the gallery and gallery, homePage and trailer Icons with up direction.
     */
    public void createIconUpAnimation(float dy, int delay) {
        iconUpAnimation = new TranslateAnimation(0, 0, 0, (-(scale * 0.3f) + 0.5f - (dy * scale)) * iconDirection);
        iconUpAnimation.setDuration(250);
        iconUpAnimation.setFillAfter(false);
        iconUpAnimation.setStartOffset(delay);
        iconUpAnimation.setAnimationListener(iconUpAnimationListener);
    }

    /**
     * Creates animation for the gallery, homePage and trailer Icons with down direction.
     */
    public void createIconDownAnimation(float dy) {
        iconDownAnimation = new TranslateAnimation(0, 0, 0, ((scale * 0.3f) + 0.5f + (dy * scale)) * iconDirection);
        iconDownAnimation.setDuration(250);
        iconDownAnimation.setFillAfter(false);
        iconDownAnimation.setAnimationListener(iconDownAnimationListener);
    }

    /**
     * Listener which updates the icons position after the animation ended.
     */
    private class IconUpAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            updateIconUpPos();
            navigationIcon.clearAnimation();
            smsIcon.clearAnimation();
            callIcon.clearAnimation();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
     * Listener which updates the icons position after the animation ended.
     */
    private class IconDownAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            updateIconDownPos();
            navigationIcon.clearAnimation();
            smsIcon.clearAnimation();
            callIcon.clearAnimation();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }

    /**
     * Updates the icons position when called.
     */
    public void updateIconDownPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(navigationIcon.getWidth(), navigationIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(smsIcon.getWidth(), smsIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(callIcon.getWidth(), callIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        navigationIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + +0.5f), (int) (scale * 23 + 0.5f), 0);
        smsIcon.setLayoutParams(lp2);
        lp3.setMargins(0, (int) (scale * (506 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        callIcon.setLayoutParams(lp3);
    }

    /**
     * Updates the icons position when called.
     */
    public void updateIconUpPos() {
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(navigationIcon.getWidth(), navigationIcon.getHeight());
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(smsIcon.getWidth(), smsIcon.getHeight());
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(callIcon.getWidth(), callIcon.getHeight());
        lp1.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_END);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_END);

        lp1.setMargins(0, (int) (scale * (439 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        navigationIcon.setLayoutParams(lp1);
        lp2.setMargins(0, (int) (scale * (383.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        smsIcon.setLayoutParams(lp2);
        lp3.setMargins(0, (int) (scale * (328.3 + iconMarginConstant - iconMarginLandscape + iconConstantSpecialCase) + 0.5f), (int) (scale * 23 + 0.5f), 0);
        callIcon.setLayoutParams(lp3);
    }

    private void GetStallDetails() {
        String responsecode;
        int j = 0;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ActivityStallDetails.this);
        jsonMarkers = preferences.getString("Markers", "");
        try {
            JSONObject jsonRootObject = new JSONObject(jsonMarkers);
            //Get the instance of JSONArray that contains JSONObjects
            JSONArray jsonArray = jsonRootObject.optJSONArray("data");
            //Iterate the jsonArray and print the info of JSONObjects
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                responsecode = jsonObject.optString("responsecode").toString();
                if (jsonObject.optString("StallLat").toString().equals(iLat) && jsonObject.optString("StallLong").toString().equals(iLon) &&
                        jsonObject.optString("StallName").equals(iName)) {
                    j = 1;
                    StallLat = Double.parseDouble(jsonObject.optString("StallLat").toString());
                    StallLong = Double.parseDouble(jsonObject.optString("StallLong").toString());
                    StallName = jsonObject.optString("StallName");
                    StallEmail = jsonObject.optString("Email");
                    StallMobile = jsonObject.optString("MobileNo");
                    StallID = jsonObject.optString("StallID");
                    ProfilePath = jsonObject.optString("ProfilePath");
                }
            }
            if (j == 0) {
                showToastFromBackground("Couldn't get stall details!");
            }
        } catch (JSONException e) {
            showToastFromBackground("An error occurred during the operation!");
            e.printStackTrace();
        }
    }

    private void setNavigationData(final Double lat, final Double lon) {
        new Thread() {
            @Override
            public void run() {
                try {
                    LatLng dLatLng = new LatLng(lat, lon);
                    GlobalClass.setdLatLng(dLatLng);
                    GoogleDirectionAPI md = new GoogleDirectionAPI();
                    Document doc = md.getDocument(GlobalClass.getcLatLng(), dLatLng,
                            GoogleDirectionAPI.MODE_DRIVING);
                    GlobalClass.setDoc(doc);
                } catch (Exception e) {
                    showToastFromBackground("There was an error fetching directions, please try again!");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //Code to show Toast message
    private void showToastFromBackground(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                /*NavUtils.navigateUpFromSameTask(this);*/
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

}
