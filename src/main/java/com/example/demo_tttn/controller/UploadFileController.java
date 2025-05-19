package com.example.demo_tttn.controller;

import com.example.demo_tttn.config.MultipartFileWrapper;
import com.example.demo_tttn.dtos.IcsFile;
import com.example.demo_tttn.dtos.User;
import com.example.demo_tttn.config.UploadProperties;
import com.example.demo_tttn.models.LichDayGiangVien;
import com.example.demo_tttn.services.IcsFileService;
import com.example.demo_tttn.services.IcsService;
import com.example.demo_tttn.services.LichDayGiangVienService;
import com.example.demo_tttn.services.PdfExcelService;
import com.example.demo_tttn.services.UserService;
import net.fortuna.ical4j.model.component.VEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/api/schedule")
public class UploadFileController {
    @Autowired
    private IcsService icsService;
    @Autowired
    private PdfExcelService pdfExcelService;
    @Autowired
    private UploadProperties uploadProperties;
    @Autowired
    private LichDayGiangVienService lichDayGiangVienService;
    @Autowired
    private IcsFileService icsFileService;
    @Autowired
    private UserService userService;

    private User getUser(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return userService.getOrCreateUser(authentication);
    }

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String generateIcsFiles(
            @RequestParam("files") List<MultipartFile> pdfFiles,
            Model model,
            OAuth2AuthenticationToken authentication) throws IOException, ParseException {
        User user = getUser(authentication);
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn ít nhất một file PDF");
            model.addAttribute("uploadedIcsFiles", icsFileService.getAllFiles(user));
            return "upload";
        }

