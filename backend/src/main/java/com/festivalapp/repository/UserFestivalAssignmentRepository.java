package com.festivalapp.repository;

import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFestivalAssignmentRepository extends JpaRepository<UserFestivalAssignment, Long> {
    Optional<UserFestivalAssignment> findByUser_Id(Long userId);
    Optional<UserFestivalAssignment> findByUser_IdAndFestival_FestivalId(Long userId, Long festivalId);
    boolean existsByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
    List<UserFestivalAssignment> findAllByFestival_FestivalIdAndRole(Long festivalId, Role role);
    List<UserFestivalAssignment> findAllByRole(Role role);
}
