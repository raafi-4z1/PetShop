package com.example.petshop.ui.transaksi;

import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshop.R;
import com.example.petshop.ui.detail.DetailItemActivity;

import java.util.ArrayList;
import java.util.Objects;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<DataClass> dataList;
    private int mRequestCode; // Ini untuk menyimpan REQUEST_CODE

    public Adapter(Context context, ArrayList<DataClass> dataList, int requestCode) {
        this.context = context;
        this.dataList = dataList;
        this.mRequestCode = requestCode;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_pembayaran, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.txtNamaHewan.setText(dataList.get(position).getNamaHewan());
        if (!dataList.get(position).getNamaHewan().equals("Tidak ada data")) {
            if (dataList.get(position).getTglPemesanan() == null) {
                holder.txtTanggal.setText("(" +
                        convertDateFormat(dataList.get(position).getTglMasuk(),
                                "yyyy-MM-dd", "dd-MM-yyyy") + ")");
            } else {
                holder.txtTanggal.setText("(" +
                        convertDateFormat(dataList.get(position).getTglPemesanan(),
                                "yyyy-MM-dd", "dd-MM-yyyy") + ")");
            }

            if (!Objects.equals(dataList.get(position).getStatus(), "WAITING")) {
                if (Objects.equals(dataList.get(position).getStatus(), "PENDING")) {
                    holder.txtKeterangan.setText(dataList.get(position).getStatus());
                    holder.txtKeterangan.setTextColor(context.getResources().getColor(R.color.teal_200));
                    holder.txtKeterangan.setBackgroundResource(R.drawable.border_teal);
                } else {
                    holder.txtKeterangan.setText(dataList.get(position).getStatus());
                    holder.txtKeterangan.setTextColor(context.getResources().getColor(R.color.orange));
                    holder.txtKeterangan.setBackgroundResource(R.drawable.border_orange);
                }
            }

            holder.recCard.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailItemActivity.class)
                        .putExtra("id_hewan", dataList.get(holder.getAdapterPosition()).getIdHewan())
                        .putExtra("pemesanan", dataList.get(holder.getAdapterPosition()).getTglPemesanan() != null)
                        .putExtra("refresh", true);
                ((Activity) context).startActivityForResult(intent, mRequestCode);
            });

        } else {
            holder.txtTanggal.setVisibility(View.GONE);
            holder.txtKeterangan.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        //    ImageView recImage;
        TextView txtNamaHewan, txtTanggal, txtKeterangan;
        CardView recCard;

        public MyViewHolder(View itemView) {
            super(itemView);
            recCard = itemView.findViewById(R.id.recCardListPembayaran);
            txtNamaHewan = itemView.findViewById(R.id.txtNamaHewanListPembayaran);
            txtTanggal = itemView.findViewById(R.id.txtTanggalListPembayaran);
            txtKeterangan = itemView.findViewById(R.id.txtKeterangan);
        }
    }
}