        List<String> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile pdfFile : pdfFiles) {
            if (!pdfFile.getContentType().equals("application/pdf")) {
                errors.add("Chỉ chấp nhận file PDF: " + pdfFile.getOriginalFilename());
                continue;
            }

            String tempDir = System.getProperty("java.io.tmpdir");
            String uniqueId = UUID.randomUUID().toString();
            Path excelDir = Paths.get(tempDir, uniqueId, "excel");
            Path pdfDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getFolder());
            Path icsDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getIcsFolder());
            Files.createDirectories(excelDir);
            Files.createDirectories(pdfDir);
            Files.createDirectories(icsDir);

            try {
                String pdfFileName = pdfFile.getOriginalFilename();
                Path pdfPath = pdfDir.resolve(pdfFileName);

                // Kiểm tra trùng lặp file
                if (icsFileService.getAllFiles(user).stream().anyMatch(f -> f.getFilePath().equals(pdfFileName))) {
                    errors.add("File " + pdfFileName + " đã tồn tại");
                    continue;
                }

                Files.write(pdfPath, pdfFile.getBytes());
                icsFileService.saveFile(pdfFileName, "PDF", pdfPath.toString(), user);

                Path tempPdfPath = excelDir.resolve("input.pdf");
                Files.write(tempPdfPath, pdfFile.getBytes());
                String excelFilePath = excelDir.resolve("output.xlsx").toString();
                pdfExcelService.convertPdfToExcel(tempPdfPath.toString(), excelFilePath);

                File excelFile = new File(excelFilePath);
                List<LichDayGiangVien> lichDayGiangViens = lichDayGiangVienService.processExcelData(
                        new MultipartFileWrapper(excelFile));

                List<String> generatedIcsFiles = icsService.generateIcsFiles(lichDayGiangViens, icsDir.toString());
                for (String icsFileName : generatedIcsFiles) {
                    Path icsPath = icsDir.resolve(icsFileName);
                    if (icsFileService.getAllFiles(user).stream().noneMatch(f -> f.getFilePath().equals(icsFileName))) {
                        icsFileService.saveFile(icsFileName, "ICS", icsPath.toString(), user);
                    }
                }

                messages.add("Tạo file ICS thành công cho " + pdfFileName);
            } catch (Exception e) {
                errors.add("Lỗi khi xử lý file " + pdfFile.getOriginalFilename() + ": " + e.getMessage());
            } finally {
                try {
                    deleteDirectory(excelDir.toFile());
                } catch (IOException e) {
                    System.err.println("Lỗi khi dọn dẹp file tạm: " + e.getMessage());
                }
            }
        }

        if (!messages.isEmpty()) {
            model.addAttribute("message", String.join("; ", messages));
        }
        if (!errors.isEmpty()) {
            model.addAttribute("error", String.join("; ", errors));
        }
        model.addAttribute("uploadedIcsFiles", icsFileService.getAllFiles(user));
        return "redirect:/api/schedule/upload";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model, OAuth2AuthenticationToken authentication) {
        User user = getUser(authentication);
        System.out.println("Truy cập endpoint /api/schedule/upload");
        try {
            List<IcsFile> files = icsFileService.getAllFiles(user);
            model.addAttribute("uploadedIcsFiles", files);
            System.out.println("Số lượng file: " + files.size());
            return "upload";
        } catch (Exception e) {
            System.out.println("Lỗi khi hiển thị danh sách file: " + e.getMessage());
            model.addAttribute("error", "Không thể tải danh sách file: " + e.getMessage());
            model.addAttribute("uploadedIcsFiles", List.of());
            return "upload";
        }
    }

    @GetMapping("/upload/download/ics/{fileName}")
    public ResponseEntity<Resource> downloadIcsFile(@PathVariable String fileName, OAuth2AuthenticationToken authentication) throws IOException {
        User user = getUser(authentication);
        System.out.println("Yêu cầu tải file ICS: " + fileName);
        Path icsDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getIcsFolder());
        Path icsPath = icsDir.resolve(fileName);

        if (!Files.exists(icsPath) || !fileName.endsWith(".ics")) {
            System.out.println("File ICS không tồn tại: " + fileName);
            return ResponseEntity.notFound().build();
        }

        // Kiểm tra quyền sở hữu
        if (icsFileService.getAllFiles(user).stream().noneMatch(f -> f.getFilePath().equals(fileName))) {
            System.out.println("User không sở hữu file ICS: " + fileName);
            return ResponseEntity.status(403).build();
        }

        FileSystemResource resource = new FileSystemResource(icsPath.toFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(icsPath.toFile().length())
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(resource);
    }

    @GetMapping("/upload/download/pdf/{fileName}")
    public ResponseEntity<Resource> downloadPdfFile(@PathVariable String fileName, OAuth2AuthenticationToken authentication) throws IOException {
        User user = getUser(authentication);
        System.out.println("Yêu cầu tải file PDF: " + fileName);
        Path pdfDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getFolder());
        Path pdfPath = pdfDir.resolve(fileName);

        if (!Files.exists(pdfPath) || !fileName.endsWith(".pdf")) {
            System.out.println("File PDF không tồn tại: " + fileName);
            return ResponseEntity.notFound().build();
        }
        // Kiểm tra quyền sở hữu
        if (icsFileService.getAllFiles(user).stream().noneMatch(f -> f.getFilePath().equals(fileName))) {
            System.out.println("User không sở hữu file PDF: " + fileName);
            return ResponseEntity.status(403).build();
        }

        FileSystemResource resource = new FileSystemResource(pdfPath.toFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfPath.toFile().length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/upload/download/ics/all")
    public ResponseEntity<Resource> downloadAllIcsFiles(OAuth2AuthenticationToken authentication) throws IOException {
        User user = getUser(authentication);
        System.out.println("Yêu cầu tải tất cả file ICS dưới dạng ZIP");

        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueId = UUID.randomUUID().toString();
        Path zipPath = Paths.get(tempDir, uniqueId, "ics_files.zip");
        Files.createDirectories(zipPath.getParent());

        List<IcsFile> icsFiles = icsFileService.getIcsFiles(user);
        if (icsFiles.isEmpty()) {
            System.out.println("Không có file ICS nào để tạo ZIP");
            return ResponseEntity.status(404).body(null);
        }

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (IcsFile icsFile : icsFiles) {
                Path icsPath = Paths.get(icsFile.getFullPath());
                if (Files.exists(icsPath)) {
                    System.out.println("Thêm file vào ZIP: " + icsFile.getFilePath());
                    ZipEntry zipEntry = new ZipEntry(icsFile.getFilePath());
                    zos.putNextEntry(zipEntry);
                    Files.copy(icsPath, zos);
                    zos.closeEntry();
                }
            }
        } catch (IOException e) {
            System.out.println("Lỗi khi tạo file ZIP: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }

        FileSystemResource resource = new FileSystemResource(zipPath.toFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ics_files.zip");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipPath.toFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/upload/delete/ics/{fileName}")
    public String deleteIcsFile(@PathVariable String fileName, Model model, OAuth2AuthenticationToken authentication) throws IOException {
        User user = getUser(authentication);
        System.out.println("Yêu cầu xóa file ICS: " + fileName);
        Path icsDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getIcsFolder());
        Path icsPath = icsDir.resolve(fileName);

        if (!Files.exists(icsPath) || !fileName.endsWith(".ics")) {
            System.out.println("File ICS không tồn tại: " + fileName);
            model.addAttribute("error", "File " + fileName + " không tồn tại hoặc không phải file ICS");
        } else if (icsFileService.getAllFiles(user).stream().noneMatch(f -> f.getFilePath().equals(fileName))) {
            System.out.println("User không sở hữu file ICS: " + fileName);
            model.addAttribute("error", "Bạn không có quyền xóa file " + fileName);
        } else {
            try {
                Files.delete(icsPath);
                icsFileService.deleteFile(fileName, user);

                System.out.println("Đã xóa file ICS: " + fileName);
                model.addAttribute("message", "Xóa file " + fileName + " thành công");
            } catch (IOException e) {
                System.out.println("Lỗi khi xóa file ICS: " + e.getMessage());
                model.addAttribute("error", "Lỗi khi xóa file " + fileName + ": " + e.getMessage());
            }
        }

        model.addAttribute("uploadedIcsFiles", icsFileService.getAllFiles(user));
        return "upload";
    }

    @PostMapping("/upload/delete/pdf/{fileName}")
    public String deletePdfFile(@PathVariable String fileName, Model model, OAuth2AuthenticationToken authentication) throws IOException {
        User user = getUser(authentication);
        System.out.println("Yêu cầu xóa file PDF: " + fileName);
        String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
        Path pdfDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getFolder());
        Path pdfPath = pdfDir.resolve(decodedFileName);

        if (!Files.exists(pdfPath)) {
            System.out.println("File PDF không tồn tại: " + decodedFileName);
            model.addAttribute("error", "File " + decodedFileName + " không tồn tại");
        } else if (!decodedFileName.toLowerCase().endsWith(".pdf")) {
            System.out.println("File không phải PDF: " + decodedFileName);
            model.addAttribute("error", "File " + decodedFileName + " không phải file PDF");
        } else if (icsFileService.getAllFiles(user).stream().noneMatch(f -> f.getFilePath().equals(decodedFileName))) {
            System.out.println("User không sở hữu file PDF: " + decodedFileName);
            model.addAttribute("error", "Bạn không có quyền xóa file " + decodedFileName);
        } else {
            try {
                Files.delete(pdfPath);
                icsFileService.deleteFile(decodedFileName, user);
                System.out.println("Đã xóa file PDF: " + decodedFileName);
                model.addAttribute("message", "Xóa file " + decodedFileName + " thành công");
            } catch (IOException e) {
                System.out.println("Lỗi khi xóa file PDF: " + e.getMessage());
                model.addAttribute("error", "Lỗi khi xóa file " + decodedFileName + ": " + e.getMessage());
            }
        }

        model.addAttribute("uploadedIcsFiles", icsFileService.getAllFiles(user));
        return "upload";
    }

    @PostMapping("/upload/delete/selected")
    public String deleteSelectedFiles(@RequestParam(value = "selectedFiles", required = false) List<String> filePaths, Model model, OAuth2AuthenticationToken authentication) {
        User user = getUser(authentication);
        System.out.println("Yêu cầu xóa nhiều file: " + filePaths);

        if (filePaths == null || filePaths.isEmpty()) {
            model.addAttribute("error", "Không có file nào được chọn để xóa");
            model.addAttribute("uploadedIcsFiles", icsFileService.getAllFiles(user));
            return "upload";
        }

        int deletedCount = 0;
        List<String> errors = new ArrayList<>();
        Path icsDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getIcsFolder());
        Path pdfDir = Paths.get(System.getProperty("user.dir"), uploadProperties.getFolder());

        for (String filePath : filePaths) {
            try {
                String decodedFileName = java.net.URLDecoder.decode(filePath, "UTF-8");
                Path fullPath;

                if (decodedFileName.toLowerCase().endsWith(".ics")) {
                    fullPath = icsDir.resolve(decodedFileName);
                } else if (decodedFileName.toLowerCase().endsWith(".pdf")) {
                    fullPath = pdfDir.resolve(decodedFileName);
                } else {
                    errors.add("File " + decodedFileName + " không phải file ICS hoặc PDF");
                    continue;
                }

                if (!Files.exists(fullPath)) {
                    errors.add("File " + decodedFileName + " không tồn tại");
                    continue;
                }

                if (icsFileService.getAllFiles(user).stream().noneMatch(f -> f.getFilePath().equals(decodedFileName))) {
                    errors.add("Bạn không có quyền xóa file " + decodedFileName);
                    continue;
                }

                Files.delete(fullPath);
                icsFileService.deleteFile(decodedFileName, user);

                deletedCount++;
                System.out.println("Đã xóa file: " + decodedFileName);
            } catch (IOException e) {
                errors.add("Lỗi khi xóa file " + filePath + ": " + e.getMessage());
                System.out.println("Lỗi khi xóa file: " + filePath + ", lỗi: " + e.getMessage());
            }
        }

        if (deletedCount > 0) {
            model.addAttribute("message", "Đã xóa thành công " + deletedCount + " file");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("error", String.join("; ", errors));
        }

        model.addAttribute("uploadedIcsFiles", icsFileService.getAllFiles(user));
        return "upload";
    }

    private void deleteDirectory(File directory) throws IOException {
        if (directory == null || !directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    Files.deleteIfExists(file.toPath());
                    System.out.println("Đã xóa file: " + file.getAbsolutePath());
                }
            }
        }

        Files.deleteIfExists(directory.toPath());
        System.out.println("Đã xóa thư mục: " + directory.getAbsolutePath());
    }

}