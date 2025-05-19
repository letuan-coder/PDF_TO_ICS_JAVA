package com.example.demo_tttn.services;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import java.util.stream.Collectors;
@Component
public class FileService {
    private final String appDirectory = System.getProperty("user.dir");
    private final String dataDirectory;
    private String maMonPath;
    private String logPath;
    @Value("${file.data.directory:Config}")
    private String configDirectoryName;
    public FileService() {

        if (this.appDirectory == null) {

            throw new IllegalStateException("Không thể xác định thư mục làm việc hiện tại");
        }

        // Kiểm tra configDirectoryName
        if (this.configDirectoryName == null) {
            this.configDirectoryName = "Config";
        }

        // Khởi tạo dataDirectory
        this.dataDirectory = Paths.get(appDirectory, configDirectoryName).toString();
        this.maMonPath = Paths.get(dataDirectory, "maMon.txt").toString();
        this.logPath = Paths.get(dataDirectory, "log.txt").toString();

        // Tạo thư mục Config nếu chưa tồn tại
        try {
            Files.createDirectories(Paths.get(dataDirectory));
        } catch (IOException e) {
        }

        // Tạo file mẫu nếu chưa tồn tại
        createFileIfNotExists(maMonPath, "CS\nGS\nCSA");
        createFileIfNotExists(logPath, "");
    }
    public List<String> readCodesFromFile() {
        try {
            Path path = Paths.get(maMonPath);
            if (Files.exists(path)) {
                return Files.readAllLines(path)
                        .stream()
                        .filter(line -> !line.trim().isEmpty())
                        .map(String::trim)
                        .collect(Collectors.toList());
            } else {

                return List.of();
            }
        } catch (IOException e) {
            return List.of();
        }
    }
    public void appendToFile(String content) {
        try {
            Files.writeString(Paths.get(logPath), content + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFileIfNotExists(String filePath, String defaultContent) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.writeString(path, defaultContent, StandardOpenOption.CREATE);

            }
        } catch (IOException e) {

        }
    }
}
