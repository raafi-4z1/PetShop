package com.example.petshop.ui.detail;

import static com.example.petshop.pelengkap.DateValidator.String2Date;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;
import static com.example.petshop.pelengkap.StringPhone.formatPhone;
import static com.example.petshop.pelengkap.StringPhone.phoneNumber;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.petshop.R;
import com.example.petshop.data.local.LocalStorage;
import com.example.petshop.data.remote.midtrans.Midtrans;
import com.example.petshop.ui.pemesanan.PemesananActivity;
import com.example.petshop.ui.penitipan.PenitipanActivity;
import com.example.petshop.ui.viewmodel.detail.DetailViewModel;
import com.example.petshop.ui.viewmodel.detail.DetailViewModelFactory;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.ShippingAddress;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Objects;

public class DetailItemActivity extends AppCompatActivity {
    Boolean isHistory;
    private DetailViewModel detailViewModel;
    private Midtrans midtrans;
    private boolean pemesanan = false;
    private String idHewan, namaHewan, jenisHewan, jumlahHewan, hargaHewan, namaPemesan, phonePemesan, emailPemesan, alamatPemesan;
    private Button btnBayar, btnCancel;

    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detail_item);

        LocalStorage localStorage = new LocalStorage(this);
        DetailViewModelFactory factory = new DetailViewModelFactory(localStorage);

        detailViewModel = new ViewModelProvider(this, factory).get(DetailViewModel.class);
        midtrans = new Midtrans(this);

        btnBayar = findViewById(R.id.btnBayar);
        btnCancel = findViewById(R.id.btnCancel);

        TextView hewan = findViewById(R.id.txtNamaHewanDetail);
        TextView jenis = findViewById(R.id.txtJenisHewanDetail);
        TextView jumlah = findViewById(R.id.txtJumlahHewanDetail);

        TextView nameProfile = findViewById(R.id.txtNameUser);
        TextView namaUser = findViewById(R.id.txtNamaPemesanDetail);
        TextView phone = findViewById(R.id.txtNoHpDetail);
        TextView txtEmail = findViewById(R.id.txtEmailDetail);
        TextView txtAlamat = findViewById(R.id.txtAlamatDetail);

        nameProfile.setText(localStorage.getNama());
        idHewan = getIntent().getStringExtra("id_hewan");
        isHistory = getIntent().getBooleanExtra("is_history", true);
        pemesanan = getIntent().getBooleanExtra("pemesanan", false);
        Log.d("TAG, onCreate: ", String.valueOf(isHistory));

        detailViewModel.getResult().observe(this, result -> {
            if (result != null) {
                try {
                    namaHewan = result.getString("namaHewan");
                    jenisHewan = result.getString("jenisHewan");
                    jumlahHewan = result.getString("jumlahHewan");
                    hargaHewan = result.getString("hargaHewan");

                    namaPemesan = result.getString("namaPemesan");
                    emailPemesan = result.getString("emailPemesan");
                    phonePemesan = result.getString("phonePemesan");
                    alamatPemesan = result.getString("alamatPemesan");

                    setLayoutDetail(result);

                    hewan.setText(result.getString("namaHewan"));
                    jenis.setText(result.getString("jenisHewan"));
                    jumlah.setText(result.getString("jumlahHewan"));

                    namaUser.setText(result.getString("namaPemesan"));
                    phone.setText("+62 " + formatPhone(result.getString("phonePemesan")));
                    txtEmail.setText(result.getString("emailPemesan"));
                    txtAlamat.setText(result.getString("alamatPemesan"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        getData();

        findViewById(R.id.detail_back_button).setOnClickListener(view -> onBackPressed());
        btnBayar.setOnClickListener(view -> transaksiMidtrans());
        btnCancel.setOnClickListener(view -> updateCancel());
    }

    private void getData() {
        JSONObject params = new JSONObject();
        try {
            params.put("id_hewan", idHewan);
            params.put("pemesanan", pemesanan ? true : "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        detailViewModel.setDataRemote(this, "user/detailitem", "post",
                true, data);
    }

    @SuppressLint("SetTextI18n")
    private void setLayoutDetail(JSONObject response) throws JSONException {
        String hargaHewanView = String.valueOf(Double.parseDouble(hargaHewan) * Double.parseDouble(jumlahHewan));
        String valJasaBayar = response.getString("valJasaBayar");

        TextView tglMasuk = findViewById(R.id.txtTglMasukDetail);
        TextView totalBayar = findViewById(R.id.txtTotalBayarDetail);

        LinearLayout linearLayoutPem = findViewById(R.id.idLinearLayoutPembayaranDetail);
        CardView cardViewKDetail = findViewById(R.id.cardViewKeteranganDetail);
        RelativeLayout relativeLayoutVA = findViewById(R.id.rLayoutVA);

        boolean stsPesRes = response.getString("stsPesRes").equals("CANCEL");
        if (stsPesRes) {
            totalBayar.setText("-");
            linearLayoutPem.removeView(btnBayar);
            linearLayoutPem.removeView(btnCancel);
            linearLayoutPem.removeView(relativeLayoutVA);
            cardViewKDetail.setVisibility(View.VISIBLE);
        } else {
            LinearLayout linearLayoutSCVW = findViewById(R.id.linearLayoutScrollViewDetail);
            linearLayoutSCVW.removeView(cardViewKDetail);
        }

        String statusRes = response.getString("statusRes");
        if (statusRes.equals("SUCCESS")) {
            linearLayoutPem.removeView(btnBayar);
            linearLayoutPem.removeView(btnCancel);
            linearLayoutPem.removeView(relativeLayoutVA);

            TextView tglBayar = findViewById(R.id.txtTanggalBayarDetail);
            hargaHewanView = hargaHewanView + " (Lunas)";
            tglBayar.setText(response.getString("tglBayar"));
        } else if (statusRes.equals("CANCEL")) {
            valJasaBayar = valJasaBayar + " (Cancel)";
            linearLayoutPem.removeView(relativeLayoutVA);
        }

        if (hargaHewan.equals("0")) {
            linearLayoutPem.removeView(btnBayar);
            linearLayoutPem.removeView(relativeLayoutVA);
        } else {
            TextView jasaBayar = findViewById(R.id.txtJasaBayarDetail);
            totalBayar.setText("Rp. " + hargaHewanView);
            jasaBayar.setText(valJasaBayar);

            if (!valJasaBayar.equals("-")) {
                CardView copy = findViewById(R.id.ivCopyDetail);
                TextView va_number = findViewById(R.id.txtVAPembayaranDetail);
                String va = response.getString("va");

                if (!va.equals("-")) {
                    String finalValJasaBayar = valJasaBayar;
                    copy.setOnClickListener(view -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(String.format("virtual account %s",
                                finalValJasaBayar), va);
                        clipboard.setPrimaryClip(clip);
                        // Only show a toast for Android 12 and lower.
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
                    });
                    va_number.setText(va);
                }

                btnBayar.setText("Ganti Metode Pembayaran");
            }
        }

        if (response.getString("isPemesanan").equals("true")) {
            RelativeLayout relativeLayoutTgl = findViewById(R.id.idRelativeLayoutTgl);
            relativeLayoutTgl.removeView(findViewById(R.id.idLinearLayoutTglKeluarDetail));

            TextView title = findViewById(R.id.txtTitleDetail);
            TextView tgl = findViewById(R.id.txtTitleTglMasukDetail);
            String tglPesan = response.getString("tglPesan");

            if (!statusRes.equals("SUCCESS") && !stsPesRes)
                if (Objects.requireNonNull(String2Date(LocalDateTime.now().format(DateTimeFormatter
                                .ofPattern("yyyy-MM-dd")))).after(String2Date(tglPesan))
                ) {
                    linearLayoutPem.removeView(btnBayar);
                    linearLayoutPem.removeView(btnCancel);
                }

            title.setText("pemesanan pet");
            tgl.setText("tanggal pemesanan");
            tglMasuk.setText(convertDateFormat(tglPesan, "yyyy-MM-dd", "dd-MM-yyyy"));
        } else {
            String tglMasRes = response.getString("tglMasRes");
            String tglKelRes = response.getString("tglKelRes");

            if (!statusRes.equals("SUCCESS") && !stsPesRes)
                if (Objects.requireNonNull(String2Date(LocalDateTime.now().format(DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss")))).after(String2Date(tglMasRes))
                ) {
                    linearLayoutPem.removeView(btnBayar);
                    if (Objects.requireNonNull(String2Date(LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            )).after(String2Date(tglKelRes)))
                        linearLayoutPem.removeView(btnCancel);
                }

            TextView tglKeluar = findViewById(R.id.txtTglKeluarDetail);
            tglMasuk.setText(convertDateFormat(tglMasRes, "yyyy-MM-dd HH:mm:ss", "HH:mm:ss dd-MM-yyyy"));
            tglKeluar.setText(convertDateFormat(tglKelRes, "yyyy-MM-dd HH:mm:ss", "HH:mm:ss dd-MM-yyyy"));
        }
    }

    public void JadwalkanLagi(View view) {
        Class<?> to = PenitipanActivity.class;
        if (pemesanan)
            to = PemesananActivity.class;

        startActivity(new Intent(getApplicationContext(), to)
                .putExtra("jadwalkanLagi", true)
                .putExtra("namaPemesan", namaPemesan)
                .putExtra("emailPemesan", emailPemesan)
                .putExtra("phonePemesan", phonePemesan)
                .putExtra("alamatPemesan", alamatPemesan)
                .putExtra("namaHewan", namaHewan)
                .putExtra("jenisHewan", jenisHewan)
                .putExtra("jumlahHewan", jumlahHewan)
        );
        finish();
    }

    private void updateTransaksi(String invoice, Double prices) {
        JSONObject params = new JSONObject();
        try {
            params.put("invoice", invoice);
            params.put("id_hewan", idHewan);
            params.put("total_harga", prices);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        detailViewModel.setTransaksi(this, "user/updatetransaksi", "post",
                true, data, true);
    }

    private void updateCancel() {
        JSONObject params = new JSONObject();
        try {
            params.put("status_pesan", "CANCEL");
            params.put("id_hewan", idHewan);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        detailViewModel.setTransaksi(this, "user/updatecancel", "post",
                true, data, false);
        startActivity(getIntent());
        finish();
    }

    public void transaksiMidtrans() {
        int jumlahItem = Integer.parseInt(jumlahHewan);
        double prices, harga = Double.parseDouble(hargaHewan);
        String orderId = "PetShop-" + namaPemesan.substring(0, 5).replace(" ", "")
                + "-" + System.currentTimeMillis();
        prices = jumlahItem * harga;

        updateTransaksi(orderId, prices);

        // Midtrans (1.26.0-SANDBOX) / (2.0.0-SANDBOX)
        TransactionRequest transactionRequest = new TransactionRequest(orderId, prices);
        ItemDetails detail = new ItemDetails(idHewan, harga, jumlahItem, namaHewan + " (" + jenisHewan + ")");
        ArrayList<ItemDetails> itemDetails = new ArrayList<>();

        itemDetails.add(detail);
        transactionRequest.setItemDetails(itemDetails);

        transactionRequest.setCustomerDetails(initCustomerDetails());
        transactionRequest.setEnabledPayments(midtrans.setEnabledPayments());
        //set expiry time object to transaction request
        transactionRequest.setExpiry(midtrans.expiryModel());

        MidtransSDK.getInstance().setTransactionRequest(transactionRequest);
        MidtransSDK.getInstance().startPaymentUiFlow(this);
    }

    private CustomerDetails initCustomerDetails() {
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerIdentifier(namaPemesan.replace(" ", "-"));
        customerDetails.setPhone("0" + phoneNumber(phonePemesan));
        customerDetails.setFirstName(namaPemesan);
        customerDetails.setEmail(emailPemesan);

        ShippingAddress shippingAddress = new ShippingAddress();
        customerDetails.setShippingAddress(shippingAddress);

        BillingAddress billingAddress = new BillingAddress();
        billingAddress.setAddress(alamatPemesan);
        customerDetails.setBillingAddress(billingAddress);

        return customerDetails;
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra("refresh", false)) {
            Intent resultIntent = new Intent().putExtra("is_history", isHistory);
            setResult(Activity.RESULT_OK, resultIntent);
        }
        super.onBackPressed(); // Menyelesaikan aktivitas.
    }
}