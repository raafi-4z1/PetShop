package com.example.petshop.LoginSignup;

import static android.content.ContentValues.TAG;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;

import com.example.petshop.MainActivity;
import com.example.petshop.R;
import com.example.petshop.WelcomeActivity;
import com.example.petshop.pelengkap.Alert;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText inpUsername, inpPassword;
    TextInputLayout usernameContainer, passwordContainer;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        AndroidThreeTen.init(this);

        inpUsername = findViewById(R.id.txtUsernameLogin);
        inpPassword = findViewById(R.id.txtPasswordLogin);
        usernameContainer = findViewById(R.id.usernameLoginContainer);
        passwordContainer = findViewById(R.id.passwordLoginContainer);

        findViewById(R.id.login_back_button).setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class)));

        usernameFocusListener();
        passwordFocusListener();
    }

    private void passwordFocusListener() {
        inpPassword.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                passwordContainer.setHelperText(validPassword());
            }
        });
    }

    private CharSequence validPassword() {
        if (Objects.requireNonNull(inpPassword.getText()).toString().isEmpty())
            return "required";
        if (inpPassword.getText().length() < 8)
            return "minimum 8 Character Password";
        return null;
    }

    private void usernameFocusListener() {
        inpUsername.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                usernameContainer.setHelperText(validUsername());
            }
        });
    }

    private CharSequence validUsername() {
        if (Objects.requireNonNull(inpUsername.getText()).toString().isEmpty())
            return "required";
        if (inpUsername.getText().length() < 8)
            return "minimum 8 Character Username";
        return null;
    }

    public void callSignUpFromLogin(View view) {
        startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        finish();
    }

    public void letTheUserLoggedIn(View view) {
        usernameContainer.setHelperText(validUsername());
        passwordContainer.setHelperText(validPassword());

        Boolean validUser = usernameContainer.getHelperText() == null;
        Boolean validPass = passwordContainer.getHelperText() == null;

        if (validUser && validPass) {
            sendLogin();
        } else {
            alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendLogin() {
        JSONObject params = new JSONObject();
        try {
            params.put("username", inpUsername.getText());
            params.put("password", inpPassword.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/login";

        Thread thread = new Thread(() -> {
            Http http = new Http(LoginActivity.this, url);
            http.setMethod("post");
            http.setData(data);
            http.send();

            runOnUiThread(() -> {
                JSONObject response = null;
                try {
                    response = new JSONObject(http.getResponse());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int code = http.getStatusCode();
                assert response != null;
                if (code == 200) {
                    try {
                        response = response.getJSONObject("data");
                        LocalStorage local = new LocalStorage(this);
                        local.setToken(response.getString("token"));
                        local.setSesi(LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                        Pair[] pairs = new Pair[1];
                        pairs[0] = new Pair<View, String>(findViewById(R.id.btnLogin), "transition_main_btn");

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                        startActivity(intent, options.toBundle());
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        alertFail(response.getString("message"), LoginActivity.this);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                inpUsername.setText("");
                inpPassword.setText("");
            });
        });
        thread.start();
    }
}