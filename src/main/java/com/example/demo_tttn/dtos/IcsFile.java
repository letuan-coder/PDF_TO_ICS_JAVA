package com.example.demo_tttn.dtos;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ics_files")
public class IcsFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ics_path", nullable = false)
    private String filePath; // Đổi tên để thống nhất với code hiện tại

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "full_path")
    private String fullPath;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public IcsFile() {}

    public IcsFile(String filePath, String fileType, Date createdAt, String fullPath, User user) {
        this.filePath = filePath;
        this.fileType = fileType;
        this.createdAt = createdAt;
        this.fullPath = fullPath;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}