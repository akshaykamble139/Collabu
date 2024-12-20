package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Repository_;

@Repository
public interface RepositoryRepository extends JpaRepository<Repository_, Long> {
    List<Repository_> findByOwner_Id(Long userId);
    List<Repository_> findByVisibility(String visibility); // Public or Private repositories
    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
