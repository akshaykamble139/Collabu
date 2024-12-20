package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByUser_Id(Long userId);
    List<ActivityLog> findByAction(String action); // Filter by action type
}
