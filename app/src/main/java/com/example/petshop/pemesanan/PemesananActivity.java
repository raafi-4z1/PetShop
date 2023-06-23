package com.example.petshop.pemesanan;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import androidx.appcompat.app.AlertDialog;
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
import com.example.petshop.pelengkap.DateValidator;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class PemesananActivity extends AppCompatActivity {
    private LocalStorage localStorage;
    private TextInputEditText fullName, email, phone, alamat, namaHewan, jenis, jumlah, tglPesan;
    private TextInputLayout fullNameContainer, emailContainer, phoneContainer, alamatContainer, namaHewanContainer, jenisContainer, jumlahContainer, tglPesanContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pemesanan);

        localStorage = new LocalStorage(this);
        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());

        fullName = findViewById(R.id.txtFullNamePemesanan);
        email = findViewById(R.id.txtEmailPemesanan);
        phone = findViewById(R.id.txtPhonePemesanan);
        alamat = findViewById(R.id.txtAlamatPemesanan);
        namaHewan = findViewById(R.id.txtNamaHewanPemesanan);
        jenis = findViewById(R.id.txtJenisHewanPemesanan);
        jumlah =  findViewById(R.id.txtJumlahPemesanan);
        tglPesan = findViewById(R.id.txtTanggalPemesanan);

        fullNameContainer = findViewById(R.id.fullNamePemesananContainer);
        emailContainer = findViewById(R.id.emailPemesananContainer);
        phoneContainer = findViewById(R.id.phonePemesananContainer);
        alamatContainer = findViewById(R.id.alamatPemesananContainer);
        namaHewanContainer = findViewById(R.id.namaHewanPemesananContainer);
        jenisContainer = findViewById(R.id.jenisHewanPemesananContainer);
        jumlahContainer =  findViewById(R.id.jumlahContainer);
        tglPesanContainer = findViewById(R.id.tanggalPemesananContainer);

        fullName.setText(localStorage.getNama());
        email.setText(localStorage.getEmail());
        phone.setText(localStorage.getTelepon());
        alamat.setText(localStorage.getAlamat());

        focusListener();

        findViewById(R.id.pesan_back_button).setOnClickListener(view
                -> finish());

        findViewById(R.id.btnPesan).setOnClickListener(view
                -> send() );
    }

    private void send() {
        fullNameContainer.setHelperText(validName());
        emailContainer.setHelperText(validEmail());
        phoneContainer.setHelperText(validPhone());
        alamatContainer.setHelperText(validAlamat());
        namaHewanContainer.setHelperText(validHewan());
        jenisContainer.setHelperText(validJenis());
        jumlahContainer.setHelperText(validJumlah());
        tglPesanContainer.setHelperText(validTglPesan());

        Boolean validName = fullNameContainer.getHelperText() == null;
        Boolean validEmail = emailContainer.getHelperText() == null;
        Boolean validPhone = phoneContainer.getHelperText() == null;
        Boolean validAlamat = alamatContainer.getHelperText() == null;
        Boolean validHewan = namaHewanContainer.getHelperText() == null;
        Boolean validJenis = jenisContainer.getHelperText() == null;
        Boolean validJumlah = jumlahContainer.getHelperText() == null;
        Boolean validTglPesan = tglPesanContainer.getHelperText() == null;

        if (validName && validEmail && validPhone && validAlamat
                && validHewan && validJenis && validJumlah && validTglPesan) {
            sendPesan();
        } else {
            Alert.alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendPesan() {
        JSONObject params = new JSONObject();
        try {
            params.put("full_name", Objects.requireNonNull(fullName.getText()).toString());
            params.put("email", Objects.requireNonNull(email.getText()).toString());
            params.put("phone", Objects.requireNonNull(phone.getText()).toString());
            params.put("alamat", Objects.requireNonNull(alamat.getText()).toString());
            params.put("nama_hewan", Objects.requireNonNull(namaHewan.getText()).toString());
            params.put("jenis_hewan", Objects.requireNonNull(jenis.getText()).toString());
            params.put("jumlah", Objects.requireNonNull(jumlah.getText()).toString());
            params.put("tgl_pesan", convertDateFormat(Objects.requireNonNull(tglPesan.getText()).toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/user/pemesanan";

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
                            startActivity(new Intent(this, MainActivity.class));
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
        fullName.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                fullNameContainer.setHelperText(validName());
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

        namaHewan.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                namaHewanContainer.setHelperText(validHewan());
            }
        });

        jenis.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                jenisContainer.setHelperText(validJenis());
            }
        });

        jumlah.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                jumlahContainer.setHelperText(validJumlah());
            }
        });

        tglPesan.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                tglPesanContainer.setHelperText(validTglPesan());
            }
        });
    }

    private CharSequence validName() {
        String data = String.valueOf(fullName.getText());

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

    private CharSequence validHewan() {
        String data = String.valueOf(namaHewan.getText());

        if (data.isEmpty())
            return "required";
        return null;
    }

    private CharSequence validJenis() {
        String data = String.valueOf(jenis.getText());

        if (data.isEmpty())
            return "required";
        return null;
    }

    private CharSequence validJumlah() {
        String data = String.valueOf(jumlah.getText());

        if (data.isEmpty())
            return "required";
        if (Integer.parseInt(data) > 15)
            return "jumlah tidak masuk akal";
        return null;
    }

    private CharSequence validTglPesan() {
        String data = String.valueOf(tglPesan.getText());
        DateValidator validator = new DateValidator();

        if (data.isEmpty())
            return "required";
        if (!data.matches("\\d{2}-\\d{2}-\\d{4}")) {
            Toast.makeText(this, "format tanggal dd-mm-yyyy", Toast.LENGTH_LONG).show();
            return "format salah";
        }
        if (!validator.isDateValid(data))
            return "tanggal tidak valid";
        return null;
    }
}