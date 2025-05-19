package com.example.demo_tttn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    private String folder;
    private String icsFolder;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getIcsFolder() {
        return icsFolder;
    }

    public void setIcsFolder(String icsFolder) {
        this.icsFolder = icsFolder;
    }
}