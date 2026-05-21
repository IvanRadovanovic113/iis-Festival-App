package com.festivalapp.repository;

import com.festivalapp.model.UserFestivalAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFestivalAssignmentRepository extends JpaRepository<UserFestivalAssignment, Long> {
    Optional<UserFestivalAssignment> findByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
}
