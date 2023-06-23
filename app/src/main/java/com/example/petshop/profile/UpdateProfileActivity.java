package com.example.petshop.profile;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petshop.LoginSignup.LoginActivity;
import com.example.petshop.MainActivity;
import com.example.petshop.R;
import com.example.petshop.pelengkap.Alert;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class UpdateProfileActivity extends AppCompatActivity {
    LocalStorage localStorage;
    TextInputEditText nama, email, phone, alamat;
    TextInputLayout namaContainer, emailContainer, phoneContainer, alamatContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_profile);

        localStorage = new LocalStorage(this);

        nama = findViewById(R.id.txtNamaUpdate);
        email = findViewById(R.id.txtEmailUpdate);
        phone = findViewById(R.id.txtPhoneUpdate);
        alamat = findViewById(R.id.txtAlamatUpdate);

        namaContainer = findViewById(R.id.namaUpdateContainer);
        emailContainer = findViewById(R.id.emailUpdateContainer);
        phoneContainer = findViewById(R.id.phoneUpdateContainer);
        alamatContainer = findViewById(R.id.alamatUpdateContainer);

        nama.setText(localStorage.getNama());
        email.setText(localStorage.getEmail());
        phone.setText(localStorage.getTelepon());
        alamat.setText(localStorage.getAlamat());

        focusListener();

        findViewById(R.id.btnKembaliUpdate).setOnClickListener(view
                -> finish());

        findViewById(R.id.btnUpdateProfile).setOnClickListener(view
                -> send() );
    }

    private void send() {
        namaContainer.setHelperText(validName());
        emailContainer.setHelperText(validEmail());
        phoneContainer.setHelperText(validPhone());
        alamatContainer.setHelperText(validAlamat());

        Boolean validName = namaContainer.getHelperText() == null;
        Boolean validEmail = emailContainer.getHelperText() == null;
        Boolean validPhone = phoneContainer.getHelperText() == null;
        Boolean validAlamat = alamatContainer.getHelperText() == null;

        if (validName && validEmail && validPhone && validAlamat) {
            sendUpdate();
        } else {
            Alert.alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendUpdate() {
        JSONObject params = new JSONObject();
        String inpNama = Objects.requireNonNull(nama.getText()).toString();
        String inpEmail = Objects.requireNonNull(email.getText()).toString();
        String inpPhone = Objects.requireNonNull(phone.getText()).toString();
        String inpAlamat = Objects.requireNonNull(alamat.getText()).toString();

        try {
            params.put("full_name", inpNama);
            params.put("email", inpEmail);
            params.put("phone", inpPhone);
            params.put("alamat", inpAlamat);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/user/updateprofile";

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

                            localStorage.setNama(inpNama);
                            localStorage.setTelepon(inpPhone);
                            localStorage.setEmail(inpEmail);
                            localStorage.setAlamat(inpAlamat);

                            startActivity(new Intent(this, ProfileActivity.class));
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
                        localStorage.setToken("");
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
            });
        });
        thread.start();
    }

    private void focusListener() {
        nama.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                namaContainer.setHelperText(validName());
            }
        });

        email.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                emailContainer.setHelperText(validEmail());
            }
        });

        phone.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                phoneContainer.setHelperText(validPhone());
            }
        });

        alamat.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                alamatContainer.setHelperText(validAlamat());
            }
        });
    }
    private CharSequence validName() {
        String data = String.valueOf(nama.getText());

        if (data.isEmpty())
            return "required";
        return null;
    }

    private CharSequence validEmail() {
        String data = String.valueOf(email.getText());

        if (data.isEmpty())
            return "required";
        if (! Patterns.EMAIL_ADDRESS.matcher(data).matches())
            return "invalid Email Address";
        return null;
    }

    private CharSequence validPhone() {
        String data = String.valueOf(phone.getText());

        if (data.isEmpty())
            return "required";
        if (data.length() > 12)
            return "max 12 number";
        return null;
    }

    private CharSequence validAlamat() {
        String data = String.valueOf(alamat.getText());

        if (data.isEmpty())
            return "required";
        return null;
    }
}