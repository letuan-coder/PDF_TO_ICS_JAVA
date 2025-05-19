package com.example.demo_tttn.repositories;

import com.example.demo_tttn.dtos.IcsFile;
import com.example.demo_tttn.dtos.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IcsFileRepository extends JpaRepository<IcsFile, Long> {
    List<IcsFile> findAllByUserOrderByCreatedAtDesc(User user);
    List<IcsFile> findByFileTypeAndUser(String fileType, User user);
    @Modifying
    @Query("UPDATE IcsFile f SET f.filePath = NULL WHERE f.filePath = :fileName AND f.user.id = :userId")
    void clearFilePathByFileNameAndUserId(String fileName, Integer userId);

    @Modifying
    @Query("UPDATE IcsFile f SET f.filePath = NULL")
    void clearAllFilePaths();
    void deleteByFilePathAndUser(String filePath, User user);
}
