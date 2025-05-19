package com.example.demo_tttn.services;

import net.sourceforge.vietpad.converter.Tcvn3Converter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PdfExcelService {
    @Autowired
    private MonHocService monHocService;
    @Autowired
    private FileService xuLyFile;
    private final Tcvn3Converter converter = new Tcvn3Converter();
    private static final String REGEX = "\\d{2}\\s{2}\\w\\s{1}|\\d\\s{2}\\w\\s{1}";
    private static final String REGEX_TWO_DIGITS ="\\b\\d{2}\\b\\s+";
    public void convertPdfToExcel(String pdfFilePath, String excelFilePath) throws IOException {
        try {
            List<String> maMonList = xuLyFile.readCodesFromFile();
            String linenew =readAndFormatPdf(pdfFilePath,maMonList);
            // Đọc file PDF bằng Apache PDFBox
            try (PDDocument document = Loader.loadPDF(new File(pdfFilePath))) {
                PDFTextStripper textStripper = new PDFTextStripper();
                StringBuilder pdfText = new StringBuilder();
                // Đọc từng trang
                int totalPages = document.getNumberOfPages();
                for (int page = 1; page <= totalPages; page++) {
                    textStripper.setStartPage(page);
                    textStripper.setEndPage(page);
                    String pageText = textStripper.getText(document);
                    pageText=readAndFormatPdf(pageText,maMonList);
                    pdfText.append(pageText).append("\n");
                }

                // Tạo workbook Excel bằng Apache POI
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Sheet1");
                    int rowNum = 1;

                    // Tách văn bản thành các dòng
                    String[] lines = pdfText.toString().split("\n");


                    String extractedString = ""; // Lưu chuỗi từ dòng có độ dài 24
                    boolean isLine12 = false; // Đánh dấu dòng có độ dài 24

                    for (String line : lines) {
                        String currentLine = line.trim();


                        // Kiểm tra độ dài của dòng
                        if (currentLine.replace(" ", "").replace("-", "").length() == 24) {
                            extractedString = currentLine.replace(" ", "").replace("-", "");
                            isLine12 = true;

                        } else {
                            // Kiểm tra regex cho 2 dấu cách liên tiếp
                            Pattern pattern = Pattern.compile(REGEX);
                            Matcher matcher = pattern.matcher(currentLine);
                            if (matcher.find()) {
                                currentLine = currentLine.substring(0, matcher.end()) +
                                        extractedString + " " +
                                        currentLine.substring(matcher.end());

                            }
                        }

                        // Nếu không phải dòng có độ dài 24, ghi vào sheet
                        if (!isLine12) {
                            Row row = sheet.createRow(rowNum - 1);
                            String[] data = currentLine.split("\\?");
                            for (int i = 0; i < data.length; i++) {
                                String cellValue = data[i].trim();
                                // Xử lý dấu cách thứ hai và chèn '_'
                                Pattern spacePattern = Pattern.compile(REGEX_TWO_DIGITS);
                                Matcher spaceMatcher = spacePattern.matcher(cellValue);
                                if (spaceMatcher.find()) {
                                    int secondSpaceIndex = spaceMatcher.end();
                                    if (secondSpaceIndex < cellValue.length() &&
                                            Character.isUpperCase(cellValue.charAt(secondSpaceIndex))) {
                                        cellValue = cellValue.substring(0, secondSpaceIndex) +
                                                "_" +
                                                cellValue.substring(secondSpaceIndex);
                                    }
                                }
                                Cell cell = row.createCell(i);
                                cell.setCellValue(cellValue);
                            }
                            rowNum++;

                        } else {
                            isLine12 = false;
                        }
                    }

                    // Lưu file Excel
                    try (FileOutputStream fos = new FileOutputStream(excelFilePath)) {
                        workbook.write(fos);

                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Lỗi khi convert PDF sang Excel: " + e.getMessage(), e);
        }
    }


    public String removeFooter(String text) {
        if (text == null) return "";

        // Nếu thấy "Thời gian học:" thì bỏ từ đó trở đi


        // Nếu còn sót "TP.HCM, Ngày" thì cũng bỏ luôn từ đó
        int index = text.indexOf("Tiết 01 (07h00 - 07h50); Tiết 02 (07h50 - 08h40); Tiết 03 (08h40 - 09h30); Tiết 04 (09h35 - 10h25); Tiết 05 (10h25 - 11h15); Tiết 06 (11h15 - 12h05); Tiết 07 (12h35 - 13h25); Tiết 08 (13h25 - 14h15); Tiết 09 (14h15 - 15h05); Tiết 10 (15h10 - 16h00); Tiết 11 (16h00 - 16h50); Tiết 12 (16h50 - 17h40)");
        if (index != -1) {
            text = text.substring(0, index);
        }
        return text.trim();
    }
    //format laại toan
    public String readAndFormatPdf(String input, List<String> maMonList) {
                String newline="";
                // Bước 1: Clean bỏ xuống dòng
                String cleanedText = cleanNewLines(input);
                // Bước 2: Thêm xuống dòng nếu gặp mã môn
                newline= insertNewLinesByMaMon(cleanedText, maMonList);
                newline = converter.convert(newline, false);
                newline =splitFooterWithNewLine(newline);
                newline =insertLineBreaks(newline);
                newline=removeFooter(newline);
                return newline;
    }
    public String insertLineBreaks(String text) {
        String[] keywords = {
                "Cán bộ giảng dạy:",
                "Điện thoại:",
                "Khoa/ Ban:",
                "Ngày bắt đầu học kỳ:",
                "Mã MH Nhóm Tổ TH Tên môn học Lớp Sĩ số Thứ Tiết học Phòng Thời gian dạy",
                "Thời Khóa Biểu Giảng Dạy",
                "Thời khóa biểu Cán bộ giảng dạy",
                "Các Môn Chưa Xếp/Không Xếp TKB",
                "Tiết 01"
        };

        for (String keyword : keywords) {
            text = text.replace(keyword, "\n"+keyword);
        }

        return text;
    }
    public String splitFooterWithNewLine(String text) {
        if (text == null) return "";

        int index = text.indexOf("Thời gian học:");
        if (index != -1) {
            // Tách ra: phần trước + xuống dòng + phần sau
            String before = text.substring(0, index).trim();
            String after = text.substring(index).trim();
            return before + "\n" + after;
        }

        return text.trim();
    }

    public String cleanNewLines(String text) {
        if (text == null) return "";
        return text.replaceAll("[\r\n]+", " ").replaceAll("\\s+", " ").trim();
    }
    public String insertNewLinesByMaMon(String text, List<String> maMonList) {
        if (text == null || maMonList == null || maMonList.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            boolean matched = false;
            for (String ma : maMonList) {
                if (i + ma.length() <= text.length() && text.substring(i, i + ma.length()).equals(ma)) {
                    // Nếu match mã môn, thêm xuống dòng trước mã môn
                    if (result.length() > 0) result.append("\n");
                    result.append(ma);
                    i += ma.length();
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                result.append(text.charAt(i));
                i++;
            }
        }
        return result.toString().replaceAll("\\n\\s+", "\n").trim();
    }

}



