package com.example.demo_tttn.services;
import com.example.demo_tttn.models.LichDayGiangVien;
import com.example.demo_tttn.models.MonHoc;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class IcsService {
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = IcsService.class.getClassLoader().getResourceAsStream("ical4j.properties")) {
            if (in == null) {
                System.out.println("File ical4j.properties không tìm thấy trong classpath. Sử dụng cấu hình mặc định.");
            } else {
                CONFIG.load(in);
                System.out.println("Đã tải file ical4j.properties thành công.");
            }
        } catch (IOException e) {
            System.out.println("Lỗi khi đọc file ical4j.properties: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Lỗi không mong muốn khi tải ical4j.properties: " + e.getMessage());
            e.printStackTrace();
        }
    }
//public void generateIcsFiles(List<LichDayGiangVien> lichDayGiangViens, String folderPath) throws IOException {
//    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
//
//    for (LichDayGiangVien lich : lichDayGiangViens) {
//        if (lich == null || lich.getGiangVien() == null) {
//            System.out.println("LichDayGiangVien hoặc GiangVien null, bỏ qua.");
//            continue;
//        }
//
//        Calendar calendar = new Calendar();
//        calendar.getProperties().add(new ProdId("-//tttn_2025//PDF_TO_ICS//"));
//        calendar.getProperties().add(Version.VERSION_2_0);
//        calendar.getProperties().add(CalScale.GREGORIAN);
//
//        if (lich.getLichDay() == null || lich.getLichDay().isEmpty()) {
//            System.out.println("Không có môn học nào cho giảng viên: " + lich.getGiangVien().getTenGiangVien() + " (lichDay size = 0)");
//            continue;
//        }
//
//        boolean hasValidEvent = false;
//        for (MonHoc monHoc : lich.getLichDay()) {
//            if (monHoc == null) {
//                System.out.println("Môn học null cho giảng viên: " + lich.getGiangVien().getTenGiangVien());
//                continue;
//            }
//
//            try {
//
//
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));
//                ZonedDateTime ngayBatDauZoned = ZonedDateTime.parse(monHoc.getThoiGianBD(), formatter);
//                ZonedDateTime ngayKetThucZoned = ZonedDateTime.parse(monHoc.getThoiGianKT(), formatter);
//                Date ngayBatDau = Date.from(ngayBatDauZoned.toInstant());
//                Date ngayKetThuc = Date.from(ngayKetThucZoned.toInstant());
//
//                long soNgay = (ngayKetThuc.getTime() - ngayBatDau.getTime()) / (1000 * 60 * 60 * 24);
//                int soLanLapLai = (int) (soNgay + 6) / 7;
//
//                DateTime start = new DateTime(getStartDateTime(monHoc.getThoiGianBD(), monHoc.getTietHoc(), monHoc.getThu()));
//                DateTime end = new DateTime(getEndDateTime(monHoc.getThoiGianKT(), monHoc.getTietHoc(), monHoc.getThu()));
//                VEvent event = new VEvent(start, end, "[" + monHoc.getNhom() + "] " + monHoc.getTenMonHoc());
//
//                event.getProperties().add(new Description(monHoc.getMaMonHoc()));
//                event.getProperties().add(new Location(monHoc.getPhong()));
//                event.getProperties().add(new Status("CONFIRMED"));
//                event.getProperties().add(new Transp("OPAQUE"));
//                event.getProperties().add(new RRule("FREQ=WEEKLY;WKST=MO;COUNT=" + soLanLapLai + ";BYDAY=" + getDayOfWeek(monHoc.getThu())));
//
//                calendar.getComponents().add(event);
//                hasValidEvent = true;
//                System.out.println("Processing MonHoc: " + monHoc.getMaMonHoc() + ", ThoiGianBD: " + monHoc.getThoiGianBD() + ", ThoiGianKT: " + monHoc.getThoiGianKT());
//            } catch (DateTimeParseException e) {
//                System.err.println("Lỗi phân tích ngày giờ cho môn học: " + monHoc.getMaMonHoc());
//                System.err.println("ThoiGianBD: " + monHoc.getThoiGianBD() + ", ThoiGianKT: " + monHoc.getThoiGianKT());
//                System.err.println("Error: " + e.getMessage());
//                e.printStackTrace();
//                continue;
//            } catch (Exception e) {
//                System.err.println("Lỗi xử lý môn học: " + monHoc.getMaMonHoc() + ", error: " + e.getMessage());
//                e.printStackTrace();
//                continue;
//            }
//        }
//
//        if (!hasValidEvent) {
//            System.out.println("Không có sự kiện hợp lệ nào cho giảng viên: " + lich.getGiangVien().getTenGiangVien() + ", không tạo file ICS.");
//            continue;
//        }
//
//        String fileName = lich.getGiangVien().getTenGiangVien().replace(" ", "_") + ".ics";
//        String filePath = Paths.get(folderPath, fileName).toString();
//        try (FileOutputStream fout = new FileOutputStream(filePath)) {
//            CalendarOutputter outputter = new CalendarOutputter();
//            outputter.output(calendar, fout);
//            System.out.println("Đã tạo file ICS: " + filePath);
//        } catch (IOException e) {
//            System.err.println("Lỗi khi lưu file ICS cho giảng viên: " + lich.getGiangVien().getTenGiangVien() + ", path: " + filePath);
//            e.printStackTrace();
//        }
//    }
//}
public List<String> generateIcsFiles(List<LichDayGiangVien> lichDayGiangViens, String folderPath) throws IOException {
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
    List<String> generatedFiles = new ArrayList<>();

    for (LichDayGiangVien lich : lichDayGiangViens) {
        if (lich == null || lich.getGiangVien() == null) {
            System.out.println("LichDayGiangVien hoặc GiangVien null, bỏ qua.");
            continue;
        }

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//tttn_2025//PDF_TO_ICS//"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        if (lich.getLichDay() == null || lich.getLichDay().isEmpty()) {
            System.out.println("Không có môn học nào cho giảng viên: " + lich.getGiangVien().getTenGiangVien() + " (lichDay size = 0)");
            continue;
        }

        boolean hasValidEvent = false;
        for (MonHoc monHoc : lich.getLichDay()) {
            if (monHoc == null) {
                System.out.println("Môn học null cho giảng viên: " + lich.getGiangVien().getTenGiangVien());
                continue;
            }

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneId.of("UTC"));
                ZonedDateTime ngayBatDauZoned = ZonedDateTime.parse(monHoc.getThoiGianBD(), formatter);
                ZonedDateTime ngayKetThucZoned = ZonedDateTime.parse(monHoc.getThoiGianKT(), formatter);
                Date ngayBatDau = Date.from(ngayBatDauZoned.toInstant());
                Date ngayKetThuc = Date.from(ngayKetThucZoned.toInstant());

                long soNgay = (ngayKetThuc.getTime() - ngayBatDau.getTime()) / (1000 * 60 * 60 * 24);
                int soLanLapLai = (int) (soNgay + 6) / 7;

                DateTime start = new DateTime(getStartDateTime(monHoc.getThoiGianBD(), monHoc.getTietHoc(), monHoc.getThu()));
                DateTime end = new DateTime(getEndDateTime(monHoc.getThoiGianBD(), monHoc.getTietHoc(), monHoc.getThu()));
                VEvent event = new VEvent(start, end, "[" + monHoc.getNhom() + "] " + monHoc.getTenMonHoc());

                event.getProperties().add(new Description(monHoc.getMaMonHoc()));
                event.getProperties().add(new Location(monHoc.getPhong()));
                event.getProperties().add(new Status("CONFIRMED"));
                event.getProperties().add(new Transp("OPAQUE"));
                event.getProperties().add(new RRule("FREQ=WEEKLY;WKST=MO;COUNT=" + soLanLapLai + ";BYDAY=" + getDayOfWeek(monHoc.getThu())));

                calendar.getComponents().add(event);
                hasValidEvent = true;
                System.out.println("Processing MonHoc: " + monHoc.getMaMonHoc() + ", ThoiGianBD: " + monHoc.getThoiGianBD() + ", ThoiGianKT: " + monHoc.getThoiGianKT());
            } catch (DateTimeParseException e) {
                System.err.println("Lỗi phân tích ngày giờ cho môn học: " + monHoc.getMaMonHoc());
                System.err.println("ThoiGianBD: " + monHoc.getThoiGianBD() + ", ThoiGianKT: " + monHoc.getThoiGianKT());
                System.err.println("Error: " + e.getMessage());
                continue;
            } catch (Exception e) {
                System.err.println("Lỗi xử lý môn học: " + monHoc.getMaMonHoc() + ", error: " + e.getMessage());
                continue;
            }
        }

        if (!hasValidEvent) {
            System.out.println("Không có sự kiện hợp lệ nào cho giảng viên: " + lich.getGiangVien().getTenGiangVien() + ", không tạo file ICS.");
            continue;
        }

        String fileName = lich.getGiangVien().getTenGiangVien().replace(" ", "_") + ".ics";
        String filePath = Paths.get(folderPath, fileName).toString();
        try (FileOutputStream fout = new FileOutputStream(filePath)) {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, fout);
            generatedFiles.add(fileName);
            System.out.println("Đã tạo file ICS: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file ICS cho giảng viên: " + lich.getGiangVien().getTenGiangVien() + ", path: " + filePath);
        }
    }

    return generatedFiles;
}
    private Date getStartDateTime(String ngayBatDau, String tietHoc, String thu) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        Date date = inputFormat.parse(ngayBatDau);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);

        // Tính ngày tương ứng với thứ
        int targetDay = Integer.parseInt(thu);
        int currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
        int dayDiff = targetDay - currentDay;
        if (dayDiff < 0) {
            dayDiff += 7;
        }
        calendar.add(java.util.Calendar.DAY_OF_MONTH, dayDiff-1);

        // Thiết lập giờ theo tiết học
        switch (tietHoc) {
            case "123":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 7);
                calendar.set(java.util.Calendar.MINUTE, 0);
                break;
            case "456":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 9);
                calendar.set(java.util.Calendar.MINUTE, 35);
                break;
            case "789":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 12);
                calendar.set(java.util.Calendar.MINUTE, 35);
                break;
            case "012":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 15);
                calendar.set(java.util.Calendar.MINUTE, 10);
                break;
            case "89012":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 13);
                calendar.set(java.util.Calendar.MINUTE, 25);
                break;
            case "23456":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 7);
                calendar.set(java.util.Calendar.MINUTE, 50);
                break;
            case "12345":
            case "123456":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 7);
                calendar.set(java.util.Calendar.MINUTE, 0);
                break;
            case "789012":
            case "78901":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 12);
                calendar.set(java.util.Calendar.MINUTE, 35);
                break;
            default:
                break;
        }

        return calendar.getTime();
    }

    private Date getEndDateTime(String ngayKetThuc, String tietHoc, String thu) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        Date date = inputFormat.parse(ngayKetThuc);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);

        // Tính ngày tương ứng với thứ
        int targetDay = Integer.parseInt(thu);
        int currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1; // Sunday = 0
        int dayDiff = targetDay - currentDay;
        if (dayDiff < 0) {
            dayDiff += 7;
        }
        calendar.add(java.util.Calendar.DAY_OF_MONTH, dayDiff-1 ); // Trừ 1 ngày như mã C#

        // Thiết lập giờ theo tiết học
        switch (tietHoc) {
            case "123":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 9);
                calendar.set(java.util.Calendar.MINUTE, 30);
                break;
            case "456":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 12);
                calendar.set(java.util.Calendar.MINUTE, 5);
                break;
            case "789":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 15);
                calendar.set(java.util.Calendar.MINUTE, 5);
                break;
            case "012":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 17);
                calendar.set(java.util.Calendar.MINUTE, 40);
                break;
            case "89012":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 17);
                calendar.set(java.util.Calendar.MINUTE, 40);
                break;
            case "23456":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 12);
                calendar.set(java.util.Calendar.MINUTE, 5);
                break;
            case "12345":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 11);
                calendar.set(java.util.Calendar.MINUTE, 15);
                break;
            case "123456":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 12);
                calendar.set(java.util.Calendar.MINUTE, 5);
                break;
            case "789012":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 17);
                calendar.set(java.util.Calendar.MINUTE, 40);
                break;
            case "78901":
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 16);
                calendar.set(java.util.Calendar.MINUTE, 50);
                break;
            default:
                break;
        }

        return calendar.getTime();
    }

    private String getDayOfWeek(String thu) {
        switch (thu) {
            case "2":
                return "MO";
            case "3":
                return "TU";
            case "4":
                return "WE";
            case "5":
                return "TH";
            case "6":
                return "FR";
            case "7":
                return "SA";
            default:
                return "";
        }
    }

}
