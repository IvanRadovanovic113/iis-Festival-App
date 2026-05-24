package com.festivalapp.repository;

import com.festivalapp.model.AdType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdTypeRepository extends JpaRepository<AdType, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<AdType> findAllByOrderByNameAsc();
}
