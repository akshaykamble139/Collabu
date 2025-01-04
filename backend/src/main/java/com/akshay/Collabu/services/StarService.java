package com.akshay.Collabu.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.StarDTO;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.Star;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.StarRepository;
import com.akshay.Collabu.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class StarService {
    @Autowired
    private StarRepository starRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    public Integer getStarCountByRepositoryId(Long repositoryId) {
        List<Star> stars = starRepository.findByRepositoryId(repositoryId);
        return stars.size();
    }
    
    @Transactional
    public StarDTO toggleStar(String username, StarDTO starDTO) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	Long repositoryId = starDTO.getRepositoryId();
        Star star = starRepository.findByUserIdAndRepositoryId(userId, repositoryId).orElse(null);

        if (star == null) {
            // Create a new star entry if not found
            star = new Star();
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));    
            
            star.setUser(user);

            Repository_ repo = repositoryRepository.findById(starDTO.getRepositoryId())
                    .orElseThrow(() -> new RuntimeException("Repository not found"));
            
            star.setRepository(repo);
            
            star.setIsActive(true);
        } else {
            // Toggle the isActive field
            star.setIsActive(!star.getIsActive());
        }

        Star savedStar = starRepository.save(star);
        
     // Update star count in cache
        Long updatedStarCount = starRepository.countByRepositoryIdAndIsActiveTrue(repositoryId);
        cacheService.updateRepositoryStarCount(repositoryId, updatedStarCount);
        
     // Persist updated count to repository
        repositoryRepository.updateStarsCount(repositoryId, updatedStarCount);
        
        return new StarDTO(savedStar.getId(), savedStar.getRepository().getId(), savedStar.getIsActive());
    }
    
    public StarDTO getStarStatus(String username, StarDTO starDTO) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	Long repositoryId = starDTO.getRepositoryId();
        Star savedStar = starRepository.findByUserIdAndRepositoryId(userId, repositoryId).orElse(null);
        if (savedStar == null) {
            return new StarDTO(null, repositoryId, false);
        }
        return new StarDTO(savedStar.getId(), savedStar.getRepository().getId(), savedStar.getIsActive());
    }

    public void deleteStar(Long id) {
    	if (!starRepository.existsById(id)) {
            throw new RuntimeException("Star not found");
        }
        starRepository.deleteById(id);
    }

}