package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
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
public class BranchCacheService {
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @Autowired
    private CacheService cacheService;

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
    @Caching(evict = {
	        @CacheEvict(value = "branches", key = "#repositoryId")
	    })
    public BranchDTO createBranchForRepositoryId(BranchDTO branchDTO, Long repositoryId) {
    	
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
        
        String key = cacheService.getRepositoryKey(repositoryId);
        
        
     // Log activity
        ActivityLog log = new ActivityLog();
        log.setAction(ActivityAction.CREATE_BRANCH);
        log.setUserId(cacheService.getUserId(key.split("-")[0]));
        log.setRepositoryId(repositoryId);
        log.setBranchId(savedBranch.getId());
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
        return mapEntityToDTO(savedBranch);
    }  
    
    @Cacheable(value = "branches", key = "#repositoryId")
	public List<BranchDTO> findBranchesByRepositoryId(Long repositoryId) {

		Repository_ repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));

    	return repo.getBranches().stream()
                .map(branch -> mapEntityToDTO(branch))
                .collect(Collectors.toList());
	}
}