package com.example.petshop.ui.penitipan;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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

public class PenitipanActivity extends AppCompatActivity {
    private TextInputEditText fullName, namaHewan, jumlah, tglMasuk, tglKeluar;
    private RadioGroup jenis;
    private RadioButton selectedRadioButton;
    private TextInputLayout fullNameContainer, namaHewanContainer, jenisContainer, jumlahContainer, tglMasukContainer, tglKeluarContainer;
    private MainViewModel mainViewModel;
    private LocalStorage local;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_penitipan);

        local = new LocalStorage(this);
        MainViewModelFactory factory = new MainViewModelFactory(local);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(local.getNama());

        bindViews();
        focusListener();

        if (getIntent().getBooleanExtra("jadwalkanLagi", false)) {
            fullName.setText(getIntent().getStringExtra("namaPemesan"));
            namaHewan.setText(getIntent().getStringExtra("namaHewan"));
            jumlah.setText(getIntent().getStringExtra("jumlahHewan"));

            if (Objects.equals(getIntent().getStringExtra("jenisHewan"), "Kucing")) {
                jenis.check(R.id.radioButtonKucing);
            } else {
                jenis.check(R.id.radioButtonAnjing);
            }
        } else
            fullName.setText(local.getNama());

        tglMasuk.setOnClickListener(view -> clickTanggal(true) );
        tglKeluar.setOnClickListener(view -> clickTanggal(false) );

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

        findViewById(R.id.penitipan_back_button).setOnClickListener(view
                -> finish());

        findViewById(R.id.btnKirim).setOnClickListener(view
                -> send() );
    }

    private void clickTanggal(Boolean isTglMasuk) {
        Calendar myCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));

        DatePickerDialog.OnDateSetListener datePickerListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker(myCalendar, isTglMasuk);
        };

        new DatePickerDialog(this, datePickerListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Calendar myCalendar, Boolean isTglMasuk) {
        int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfDay) -> {
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minuteOfDay);
            setDateTime(myCalendar, isTglMasuk);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void setDateTime(Calendar myCalendar, Boolean isTglMasuk) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.forLanguageTag("id"));
        String formattedDateTime = dateTimeFormat.format(myCalendar.getTime());

        if (isTglMasuk) {
            tglMasuk.setText(formattedDateTime);
        } else {
            tglKeluar.setText(formattedDateTime);
        }
    }

    private void bindViews() {
        fullName = findViewById(R.id.txtFullNamePenitipan);
        namaHewan = findViewById(R.id.txtNamaHewanPenitipan);
        jenis = findViewById(R.id.radioGroupJenisHewanPenitipan);
        jumlah =  findViewById(R.id.txtJumlahPenitipan);
        tglMasuk = findViewById(R.id.txtTanggalMasukPenitipan);
        tglKeluar = findViewById(R.id.txtTanggalKeluarPenitipan);

        fullNameContainer = findViewById(R.id.fullNamePenitipanContainer);
        namaHewanContainer = findViewById(R.id.namaHewanPenitipanContainer);
        jenisContainer = findViewById(R.id.jenisHewanPenitipanContainer);
        jumlahContainer =  findViewById(R.id.jumlahPenitipanContainer);
        tglMasukContainer = findViewById(R.id.tanggalMasukPenitipanContainer);
        tglKeluarContainer = findViewById(R.id.tanggalKeluarPenitipanContainer);

        // Disable focusability of the TextInputEditText
        tglMasuk.setFocusable(false);
        tglKeluar.setFocusable(false);
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
            alertFail("Tolong dicek kembali", this);
        }
    }

    private void sendKirim() {
        JSONObject params = new JSONObject();
        try {
            params.put("full_name", Objects.requireNonNull(fullName.getText()).toString());
            params.put("nama_hewan", Objects.requireNonNull(namaHewan.getText()).toString());
            params.put("jenis_hewan", selectedRadioButton.getText().toString());
            params.put("jumlah", Objects.requireNonNull(jumlah.getText()).toString());
            params.put("tgl_masuk", convertDateFormat(Objects.requireNonNull(tglMasuk.getText()).toString(), "HH:mm:ss dd-MM-yyyy", "yyyy-MM-dd HH:mm:ss"));
            params.put("tgl_keluar", convertDateFormat(Objects.requireNonNull(tglKeluar.getText()).toString(), "HH:mm:ss dd-MM-yyyy", "yyyy-MM-dd HH:mm:ss"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        mainViewModel.setDataRemote(this, "user/penitipan", "post",
                true, data, null);
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

    private CharSequence validTglMasuk() {
        String data = String.valueOf(tglMasuk.getText());
        DateValidator validator = new DateValidator();

        if (data.isEmpty())
            return "required";
        if (!data.matches("\\d{2}:\\d{2}:\\d{2} \\d{2}-\\d{2}-\\d{4}")) {
            // Regex pattern for "HH:mm:ss dd-MM-yyyy"
            Toast.makeText(this, "format tanggal HH:mm:ss dd-MM-yyyy", Toast.LENGTH_LONG).show();
            return "format salah";
        }
        if (!DateValidator.isdateTimeValid(data))
            return "DateTime tidak valid";

        return null;
    }

    private CharSequence validTglKeluar() {
        String dataTglMasuk = String.valueOf(tglMasuk.getText());
        String data = String.valueOf(tglKeluar.getText());

        if (data.isEmpty())
            return "required";
        if (!data.matches("\\d{2}:\\d{2}:\\d{2} \\d{2}-\\d{2}-\\d{4}")) {
            // Regex pattern for "HH:mm:ss dd-MM-yyyy"
            Toast.makeText(this, "format tanggal HH:mm:ss dd-MM-yyyy", Toast.LENGTH_LONG).show();
            return "format salah";
        }
        if (!DateValidator.isdateTimeValid(data))
            return "DateTime tidak valid";
        if (DateValidator.isdateTimeSmaller(data, dataTglMasuk))
            return "DateTime keluar tidak valid";

        return null;
    }
}