package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Commit;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {
    List<Commit> findByRepositoryId(Long repositoryId);
    List<Commit> findByBranchId(Long branchId);
}
