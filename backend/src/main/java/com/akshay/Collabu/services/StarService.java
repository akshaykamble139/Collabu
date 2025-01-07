package com.akshay.Collabu.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.BranchDTO;
import com.akshay.Collabu.dto.StarDTO;
import com.akshay.Collabu.models.Branch;
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
    
    public StarDTO mapEntityToDTO(Star star) {
		StarDTO resultDto = new StarDTO();
		if (star == null) {
			resultDto.setIsActive(false);
		}
		else {
			resultDto.setId(star.getId()); 
	        resultDto.setIsActive(star.getIsActive());
		}
        return resultDto;
	}
    
    @Transactional
    public StarDTO toggleStar(String username, StarDTO starDTO) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	Long repositoryId = cacheService.getRepositoryId(starDTO.getOwnerUsername() + "-" + starDTO.getRepositoryName());
    	if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
        Star star = starRepository.findByUserIdAndRepositoryId(userId, repositoryId).orElse(null);

        if (star == null) {
            // Create a new star entry if not found
            star = new Star();
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));    
            
            star.setUser(user);

            Repository_ repo = repositoryRepository.findById(repositoryId)
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
        
        return mapEntityToDTO(savedStar);
    }
    
    public StarDTO getStarStatus(String username, StarDTO starDTO) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	
    	Long repositoryId = cacheService.getRepositoryId(starDTO.getOwnerUsername() + "-" + starDTO.getRepositoryName());
    	if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	
//    	Repository_ repo = repositoryRepository.findById(repositoryId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
//		
//		if (!starDTO.getOwnerUsername().equals(username) && !repo.getVisibility().equals("public")) {
//			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
//		}
        Star savedStar = starRepository.findByUserIdAndRepositoryId(userId, repositoryId).orElse(null);
        
        return mapEntityToDTO(savedStar);
    }

    public void deleteStar(Long id) {
    	if (!starRepository.existsById(id)) {
            throw new RuntimeException("Star not found");
        }
        starRepository.deleteById(id);
    }

}