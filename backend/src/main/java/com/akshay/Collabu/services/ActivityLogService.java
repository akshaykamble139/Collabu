package com.akshay.Collabu.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.ActivityLogDTO;
import com.akshay.Collabu.models.ActivityAction;
import com.akshay.Collabu.models.ActivityLog;
import com.akshay.Collabu.repositories.ActivityLogRepository;

@Service
public class ActivityLogService {
    @Autowired
    private ActivityLogRepository activityLogRepository;

    // Convert DTO to Entity
    private ActivityLog mapToEntity(ActivityLogDTO dto) {
        ActivityLog log = new ActivityLog();
        log.setId(dto.getId());
        log.setUserId(dto.getUserId());
        log.setRepositoryId(dto.getRepositoryId());
        log.setBranchId(dto.getBranchId());
        log.setFileId(dto.getFileId());
        log.setAction(ActivityAction.valueOf(dto.getAction()));
        log.setTimestamp(dto.getTimestamp());
        return log;
    }

    // Convert Entity to DTO
    private ActivityLogDTO mapToDTO(ActivityLog entity) {
        return new ActivityLogDTO(
            entity.getId(),
            entity.getUserId(),
            entity.getRepositoryId(),
            entity.getBranchId(),
            entity.getFileId(),
            entity.getAction().toString(),
            entity.getTimestamp()
        );
    }

    // Create or Update an ActivityLog
    public ActivityLogDTO createOrUpdateActivityLog(ActivityLogDTO activityLogDTO) {
        ActivityLog activityLog = mapToEntity(activityLogDTO);
        ActivityLog savedActivityLog = activityLogRepository.save(activityLog);
        return mapToDTO(savedActivityLog);
    }

    // Get ActivityLog by ID
    public ActivityLogDTO getActivityLogById(Long id) {
        ActivityLog log = activityLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ActivityLog not found with ID: " + id));
        return mapToDTO(log);
    }

    // Get All ActivityLogs
    public List<ActivityLogDTO> getAllActivityLogs() {
        List<ActivityLog> logs = activityLogRepository.findAll();
        return logs.stream().map(this::mapToDTO).toList();
    }

    // Delete ActivityLog by ID
    public void deleteActivityLogById(Long id) {
        if (!activityLogRepository.existsById(id)) {
            throw new RuntimeException("ActivityLog not found with ID: " + id);
        }
        activityLogRepository.deleteById(id);
    }
}
