package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.BranchDTO;
import com.akshay.Collabu.models.ActivityAction;
import com.akshay.Collabu.models.ActivityLog;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.ActivityLogRepository;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

import jakarta.transaction.Transactional;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @Autowired
    private CacheService cacheService;

    public List<BranchDTO> getBranchesByRepoId(Long repoId, UserDetails userDetails) {
		Repository_ repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
		
		if (!repo.getOwner().getUsername().equals(userDetails.getUsername()) && !repo.getVisibility().equals("public")) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
		}
        return repo.getBranches().stream()
                .map(branch -> mapEntityToDTO(branch))
                .collect(Collectors.toList());
    }

    public BranchDTO mapEntityToDTO(Branch branch) {
		BranchDTO resultDto = new BranchDTO();
		resultDto.setId(branch.getId()); 
        resultDto.setName(branch.getName());
        resultDto.setRepositoryName(branch.getRepository().getName());
        resultDto.setDefaultBranch(branch.getIsDefault());
        resultDto.setParentBranchName(branch.getParentBranch() != null ? branch.getParentBranch().getName() : null);
       
        return resultDto;
	}
    
    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO, UserDetails userDetails) {
    	if (branchDTO.getParentBranchName() == null || branchDTO.getParentBranchName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent source branch name missing from request");
    	}
    	
        // Check if a branch with the same name already exists for this repository
    	Long repositoryId = cacheService.getRepositoryId(userDetails.getUsername() + "-" + branchDTO.getRepositoryName());
        boolean exists = branchRepository.existsByRepositoryIdAndName(repositoryId,branchDTO.getName());
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Branch with this name already exists for this repository.");
        }
        Branch branch = new Branch();
        
        branch.setName(branchDTO.getName());
        
        Repository_ repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        
        Branch parentBranch = repo.getBranches().stream().filter(brnch -> brnch.getName().equals(branchDTO.getParentBranchName())).findFirst().
        					orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect parent source branch name in the request"));
        
        branch.setRepository(repo);
        branch.setParentBranch(parentBranch);
        Branch savedBranch = branchRepository.save(branch);
        
     // Log activity
        ActivityLog log = new ActivityLog();
        log.setAction(ActivityAction.CREATE_BRANCH);
        log.setUserId(cacheService.getUserId(userDetails.getUsername()));
        log.setRepositoryId(repositoryId);
        log.setBranchId(savedBranch.getId());
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
        return mapEntityToDTO(savedBranch);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User id doesn't exist for this username");    		
    	}
    	Repository_ repo = repositoryRepository.findByNameAndOwnerId(repoName, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));

    	if (repo.getVisibility().equalsIgnoreCase("private") && !repo.getOwner().getUsername().equals(userDetails.getUsername())) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	return repo.getBranches().stream()
                .map(branch -> mapEntityToDTO(branch))
                .collect(Collectors.toList());
	}
}