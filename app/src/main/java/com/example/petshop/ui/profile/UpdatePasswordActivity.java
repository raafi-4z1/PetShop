package com.example.petshop.ui.profile;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.KeyStoreHelper.decodeKeyFromString;
import static com.example.petshop.pelengkap.KeyStoreHelper.encryptionOrDecryptionAES;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.ui.LoginSignup.LoginActivity;
import com.example.petshop.ui.viewmodel.MainViewModel;
import com.example.petshop.ui.viewmodel.MainViewModelFactory;
import com.example.petshop.R;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.pelengkap.Alert;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class UpdatePasswordActivity extends AppCompatActivity {
    private TextInputEditText oldPassword, newPassword, confirmPass;
    private TextInputLayout oldPasswordContainer, newPasswordContainer, confirmPassContainer;
    private MainViewModel mainViewModel;
    private String newPassStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_password);

        LocalStorage local = new LocalStorage(this);
        MainViewModelFactory factory = new MainViewModelFactory(local);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        oldPassword = findViewById(R.id.txtOldPasswordUpdate);
        newPassword = findViewById(R.id.txtNewPasswordUpdate);
        confirmPass = findViewById(R.id.txtConfirmPasswordUpdate);

        oldPasswordContainer = findViewById(R.id.oldPasswordUpdateContainer);
        newPasswordContainer = findViewById(R.id.newPasswordUpdateContainer);
        confirmPassContainer = findViewById(R.id.confirmPasswordUpdateContainer);

        mainViewModel.getResult().observe(this, result -> {
            if (result != null)
                setPass(result, local);
        });
        
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
        newPassStr = Objects.requireNonNull(newPassword.getText()).toString();

        try {
            params.put("oldPassword", Objects.requireNonNull(oldPassword.getText()).toString());
            params.put("newPassword", newPassStr);
            params.put("confirmNewPassword", Objects.requireNonNull(confirmPass.getText()).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        mainViewModel.setDataRemote(this, "user/updatepassword", "post",
                true, data, null);
    }

    private void setPass(JSONObject result, LocalStorage local) {
        try {
            int code = Integer.parseInt(result.getString("code"));

            switch (code) {
                case 200:
                    if (local.getRemember()) {
                        String encrypt;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            encrypt = encryptionOrDecryptionAES(newPassStr,
                                    decodeKeyFromString(local.getKey()), true);
                        } else
                            encrypt = newPassStr;
                        local.setPassword(encrypt);
                    }

                    Toast.makeText(this, result.getString("data"), Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case 401:
                    kode401(result.getString("message"), this);
                    local.setToken("");
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                default:
                    alertFail(result.getString("message"), this);
                    oldPassword.setText("");
                    newPassword.setText("");
                    confirmPass.setText("");
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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