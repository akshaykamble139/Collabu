package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.UserRepository;

@Service
public class RepositoryService {
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CacheService cacheService;
    
    public RepositoryDTO getRepositoryById(Long id) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        return mapEntityToDTO(repo);
    }

	private RepositoryDTO mapEntityToDTO(Repository_ repo) {
		return new RepositoryDTO(
        		repo.getId(), 
        		repo.getName(), 
        		repo.getDescription(), 
        		repo.getOwner().getUsername(), 
        		repo.getVisibility().equals("public"),
                repo.getForksCount() != null,
                repo.getForkedFrom() != null ? repo.getForkedFrom().getId() : null);
	}

    public RepositoryDTO createRepository(RepositoryDTO repositoryDTO) {
    	Long ownerId = cacheService.getUserId(repositoryDTO.getOwnerUsername());
    	
    	if (ownerId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
        // Check if a repository with the same name already exists for the user
        boolean exists = repositoryRepository.existsByNameAndOwnerId(repositoryDTO.getName(), ownerId);
        if (exists) {
            throw new RuntimeException("Repository with this name already exists for the user.");
        }

        // Create and populate the repository object
        Repository_ repository = new Repository_();
        repository.setName(repositoryDTO.getName());
        repository.setDescription(repositoryDTO.getDescription());
        repository.setOwner(userRepository.findById(ownerId)
            .orElseThrow(() -> new RuntimeException("Owner not found")));
        repository.setVisibility(repositoryDTO.isPublic() ? "public" : "private"); // Set visibility

        // If it's a fork, associate it with the parent repository
        if (repositoryDTO.isForked() && repositoryDTO.getParentRepositoryId() != null) {
            Repository_ parentRepo = repositoryRepository.findById(repositoryDTO.getParentRepositoryId())
                .orElseThrow(() -> new RuntimeException("Parent repository not found for forking."));
            repository.setForkedFrom(parentRepo);
        }

        // Save and return the created repository as DTO
        Repository_ savedRepo = repositoryRepository.save(repository);
        return mapEntityToDTO(savedRepo);
    }


    public RepositoryDTO updateRepository(Long id, RepositoryDTO repositoryDTO) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        repo.setName(repositoryDTO.getName());
        repo.setDescription(repositoryDTO.getDescription());
        Repository_ updatedRepo = repositoryRepository.save(repo);
        return mapEntityToDTO(updatedRepo);
    }

    public void deleteRepository(Long id) {
        if (!repositoryRepository.existsById(id)) {
            throw new RuntimeException("Repository not found");
        }
        repositoryRepository.deleteById(id);
    }

	public RepositoryDTO findByUsernameAndName(String username, String repoName) {
    	Long ownerId = cacheService.getUserId(username);
    	if (ownerId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	Repository_ repo = repositoryRepository.findByNameAndOwnerId(repoName, ownerId)
                .orElseThrow(() -> new RuntimeException("Repository not found"));

		return mapEntityToDTO(repo);
	}

	public List<RepositoryDTO> getRepositoriesByUserName(String userName) {
		Long ownerId = cacheService.getUserId(userName);
    	if (ownerId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	
    	List<Repository_> repositories = repositoryRepository.findByOwnerId(ownerId);
        return repositories.stream()
                .map(repo -> mapEntityToDTO(repo))
                .collect(Collectors.toList());
	}
}

