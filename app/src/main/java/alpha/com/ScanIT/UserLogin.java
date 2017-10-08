package alpha.com.ScanIT;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import alpha.com.ScanIT.databases.TinyDB;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserLogin extends Activity {

    private static final String TAG = "LoginActivity";
    private static final String LOGIN_PASS_ASSISTANT = "CDARESORT";
    private static final String LOGIN_PASS_ADMIN = "CODEPURPLE";
    private static boolean LOGIN_SUCCESS = false;
    private static boolean LOGIN_FAIL = false;
    private static String USER_LOGGED = "";
    private static String USER_ACTUAL = "";
    private static Integer LOGIN_SKIP = 0;

    @Bind(R.id.UserText)
    EditText _UserText;
    @Bind(R.id.UserPassword)
    EditText _UserPassword;
    @Bind(R.id.login_button)
    Button _UserLogin;

    @OnClick(R.id.login_button)
    public void login_button(View view) {
        if (!validate()) {
        } else {
            login();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (loadPreferences() == 1) {
            onLoginSuccess();
            //  To reset
            //  TinyDB tinydb = new TinyDB(this);
            //  tinydb.remove("LOGIN_SKIP");
        }

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Titillium-SemiboldUpright.otf");
        _UserText.setTypeface(custom_font);
        _UserText.setTextSize(20);
        _UserPassword.setTypeface(custom_font);
        _UserPassword.setTextSize(20);

        _UserLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!validate()) {
                } else {
                    login();
                }
            }
        });

    }

    private void login() {

        if (!validate()) {
            onLoginFailed();
        }

        _UserLogin.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(UserLogin.this, R.style.MyGravity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String user = _UserText.getText().toString();
        String password = _UserPassword.getText().toString();

        if (password.equals(LOGIN_PASS_ASSISTANT)) {
            USER_LOGGED = "Assistant";
            USER_ACTUAL = user;
            LOGIN_SUCCESS = true;
        }
        if (password.equals(LOGIN_PASS_ADMIN)) {
            USER_LOGGED = "Manager";
            USER_ACTUAL = user;
            LOGIN_SUCCESS = true;
        }
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                        if (LOGIN_SUCCESS && !LOGIN_FAIL) {
                            onLoginSuccess();
                            LOGIN_SUCCESS = false;
                            savePreferences(USER_LOGGED, USER_ACTUAL);
                        } else {
                            //Todo Lock out after failed attempts, manager override
                            LOGIN_FAIL = true;
                            onLoginFailed();
                            Snackbar.make(getWindow().getDecorView().getRootView(), "Password is incorrect.", Snackbar.LENGTH_LONG).setDuration(2000).show();
                        }
                    }
                }, 1500);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void onLoginSuccess() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, 0);
    }

    private Integer loadPreferences() {
        TinyDB tinydb = new TinyDB(this);
        Integer LOGIN_SKIP = tinydb.getInt("LOGIN_SKIP", 0);

        return LOGIN_SKIP;
    }

    private void savePreferences(String user, String user_) {
        TinyDB tinydb = new TinyDB(this);
        LOGIN_SKIP = 1;
        tinydb.putInt("LOGIN_SKIP", LOGIN_SKIP);
        tinydb.putString("LOGGED_IN", user);
        tinydb.putString("LOGGED_ACTUAL", user_);
    }

    private void onLoginFailed() {
        LOGIN_FAIL = false;
        _UserLogin.setEnabled(true);
    }

    private boolean validate() {

        boolean valid = true;

        String user = _UserText.getText().toString();
        String password = _UserPassword.getText().toString();

        if (user.isEmpty() && !password.isEmpty()) {
            _UserText.setError("Please enter a username.");
            valid = false;
        } else {
            _UserText.setError(null);
        }

        if (password.isEmpty() && !user.isEmpty()) {
            _UserPassword.setError("Please enter your password.");
            valid = false;
        } else {
            _UserPassword.setError(null);
        }
        if (password.isEmpty() && user.isEmpty()) {
            _UserText.setError("Please enter a username. ");
            _UserPassword.setError("Please enter your password.");
            valid = false;
        }

        return valid;
    }
}