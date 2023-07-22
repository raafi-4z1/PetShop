package com.example.petshop.penitipan;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class PenitipanActivity extends AppCompatActivity {
    private TextInputEditText fullName, namaHewan, jenis, jumlah, tglMasuk, tglKeluar;
    private TextInputLayout fullNameContainer, namaHewanContainer, jenisContainer, jumlahContainer, tglMasukContainer, tglKeluarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_penitipan);

        LocalStorage localStorage = new LocalStorage(this);
        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());

        fullName = findViewById(R.id.txtFullNamePenitipan);
        namaHewan = findViewById(R.id.txtNamaHewanPenitipan);
        jenis = findViewById(R.id.txtJenisHewanPenitipan);
        jumlah =  findViewById(R.id.txtJumlahPenitipan);
        tglMasuk = findViewById(R.id.txtTanggalMasukPenitipan);
        tglKeluar = findViewById(R.id.txtTanggalKeluarPenitipan);

        fullNameContainer = findViewById(R.id.fullNamePenitipanContainer);
        namaHewanContainer = findViewById(R.id.namaHewanPenitipanContainer);
        jenisContainer = findViewById(R.id.jenisHewanPenitipanContainer);
        jumlahContainer =  findViewById(R.id.jumlahPenitipanContainer);
        tglMasukContainer = findViewById(R.id.tanggalMasukPenitipanContainer);
        tglKeluarContainer = findViewById(R.id.tanggalKeluarPenitipanContainer);

        fullName.setText(localStorage.getNama());
        focusListener();

        findViewById(R.id.penitipan_back_button).setOnClickListener(view
                -> finish());

        findViewById(R.id.btnKirim).setOnClickListener(view
                -> send() );
    }

    private void send() {
        fullNameContainer.setHelperText(validName());
        namaHewanContainer.setHelperText(validHewan());
        jenisContainer.setHelperText(validJenis());
        jumlahContainer.setHelperText(validJumlah());
        tglMasukContainer.setHelperText(validTglMasuk());
        tglKeluarContainer.setHelperText(validTglKeluar());

        Boolean validName = fullNameContainer.getHelperText() == null;
        Boolean validHewan = namaHewanContainer.getHelperText() == null;
        Boolean validJenis = jenisContainer.getHelperText() == null;
        Boolean validJumlah = jumlahContainer.getHelperText() == null;
        Boolean validTglMasuk = tglMasukContainer.getHelperText() == null;
        Boolean validTglKeluar = tglKeluarContainer.getHelperText() == null;

        if (validName && validHewan && validJenis && validJumlah && validTglMasuk && validTglKeluar) {
            sendKirim();
        } else {
            Alert.alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendKirim() {
        JSONObject params = new JSONObject();
        try {
            params.put("full_name", Objects.requireNonNull(fullName.getText()).toString());
            params.put("nama_hewan", Objects.requireNonNull(namaHewan.getText()).toString());
            params.put("jenis_hewan", Objects.requireNonNull(jenis.getText()).toString());
            params.put("jumlah", Objects.requireNonNull(jumlah.getText()).toString());
            params.put("tgl_masuk", convertDateFormat(Objects.requireNonNull(tglMasuk.getText()).toString()));
            params.put("tgl_keluar", convertDateFormat(Objects.requireNonNull(tglKeluar.getText()).toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/user/penitipan";

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
                LocalStorage local = new LocalStorage(this);

                int code = http.getStatusCode();
                assert response != null;
                switch (code) {
                    case 201:
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

        tglMasuk.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                tglMasukContainer.setHelperText(validTglMasuk());
            }
        });

        tglKeluar.setOnFocusChangeListener((v, focused) -> {
            if (!focused) {
                tglKeluarContainer.setHelperText(validTglKeluar());
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

    private CharSequence validTglMasuk() {
        String data = String.valueOf(tglMasuk.getText());
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

    private CharSequence validTglKeluar() {
        String dataTglMasuk = String.valueOf(tglMasuk.getText());
        String data = String.valueOf(tglKeluar.getText());
        DateValidator validator = new DateValidator();

        if (data.isEmpty())
            return "required";
        if (!data.matches("\\d{2}-\\d{2}-\\d{4}")) {
            Toast.makeText(this, "format tanggal dd-mm-yyyy", Toast.LENGTH_LONG).show();
            return "format salah";
        }
        if (!validator.isDateValid(data))
            return "tanggal tidak valid";
        if (!dataTglMasuk.isEmpty())
            if (!validator.isTanggalValid(data, dataTglMasuk))
                return "tanggal keluar tidak valid";
        return null;
    }
}