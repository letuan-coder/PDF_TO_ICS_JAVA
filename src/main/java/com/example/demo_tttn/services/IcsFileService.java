package com.example.demo_tttn.services;

import com.example.demo_tttn.dtos.IcsFile;
import com.example.demo_tttn.dtos.User;
import com.example.demo_tttn.repositories.IcsFileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class IcsFileService {
    @Autowired
    private IcsFileRepository icsFileRepository;

    public void saveFile(String filePath, String fileType, String fullPath, User user) {
        IcsFile icsFile = new IcsFile(filePath, fileType, new Date(), fullPath, user);
        icsFileRepository.save(icsFile);
    }

    public List<IcsFile> getAllFiles(User user) {
        return icsFileRepository.findAllByUserOrderByCreatedAtDesc(user);
    }
    @Transactional
    public void deleteFile(String filePath, User user) {
        icsFileRepository.deleteByFilePathAndUser(filePath, user);
    }

    public List<IcsFile> getIcsFiles(User user) {
        return icsFileRepository.findByFileTypeAndUser("ICS", user);
    }
    public void clearFilePath(String filePath, User user) {
        icsFileRepository.clearFilePathByFileNameAndUserId(filePath, user.getId());
    }

    @Transactional
    public void clearAllFilePaths() {
        icsFileRepository.clearAllFilePaths();
    }
}
