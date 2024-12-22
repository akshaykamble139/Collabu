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

    public List<RepositoryDTO> getRepositoriesByUserId(Long userId) {
        List<Repository_> repositories = repositoryRepository.findByOwnerId(userId);
        return repositories.stream()
                .map(repo -> new RepositoryDTO(
                		repo.getId(), 
                		repo.getName(), 
                		repo.getDescription(), 
                		repo.getOwner().getId(), 
                		repo.getVisibility().equals("public"),
                        repo.getForksCount() != null,
                        repo.getForkedFrom() != null ? repo.getForkedFrom().getId() : null))
                .collect(Collectors.toList());
    }
    
    public RepositoryDTO getRepositoryById(Long id) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        return new RepositoryDTO(
        		repo.getId(), 
        		repo.getName(), 
        		repo.getDescription(), 
        		repo.getOwner().getId(), 
        		repo.getVisibility().equals("public"),
                repo.getForksCount() != null,
                repo.getForkedFrom() != null ? repo.getForkedFrom().getId() : null);
    }

    public RepositoryDTO createRepository(RepositoryDTO repositoryDTO) {
    	Long ownerId = repositoryDTO.getOwnerId();
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
            repository.setForkedFrom(repository);
        }

        // Save and return the created repository as DTO
        Repository_ savedRepo = repositoryRepository.save(repository);
        return new RepositoryDTO(
            savedRepo.getId(),
            savedRepo.getName(),
            savedRepo.getDescription(),
            savedRepo.getOwner().getId(),
            savedRepo.getVisibility().equals("public"),
            savedRepo.getForksCount() != null,
            savedRepo.getForkedFrom() != null ? savedRepo.getForkedFrom().getId() : null
        );
    }


    public RepositoryDTO updateRepository(Long id, RepositoryDTO repositoryDTO) {
        Repository_ repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        repo.setName(repositoryDTO.getName());
        repo.setDescription(repositoryDTO.getDescription());
        Repository_ updatedRepo = repositoryRepository.save(repo);
        return new RepositoryDTO(
        		updatedRepo.getId(), 
        		updatedRepo.getName(), 
        		updatedRepo.getDescription(), 
        		updatedRepo.getOwner().getId(),
        		updatedRepo.getVisibility().equals("public"),
                updatedRepo.getForksCount() != null,
                updatedRepo.getForkedFrom() != null ? repo.getForkedFrom().getId() : null
        		);
    }

    public void deleteRepository(Long id) {
        if (!repositoryRepository.existsById(id)) {
            throw new RuntimeException("Repository not found");
        }
        repositoryRepository.deleteById(id);
    }
}

