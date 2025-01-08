package com.akshay.Collabu.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.StarDTO;
import com.akshay.Collabu.models.Star;
import com.akshay.Collabu.repositories.StarRepository;

@Service
public class StarService {
	
	@Autowired
	private StarCacheService starCacheService;
	
    @Autowired
    private StarRepository starRepository;
    
    @Autowired
    private CacheService cacheService;
    
    public Integer getStarCountByRepositoryId(Long repositoryId) {
        List<Star> stars = starRepository.findByRepositoryId(repositoryId);
        return stars.size();
    }
    
    public StarDTO mapEntityToDTO(Star star) {
    	return starCacheService.mapEntityToDTO(star);
	}
    
    public StarDTO toggleStar(String username, StarDTO starDTO) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User doesn't exist");    		
    	}
    	Long ownerId = cacheService.getUserId(starDTO.getOwnerUsername());
    	
    	return starCacheService.toggleStarForRepositoryOfOwnerId(userId, starDTO.getRepositoryName(), ownerId);
    }
    
    public StarDTO getStarStatus(String username, StarDTO starDTO) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User doesn't exist");    		
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