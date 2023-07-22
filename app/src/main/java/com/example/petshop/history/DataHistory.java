package com.example.petshop.history;

public class DataHistory {
    private final String id_pemesanan, id_penitipan, id_hewan, nama_hewan, datetime;
    public DataHistory(String id_pemesanan, String id_penitipan, String id_hewan, String nama_hewan, String datetime) {
        this.id_pemesanan = id_pemesanan;
        this.id_penitipan = id_penitipan;
        this.id_hewan = id_hewan;
        this.nama_hewan = nama_hewan;
        this.datetime = datetime;
    }

    public String getIdPemesanan() {
        return this.id_pemesanan;
    }

    public String getIdPenitipan() {
        return this.id_penitipan;
    }

    public String getIdHewan() {
        return this.id_hewan;
    }

    public String getNamaHewa() {
        return this.nama_hewan;
    }

    public String getDatetime() {
        return this.datetime;
    }
}