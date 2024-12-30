package com.akshay.Collabu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Diff;

@Repository
public interface DiffRepository extends JpaRepository<Diff, Long> {
    List<Diff> findByFileId(Long fileId);

    Optional<Diff> findByFileIdAndVersionFromAndVersionTo(Long fileId, int versionFrom, int versionTo);

}
