package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByRepositoryId(Long repositoryId);
    List<File> findByType(String type); // Fetch files or directories
    boolean existsByNameAndPathAndRepositoryId(String name, String path, Long repositoryId);
}

