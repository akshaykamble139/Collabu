package com.akshay.Collabu.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "file_contents")
@Data
public class FileContent {
    @Id
    @Column(length = 64) // SHA-1 hash length
    private String hash;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = true) // Explicitly set LONGBLOB
    private byte[] content;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileContentLocation location; // 'db' or 's3'

    @Column(name = "storage_url")
    private String storageUrl; // Optional: S3 URL for location='s3'
}


