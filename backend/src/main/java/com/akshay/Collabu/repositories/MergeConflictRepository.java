package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.MergeConflict;

@Repository
public interface MergeConflictRepository extends JpaRepository<MergeConflict, Long> {
    List<MergeConflict> findByPullRequestId(Long pullRequestId);

    List<MergeConflict> findByPullRequestIdAndResolved(Long pullRequestId, boolean resolved);
}

