package com.example.demo_tttn.services;

import com.example.demo_tttn.models.MonHoc;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class MonHocService {
    @Autowired
    private FileService xuLyFile;
    public static int TongSoMonHoc = 0;
    public MonHoc processMonHoc(String cellValue) {
    List<String> maMonList = xuLyFile.readCodesFromFile();

    if (cellValue == null || cellValue.trim().isEmpty()) {
        System.out.println("Cellvalue is null");
        return null;
    }
    String[] rawParts = cellValue.split("\\s+");
    List<String> partsList = new ArrayList<>(Arrays.asList(rawParts));

    if (partsList.size() < 7) { // Kiểm tra tối thiểu số phần cần thiếtpost
        return null;
    }
        if (partsList.get(partsList.size() - 2).length() == 1) {
            String phongGop = partsList.get(partsList.size() - 3) + partsList.get(partsList.size() - 2);
            partsList.set(partsList.size() - 3, phongGop);
            partsList.remove(partsList.size() - 2);
        }
    String[] parts = partsList.toArray(new String[0]);

    try {

        // Lấy mã môn học
        String maMonHoc = parts[0];

        // Lấy nhóm
        String nhom = parts[1];

        // Lấy thời gian
        String thoiGian = parts[parts.length - 1];
        String[] thoiGianParts = thoiGian.split("-");
        if (thoiGianParts.length != 2) {
            return null;
        }
        String ngayBatDau = thoiGianParts[0];
        String ngayKetThuc = thoiGianParts[1];

        // Định dạng ngày
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        LocalDate ngayBatDauDate = LocalDate.parse(ngayBatDau, inputFormatter);
        String formattedNgayBatDau = ngayBatDauDate.atStartOfDay().format(outputFormatter);
        String formattedNgayKetThuc = LocalDate.parse(ngayKetThuc, inputFormatter).atStartOfDay().format(outputFormatter);
        // Lấy phòng
        String phong = parts[parts.length - 2];
        if (phong.contains("*")) {
            return null;
        }
        // Lấy tiết học
        String tietHoc = parts[parts.length - 3].replace("-", "");

        // Lấy thứ
        String thu = parts[parts.length - 4];

        // Lấy sĩ số
        String siSo = parts[parts.length - 5];

        // Lấy lớp
        String lop = parts[parts.length - 6];

        // Lấy tên môn học (từ sau nhom đến trước lop)
        StringBuilder tenMonHoc = new StringBuilder();
        for (int i = 2; i < parts.length - 6; i++) {
            if (!parts[i].isEmpty()) {
                tenMonHoc.append(parts[i]).append(" ");
            }
        }
        String tenMonHocStr = tenMonHoc.toString().trim();
        if (tenMonHocStr.isEmpty()) {
            System.out.println("Tên môn học không hợp lệ cho: " + maMonHoc);
            return null;
        }

        return new MonHoc(maMonHoc, nhom, tenMonHocStr, thoiGian, formattedNgayBatDau, formattedNgayKetThuc, phong, tietHoc, thu, siSo, lop);

    } catch (Exception e) {
        System.out.println("Lỗi khi xử lý môn học: " + cellValue + ", lỗi: " + e.getMessage());
        return null;
    }
}
    public List<MonHoc> processMonHocData(Sheet sheet, int startRow) {
        List<MonHoc> lichDay = new ArrayList<>();
        List<String> maMonList = xuLyFile.readCodesFromFile();

        for (int rowIndex = startRow + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            String cellValue = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                    .getStringCellValue();
            if (cellValue == null) {
                continue;
            }

            if (cellValue.startsWith("Thời gian học:")) {
                break; // Dừng khi gặp "Thời gian học:"
            } else if (compareMaMon(cellValue, maMonList)) {
                MonHoc monHoc = processMonHoc(cellValue);
                if (monHoc != null) {
                    lichDay.add(monHoc);
                }
            } else {
                xuLyFile.appendToFile(cellValue); // Ghi vào file
            }
        }
            return lichDay;
    }

    public boolean compareMaMon(String cell, List<String> maMonList) {
        return maMonList.stream().anyMatch(ma -> cell.startsWith(ma));
    }
    public int countMaMon(String inputExcel) throws IOException {
        // Kiểm tra file Excel tồn tại
        File excelFile = new File(inputExcel);
        if (!excelFile.exists()) {
            throw new IOException("File Excel không tồn tại: " + inputExcel);
        }

        int count = 0;
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            // Lấy sheet đầu tiên
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IOException("Sheet đầu tiên không tồn tại trong file Excel");
            }

            // Lấy danh sách mã môn học từ FileService
            List<String> maMonList = xuLyFile.readCodesFromFile();

            // Duyệt từng dòng
            for (Row row : sheet) {
                // Bỏ qua dòng đầu tiên nếu là header
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Duyệt từng ô trong dòng
                for (Cell cell : row) {
                    // Lấy giá trị ô trực tiếp
                    String cellValue = null;
                    if (cell != null) {
                        if (cell.getCellType() == CellType.STRING) {
                            cellValue = cell.getStringCellValue().trim();
                        } else if (cell.getCellType() == CellType.NUMERIC) {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                cellValue = cell.getDateCellValue().toString();
                            } else {
                                cellValue = String.valueOf((int) cell.getNumericCellValue());
                            }
                        } else if (cell.getCellType() == CellType.BLANK) {
                            cellValue = null;
                        } else {
                            cellValue = cell.toString().trim();
                        }
                    }

                    // Kiểm tra nếu ô không rỗng và chứa mã môn học
                    if (cellValue != null && !cellValue.isEmpty() && compareMaMon(cellValue, maMonList)) {
                        count++;
                        break; // Tìm thấy mã môn học trong dòng, chuyển sang dòng tiếp theo
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }

        return count;
    }
}
