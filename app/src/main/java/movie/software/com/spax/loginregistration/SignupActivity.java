package movie.software.com.spax.loginregistration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private static ProgressDialog progressDialog;
    private Boolean isSignupFailed = false;

    private String fname,lname,username,email,mobile,password,reEnterPassword,deviceid,strResult;

    @Bind(R.id.input_fname)
    EditText _fnameText;
    @Bind(R.id.input_lname)
    EditText _lnameText;
    @Bind(R.id.input_username)
    EditText _username;
    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_mobile)
    EditText _mobileText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @Bind(R.id.btn_signup)
    Button _signupButton;
    @Bind(R.id.link_login)
    TextView _loginLink;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        deviceid = intent.getStringExtra("DeviceIMEI");

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate())
                    new processSignup().execute();
                else
                    showToastFromBackground("Please review input details!");
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    /*
     * Async Task to make http call
     */
    private class processSignup extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls
            _signupButton.setEnabled(false);

            progressDialog = new ProgressDialog(SignupActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            progressDialog.setProgress(0);
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                fname = _fnameText.getText().toString();
                lname = _lnameText.getText().toString();
                username = _username.getText().toString();
                email = _emailText.getText().toString();
                mobile = _mobileText.getText().toString();
                password = _passwordText.getText().toString();
                reEnterPassword = _reEnterPasswordText.getText().toString();
                // On complete call either onLoginSuccess or onLoginFailed
                String[] UserInfo;
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("Request", "2"));
                nameValuePair.add(new BasicNameValuePair("FirstName", fname));
                nameValuePair.add(new BasicNameValuePair("LastName", lname));
                nameValuePair.add(new BasicNameValuePair("UserID", email));
                nameValuePair.add(new BasicNameValuePair("Email", email));
                nameValuePair.add(new BasicNameValuePair("Phone", mobile));
                nameValuePair.add(new BasicNameValuePair("Password", password));
                nameValuePair.add(new BasicNameValuePair("DeviceIMEI", deviceid));
                strResult = HttpPostClass.PostAsIs(nameValuePair);
                UserInfo = strResult.split("-");
                // On complete call either onLoginSuccess or onLoginFailed
                if (UserInfo[0].equals("00")) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignupActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("FirstName", fname);
                    editor.putString("LastName", lname);
                    editor.putString("UserID", email);
                    editor.putString("Email", email);
                    editor.putString("Phone", mobile);
                    editor.putString("Password", password);
                    editor.putString("DeviceIMEI", deviceid);
                    editor.apply();
                    onSignupSuccess();
                }
                else if (UserInfo[0].equals("98"))
                    showToastFromBackground("User email already registered!");
                else if (UserInfo[0].equals("99"))
                    showToastFromBackground("An error occurred during processing of the current request!");
                else
                    showToastFromBackground("An error has occurred!");
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
                _signupButton.setEnabled(true);
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                showToastFromBackground("An error has  occurred!");
            }
        }
    }

    public void onSignupSuccess() {
        //_signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        showToastFromBackground("Success...");
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        showToastFromBackground("Login failed");
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        fname = _fnameText.getText().toString();
        lname = _lnameText.getText().toString();
        username = _username.getText().toString();
        email = _emailText.getText().toString();
        mobile = _mobileText.getText().toString();
        password = _passwordText.getText().toString();
        reEnterPassword = _reEnterPasswordText.getText().toString();

        if (fname.isEmpty() || fname.length() < 3) {
            _fnameText.setError("at least 3 characters");
            valid = false;
        } else {
            _fnameText.setError(null);
        }

        if (lname.isEmpty() || lname.length() < 3) {
            _lnameText.setError("at least 3 characters");
            valid = false;
        } else {
            _lnameText.setError(null);
        }

        /*if (username.isEmpty()) {
            _username.setError("Enter Valid Address");
            valid = false;
        } else {
            _username.setError(null);
        }*/


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Password Do not match");
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
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