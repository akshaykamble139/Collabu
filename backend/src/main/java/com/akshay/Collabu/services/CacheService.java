package com.akshay.Collabu.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.akshay.Collabu.repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CacheService {

    // Cache for userId ↔ username
    private final Map<Long, String> userIdToUsernameCache = new ConcurrentHashMap<>();
    private final Map<String, Long> usernameToUserIdCache = new ConcurrentHashMap<>();

    private final UserRepository userRepository;

    public CacheService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Load cache at startup
    @PostConstruct
    public void loadCache() {
        userRepository.findAll().forEach(user -> {
            userIdToUsernameCache.put(user.getId(), user.getUsername());
            usernameToUserIdCache.put(user.getUsername(), user.getId());
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

    // Optional: Clear cache manually (for admin actions)
    public void clearCache() {
        userIdToUsernameCache.clear();
        usernameToUserIdCache.clear();
        loadCache();
    }
}

