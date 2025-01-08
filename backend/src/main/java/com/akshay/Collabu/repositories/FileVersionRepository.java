package com.akshay.Collabu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.FileVersion;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {
    List<FileVersion> findByFileIdOrderByVersionNumberDesc(Long fileId);

    Optional<FileVersion> findByFileIdAndVersionNumber(Long fileId, int versionNumber);

	List<FileVersion> findByFileId(Long fileId);
}

