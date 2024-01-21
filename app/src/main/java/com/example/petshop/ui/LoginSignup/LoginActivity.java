package com.example.petshop.ui.LoginSignup;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.loading;
import static com.example.petshop.pelengkap.KeyStoreHelper.*;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petshop.R;
import com.example.petshop.WelcomeActivity;
import com.example.petshop.data.PetshopCallback;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.data.repository.MainRepository;
import com.example.petshop.ui.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private MainRepository mainRepository;
    private TextInputEditText inpUsername, inpPassword;
    private TextInputLayout usernameContainer, passwordContainer;
    private LocalStorage local;
    private AlertDialog dialog;
    private CheckBox checkBox;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        AndroidThreeTen.init(this);

        local = new LocalStorage(this);
        dialog = loading(LoginActivity.this);
        mainRepository = new MainRepository();

        checkBox = findViewById(R.id.ckBoxLogin);
        bindViews();

        findViewById(R.id.login_back_button).setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class)));

        usernameFocusListener();
        passwordFocusListener();
    }

    private void bindViews() {
        inpUsername = findViewById(R.id.txtUsernameLogin);
        inpPassword = findViewById(R.id.txtPasswordLogin);
        usernameContainer = findViewById(R.id.usernameLoginContainer);
        passwordContainer = findViewById(R.id.passwordLoginContainer);

        if (local.getRemember()) {
            checkBox.setChecked(true);
            String userStr = local.getUsername(), passStr = local.getPassword();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                    !Objects.equals(local.getKey(), "null")) {
                try {
                    userStr = encryptionOrDecryptionAES(
                            userStr,
                            decodeKeyFromString(local.getKey()),
                            false);
                    passStr = encryptionOrDecryptionAES(
                            passStr,
                            decodeKeyFromString(local.getKey()),
                            false);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            inpUsername.setText(userStr);
            inpPassword.setText(passStr);
        }
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

    @SuppressLint("LongLogTag")
    public void letTheUserLoggedIn(View view) {
        usernameContainer.setHelperText(validUsername());
        passwordContainer.setHelperText(validPassword());

        Boolean validUser = usernameContainer.getHelperText() == null;
        Boolean validPass = passwordContainer.getHelperText() == null;

        if (validUser && validPass) {
            dialog.show();
            sendLogin();
        } else {
            alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendLogin() {
        JSONObject params = new JSONObject();
        String userString = Objects.requireNonNull(inpUsername.getText()).toString();
        String passString = Objects.requireNonNull(inpPassword.getText()).toString();

        try {
            params.put("username", userString);
            params.put("password", passString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        mainRepository.remoteRepository("login", "post", local, false, data,
                new PetshopCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        if (result != null) {
                            try {
                                int code = Integer.parseInt(result.getString("code"));
                                if (code == 200) {
                                    result = result.getJSONObject("data");

                                    local.setToken(result.getString("token"));
                                    local.setSesi(LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                                    local.setRemember(checkBox.isChecked());

                                    if (checkBox.isChecked()) {
                                        String encryptPass, encryptUser;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            if (Objects.equals(local.getKey(), "null"))
                                                local.setKey(encodeKeyToString(getOrCreateSecretKey()));

                                            encryptUser = encryptionOrDecryptionAES(userString,
                                                    decodeKeyFromString(local.getKey()), true);
                                            encryptPass = encryptionOrDecryptionAES(passString,
                                                    decodeKeyFromString(local.getKey()), true);
                                        } else {
                                            encryptUser = userString;
                                            encryptPass = passString;
                                        }
                                        local.setUsername(encryptUser);
                                        local.setPassword(encryptPass);
                                    }

                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                                        Pair[] pairs = new Pair[1];
                                        pairs[0] = new Pair<View, String>(findViewById(R.id.btnLogin), "transition_main_btn");

                                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                                        startActivity(intent, options.toBundle());
                                        finish();
                                    });
                                } else {
                                    JSONObject finalResult = result;
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        inpUsername.setText("");
                                        inpPassword.setText("");
                                        try {
                                            alertFail(finalResult.getString("message"), LoginActivity.this);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        dismissLoading(dialog);
                    }

                    @Override
                    public void onFailure(String message) {
                        dismissLoading(dialog);
                        new Handler(Looper.getMainLooper()).post(() -> alertFail(message, LoginActivity.this));
                    }

                    @Override
                    public void dismissLoading(AlertDialog dialog) {
                        if (dialog != null)
                            new Handler(Looper.getMainLooper()).post(dialog::dismiss);
                    }
                });
    }
}