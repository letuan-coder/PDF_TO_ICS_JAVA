package com.example.demo_tttn.controller;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@Controller
@RequestMapping("/form")
public class HomeController {


    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("file")
            MultipartFile file,
            Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn file PDF!");
            return "upload";
        }

        try {
            Path path = Paths.get("uploads/" + file.getOriginalFilename());
            Files.createDirectories(path.getParent()); // Tạo thư mục nếu chưa có
            Files.write(path, file.getBytes());

            model.addAttribute("message", "Đã upload thành công: " + file.getOriginalFilename());
        } catch (IOException e) {
            model.addAttribute("error", "Upload thất bại: " + e.getMessage());
        }

        return "upload";
    }

}
