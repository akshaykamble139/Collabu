package com.akshay.Collabu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Star;

@Repository
public interface StarRepository extends JpaRepository<Star, Long> {
    List<Star> findByUserId(Long userId);
    List<Star> findByRepositoryId(Long repositoryId);
    Optional<Star> findByUserIdAndRepositoryId(Long userId, Long repositoryId);
}
