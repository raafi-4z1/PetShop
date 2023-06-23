package com.example.petshop.transaksi;

public class DataClass {
    private final String nama_hewan, status, id_transaksi, tanggal_masuk, tanggal_pemesanan;

    DataClass(String nama_hewan, String status, String id_transaksi, String tanggal_masuk, String tanggal_pemesanan) {
        this.nama_hewan = nama_hewan;
        this.status = status;
        this.id_transaksi = id_transaksi;
        this.tanggal_masuk = tanggal_masuk;
        this.tanggal_pemesanan = tanggal_pemesanan;
    }

    public String getNamaHewan() {
        return this.nama_hewan;
    }

    public String getStatus() {
        return this.status;
    }

    public String getTransaksi() {
        return this.id_transaksi;
    }

    public String getTglMasuk() {
        return this.tanggal_masuk;
    }

    public String getTglPemesanan() {
        return this.tanggal_pemesanan;
    }
}