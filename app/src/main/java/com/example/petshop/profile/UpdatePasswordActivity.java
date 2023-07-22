package com.example.petshop.profile;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.petshop.LoginSignup.LoginActivity;
import com.example.petshop.R;
import com.example.petshop.pelengkap.Alert;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class UpdatePasswordActivity extends AppCompatActivity {
    TextInputEditText oldPassword, newPassword, confirmPass;
    TextInputLayout oldPasswordContainer, newPasswordContainer, confirmPassContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_password);

        oldPassword = findViewById(R.id.txtOldPasswordUpdate);
        newPassword = findViewById(R.id.txtNewPasswordUpdate);
        confirmPass = findViewById(R.id.txtConfirmPasswordUpdate);

        oldPasswordContainer = findViewById(R.id.oldPasswordUpdateContainer);
        newPasswordContainer = findViewById(R.id.newPasswordUpdateContainer);
        confirmPassContainer = findViewById(R.id.confirmPasswordUpdateContainer);
        
        findViewById(R.id.btnUpdatePassword).setOnClickListener(view 
                -> send());

        findViewById(R.id.btnKembaliPassword).setOnClickListener(view
                -> finish());

        focusListener();
    }

    private void send() {
        oldPasswordContainer.setHelperText(validOldPassword());
        newPasswordContainer.setHelperText(validNewPassword());
        confirmPassContainer.setHelperText(validConfirmPass());

        Boolean validOldPass = oldPasswordContainer.getHelperText() == null;
        Boolean validNewPass = newPasswordContainer.getHelperText() == null;
        Boolean validConPass = confirmPassContainer.getHelperText() == null;

        if (validOldPass && validNewPass && validConPass) {
            sendUpdate();
        } else {
            Alert.alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendUpdate() {
        JSONObject params = new JSONObject();
        try {
            params.put("oldPassword", Objects.requireNonNull(oldPassword.getText()).toString());
            params.put("newPassword", Objects.requireNonNull(newPassword.getText()).toString());
            params.put("confirmNewPassword", Objects.requireNonNull(confirmPass.getText()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/user/updatepassword";

        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setToken(true);
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
                switch (code) {
                    case 200:
                        try {
                            Toast.makeText(this, response.getString("data"), Toast.LENGTH_LONG).show();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 401:
                        try {
                            kode401(response.getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        LocalStorage local = new LocalStorage(this);
                        local.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(response.getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
                oldPassword.setText("");
                newPassword.setText("");
                confirmPass.setText("");
            });
        });
        thread.start();
    }

    private void focusListener() {
        oldPassword.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                oldPasswordContainer.setHelperText(validOldPassword());
            }
        });
        
        newPassword.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                newPasswordContainer.setHelperText(validNewPassword());
            }
        });

        confirmPass.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                confirmPassContainer.setHelperText(validConfirmPass());
            }
        });
    }

    private CharSequence validOldPassword() {
        String passwordText = String.valueOf(oldPassword.getText());
        if (passwordText.isEmpty())
            return "required";
        if (passwordText.length() < 8)
            return "Minimum 8 Character Password";
        return null;
    }

    private CharSequence validNewPassword() {
        String passwordText = String.valueOf(newPassword.getText());
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
        String passwordText = String.valueOf(newPassword.getText());
        if (confirmPassText.isEmpty())
            return "required";
        if (!confirmPassText.equals(passwordText))
            return "Confirm Password is not the same as New Password";
        return null;
    }
}