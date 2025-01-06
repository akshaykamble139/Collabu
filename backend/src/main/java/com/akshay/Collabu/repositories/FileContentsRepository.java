package com.akshay.Collabu.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.FileContent;

@Repository
public interface FileContentsRepository extends JpaRepository<FileContent, String>{
	Optional<FileContent> findByHash(String hash);
}