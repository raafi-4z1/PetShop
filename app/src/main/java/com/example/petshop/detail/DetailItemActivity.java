package com.example.petshop.detail;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.String2Date;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;
import static com.example.petshop.pelengkap.StringPhone.formatPhone;
import static com.example.petshop.pelengkap.StringPhone.phoneNumber;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.example.petshop.LoginSignup.LoginActivity;
import com.example.petshop.R;
import com.example.petshop.pelengkap.Http;
import com.example.petshop.pelengkap.LocalStorage;
import com.example.petshop.pemesanan.PemesananActivity;
import com.example.petshop.penitipan.PenitipanActivity;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.BillingAddress;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ExpiryModel;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.ShippingAddress;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
import com.midtrans.sdk.uikit.api.model.PaymentType;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DetailItemActivity extends AppCompatActivity implements TransactionFinishedCallback {
    private boolean pemesanan = false;
    private String idHewan, namaHewan, jenisHewan, jumlahHewan, hargaHewan, namaPemesan, phonePemesan, emailPemesan, alamatPemesan;
    private LocalStorage localStorage;
    MyHandlerThread myHandlerThread;
    private Button btnBayar, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_detail_item);

        pemesanan = getIntent().getBooleanExtra("pemesanan", false);
        localStorage = new LocalStorage(this);

        btnBayar = findViewById(R.id.btnBayar);
        btnCancel = findViewById(R.id.btnCancel);
        idHewan = getIntent().getStringExtra("id_hewan");

        TextView nameProfile = findViewById(R.id.txtNameUser);
        nameProfile.setText(localStorage.getNama());
        findViewById(R.id.detail_back_button).setOnClickListener(view -> finish());

        myHandlerThread = new MyHandlerThread(DetailItemActivity.this);
        getData();
        initMidtransSdk();

        btnBayar.setOnClickListener(view -> transaksi());
        btnCancel.setOnClickListener(view -> updateCancel());
    }

    private void transaksi() {
        int jumlahItem = Integer.parseInt(jumlahHewan);
        double prices, harga = Double.parseDouble(hargaHewan);
        String orderId = "PetShop-" + namaPemesan.substring(0, 5).replace(" ", "")
                + "-" + System.currentTimeMillis();
        prices = jumlahItem * harga;

        updateTransaksi(orderId);

        // Midtrans (1.26.0-SANDBOX) / (2.0.0-SANDBOX)
        TransactionRequest transactionRequest = new TransactionRequest(orderId, prices);
        ItemDetails detail = new ItemDetails(idHewan, harga, jumlahItem, namaHewan + " (" + jenisHewan + ")");
        ArrayList<ItemDetails> itemDetails = new ArrayList<>();

        itemDetails.add(detail);
        transactionRequest.setItemDetails(itemDetails);

        transactionRequest.setCustomerDetails(initCustomerDetails());
        transactionRequest.setEnabledPayments(setEnabledPayments());
        //set expiry time object to transaction request
        transactionRequest.setExpiry(expiryModel());

        MidtransSDK.getInstance().setTransactionRequest(transactionRequest);
        MidtransSDK.getInstance().startPaymentUiFlow(DetailItemActivity.this);
    }

    private ExpiryModel expiryModel() {
        // set expiry time
        ExpiryModel expiryModel = new ExpiryModel();
        // set the formatted time to expiry model
        expiryModel.setStartTime(getFormattedTime(System.currentTimeMillis()));
        expiryModel.setDuration(45);
        // set time unit
        expiryModel.setUnit(ExpiryModel.UNIT_MINUTE);

        return expiryModel;
    }

    private String getFormattedTime(long currentTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault());
        Date date = new Date(currentTimeMillis);
        return sdf.format(date);
    }

    private List<String> setEnabledPayments() {
        // Set the payment methods
        // QRIS Tidak bisa digunakan (On mobile platform you are automatically redirected to ShopeePay/GoPay Simulator.)
        // CSTORE For Indomaret and Alfamart and Other (Error saat melakukan pembayaran)
        List<String> enabledPayments = new ArrayList<>();
        enabledPayments.add(PaymentType.GOPAY); // untuk SANDBOX tidak bisa digunakan
        enabledPayments.add(PaymentType.SHOPEEPAY);
        enabledPayments.add(PaymentType.ALFAMART);
        enabledPayments.add(PaymentType.INDOMARET);
//        enabledPayments.add(PaymentType.BANK_TRANSFER);
        enabledPayments.add(PaymentType.BCA_VA);
        enabledPayments.add(PaymentType.E_CHANNEL); // Mandiri Bill
        enabledPayments.add(PaymentType.BNI_VA);
        enabledPayments.add(PaymentType.BRI_VA);
        enabledPayments.add(PaymentType.PERMATA_VA);

        return enabledPayments;
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

    private void initMidtransSdk() {
        String url = getString(R.string.api_server) + "/midtrans/";
        SdkUIFlowBuilder.init()
                .setClientKey(getString(R.string.client_key))
                .setContext(DetailItemActivity.this)
                .setTransactionFinishedCallback(this)
                .setMerchantBaseUrl(url)
                .enableLog(true)
                .setColorTheme(new CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                .setLanguage("id")
                .buildSDK();
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
        String url = getString(R.string.api_server) + "/user/detailitem";

        myHandlerThread.start();

        @SuppressLint({"NotifyDataSetChanged", "ShowToast", "SetTextI18n"})
        Handler mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int code = msg.arg1;
                String responseJson = (String) msg.obj;

                switch (code) {
                    case 200:
                        try {
                            JSONObject response = new JSONObject(responseJson).getJSONObject("data");
                            setVarGlb(response);

                            TextView hewan = findViewById(R.id.txtNamaHewanDetail);
                            TextView jenis = findViewById(R.id.txtJenisHewanDetail);
                            TextView jumlah = findViewById(R.id.txtJumlahHewanDetail);

                            TextView namaUser = findViewById(R.id.txtNamaPemesanDetail);
                            TextView phone = findViewById(R.id.txtNoHpDetail);
                            TextView txtEmail = findViewById(R.id.txtEmailDetail);
                            TextView txtAlamat = findViewById(R.id.txtAlamatDetail);

                            setLayoutDetail(response);

                            hewan.setText(namaHewan);
                            jenis.setText(jenisHewan);
                            jumlah.setText(jumlahHewan);

                            namaUser.setText(namaPemesan);
                            phone.setText("+62 " + formatPhone(phonePemesan));
                            txtEmail.setText(emailPemesan);
                            txtAlamat.setText(alamatPemesan);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 204:
                        Toast.makeText(DetailItemActivity.this,
                                "Ada kesalahan sistem atau data dalam penyimpanan tidak lengkap",
                                Toast.LENGTH_LONG).show();

                        break;
                    case 401:
                        try {
                            kode401(new JSONObject(responseJson).getString("message"), DetailItemActivity.this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        localStorage.setToken("");
                        startActivity(new Intent(DetailItemActivity.this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(new JSONObject(responseJson).getString("message"), DetailItemActivity.this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
            }
        };

        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setData(data);
            http.setToken(true);
            http.send();

            Message message = mainHandler.obtainMessage();
            message.arg1 = http.getStatusCode();
            message.obj = http.getResponse();
            mainHandler.sendMessage(message);
        });
        thread.start();
    }

    @SuppressLint("SetTextI18n")
    private void setLayoutDetail(JSONObject response) throws JSONException {
        String hargaHewanView = String.valueOf(Double.parseDouble(hargaHewan) * Double.parseDouble(jumlahHewan));
        String valJasaBayar = response.getString("jenis_pembayaran").equals("null") ? "-" : response.getString("jenis_pembayaran");

        TextView tglMasuk = findViewById(R.id.txtTglMasukDetail);
        TextView totalBayar = findViewById(R.id.txtTotalBayarDetail);

        LinearLayout linearLayoutPem = findViewById(R.id.idLinearLayoutPembayaranDetail);
        CardView cardViewKDetail = findViewById(R.id.cardViewKeteranganDetail);
        RelativeLayout relativeLayoutVA = findViewById(R.id.rLayoutVA);

        boolean stsPesRes = response.getString("status_pesan").equals("CANCEL");
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

        String statusRes = response.getString("status");
        if (statusRes.equals("SUCCESS")) {
            linearLayoutPem.removeView(btnBayar);
            linearLayoutPem.removeView(btnCancel);
            linearLayoutPem.removeView(relativeLayoutVA);

            TextView tglBayar = findViewById(R.id.txtTanggalBayarDetail);
            hargaHewanView = hargaHewanView + " (Lunas)";

            tglBayar.setText(convertDateFormat(response.getString("tanggal_bayar"),
                    "yyyy-MM-dd HH:mm:ss", "EEEE, d MMMM yyyy - HH:mm"));
        } else if (statusRes.equals("CANCEL")) {
            valJasaBayar = valJasaBayar + " (cancel)";
            linearLayoutPem.removeView(relativeLayoutVA);
        }

        if (hargaHewan.equals("0")) {
            linearLayoutPem.removeView(btnBayar);
            linearLayoutPem.removeView(relativeLayoutVA);
        } else {
            TextView jasaBayar = findViewById(R.id.txtJasaBayarDetail);

            totalBayar.setText("Rp. " + hargaHewanView);
            jasaBayar.setText(valJasaBayar);

            if (valJasaBayar.equals("-")) {
                linearLayoutPem.removeView(relativeLayoutVA);
            } else {
                CardView copy = findViewById(R.id.ivCopyDetail);
                TextView va_number = findViewById(R.id.txtVAPembayaranDetail);
                String va = response.getString("va_number").equals("null") ?
                        "-" : response.getString("va_number");

                if (va.equals("-")) {
                    relativeLayoutVA.removeView(copy);
                } else {
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

        if (response.getString("pemesanan").equals("true")) {
            RelativeLayout relativeLayoutTgl = findViewById(R.id.idRelativeLayoutTgl);
            relativeLayoutTgl.removeView(findViewById(R.id.idLinearLayoutTglKeluarDetail));

            TextView title = findViewById(R.id.txtTitleDetail);
            TextView tgl = findViewById(R.id.txtTitleTglMasukDetail);
            String tglPesan = response.getString("tanggal_pemesanan");

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
            String tglMasRes = response.getString("tanggal_masuk");
            String tglKelRes = response.getString("tanggal_keluar");

            if (!statusRes.equals("SUCCESS") && !stsPesRes)
                if (Objects.requireNonNull(String2Date(LocalDateTime.now().format(DateTimeFormatter
                        .ofPattern("yyyy-MM-dd")))).after(String2Date(tglMasRes))
                ) {
                    linearLayoutPem.removeView(btnBayar);
                    if (Objects.requireNonNull(String2Date(LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            )).after(String2Date(tglKelRes)))
                        linearLayoutPem.removeView(btnCancel);
                }

            TextView tglKeluar = findViewById(R.id.txtTglKeluarDetail);
            tglMasuk.setText(convertDateFormat(tglMasRes, "yyyy-MM-dd", "dd-MM-yyyy"));
            tglKeluar.setText(convertDateFormat(tglKelRes, "yyyy-MM-dd", "dd-MM-yyyy"));
        }
    }

    private void setVarGlb(JSONObject response) throws JSONException {
        namaHewan = response.getString("nama_hewan");
        jenisHewan = response.getString("jenis");
        jumlahHewan = response.getString("jumlah");
        hargaHewan = response.getString("harga");

        namaPemesan = response.getString("nama_lengkap").equals("null") ? "-" : response.getString("nama_lengkap");
        emailPemesan = response.getString("email").equals("null") ? "-" : response.getString("email");
        phonePemesan = response.getString("telepon").equals("null") ? "-" : response.getString("telepon");
        alamatPemesan = response.getString("alamat").equals("null") ? "-" : response.getString("alamat");
    }

    public void JadwalkanLagi(View view) {
        Class<?> to = PenitipanActivity.class;
        if (pemesanan)
            to = PemesananActivity.class;

        startActivity(new Intent(getApplicationContext(), to));
        finish();
    }

    @Override
    @SuppressLint("LongLogTag")
    public void onTransactionFinished(TransactionResult result) {
        if (result.getResponse() != null) {
            switch (result.getStatus()) {
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(this, "Transaction Finished. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(getIntent());
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(this, "Transaction Pending. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(getIntent());
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(this, "Transaction Failed. ID: " + result.getResponse().getTransactionId() + ". Message: " + result.getResponse().getStatusMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        } else if (result.isTransactionCanceled()) {
            Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_LONG).show();
        } else {
            if (result.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)) {
                Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Transaction Finished with failure.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateTransaksi(String invoice) {
        JSONObject params = new JSONObject();
        try {
            params.put("invoice", invoice);
            params.put("id_hewan", idHewan);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = params.toString();
        String url = getString(R.string.api_server) + "/user/updatetransaksi";

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

                assert response != null;
                switch (http.getStatusCode()) {
                    case 200:
                        try {
                            Log.d("TAG, updateTransaksi: ", response.getString("data"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
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
            });
        });
        thread.start();
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
        String url = getString(R.string.api_server) + "/user/updatecancel";

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

                assert response != null;
                switch (http.getStatusCode()) {
                    case 200:
                        try {
                            Toast.makeText(this, response.getString("data"), Toast.LENGTH_LONG).show();
                            finish();
                            startActivity(getIntent());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
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
            });
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Selesaikan MyHandlerThread saat Activity dihancurkan
        myHandlerThread.quitThread();
    }
}