package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.dto.ActivityLogDTO;
import com.akshay.Collabu.services.ActivityLogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/activity-logs")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    // Create a new ActivityLog
    @PostMapping
    public ResponseEntity<ActivityLogDTO> createActivityLog(@RequestBody @Valid ActivityLogDTO activityLogDTO) {
    	activityLogDTO.setId(null);
        ActivityLogDTO createdLog = activityLogService.createOrUpdateActivityLog(activityLogDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLog);
    }

    // Get ActivityLog by ID
    @GetMapping("/{id}")
    public ResponseEntity<ActivityLogDTO> getActivityLogById(@PathVariable Long id) {
        ActivityLogDTO log = activityLogService.getActivityLogById(id);
        return ResponseEntity.ok(log);
    }

    // Get all ActivityLogs
    @GetMapping
    public ResponseEntity<List<ActivityLogDTO>> getAllActivityLogs() {
        List<ActivityLogDTO> logs = activityLogService.getAllActivityLogs();
        return ResponseEntity.ok(logs);
    }

    // Update an existing ActivityLog
    @PutMapping("/{id}")
    public ResponseEntity<ActivityLogDTO> updateActivityLog(
        @PathVariable Long id,
        @RequestBody @Valid ActivityLogDTO activityLogDTO
    ) {
        activityLogDTO.setId(id);
        ActivityLogDTO updatedLog = activityLogService.createOrUpdateActivityLog(activityLogDTO);
        return ResponseEntity.ok(updatedLog);
    }

    // Delete ActivityLog by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteActivityLog(@PathVariable Long id) {
        activityLogService.deleteActivityLogById(id);
        return ResponseEntity.ok("ActivityLog deleted successfully.");
    }
}
