package com.example.petshop.transaksi;

import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshop.R;

import java.util.ArrayList;
import java.util.Objects;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<DataClass> dataList;

    public Adapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
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

            if (dataList.get(position).getStatus() != null) {
                if (Objects.equals(dataList.get(position).getStatus(), "lunas")) {
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
                Log.d("TAG, onBindViewHolder: ", String.valueOf(dataList.get(holder.getAdapterPosition()).getNamaHewan()));
                Log.d("TAG, onBindViewHolder: ", String.valueOf(dataList.get(holder.getAdapterPosition()).getStatus()));
                Log.d("TAG, onBindViewHolder: ", String.valueOf(dataList.get(holder.getAdapterPosition()).getTglMasuk()));
                Log.d("TAG, onBindViewHolder: ", String.valueOf(dataList.get(holder.getAdapterPosition()).getTglPemesanan()));
                Log.d("TAG, onBindViewHolder: ", String.valueOf(dataList.get(holder.getAdapterPosition()).getTransaksi()));

                if ( dataList.get(holder.getAdapterPosition()).getTransaksi() != null) {
                    context.startActivity(new Intent(context, TransaksiActivity.class)
                            .putExtra("idTransaksi", dataList.get(holder.getAdapterPosition()).getTransaksi()));
                } else {
                    Toast.makeText(context, "Tunggu Admin mengecek dan memberikan harga", Toast.LENGTH_SHORT).show();
                }
    //            dataList.clear();
    //            notifyDataSetChanged();
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