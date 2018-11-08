package movie.software.com.spax.loginregistration;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import movie.software.com.spax.MainActivity;
import movie.software.com.spax.R;
import movie.software.com.spax.custom.HttpPostClass;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static String deviceid, strResult;
    private Boolean isLoginFailed = false;
    private static ProgressDialog progressDialog;

    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.btn_login)
    Button _loginButton;
    @Bind(R.id.link_signup)
    TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        /* All version with SDK_INT < 22 grant permissions on install time. */
        //this is where we check for permissions for android 6 and above
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askPermissions();
        } else {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                deviceid = telephonyManager.getDeviceId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validate())
                    new processLogin().execute();
                else
                    showToastFromBackground("Enter valid details!");
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                intent.putExtra("DeviceIMEI", deviceid);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public static final int MY_PERMISSIONS_REQUEST_STATE = 99;

    public void askPermissions() {
        /* All version with SDK_INT < 22 grant permissions on install time. */
        if (Build.VERSION.SDK_INT > 22) {
            ArrayList<String> arrPerm = new ArrayList<>();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                arrPerm.add(Manifest.permission.READ_PHONE_STATE);
            } else {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    deviceid = telephonyManager.getDeviceId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!arrPerm.isEmpty()) {
                String[] permissions = new String[arrPerm.size()];
                permissions = arrPerm.toArray(permissions);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_STATE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        String permission = permissions[i];
                        if (Manifest.permission.READ_PHONE_STATE.equals(permission)) {
                            //if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                // you now have permission
                                try {
                                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                    deviceid = telephonyManager.getDeviceId();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showToastFromBackground("Application might not function properly! You need to allow permissions on the app!");
                            }
                            //}
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showToastFromBackground("Application might not function properly! You need to allow permissions on the app!");
                    //showToastFromBackground("Grant all permissions.");
                    finish();
                }
                break;
            }
        }
    }

    /*
     * Async Task to make http call
     */
    private class processLogin extends AsyncTask<Void, Void, Void> {
        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            _loginButton.setEnabled(false);

            progressDialog = new ProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.setProgress(0);
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                final String email = _emailText.getText().toString();
                final String password = _passwordText.getText().toString();
                // On complete call either onLoginSuccess or onLoginFailed
                List<NameValuePair> nameValuePair = new ArrayList<>();
                nameValuePair.add(new BasicNameValuePair("Request", "1"));
                nameValuePair.add(new BasicNameValuePair("UserID", email));
                nameValuePair.add(new BasicNameValuePair("DeviceIMEI", deviceid));
                nameValuePair.add(new BasicNameValuePair("Password", password));
                strResult = HttpPostClass.PostAsIs(nameValuePair);
                String[] Split = strResult.split(":");
                if (Split[0].equals("00")) {
                    //00-John-Spax-0714702094-860648035729982-johnspax@gmail.com-False-----
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("FirstName", Split[1]);
                    editor.putString("LastName", Split[2]);
                    editor.putString("UserID", Split[3]);
                    editor.putString("Email", Split[3]);
                    editor.putString("Phone", Split[5]);
                    editor.putString("HomeLong", Split[6]);
                    editor.putString("HomeLat", Split[7]);
                    editor.putString("ProfilePath", Split[8].replace('-', ':'));
                    editor.putString("Password", password);
                    editor.putString("DeviceIMEI", deviceid);
                    editor.apply();
                    onLoginSuccess();
                } else if (Split[0].equals("98")) {
                    showToastFromBackground("Connection is very slow, check data/wifi connectivity!");
                } else {
                    isLoginFailed = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToastFromBackground("An error has occurred in the application!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                if (isLoginFailed == true) {
                    showToastFromBackground("Login failed");
                }
                _loginButton.setEnabled(true);
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                showToastFromBackground("An error has  occurred!");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        //_loginButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        showToastFromBackground("Login failed");
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
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
