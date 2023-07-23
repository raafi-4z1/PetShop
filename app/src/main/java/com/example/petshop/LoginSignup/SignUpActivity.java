package com.example.petshop.LoginSignup;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.loading;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText fullName, username, telephone, password, confirmPass;
    private TextInputLayout fullNameContainer, telephoneContainer, confirmPassContainer, usernameContainer, passwordContainer;
    private AlertDialog dialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);
        AndroidThreeTen.init(this);

        bindViews();
        dialog = loading(SignUpActivity.this);

        findViewById(R.id.signup_back_button).setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), WelcomeActivity.class)));

        focusListener();
    }

    private void bindViews() {
        fullName = findViewById(R.id.txtFullNameRegister);
        username = findViewById(R.id.txtUsernameRegister);
        telephone = findViewById(R.id.txtPhoneRegister);
        password = findViewById(R.id.txtPasswordRegister);
        confirmPass = findViewById(R.id.txtConfirmPasswordRegister);

        fullNameContainer = findViewById(R.id.fullNameRegisterContainer);
        telephoneContainer = findViewById(R.id.phoneRegisterContainer);
        confirmPassContainer = findViewById(R.id.confirmPasswordRegisterContainer);
        usernameContainer = findViewById(R.id.usernameRegisterContainer);
        passwordContainer = findViewById(R.id.passwordRegisterContainer);
    }

    private void focusListener() {
        fullName.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                fullNameContainer.setHelperText(validName());
            }
        });

        username.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                usernameContainer.setHelperText(validUsername());
            }
        });

        telephone.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                telephoneContainer.setHelperText(validPhone());
            }
        });

        password.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                passwordContainer.setHelperText(validPassword());
            }
        });

        confirmPass.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                confirmPassContainer.setHelperText(validConfirmPass());
            }
        });
    }

    private CharSequence validName() {
        String name = String.valueOf(fullName.getText()).trim();
        if (name.isEmpty())
            return "required";
        if (name.length() < 5)
            return "Minimum 5 Character Name";
        return null;
    }

    private CharSequence validUsername() {
        String user = String.valueOf(username.getText());
        if (user.isEmpty())
            return "required";
        if (user.length() < 8)
            return "Minimum 8 Character Username";
        return null;
    }

    private CharSequence validPhone() {
        String phone = String.valueOf(telephone.getText());
        if (phone.isEmpty())
            return "required";
        if (phone.length() > 12)
            return "Maximum 12 number";
        return null;
    }

    private CharSequence validPassword() {
        String passwordText = String.valueOf(password.getText());
        if (passwordText.isEmpty())
            return "required";
        if (passwordText.length() < 8)
            return "Minimum 8 Character Password";
        if (!passwordText.matches(".*[A-Z].*"))
            return "Must Contain  1 Upper-case Character";
        if (!passwordText.matches(".*[a-z].*"))
            return "Must Contain  1 Lower-case Character";
        if (!passwordText.matches(".*[!@#\\$%^&*+=].*"))
            return "Must Contain  1 Special Character (!@#\\$%^&*+=)";
        return null;
    }

    private CharSequence validConfirmPass() {
        String confirmPassText = String.valueOf(confirmPass.getText());
        String passwordText = String.valueOf(password.getText());
        if (confirmPassText.isEmpty())
            return "required";
        if (!confirmPassText.equals(passwordText))
            return "Confirm Password is not the same as Password";
        return null;
    }

    public void callLoginFromSignUp(View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    public void callRegisterScreen(View view) {
        fullNameContainer.setHelperText(validName());
        usernameContainer.setHelperText(validUsername());
        telephoneContainer.setHelperText(validPhone());
        passwordContainer.setHelperText(validPassword());
        confirmPassContainer.setHelperText(validConfirmPass());

        Boolean validName = fullNameContainer.getHelperText() == null;
        Boolean validUser = usernameContainer.getHelperText() == null;
        Boolean validPhone = telephoneContainer.getHelperText() == null;
        Boolean validPass = passwordContainer.getHelperText() == null;
        Boolean validConPass = confirmPassContainer.getHelperText() == null;

        if (validName && validUser && validPhone && validPass && validConPass) {
            dialog.show();
            sendRegister();
        } else {
            Alert.alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendRegister() {
        JSONObject params = new JSONObject();
        try {
            params.put("name", Objects.requireNonNull(fullName.getText()).toString());
            params.put("username", Objects.requireNonNull(username.getText()).toString());
            params.put("phone", Objects.requireNonNull(telephone.getText()).toString());
            params.put("password", Objects.requireNonNull(password.getText()).toString());
            params.put("confirmPassword", Objects.requireNonNull(confirmPass.getText()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/register";

        Thread thread = new Thread(() -> {
            Http http = new Http(SignUpActivity.this, url);
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
                if (code == 201) {
                    try {
                        response = response.getJSONObject("data");
                        LocalStorage local = new LocalStorage(this);
                        local.setToken(response.getString("token"));
                        local.setSesi(LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                        Pair[] pairs = new Pair[1];
                        pairs[0] = new Pair<View, String>(findViewById(R.id.btnRegister), "transition_main_btn");

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignUpActivity.this, pairs);
                        startActivity(intent, options.toBundle());
                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String originalString = response.getString("message");
                        if (originalString.contains("Duplicate")) {
                            originalString = "Duplicate for key 'username'";
                            username.setText("");
                        }

                        alertFail(originalString, this);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                dialog.dismiss();
                password.setText("");
                confirmPass.setText("");
            });
        });
        thread.start();
    }
}