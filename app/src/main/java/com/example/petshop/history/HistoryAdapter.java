package com.example.petshop.history;

import static com.example.petshop.pelengkap.DateValidator.convertDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshop.R;

import java.util.ArrayList;

interface ItemClickListener {
    void onItemClick(int position);
}

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private final Context context;
    private final ArrayList<DataHistory> histories;
    private ItemClickListener itemClickListener;

    public HistoryAdapter(Context context) {
        this.context = context;
        this.histories = new ArrayList<>();
    }

    public void addSingleHistory(DataHistory history) {
        histories.add(history);
        notifyItemInserted(histories.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAllHistory(ArrayList<DataHistory> history) {
        histories.addAll(history);
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        DataHistory history = histories.get(position);

        if (history.getNamaHewa().equals("penanda")) {
            return PENANDA;
        } else
            return ITEM;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        
        if (viewType == PENANDA) {
            view = inflater.inflate(R.layout.recycler_penanda_history, parent, false);
            return new PenandaHistoryViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.recycler_item_history, parent, false);
            return new ItemHistoryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DataHistory history = histories.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    private static final int PENANDA = 0;
    private static final int ITEM = 1;

    private static class PenandaHistoryViewHolder extends HistoryViewHolder {
        private final TextView txtPenanda;

        PenandaHistoryViewHolder(View view) {
            super(view);
            txtPenanda = view.findViewById(R.id.textViewPenanda);
        }

        @SuppressLint("SetTextI18n")
        @Override
        void bind(DataHistory history) {
            if (history.getIdPenitipan() != null) {
                txtPenanda.setText("penitipan");
            } else if (history.getIdTransaksi() != null)
                txtPenanda.setText("transaksi");
        }
    }

    private class ItemHistoryViewHolder extends HistoryViewHolder {
        private final TextView txtItemHistory;
        public ItemHistoryViewHolder(View view) {
            super(view);
            txtItemHistory = view.findViewById(R.id.txtNamaHewanListPembayaran);
        }

        @SuppressLint("SetTextI18n")
        @Override
        void bind(DataHistory history) {
            if (!history.getDatetime().equals("kosong")) {
                txtItemHistory.setText(history.getNamaHewa() + " (" +
                        convertDateFormat(history.getDatetime(),
                                "yyyy-MM-dd HH:mm:ss", "HH:mm:ss dd-MM-yyyy") + ")");

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(position);
                        Log.d("TAG, bind: ", String.valueOf(position));
    //                    if (history.getIdPemesanan() != null) {
    //                        itemClickListener.onItemClick(position);
    //                    } else if (history.getIdPenitipan() != null) {
    //                        itemClickListener.onItemClick(position);
    //                    } else if (history.getIdTransaksi() != null) {
    //                        itemClickListener.onItemClick(position);
    //                    }
                    }
                });
            } else {
                txtItemHistory.setText(history.getNamaHewa());
            }
        }
    }

    abstract static class HistoryViewHolder extends RecyclerView.ViewHolder {
        HistoryViewHolder(View view) {
            super(view);
        }

        abstract void bind(DataHistory history);
    }
}