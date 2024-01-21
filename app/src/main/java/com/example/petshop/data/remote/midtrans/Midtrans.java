package com.example.petshop.data.remote.midtrans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petshop.pelengkap.Credentials;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.ExpiryModel;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
import com.midtrans.sdk.uikit.api.model.PaymentType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Midtrans {
    private final Context context;
    public Midtrans(Context context) {
        this.context = context;
        SdkUIFlowBuilder.init()
                .setClientKey(Credentials.CLIENT_KEY)
                .setContext(context)
                .setTransactionFinishedCallback(this::onTransactionFinished)
                .setMerchantBaseUrl(Credentials.BASE_URL + "midtrans/")
                .enableLog(true)
                .setColorTheme(new CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                .setLanguage("id")
                .buildSDK();
    }

    @SuppressLint("LongLogTag")
    private void onTransactionFinished(TransactionResult result) {
        if (result.getResponse() != null) {
            switch (result.getStatus()) {
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(context, "Transaction Finished. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    context.startActivity(((AppCompatActivity) context).getIntent());
                    ((AppCompatActivity) context).finish();
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(context, "Transaction Pending. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    context.startActivity(((AppCompatActivity) context).getIntent());
                    ((AppCompatActivity) context).finish();
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(context, "Transaction Failed. ID: " + result.getResponse().getTransactionId() + ". Message: " + result.getResponse().getStatusMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        } else if (result.isTransactionCanceled()) {
            Toast.makeText(context, "Transaction Canceled", Toast.LENGTH_LONG).show();
        } else {
            if (result.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)) {
                Toast.makeText(context, "Transaction Invalid", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Transaction Finished with failure.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public ExpiryModel expiryModel() {
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

    public List<String> setEnabledPayments() {
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
}