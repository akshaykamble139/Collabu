package com.akshay.Collabu.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.StarRepository;
import com.akshay.Collabu.repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CacheService {

    // Cache for userId ↔ username
    private final Map<Long, String> userIdToUsernameCache = new ConcurrentHashMap<>();
    private final Map<String, Long> usernameToUserIdCache = new ConcurrentHashMap<>();
    
    // Cache for star count of repository
    private final Map<Long, Long> repositoryStarCache = new ConcurrentHashMap<>();

    // Cache for fork count of repository
    private final Map<Long, Long> repositoryForkCache = new ConcurrentHashMap<>();

 // Cache for repositoryId ↔ username-repositoryName
    private final Map<Long, String> repositoryIdToNameCache = new ConcurrentHashMap<>();
    private final Map<String, Long> nameToRepositoryIdCache = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StarRepository starRepository;

    @Autowired
    private RepositoryRepository repositoryRepository;

    // Load cache at startup
    @PostConstruct
    public void loadCache() {
        userRepository.findAll().forEach(user -> {
            userIdToUsernameCache.put(user.getId(), user.getUsername());
            usernameToUserIdCache.put(user.getUsername(), user.getId());
        });
        
        repositoryRepository.findAll().forEach(repo -> {
            String key = repo.getOwner().getUsername() + "-" + repo.getName();
            repositoryIdToNameCache.put(repo.getId(), key);
            nameToRepositoryIdCache.put(key, repo.getId());
            repositoryStarCache.put(repo.getId(), starRepository.countByRepositoryIdAndIsActiveTrue(repo.getId()));
        });
        
        List<Object[]> forkCounts = repositoryRepository.countForksForAllRepositories();
        forkCounts.forEach(result -> {
            Long repositoryId = (Long) result[0];
            Long forkCount = (Long) result[1];
            repositoryForkCache.put(repositoryId, forkCount);
        });
    }

    // Get username by userId
    public String getUsername(Long userId) {
    	if (userIdToUsernameCache.containsKey(userId)) {
    		return userIdToUsernameCache.get(userId);
    	}
    	return fetchAndCacheUsername(userId);
    }

    // Get userId by username
    public Long getUserId(String username) {
    	if (usernameToUserIdCache.containsKey(username)) {
    		return usernameToUserIdCache.get(username);
    	}
        return fetchAndCacheUserId(username);
    }

    // Fetch username from DB if not in cache
    private String fetchAndCacheUsername(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userIdToUsernameCache.put(userId, user.getUsername());
                    usernameToUserIdCache.put(user.getUsername(), userId);
                    return user.getUsername();
                })
                .orElse(null);
    }

    // Fetch userId from DB if not in cache
    private Long fetchAndCacheUserId(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    usernameToUserIdCache.put(username, user.getId());
                    userIdToUsernameCache.put(user.getId(), username);
                    return user.getId();
                })
                .orElse(null);
    }
    
    public void updateRepositoryStarCount(Long repositoryId, Long starCount) {
        repositoryStarCache.put(repositoryId, starCount);
    }
    
    public Long getRepositoryStarCount(Long repositoryId) {
    	if (repositoryStarCache.containsKey(repositoryId)) {
    		return repositoryStarCache.get(repositoryId);
    	}
        return fetchAndCacheStarCount(repositoryId);
    }

    private Long fetchAndCacheStarCount(Long repositoryId) {
        Long starCount = starRepository.countByRepositoryIdAndIsActiveTrue(repositoryId);
        repositoryStarCache.put(repositoryId, starCount);
        return starCount;
    }

 // Fork cache methods
    public void updateRepositoryForkCount(Long repositoryId, Long forkCount) {
        repositoryForkCache.put(repositoryId, forkCount);
    }

    public Long getRepositoryForkCount(Long repositoryId) {
        if (repositoryForkCache.containsKey(repositoryId)) {
            return repositoryForkCache.get(repositoryId);
        }
        return fetchAndCacheForkCount(repositoryId);
    }

    private Long fetchAndCacheForkCount(Long repositoryId) {
    	Long forkCount = repositoryRepository.countByForkedFromId(repositoryId);
        repositoryForkCache.put(repositoryId, forkCount);
        return forkCount;
    }

 // Get username-repoName by repositoryId
    public String getRepositoryKey(Long repositoryId) {
        if (repositoryIdToNameCache.containsKey(repositoryId)) {
            return repositoryIdToNameCache.get(repositoryId);
        }
        return fetchAndCacheRepositoryKey(repositoryId);
    }

    // Get repositoryId by username-repoName
    public Long getRepositoryId(String repositoryKey) {
        if (nameToRepositoryIdCache.containsKey(repositoryKey)) {
            return nameToRepositoryIdCache.get(repositoryKey);
        }
        return fetchAndCacheRepositoryId(repositoryKey);
    }

    // Fetch from DB if not in cache (repositoryId → key)
    private String fetchAndCacheRepositoryKey(Long repositoryId) {
        return repositoryRepository.findById(repositoryId)
                .map(repo -> {
                    String key = repo.getOwner().getUsername() + "-" + repo.getName();
                    repositoryIdToNameCache.put(repositoryId, key);
                    nameToRepositoryIdCache.put(key, repositoryId);
                    return key;
                })
                .orElse(null);
    }

    // Fetch from DB if not in cache (key → repositoryId)
    private Long fetchAndCacheRepositoryId(String repositoryKey) {
        String[] parts = repositoryKey.split("-");
        if (parts.length != 2) return null;
        String username = parts[0];
        String repoName = parts[1];
        
        return repositoryRepository.findByOwnerUsernameAndName(username, repoName)
                .map(repo -> {
                    repositoryIdToNameCache.put(repo.getId(), repositoryKey);
                    nameToRepositoryIdCache.put(repositoryKey, repo.getId());
                    return repo.getId();
                })
                .orElse(null);
    }

    public void updateRepositoryKey(Long repositoryId, String username, String repositoryName) {
        String key = username + "-" + repositoryName;
        repositoryIdToNameCache.put(repositoryId, key);
        nameToRepositoryIdCache.put(key, repositoryId);
    }

    public void removeRepositoryFromCache(Long repositoryId) {
        String key = repositoryIdToNameCache.remove(repositoryId);
        if (key != null) {
            nameToRepositoryIdCache.remove(key);
        }
    }

    // Clear cache manually (for admin actions)
    public void clearCache() {
        userIdToUsernameCache.clear();
        usernameToUserIdCache.clear();
        repositoryIdToNameCache.clear();
        nameToRepositoryIdCache.clear();
        repositoryStarCache.clear();
        loadCache();
    }
}

