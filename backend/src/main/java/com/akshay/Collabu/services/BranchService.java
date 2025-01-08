package com.akshay.Collabu.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.BranchDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.repositories.BranchRepository;

@Service
public class BranchService {
	
	@Autowired
	private BranchCacheService branchCacheService;
	
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private CacheService cacheService;

    public List<BranchDTO> getBranchesByRepoId(Long repoId, UserDetails userDetails) {
    	Long ownerId = cacheService.getUserId(userDetails.getUsername());
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User doesn't exist");    		
    	}
    	    	
    	Boolean isPublic = cacheService.getRepositoryVisibility(repoId);
    	
    	if (!isPublic) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	
		return branchCacheService.findBranchesByRepositoryId(repoId);          
    }

    public BranchDTO mapEntityToDTO(Branch branch) {
    	
    	return branchCacheService.mapEntityToDTO(branch);
	}
    
    public BranchDTO createBranch(BranchDTO branchDTO, UserDetails userDetails) {
    	if (branchDTO.getParentBranchName() == null || branchDTO.getParentBranchName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent source branch name missing from request");
    	}
    	
        // Check if a branch with the same name already exists for this repository
    	Long repositoryId = cacheService.getRepositoryId(userDetails.getUsername() + "-" + branchDTO.getRepositoryName());
    	if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	
    	return branchCacheService.createBranchForRepositoryId(branchDTO, repositoryId);
    }

    public BranchDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
        		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Branch not found"));
        return mapEntityToDTO(branch);
    }
    
    public void deleteBranchById(Long id) {
    	if (!branchRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found");
        }
    	branchRepository.deleteById(id);
	}
    
    public List<Branch> getChildBranches(Long parentBranchId) {
        return branchRepository.findByParentBranchId(parentBranchId);
    }

	public List<BranchDTO> findByUsernameAndRepositoryName(String username, String repoName, UserDetails userDetails) {
		Long ownerId = cacheService.getUserId(username);
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User doesn't exist");    		
    	}
    	    	
    	Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);
    	if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	Boolean isPublic = cacheService.getRepositoryVisibility(repositoryId);
    	
    	if (!username.equals(userDetails.getUsername()) && !isPublic) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	
		return branchCacheService.findBranchesByRepositoryId(repositoryId);
	}
}