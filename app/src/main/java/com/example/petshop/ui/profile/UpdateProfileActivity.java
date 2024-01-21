package com.example.petshop.ui.profile;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

public class UpdateProfileActivity extends AppCompatActivity {
    private LocalStorage local;
    private MainViewModel mainViewModel;
    private TextInputEditText nama, email, phone, alamat;
    private TextInputLayout namaContainer, emailContainer, phoneContainer, alamatContainer;
    private String inpNama, inpEmail, inpPhone, inpAlamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_profile);

        local = new LocalStorage(this);
        MainViewModelFactory factory = new MainViewModelFactory(local);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        nama = findViewById(R.id.txtNamaUpdate);
        email = findViewById(R.id.txtEmailUpdate);
        phone = findViewById(R.id.txtPhoneUpdate);
        alamat = findViewById(R.id.txtAlamatUpdate);

        namaContainer = findViewById(R.id.namaUpdateContainer);
        emailContainer = findViewById(R.id.emailUpdateContainer);
        phoneContainer = findViewById(R.id.phoneUpdateContainer);
        alamatContainer = findViewById(R.id.alamatUpdateContainer);

        nama.setText(local.getNama());
        email.setText(local.getEmail());
        phone.setText(local.getTelepon());
        alamat.setText(local.getAlamat());

        focusListener();

        mainViewModel.getResult().observe(this, result -> {
            if (result != null) {
                try {
                    int code = Integer.parseInt(result.getString("code"));
                    switch (code) {
                        case 200:
                            Toast.makeText(this, result.getString("data"), Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, ProfileActivity.class));

                            local.setNama(inpNama);
                            local.setTelepon(inpPhone);
                            local.setEmail(inpEmail);
                            local.setAlamat(inpAlamat);
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
                            break;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

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
        inpNama = Objects.requireNonNull(nama.getText()).toString();
        inpEmail = Objects.requireNonNull(email.getText()).toString();
        inpPhone = Objects.requireNonNull(phone.getText()).toString();
        inpAlamat = Objects.requireNonNull(alamat.getText()).toString();

        try {
            params.put("full_name", inpNama);
            params.put("email", inpEmail);
            params.put("phone", inpPhone);
            params.put("alamat", inpAlamat);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        mainViewModel.setDataRemote(this, "user/updateprofile", "post",
                true, data, null);
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