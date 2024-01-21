package com.example.petshop.ui.pemesanan;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.ui.LoginSignup.LoginActivity;
import com.example.petshop.ui.viewmodel.MainViewModel;
import com.example.petshop.ui.viewmodel.MainViewModelFactory;
import com.example.petshop.R;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.pelengkap.Alert;
import com.example.petshop.pelengkap.DateValidator;
import com.example.petshop.ui.transaksi.TransaksiLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class PemesananActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private LocalStorage local;
    private TextInputEditText fullName, email, phone, alamat, namaHewan, jumlah, tglPesan;
    private RadioGroup jenis;
    private RadioButton selectedRadioButton;
    private TextInputLayout fullNameContainer, emailContainer, phoneContainer, alamatContainer, namaHewanContainer, jenisContainer, jumlahContainer, tglPesanContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pemesanan);

        local = new LocalStorage(this);
        MainViewModelFactory factory = new MainViewModelFactory(local);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        bindViews();
        setTV();
        focusListener();

        mainViewModel.getResult().observe(this, result -> {
            if (result != null) {
                try {
                    int code = Integer.parseInt(result.getString("code"));
                    switch (code) {
                        case 201:
                            Toast.makeText(this, result.getString("data"), Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, TransaksiLayout.class));
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

        tglPesan.setOnClickListener(view -> clickTanggal());
        findViewById(R.id.pesan_back_button).setOnClickListener(view -> finish());
        findViewById(R.id.btnPesan).setOnClickListener(view -> send());
    }

    private void bindViews() {
        fullName = findViewById(R.id.txtFullNamePemesanan);
        email = findViewById(R.id.txtEmailPemesanan);
        phone = findViewById(R.id.txtPhonePemesanan);
        alamat = findViewById(R.id.txtAlamatPemesanan);
        namaHewan = findViewById(R.id.txtNamaHewanPemesanan);
        jenis = findViewById(R.id.radioGroupJenisHewanPemesanan);
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

        // Disable focusability of the TextInputEditText
        tglPesan.setFocusable(false);
    }

    private void setTV() {
        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(local.getNama());

        if (getIntent().getBooleanExtra("jadwalkanLagi", false)) {
            fullName.setText(getIntent().getStringExtra("namaPemesan"));
            email.setText(getIntent().getStringExtra("emailPemesan"));
            phone.setText(getIntent().getStringExtra("phonePemesan"));
            alamat.setText(getIntent().getStringExtra("alamatPemesan"));

            namaHewan.setText(getIntent().getStringExtra("namaHewan"));
            jumlah.setText(getIntent().getStringExtra("jumlahHewan"));

            if (Objects.equals(getIntent().getStringExtra("jenisHewan"), "Kucing")) {
                jenis.check(R.id.radioButtonKucing);
            } else {
                jenis.check(R.id.radioButtonAnjing);
            }
        } else {
            fullName.setText(local.getNama());
            email.setText(local.getEmail());
            phone.setText(local.getTelepon());
            alamat.setText(local.getAlamat());
        }
    }

    private void clickTanggal() {
        Calendar myCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));

        DatePickerDialog.OnDateSetListener datePickerListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setTanggal(myCalendar);
        };

        new DatePickerDialog(this, datePickerListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setTanggal(Calendar myCalendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("id"));
        String formattedDate = dateFormat.format(myCalendar.getTime());

        tglPesan.setText(formattedDate);
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
            params.put("jenis_hewan", selectedRadioButton.getText().toString());
            params.put("jumlah", Objects.requireNonNull(jumlah.getText()).toString());
            params.put("tgl_pesan", convertDateFormat(Objects.requireNonNull(tglPesan.getText()).toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        mainViewModel.setDataRemote(this, "user/pemesanan", "post",
                true, data, null);
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
        String data = String.valueOf(fullName.getText()).trim();
        if (data.isEmpty())
            return "required";
        if (data.length() < 5)
            return "Minimum 5 Character Name";
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
        int selectRBById = jenis.getCheckedRadioButtonId();
        if (selectRBById == -1)
            return "required";

        selectedRadioButton = findViewById(selectRBById);
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
            Toast.makeText(this, "format tanggal dd-MM-yyyy", Toast.LENGTH_LONG).show();
            return "format salah";
        }
        if (validator.isdateValid(data))
            return "tanggal tidak valid";
        return null;
    }
}