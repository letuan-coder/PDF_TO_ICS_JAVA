package com.example.demo_tttn.models;

public class GiangVien {
    private String maGiangVien;
    private String tenGiangVien;
    private String sdt;
    private String khoa;
    public String getMaGiangVien() {
        return maGiangVien;
    }

    // Setter cho maGiangVien
    public void setMaGiangVien(String maGiangVien) {
        this.maGiangVien = maGiangVien;
    }

    // Getter cho tenGiangVien
    public String getTenGiangVien() {
        return tenGiangVien;
    }

    // Setter cho tenGiangVien
    public void setTenGiangVien(String tenGiangVien) {
        this.tenGiangVien = tenGiangVien;
    }

    // Getter cho sdt
    public String getSdt() {
        return sdt;
    }

    // Setter cho sdt
    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    // Getter cho khoa
    public String getKhoa() {
        return khoa;
    }

    // Setter cho khoa
    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }
    public GiangVien(String maGiangVien, String tenGiangVien, String sdt, String khoa) {
        this.maGiangVien = maGiangVien;
        this.tenGiangVien = tenGiangVien;
        this.sdt = sdt;
        this.khoa = khoa;
    }
    public GiangVien(){

    }

}
