package com.akshay.Collabu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Repository_;

@Repository
public interface RepositoryRepository extends JpaRepository<Repository_, Long> {
    List<Repository_> findByOwnerId(Long userId);
    List<Repository_> findByVisibility(String visibility); // Public or Private repositories
    boolean existsByNameAndOwnerId(String name, Long ownerId);
	Optional<Repository_> findByNameAndOwnerId(String repoName, Long ownerId);
}
