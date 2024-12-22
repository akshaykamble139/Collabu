package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.akshay.Collabu.dto.ActivityLogDTO;
import com.akshay.Collabu.models.ActivityAction;
import com.akshay.Collabu.models.ActivityLog;
import com.akshay.Collabu.repositories.ActivityLogRepository;

class ActivityLogServiceTest {
	
	public static final Logger logger = LoggerFactory.getLogger(ActivityLogServiceTest.class);

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetActivityLogsByUserId() {        
        ActivityLog log = new ActivityLog();
        log.setId(1L);
        log.setUserId(2L);
        log.setRepositoryId(3L);
        log.setAction(ActivityAction.CREATE_REPOSITORY);
        
        when(activityLogRepository.findById(anyLong())).thenReturn(Optional.empty());
       
        try {
            activityLogService.getActivityLogById(1L);
        }
        catch (Exception e) {
			logger.info("Activity Log doesn't exist for id: {}", 1L);
		}
        
        when(activityLogRepository.findById(anyLong())).thenReturn(Optional.of(log));
        
        ActivityLogDTO result = activityLogService.getActivityLogById(1L);
        assertEquals(ActivityAction.CREATE_REPOSITORY.toString(), result.getAction().toString());
    }

    @Test
    void testCreateActivityLog() {
    	ActivityLog log = new ActivityLog();
        log.setId(1L);
        log.setUserId(2L);
        log.setRepositoryId(3L);
        log.setAction(ActivityAction.COMMIT);
        when(activityLogRepository.save(any())).thenReturn(log);

        ActivityLogDTO dto = new ActivityLogDTO(
                log.getId(),
                log.getUserId(),
                log.getRepositoryId(),
                log.getBranchId(),
                log.getFileId(),
                log.getAction().toString(),
                log.getTimestamp());
        
        ActivityLogDTO result = activityLogService.createOrUpdateActivityLog(dto);
        assertEquals(ActivityAction.COMMIT.toString(), result.getAction().toString());
    }
        
    @Test
    void testGetAllActivityLogs() {        
        ActivityLog log = new ActivityLog();
        log.setId(1L);
        log.setUserId(2L);
        log.setRepositoryId(3L);
        log.setAction(ActivityAction.CREATE_REPOSITORY);
        
        List<ActivityLog> list = new ArrayList<ActivityLog>();
        list.add(log);
        
        when(activityLogRepository.findAll()).thenReturn(list);
               
        List<ActivityLogDTO> result = activityLogService.getAllActivityLogs();
        assertEquals(ActivityAction.CREATE_REPOSITORY.toString(), result.get(0).getAction().toString());
    }
    
	@Test
	void testDeleteActivityLogById() {
	                    
	    when(activityLogRepository.existsById(anyLong())).thenReturn(false);
	    
	    try {
	    	activityLogService.deleteActivityLogById(1L);
	    }
	    catch (Exception e) {
			logger.info("Star doesn't exist for id: {}", 1L);
		}
	
	    when(activityLogRepository.existsById(anyLong())).thenReturn(true);
	
		activityLogService.deleteActivityLogById(1L);       
	}    
    
}
