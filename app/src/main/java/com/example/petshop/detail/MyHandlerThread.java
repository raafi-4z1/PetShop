package com.example.petshop.detail;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class MyHandlerThread extends HandlerThread {
    private Handler handler;
    private Context context;

    public MyHandlerThread(Context context) {
        super("MyHandlerThread");
        this.context = context;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Lakukan tugas berat di sini, seperti akses ke jaringan atau komputasi rumit
                // Contoh:
                // getDataFromServer();

                // Setelah tugas selesai, kirim pesan untuk melakukan tindakan di thread utama
                sendEmptyMessage(MSG_FINISH);
            }
        };
    }

    private static final int MSG_FINISH = 1;

    public void doBackgroundTask() {
        // Kirim pesan ke handler untuk melakukan tugas di thread latar belakang
        handler.sendEmptyMessage(0);
    }

    // Dipanggil untuk menyelesaikan HandlerThread
    public void quitThread() {
        handler.sendEmptyMessage(MSG_FINISH);
    }
}
