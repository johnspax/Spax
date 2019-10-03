/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package movie.software.com.spax.loginregistration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import movie.software.com.spax.R;
import movie.software.com.spax.camerafileupload.AndroidMultiPartEntity;
import movie.software.com.spax.custom.HttpPostClass;
import movie.software.com.spax.other.CircleTransform;
import movie.software.com.spax.src.Config;
import movie.software.com.spax.utils.ImagePicker;

public class ProfileSettings extends AppCompatActivity {

    private EditText _fname, _lname, _etMobile, _etEmail, _etLocation;
    private RelativeLayout _rlMobiletv, _rlMobileet, _rlEmailtv, _rlEmailet, _rlLocationtv, _rlLocationet;
    private ImageView _profileheaderimg, _profileimg, imageView,_ivHomeLocation;
    private TextView _tvMobile, _tvEmail, _tvLocation, _tvHomeLocation;
    private Button _btnSave, _btnCancel, _btnSaveHome, _btnCancelHome;
    public static final String EXTRA_NAME = "cheese_name";
    private String strResult, UserID, Status = "99", newProfImg="";
    private static final int PICK_IMAGE_ID = 234, PICK_HOME_LOCATION = 254; // the number doesn't matter

    SharedPreferences preferences;
    private ProgressBar progressBar;
    private TextView txtPercentage;
    long totalSize = 0;
    private File filePath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra(EXTRA_NAME);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);

        imageView = (ImageView) findViewById(R.id.backdrop);
        _fname = (EditText) findViewById(R.id.input_fname);
        _lname = (EditText) findViewById(R.id.input_lname);
        _etMobile = (EditText) findViewById(R.id.etMobile);
        _etEmail = (EditText) findViewById(R.id.etEmail);
        _etLocation = (EditText) findViewById(R.id.etLocation);
        _rlMobiletv = (RelativeLayout) findViewById(R.id.rlMobiletv);
        _rlMobileet = (RelativeLayout) findViewById(R.id.rlMobileet);
        _rlEmailtv = (RelativeLayout) findViewById(R.id.rlEmailtv);
        _rlEmailet = (RelativeLayout) findViewById(R.id.rlEmailet);
        _rlLocationtv = (RelativeLayout) findViewById(R.id.rlLocationtv);
        _rlLocationet = (RelativeLayout) findViewById(R.id.rlLocationet);
        _tvMobile = (TextView) findViewById(R.id.tvNumber1);
        _tvEmail = (TextView) findViewById(R.id.tvNumber3);
        _tvLocation = (TextView) findViewById(R.id.tvNumber5);
        _tvHomeLocation = (TextView) findViewById(R.id.tvHomeLocation);
        _ivHomeLocation = (ImageView) findViewById(R.id.ivContactItem5);
        _btnSave = (Button) findViewById(R.id.btnSave);
        _btnCancel = (Button) findViewById(R.id.btnCancel);
        _btnSaveHome = (Button) findViewById(R.id.btnSaveHome);
        _btnCancelHome = (Button) findViewById(R.id.btnCancelHome);
        _btnSave.setVisibility(View.GONE);
        _btnCancel.setVisibility(View.GONE);
        _btnSaveHome.setVisibility(View.GONE);
        _btnCancelHome.setVisibility(View.GONE);

        _profileimg = (ImageView) findViewById(R.id.img_profile);

        setAllProfileInfo();

        _profileimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(ProfileSettings.this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });

        _tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ProfileSettings.this), PICK_HOME_LOCATION);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                _tvLocation.setBackgroundColor(Color.LTGRAY);//set the color here
                _tvHomeLocation.setBackgroundColor(Color.LTGRAY);
                showToastFromBackground("Please wait a moment...");
            }
        });
        _ivHomeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ProfileSettings.this), PICK_HOME_LOCATION);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                _tvLocation.setBackgroundColor(Color.LTGRAY);//set the color here
                _tvHomeLocation.setBackgroundColor(Color.LTGRAY);
                showToastFromBackground("Please wait a moment...");
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_icon);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Edit Profile and hit save.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //You have to enable these features
                _fname.setEnabled(true);
                _fname.setFocusable(true);
                _fname.setFocusableInTouchMode(true);
                _fname.requestFocus();
                _lname.setEnabled(true);
                _lname.setFocusable(true);
                _lname.setFocusableInTouchMode(true);
                //These Layouts have to be visible/gone
                _rlMobiletv.setVisibility(View.GONE);
                _rlMobileet.setVisibility(View.VISIBLE);
                _rlEmailtv.setVisibility(View.GONE);
                _rlEmailet.setVisibility(View.VISIBLE);
                //_rlLocationtv.setVisibility(View.GONE);
                _rlLocationet.setVisibility(View.GONE);
                _btnSave.setVisibility(View.VISIBLE);
                _btnCancel.setVisibility(View.VISIBLE);

            }
        });

        _btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateRecords();
            }
        });
        _btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _fname.setEnabled(false);
                _fname.setFocusable(false);
                _fname.setFocusableInTouchMode(false);
                _lname.setEnabled(false);
                _lname.setFocusable(false);
                _lname.setFocusableInTouchMode(false);
                //These Layouts have to be visible/gone
                _rlMobiletv.setVisibility(View.VISIBLE);
                _rlMobileet.setVisibility(View.GONE);
                _rlEmailtv.setVisibility(View.VISIBLE);
                _rlEmailet.setVisibility(View.GONE);
                //_rlLocationtv.setVisibility(View.GONE);
                _rlLocationet.setVisibility(View.VISIBLE);
                _btnSave.setVisibility(View.GONE);
                _btnCancel.setVisibility(View.GONE);
            }
        });
    }

    private void setAllProfileInfo() {
        // loading header background image
        Glide.with(this).load(R.drawable.nav_menu_header_bg).centerCrop().into(imageView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
        String ProfImg = preferences.getString("profile_img", "profile_img.png");
        String ProfileUrl = preferences.getString("ProfilePath", "");
        // Loading profile image
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "SpaxMovieDB" + File.separator);
        File file = new File(root, ProfImg);
        //Read image from file resource
        //Was changed now reads from saved url in server
        //if (file.exists()) {
        if (!ProfileUrl.equals("")) {
            Glide.with(this).load(ProfileUrl)
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(_profileimg);
        } else {
            //Read image from drawable resource
            Glide.with(this).load(R.drawable.profile_default)
                    .crossFade()
                    .thumbnail(0.5f)
                    .bitmapTransform(new CircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(_profileimg);
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
        UserID = sp.getString("UserID", "");
        _fname.setText(sp.getString("FirstName", ""));
        _lname.setText(sp.getString("LastName", ""));
        _etEmail.setText(sp.getString("Email", ""));
        _tvEmail.setText(sp.getString("Email", ""));
        _etMobile.setText(sp.getString("Phone", ""));
        _tvMobile.setText(sp.getString("Phone", ""));
        /*_etLocation.setText(sp.getString("Location", "254, Kenya, Africa"));*/
        _tvLocation.setText(sp.getString("HomeName", "Add Home Location"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "SpaxMovieDB" + File.separator);
            if (!root.exists())
                root.mkdirs();
            switch (requestCode) {
                case PICK_IMAGE_ID:
                    //we need a random pic name
                    Random r = new Random();
                    int i = r.nextInt(1000 - 10) + 10;
                    //String ProfImg = "profile_img" + i + ".png";
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
                    String ProfImg = preferences.getString("profile_img", "profile_img.png");
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                    // TODO use bitmap
                    if (bitmap != null) {
                        File file = new File(root, ProfImg);
                        if (file.exists()) {
                            file.delete();
                        }
                        try {
                            newProfImg = "profile_img" + i + ".png";
                            file = new File(root, newProfImg);
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            out.close();

                            filePath = file;

                            // uploading the file to server
                            new UploadFileToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case PICK_HOME_LOCATION:
                    if (resultCode == RESULT_OK) {
                        Place place = PlacePicker.getPlace(data, this);
                        //set home location in shared prefs
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("HomeName", place.getName().toString());
                        editor.putFloat("HomeLat", (float) place.getLatLng().latitude);
                        editor.putFloat("HomeLong", (float) place.getLatLng().longitude);
                        editor.apply();
                        //This is how you will get the Longitute/Latitude back:
                        /*double lat = (double)prefs.getFloat("HomeLat", 0);
                        double lon = (double)prefs.getFloat("HomeLong", 0);*/
                        _tvLocation.setText(place.getName());
                        _tvLocation.setBackgroundColor(Color.rgb(238,238,238));//set the color here
                        _tvHomeLocation.setBackgroundColor(Color.rgb(238,238,238));
                        String toastMsg = String.format("Place: %s", place.getName());
                        Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateRecords() {
        Log.d("Profile", "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        //_signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ProfileSettings.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setProgress(0);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        // TODO: Implement your own signup logic here.

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while (jumpTime < totalProgressTime) {
                    try {
                        String[] UserInfo;
                        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                        nameValuePair.add(new BasicNameValuePair("Request", "4"));
                        nameValuePair.add(new BasicNameValuePair("FirstName", _fname.getText().toString()));
                        nameValuePair.add(new BasicNameValuePair("LastName", _lname.getText().toString()));
                        nameValuePair.add(new BasicNameValuePair("UserID", _etEmail.getText().toString()));
                        nameValuePair.add(new BasicNameValuePair("Email", _etEmail.getText().toString()));
                        nameValuePair.add(new BasicNameValuePair("Phone", _etMobile.getText().toString()));
                        strResult = HttpPostClass.PostAsIs(nameValuePair);
                        UserInfo = strResult.split("-");
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (UserInfo[0].toString().equals("00")) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("FirstName", _fname.getText().toString());
                            editor.putString("LastName", _lname.getText().toString());
                            editor.putString("UserID", _etEmail.getText().toString());
                            editor.putString("Email", _etEmail.getText().toString());
                            editor.putString("Phone", _etMobile.getText().toString());
                            editor.apply();
                            onUpdateSuccess();
                        } else
                            showToastFromBackground("Invalid Password Entered!");
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

    public void onUpdateSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _btnSave.setEnabled(true);
                //You have to disable these features
                _fname.setEnabled(false);
                _fname.setFocusable(false);
                _fname.setFocusableInTouchMode(false);
                _lname.setEnabled(false);
                _lname.setFocusable(false);
                _lname.setFocusableInTouchMode(false);
                //These Layouts have to be visible/gone
                _rlMobiletv.setVisibility(View.VISIBLE);
                _rlMobileet.setVisibility(View.GONE);
                _rlEmailtv.setVisibility(View.VISIBLE);
                _rlEmailet.setVisibility(View.GONE);
                _rlLocationtv.setVisibility(View.VISIBLE);
                _rlLocationet.setVisibility(View.GONE);
                _btnSave.setVisibility(View.GONE);
                _btnCancel.setVisibility(View.GONE);
            }
        });
        setResult(RESULT_OK, null);
    }

    /**
     * Uploading the file to server
     */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            //Making percentager textview visible
            txtPercentage.setVisibility(View.VISIBLE);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                //File sourceFile = new File(filePath.getPath());

                // Adding file data to http body
                entity.addPart("profile_image", new FileBody(filePath));

                // Extra parameters if you want to pass to server
                entity.addPart("request", new StringBody("15"));
                entity.addPart("UserID", new StringBody(preferences.getString("UserID", "")));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("SERVER RESPONSE", "Response from server: " + result);

            progressBar.setVisibility(View.GONE);
            txtPercentage.setVisibility(View.GONE);

            //set new pic in shared prefs folder
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ProfileSettings.this);
            String[] Split = prefs.getString("UserID", "").split("@");
            String oldProfilePath = prefs.getString("ProfilePath","");
            String ProfilePath = oldProfilePath.substring(0, oldProfilePath.lastIndexOf("/") + 1) //"http://195.202.86.4/RequestProcessor/ProfileAdmin/"
                    + Split[0] + "_" + newProfImg;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profile_img", newProfImg);
            editor.putString("ProfilePath", ProfilePath);
            editor.apply();

            //Update User info
            setAllProfileInfo();

            // showing the server response in an alert dialog
            showToastFromBackground("Success! Loading image...");
            //showAlert(result);

            super.onPostExecute(result);
        }
    }

    public void onSignupFailed() {
        showToastFromBackground("Login failed");
        _btnSave.setEnabled(true);
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

    public boolean validate() {
        boolean valid = true;
        String fname, lname, username, email, mobile;
        fname = _fname.getText().toString();
        lname = _lname.getText().toString();
        email = _etEmail.getText().toString();
        mobile = _etMobile.getText().toString();

        if (fname.isEmpty() || fname.length() < 3) {
            _fname.setError("at least 3 characters");
            valid = false;
        } else {
            _fname.setError(null);
        }

        if (lname.isEmpty() || lname.length() < 3) {
            _lname.setError("at least 3 characters");
            valid = false;
        } else {
            _lname.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _etEmail.setError("enter a valid email address");
            valid = false;
        } else {
            _etEmail.setError(null);
        }

        if (mobile.isEmpty() || mobile.length() != 10) {
            _etMobile.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _etMobile.setError(null);
        }

        return valid;
    }

    @Override
    public void onResume() {
        super.onResume();
        _tvLocation.setBackgroundColor(Color.rgb(238,238,238));//set the color here
        _tvHomeLocation.setBackgroundColor(Color.rgb(238,238,238));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);*/
        finish();
    }
}
