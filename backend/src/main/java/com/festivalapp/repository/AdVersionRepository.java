package com.festivalapp.repository;

import com.festivalapp.model.AdVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdVersionRepository extends JpaRepository<AdVersion, Long> {

    List<AdVersion> findAllByAd_AdIdOrderByVersionNumberDesc(Long adId);

    Optional<AdVersion> findByAd_AdIdAndVersionNumber(Long adId, Integer versionNumber);
}
