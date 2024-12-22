package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.PullRequest;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
    List<PullRequest> findByRepositoryId(Long repositoryId);
    List<PullRequest> findByCreatedById(Long userId);
    List<PullRequest> findByStatus(String status); // Fetch by open, closed, merged status
}
