package com.example.demo_tttn.models;
import java.util.List;

public class LichDayGiangVien {
    private GiangVien giangVien;
    private List<MonHoc> lichDay;
    public LichDayGiangVien(GiangVien giangVien, List<MonHoc> lichDay) {
        this.giangVien = giangVien;
        this.lichDay = lichDay;
    }

    public LichDayGiangVien() {
    }
    // Getter cho giangVien
    public GiangVien getGiangVien() {
        return giangVien;
    }

    // Setter cho giangVien
    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }

    // Getter cho lichDay
    public List<MonHoc> getLichDay() {
        return lichDay;
    }

    // Setter cho lichDay
    public void setLichDay(List<MonHoc> lichDay) {
        this.lichDay = lichDay;
    }
}
