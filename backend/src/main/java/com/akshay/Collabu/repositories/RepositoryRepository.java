package com.akshay.Collabu.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Repository_;

@Repository
public interface RepositoryRepository extends JpaRepository<Repository_, Long> {
    List<Repository_> findByOwnerId(Long userId);
    List<Repository_> findByOwnerIdAndVisibility(Long userId, String visibility);
    List<Repository_> findByVisibility(String visibility); // Public or Private repositories
    boolean existsByNameAndOwnerId(String name, Long ownerId);
	Optional<Repository_> findByNameAndOwnerId(String repoName, Long ownerId);
	
	@Modifying
	@Query("UPDATE Repository_ r SET r.starsCount = :starsCount WHERE r.id = :repositoryId")
	void updateStarsCount(@Param("repositoryId") Long repositoryId, @Param("starsCount") Long starsCount);
	
	@Query("SELECT r.forkedFrom.id, COUNT(r) FROM Repository_ r WHERE r.forkedFrom IS NOT NULL GROUP BY r.forkedFrom")
    List<Object[]> countForksForAllRepositories();
    
    Long countByForkedFromId(Long repositoryId);
	Optional<Repository_> findByOwnerUsernameAndName(String username, String repoName);
}
