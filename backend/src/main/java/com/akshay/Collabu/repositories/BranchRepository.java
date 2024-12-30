package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByRepositoryId(Long repositoryId);
    Branch findByRepositoryIdAndIsDefaultTrue(Long repositoryId); // Find the default branch
	boolean existsByRepositoryIdAndName(Long repositoryId ,String branchName);
    List<Branch> findByParentBranchId(Long parentBranchId);

}
