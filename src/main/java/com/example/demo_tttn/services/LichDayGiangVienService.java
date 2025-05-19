package com.example.demo_tttn.services;

import com.example.demo_tttn.models.GiangVien;
import com.example.demo_tttn.models.LichDayGiangVien;
import com.example.demo_tttn.models.MonHoc;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class LichDayGiangVienService {
    @Autowired
    private MonHocService monHocService;
    @Autowired
    private GiangVienService giangVienService;
    public List<LichDayGiangVien> processExcelData(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {

            throw new IOException("Tệp Excel không hợp lệ hoặc trống.");
        }

        List<LichDayGiangVien> lichDayGiangViens = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                String cellValue = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                        .getStringCellValue();
                if (cellValue != null && (cellValue.startsWith("Cán bộ giảng dạy") || cellValue.startsWith("CBGD"))) {
                    GiangVien giangVien = giangVienService.processGiangVien(cellValue);
                    List<MonHoc> lichDay = monHocService.processMonHocData(sheet,rowIndex);
                    if (lichDay == null) {

                        return lichDayGiangViens;
                    }
                    lichDayGiangViens.add(new LichDayGiangVien(giangVien, lichDay));
                }
            }
        } catch (IOException e) {

            throw new IOException("Không thể xử lý tệp Excel: " + e.getMessage());
        }

        return lichDayGiangViens;
    }
}
