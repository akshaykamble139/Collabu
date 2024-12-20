package com.akshay.Collabu.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.akshay.Collabu.models.Star;

@Repository
public interface StarRepository extends JpaRepository<Star, Long> {
    List<Star> findByUser_Id(Long userId);
    List<Star> findByRepository_Id(Long repositoryId);
}
