package com.example.petshop.ui.transaksi;

public class DataClass {
    private final String nama_hewan, status, id_hewan, tanggal_masuk, tanggal_pemesanan;

    DataClass(String nama_hewan, String status, String id_hewan, String tanggal_masuk, String tanggal_pemesanan) {
        this.nama_hewan = nama_hewan;
        this.status = status;
        this.id_hewan = id_hewan;
        this.tanggal_masuk = tanggal_masuk;
        this.tanggal_pemesanan = tanggal_pemesanan;
    }

    public String getNamaHewan() {
        return this.nama_hewan;
    }

    public String getStatus() {
        return this.status;
    }

    public String getIdHewan() {
        return this.id_hewan;
    }

    public String getTglMasuk() {
        return this.tanggal_masuk;
    }

    public String getTglPemesanan() {
        return this.tanggal_pemesanan;
    }
}