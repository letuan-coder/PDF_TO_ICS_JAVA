package com.example.demo_tttn.dtos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    @Column(name = "file_url",nullable = false,length =300)
    private String FileUrl;

    @Column(name = "filepath",nullable = false,length =300)
    private String FilePath;

}
