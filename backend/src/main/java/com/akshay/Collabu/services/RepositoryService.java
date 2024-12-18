package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.RepositoryRepository;

@Service
public class RepositoryService {
    @Autowired
    private RepositoryRepository repositoryRepository;

    public List<RepositoryDTO> getRepositoriesByUserId(Long userId) {
        List<Repository_> repositories = repositoryRepository.findByOwner_Id(userId);
        return repositories.stream()
                .map(repo -> new RepositoryDTO(repo.getId(), repo.getName(), repo.getDescription(), repo.getOwner().getId()))
                .collect(Collectors.toList());
    }

    public RepositoryDTO createRepository(RepositoryDTO repositoryDTO) {
    	Repository_ repo = new Repository_();
        repo.setName(repositoryDTO.getName());
        repo.setDescription(repositoryDTO.getDescription());
        // Assume that the owner is already fetched (e.g., through security context or API input)
        Repository_ savedRepo = repositoryRepository.save(repo);
        return new RepositoryDTO(savedRepo.getId(), savedRepo.getName(), savedRepo.getDescription(), savedRepo.getOwner().getId());
    }
}
