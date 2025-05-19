package com.example.demo_tttn.models;

public class MonHoc {

    private String maMonHoc;
    private String nhom;
    private String tenMonHoc;
    private String thoiGian;
    private String thoiGianBD;
    private String thoiGianKT;
    private String phong;
    private String tietHoc;
    private String thu;
    private String siSo;
    private String lop;
    public MonHoc(String maMonHoc, String nhom, String tenMonHoc, String thoiGian, String thoiGianBD, String thoiGianKT,
                  String phong, String tietHoc, String thu, String siSo, String lop) {
        this.maMonHoc = maMonHoc;
        this.nhom = nhom;
        this.tenMonHoc = tenMonHoc;
        this.thoiGian = thoiGian;
        this.thoiGianBD = thoiGianBD;
        this.thoiGianKT = thoiGianKT;
        this.phong = phong;
        this.tietHoc = tietHoc;
        this.thu = thu;
        this.siSo = siSo;
        this.lop = lop;
    }
    public MonHoc(){
    }

    // Getter và Setter cho maMonHoc
    public String getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(String maMonHoc) {
        this.maMonHoc = maMonHoc;
    }

    // Getter và Setter cho nhom
    public String getNhom() {
        return nhom;
    }

    public void setNhom(String nhom) {
        this.nhom = nhom;
    }

    // Getter và Setter cho tenMonHoc
    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    // Getter và Setter cho thoiGian
    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    // Getter và Setter cho thoiGianBD
    public String getThoiGianBD() {
        return thoiGianBD;
    }

    public void setThoiGianBD(String thoiGianBD) {
        this.thoiGianBD = thoiGianBD;
    }

    // Getter và Setter cho thoiGianKT
    public String getThoiGianKT() {
        return thoiGianKT;
    }

    public void setThoiGianKT(String thoiGianKT) {
        this.thoiGianKT = thoiGianKT;
    }

    // Getter và Setter cho phong
    public String getPhong() {
        return phong;
    }

    public void setPhong(String phong) {
        this.phong = phong;
    }

    // Getter và Setter cho tietHoc
    public String getTietHoc() {
        return tietHoc;
    }

    public void setTietHoc(String tietHoc) {
        this.tietHoc = tietHoc;
    }

    // Getter và Setter cho thu
    public String getThu() {
        return thu;
    }

    public void setThu(String thu) {
        this.thu = thu;
    }

    // Getter và Setter cho siSo
    public String getSiSo() {
        return siSo;
    }

    public void setSiSo(String siSo) {
        this.siSo = siSo;
    }

    // Getter và Setter cho lop
    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }
}
