package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.ForkRequestDTO;
import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.RepositoryRepository;

@Service
public class RepositoryService {
	public static final Logger logger = LoggerFactory.getLogger(RepositoryService.class);

    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private RepositoryCacheService repositoryCacheService;
    
    @Autowired
    RedisConnectionService redisConnectionService;
    
    public RepositoryDTO getRepositoryById(Long id) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        return mapEntityToDTO(repo);
    }

	public RepositoryDTO mapEntityToDTO(Repository_ repo) {
		return repositoryCacheService.mapEntityToDTO(repo);
	}

    public RepositoryDTO createRepository(RepositoryDTO repositoryDTO) {
    	Long ownerId = cacheService.getUserId(repositoryDTO.getOwnerUsername());
    	
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Owner not found");    		
    	}
    	
    	return repositoryCacheService.createRepositoryForOwnerId(repositoryDTO, ownerId);
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

    public void deleteRepository(Long id, UserDetails userDetails) {
    	Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        
    	if (!userDetails.getUsername().equals(repo.getOwner().getUsername())) {
        	throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized to delete repository owned by other users");
    	}
    	
    	Long ownerId = repo.getOwner().getId();
    	
    	repositoryCacheService.deleteRepositoryForOwnerId(id, repo, ownerId);
    }
    
	public RepositoryDTO findByUsernameAndName(String username, String repoName, UserDetails userDetails) {
    	Long ownerId = cacheService.getUserId(username);
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User doesn't exist");    		
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User doesn't exist");    		
    	}   	

    	if (userName.equals(userDetails.getUsername())) {
    		return repositoryCacheService.getAllRepositoriesByOwnerId(ownerId);
    	}
    	else {
    		return repositoryCacheService.getPublicRepositoriesByOwnerId(ownerId);
    	}
	}
	
    public RepositoryDTO forkRepository(ForkRequestDTO forkRequestDTO, UserDetails userDetails) {
								
		Long ownerId = cacheService.getUserId(userDetails.getUsername());
    	
    	if (ownerId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User doesn't exist");    		
    	}
        
    	return repositoryCacheService.forkRepositoryByOwnerId(forkRequestDTO, ownerId);
    }
}

