package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
public class RepositoryService {
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
    
    public RepositoryDTO getRepositoryById(Long id) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        return mapEntityToDTO(repo);
    }

	private RepositoryDTO mapEntityToDTO(Repository_ repo) {
		return new RepositoryDTO(
        		repo.getId(), 
        		repo.getName(), 
        		repo.getDescription(), 
        		repo.getOwner().getUsername(), 
        		repo.getVisibility().equals("public"),
                repo.getForksCount() != null && repo.getForksCount() > 0,
                repo.getForkedFrom() != null ? repo.getForkedFrom().getId() : null);
	}

    public RepositoryDTO createRepository(RepositoryDTO repositoryDTO) {
    	Long ownerId = cacheService.getUserId(repositoryDTO.getOwnerUsername());
    	
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User id doesn't exist for this username");    		
    	}
        // Check if a repository with the same name already exists for the user
        boolean exists = repositoryRepository.existsByNameAndOwnerId(repositoryDTO.getName(), ownerId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Repository with this name already exists for the user.");
        }

        // Create and populate the repository object
        Repository_ repository = new Repository_();
        repository.setName(repositoryDTO.getName());
        repository.setDescription(repositoryDTO.getDescription());
        repository.setOwner(userRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Owner not found")));
        repository.setVisibility(repositoryDTO.isPublic() ? "public" : "private"); // Set visibility

        // If it's a fork, associate it with the parent repository
        if (repositoryDTO.isForked() && repositoryDTO.getParentRepositoryId() != null) {
            Repository_ parentRepo = repositoryRepository.findById(repositoryDTO.getParentRepositoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Parent repository not found for forking."));
            repository.setForkedFrom(parentRepo);
            
         // Increment fork count of parent repository
            parentRepo.setForksCount(parentRepo.getForksCount() + 1);
            repositoryRepository.save(parentRepo);  // Save updated fork count
        }

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


    public RepositoryDTO updateRepository(Long id, RepositoryDTO repositoryDTO) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        repo.setName(repositoryDTO.getName());
        repo.setDescription(repositoryDTO.getDescription());
        repo.setUpdatedAt(LocalDateTime.now()); 
       
        Repository_ updatedRepo = repositoryRepository.save(repo);
        return mapEntityToDTO(updatedRepo);
    }

    @Transactional
    public void deleteRepository(Long id, UserDetails userDetails) {
    	Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        
    	if (!userDetails.getUsername().equals(repo.getOwner().getUsername())) {
        	throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized to delete repository owned by other users");
    	}
    	
    	Long ownerId = repo.getOwner().getId();
    	
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

	public RepositoryDTO findByUsernameAndName(String username, String repoName, UserDetails userDetails) {
    	Long ownerId = cacheService.getUserId(username);
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User id doesn't exist for this username");    		
    	}
    	Repository_ repo = repositoryRepository.findByNameAndOwnerId(repoName, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));

    	if (repo.getVisibility().equalsIgnoreCase("private") && !repo.getOwner().getUsername().equals(userDetails.getUsername())) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
		return mapEntityToDTO(repo);
	}

	public List<RepositoryDTO> getRepositoriesByUserName(String userName, UserDetails userDetails) {
		Long ownerId = cacheService.getUserId(userName);
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User id doesn't exist for this username");    		
    	}
    	
    	List<Repository_> repositories = repositoryRepository.findByOwnerId(ownerId);
        return repositories.stream()
        		.filter(repo -> repo.getVisibility().equalsIgnoreCase("public") 
        				|| repo.getVisibility().equalsIgnoreCase("private") && repo.getOwner().getUsername().equals(userDetails.getUsername()))
                .map(repo -> mapEntityToDTO(repo))
                .collect(Collectors.toList());
	}
}

