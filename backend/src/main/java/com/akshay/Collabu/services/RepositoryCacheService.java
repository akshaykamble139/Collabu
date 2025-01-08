package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.ForkRequestDTO;
import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.models.ActivityAction;
import com.akshay.Collabu.models.ActivityLog;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Commit;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.ActivityLogRepository;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.CommitRepository;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class RepositoryCacheService {
	public static final Logger logger = LoggerFactory.getLogger(RepositoryCacheService.class);

    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private CommitRepository commitRepository;
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    RedisConnectionService redisConnectionService;

	public RepositoryDTO mapEntityToDTO(Repository_ repo) {
		RepositoryDTO resultDto = new RepositoryDTO();
		resultDto.setId(repo.getId()); 
        resultDto.setName(repo.getName());
        resultDto.setDescription(repo.getDescription());
        resultDto.setOwnerUsername(repo.getOwner().getUsername());
        resultDto.setPublicRepositoryOrNot(repo.getVisibility().equals("public"));
        resultDto.setRepositoryForkedOrNot(repo.getForksCount() != null && repo.getForksCount() > 0);
        resultDto.setParentRepositoryId(repo.getForkedFrom() != null ? repo.getForkedFrom().getId() : null);
        resultDto.setStarCount(repo.getStarsCount());
        resultDto.setForkCount(repo.getForksCount());
        
        return resultDto;
	}

	@Transactional
    @Caching(evict = {
	        @CacheEvict(value = "repositories", key = "#ownerId"),
	        @CacheEvict(value = "repositories", key = "#ownerId + ':public'")
	    })
    public RepositoryDTO createRepositoryForOwnerId(RepositoryDTO repositoryDTO, Long ownerId) {
        // Check if a repository with the same name already exists for the user
        boolean exists = repositoryRepository.existsByNameAndOwnerId(repositoryDTO.getName(), ownerId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Repository name already exists for this user");
        }

        // Create and populate the repository object
        Repository_ repository = new Repository_();
        repository.setName(repositoryDTO.getName());
        repository.setDescription(repositoryDTO.getDescription());
        repository.setOwner(userRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Owner not found")));
        repository.setVisibility(repositoryDTO.isPublicRepositoryOrNot() ? "public" : "private"); // Set visibility

        // Save and return the created repository as DTO
        Repository_ savedRepo = repositoryRepository.save(repository);
        
     // Create 'main' branch automatically
        Branch mainBranch = new Branch();
        mainBranch.setName("main");
        mainBranch.setRepository(savedRepo);
        mainBranch.setIsDefault(true);
        branchRepository.save(mainBranch);
        
     // ==== Create Initial Commit ====
        Commit initialCommit = new Commit();
        initialCommit.setMessage("Initial commit");
        initialCommit.setRepository(savedRepo);
        initialCommit.setBranch(mainBranch);
        initialCommit.setUser(savedRepo.getOwner());
        initialCommit.setTimestamp(LocalDateTime.now());
		Commit savedCommit = commitRepository.save(initialCommit);

        // Update branch with the commit
        mainBranch.setLastCommit(savedCommit);
        branchRepository.save(mainBranch);

        // Log activity
        ActivityLog log = new ActivityLog();
        log.setAction(ActivityAction.CREATE_REPOSITORY);
        log.setUserId(savedRepo.getOwner().getId());
        log.setRepositoryId(savedRepo.getId());
        log.setBranchId(mainBranch.getId());
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
        return mapEntityToDTO(savedRepo);
    }
    
    @Transactional
    @Caching(evict = {
	        @CacheEvict(value = "repositories", key = "#ownerId"),
	        @CacheEvict(value = "repositories", key = "#ownerId + ':public'")
	    })
    public void deleteRepositoryForOwnerId(Long id, Repository_ repo, Long ownerId) {
    	// Detach references in branches
        List<Branch> branches = branchRepository.findByRepositoryId(id);
        for (Branch branch : branches) {
            branch.setLastCommit(null);
            branchRepository.save(branch); // Save changes to detach references
        }

        // Delete all commits associated with this repository
        List<Commit> commits = commitRepository.findByRepositoryId(id);
        commitRepository.deleteAll(commits);

        // Delete all branches associated with this repository
        branchRepository.deleteAll(branches);

    	
    	// Delete all files associated with this repository
        List<File> files = fileRepository.findByRepositoryId(id);
        fileRepository.deleteAll(files);
        
        // Delete repository itself
        repositoryRepository.delete(repo);
        
        // Log activity
        
        ActivityLog log = new ActivityLog();
        log.setUserId(ownerId);
        log.setRepositoryId(id);
        log.setAction(ActivityAction.DELETE_REPOSITORY);
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
    }
	
    @Cacheable(value = "repositories", key = "#ownerId")
	public List<RepositoryDTO> getAllRepositoriesByOwnerId(Long ownerId) { 	    	
    	List<Repository_> repositories = repositoryRepository.findByOwnerId(ownerId)
    									.stream()
    									.map(repo -> {
    								        repo.setStarsCount(cacheService.getRepositoryStarCount(repo.getId()));
    								        repo.setForksCount(cacheService.getRepositoryForkCount(repo.getId()));
    								        return repo;
    								    })
    									.collect(Collectors.toList());
    	
        return repositories.stream().map(repo -> mapEntityToDTO(repo)).collect(Collectors.toList());
	}
	
    @Cacheable(value = "repositories", key = "#ownerId + ':public'")
	public List<RepositoryDTO> getPublicRepositoriesByOwnerId(Long ownerId) { 	    	
    	List<Repository_> repositories = repositoryRepository.findByOwnerIdAndVisibility(ownerId, "public")
    									.stream()
    									.map(repo -> {
    								        repo.setStarsCount(cacheService.getRepositoryStarCount(repo.getId()));
    								        repo.setForksCount(cacheService.getRepositoryForkCount(repo.getId()));
    								        return repo;
    								    })
    									.collect(Collectors.toList());
    	
        return repositories.stream().map(repo -> mapEntityToDTO(repo)).collect(Collectors.toList());
	}

	@Transactional
    @Caching(evict = {
	        @CacheEvict(value = "repositories", key = "#ownerId"),
	        @CacheEvict(value = "repositories", key = "#ownerId + ':public'")
	    })
	public RepositoryDTO forkRepositoryByOwnerId(ForkRequestDTO forkRequestDTO, Long ownerId) {
		
		Repository_ parentRepo = repositoryRepository.findById(forkRequestDTO.getRepositoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
						
        // Check if a repository with the same name already exists for the user
        boolean exists = repositoryRepository.existsByNameAndOwnerId(forkRequestDTO.getName(), ownerId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Repository with this name already exists for the user.");
        }

        // Create and populate the repository object
        Repository_ repository = new Repository_();
        repository.setName(forkRequestDTO.getName());
        repository.setDescription(parentRepo.getDescription());
        repository.setOwner(userRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Owner not found")));
        repository.setVisibility("public"); // Set visibility

        // It's a fork so associating it with the parent repository
        repository.setForkedFrom(parentRepo);
            
        // Increment fork count of parent repository
        Long forkCount = cacheService.getRepositoryForkCount(forkRequestDTO.getRepositoryId()) + 1;
        cacheService.updateRepositoryForkCount(forkRequestDTO.getRepositoryId(), forkCount);
        
        parentRepo.setForksCount(forkCount);
        repositoryRepository.save(parentRepo);  // Save updated fork count

        // Save and return the created repository as DTO
        Repository_ savedRepo = repositoryRepository.save(repository);
        
        // Create 'main' branch automatically
        Branch mainBranch = new Branch();
        mainBranch.setName("main");
        mainBranch.setRepository(savedRepo);
        mainBranch.setIsDefault(true);
        branchRepository.save(mainBranch);
        
        // ==== Create Initial Commit ====
        Commit initialCommit = new Commit();
        initialCommit.setMessage("Forked from " + parentRepo.getOwner().getUsername() + "/" + parentRepo.getName());
        initialCommit.setRepository(savedRepo);
        initialCommit.setBranch(mainBranch);
        initialCommit.setUser(savedRepo.getOwner());
        initialCommit.setTimestamp(LocalDateTime.now());
		Commit savedCommit = commitRepository.save(initialCommit);

        // Update branch with the commit
        mainBranch.setLastCommit(savedCommit);
        branchRepository.save(mainBranch);

        // Log activity
        ActivityLog log = new ActivityLog();
        log.setAction(ActivityAction.FORK_REPOSITORY);
        log.setUserId(savedRepo.getOwner().getId());
        log.setRepositoryId(savedRepo.getId());
        log.setBranchId(mainBranch.getId());
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
        return mapEntityToDTO(savedRepo);
    }
}


