package com.example.petshop.detail;

import static com.example.petshop.pelengkap.Alert.alertFail;
import static com.example.petshop.pelengkap.Alert.kode401;
import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.ShippingAddress;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DetailItemActivity extends AppCompatActivity implements TransactionFinishedCallback {
    private boolean pemesanan = false;
    private String idHewan, namaHewan, jenisHewan, jumlahHewan, hargaHewan, namaPemesan, phonePemesan, emailPemesan, alamatPemesan;
    private LocalStorage localStorage;
    Button btnBayar, btnCancel;

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

        TransactionRequest transactionRequest = new TransactionRequest(orderId, prices);
        ItemDetails detail = new ItemDetails(idHewan, harga, jumlahItem, namaHewan + " (" + jenisHewan + ")");
        ArrayList<ItemDetails> itemDetails = new ArrayList<>();

        itemDetails.add(detail);
        transactionRequest.setCustomerDetails(initCustomerDetails());

//        // set expiry time
//        ExpiryModel expiryModel = new ExpiryModel();
//
//        // set start time
//        long timeInMili = System.currentTimeMillis();
//        // format the time
//        @SuppressLint("SimpleDateFormat")
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
//        df.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
//        // format the time as a string
//        String nowAsISO = df.format(new Date(timeInMili));
//        // set the formatted time to expiry model
//        expiryModel.setStartTime(Utils.getFormattedTime(timeInMili));
//        expiryModel.setDuration(1);
//        // set time unit
//        expiryModel.setUnit(ExpiryModel.UNIT_MINUTE);
//        //set expiry time object to transaction request
//        transactionRequest.setExpiry(expiryModel);

        transactionRequest.setItemDetails(itemDetails);
        MidtransSDK.getInstance().setTransactionRequest(transactionRequest);
        MidtransSDK.getInstance().startPaymentUiFlow(DetailItemActivity.this);
    }

    private CustomerDetails initCustomerDetails() {
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerIdentifier(namaPemesan.replace(" ", "-"));
        customerDetails.setPhone(phonePemesan);
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
                .setClientKey("SB-Mid-client-H5G3QBcJYRcE6cen")
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

        @SuppressLint({"NotifyDataSetChanged", "ShowToast", "SetTextI18n"})
        Thread thread = new Thread(() -> {
            Http http = new Http(this, url);
            http.setMethod("post");
            http.setData(data);
            http.setToken(true);
            http.send();

            runOnUiThread(() -> {
                int code = http.getStatusCode();
                switch (code) {
                    case 200:
                        try {
                            JSONObject response = new JSONObject(http.getResponse()).getJSONObject("data");

                            namaHewan = response.getString("nama_hewan");
                            jenisHewan = response.getString("jenis");
                            jumlahHewan = response.getString("jumlah");
                            hargaHewan = response.getString("harga");

                            String valJasaBayar = response.getString("jenis_pembayaran").equals("null") ? "-" : response.getString("jenis_pembayaran");

                            namaPemesan = response.getString("nama_lengkap");
                            emailPemesan = response.getString("email").equals("null") ? "-" : response.getString("email");
                            phonePemesan = response.getString("telepon");
                            alamatPemesan = response.getString("alamat").equals("null") ? "-" : response.getString("alamat");

                            TextView hewan = findViewById(R.id.txtNamaHewanDetail);
                            TextView jenis = findViewById(R.id.txtJenisHewanDetail);
                            TextView jumlah = findViewById(R.id.txtJumlahHewanDetail);
                            TextView tglMasuk = findViewById(R.id.txtTglMasukDetail);

                            TextView namaUser = findViewById(R.id.txtNamaPemesanDetail);
                            TextView phone = findViewById(R.id.txtNoHpDetail);
                            TextView txtEmail = findViewById(R.id.txtEmailDetail);
                            TextView txtAlamat = findViewById(R.id.txtAlamatDetail);

                            TextView totalBayar = findViewById(R.id.txtTotalBayarDetail);
                            TextView jasaBayar = findViewById(R.id.txtJasaBayarDetail);
                            TextView tglBayar = findViewById(R.id.txtTanggalBayarDetail);
                            TextView va_number = findViewById(R.id.txtVAPembayaranDetail);

                            LinearLayout linearLayoutPem = findViewById(R.id.idLinearLayoutPembayaranDetail);
                            LinearLayout linearLayoutVA = findViewById(R.id.lLayoutVA);

                            if (response.getString("status_pesan").equals("CANCEL")) {
                                totalBayar.setText("-");
                                linearLayoutPem.removeView(btnBayar);
                                linearLayoutPem.removeView(btnCancel);
                                linearLayoutPem.removeView(linearLayoutVA);
                            } else {
                                LinearLayout linearLayoutSCVW = findViewById(R.id.linearLayoutScrollViewDetail);
                                CardView cardViewKDetail = findViewById(R.id.cardViewKeteranganDetail);
                                linearLayoutSCVW.removeView(cardViewKDetail);
                            }

                            if (response.getString("status").equals("SUCCESS")) {
                                linearLayoutPem.removeView(btnBayar);
                                linearLayoutPem.removeView(btnCancel);
                                linearLayoutPem.removeView(linearLayoutVA);

                                hargaHewan = String.format("%s (Lunas)", Double.parseDouble(hargaHewan) * Double.parseDouble(jumlahHewan));
                                tglBayar.setText(convertDateFormat(
                                        response.getString("tanggal_bayar"),
                                        "yyyy-MM-dd HH:mm:ss",
                                        "EEEE, d MMMM yyyy - HH:mm"));
                            }

                            if (hargaHewan.equals("0")) {
                                linearLayoutPem.removeView(btnBayar);
                                linearLayoutPem.removeView(linearLayoutVA);
                            } else {
                                totalBayar.setText(String.valueOf(Double.parseDouble(hargaHewan) * Double.parseDouble(jumlahHewan)));
                                jasaBayar.setText(valJasaBayar);

                                if (valJasaBayar.equals("-")) {
                                    linearLayoutPem.removeView(linearLayoutVA);
                                } else {
                                    va_number.setText(response.getString("va_number").equals("null") ?
                                            "-" : response.getString("va_number"));
                                    btnBayar.setText("Ganti Metode Pembayaran");
                                }
                            }

                            if (response.getString("pemesanan").equals("true")) {
                                RelativeLayout relativeLayoutTgl = findViewById(R.id.idRelativeLayoutTgl);
                                relativeLayoutTgl.removeView(findViewById(R.id.idLinearLayoutTglKeluarDetail));

                                TextView title = findViewById(R.id.txtTitleDetail);
                                TextView tgl = findViewById(R.id.txtTitleTglMasukDetail);

                                title.setText("pemesanan pet");
                                tgl.setText("tanggal pemesanan");
                                tglMasuk.setText(convertDateFormat(
                                        response.getString("tanggal_pemesanan"),
                                        "yyyy-MM-dd",
                                        "dd-MM-yyyy"));
                            } else {
                                TextView tglKeluar = findViewById(R.id.txtTglKeluarDetail);
                                tglMasuk.setText(convertDateFormat(
                                        response.getString("tanggal_masuk"),
                                        "yyyy-MM-dd",
                                        "dd-MM-yyyy"));
                                tglKeluar.setText(convertDateFormat(
                                        response.getString("tanggal_keluar"),
                                        "yyyy-MM-dd",
                                        "dd-MM-yyyy"));
                            }

                            hewan.setText(namaHewan);
                            jenis.setText(jenisHewan);
                            jumlah.setText(jumlahHewan);

                            namaUser.setText(namaPemesan);
                            phone.setText(phonePemesan);
                            txtEmail.setText(emailPemesan);
                            txtAlamat.setText(alamatPemesan);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 204:
                        Toast.makeText(this,
                                "Ada kesalahan sistem atau data dalam penyimpanan tidak lengkap", Toast.LENGTH_LONG);
                        finish();

                        break;
                    case 401:
                        try {
                            kode401(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        localStorage.setToken("");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();

                        break;
                    default:
                        try {
                            alertFail(new JSONObject(http.getResponse()).getString("message"), this);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        break;
                }
            });
        });
        thread.start();
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
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(this, "Transaction Pending. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
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
}