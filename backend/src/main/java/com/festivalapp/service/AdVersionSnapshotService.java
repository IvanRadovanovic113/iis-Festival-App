package com.festivalapp.service;

import com.festivalapp.model.Ad;
import com.festivalapp.model.AdVersion;
import com.festivalapp.repository.AdVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdVersionSnapshotService {

    private final AdVersionRepository adVersionRepository;

    @Transactional
    public void captureSnapshot(Ad ad) {
        boolean alreadyExists = adVersionRepository.findByAd_AdIdAndVersionNumber(ad.getAdId(), ad.getVersionNumber()).isPresent();
        if (alreadyExists) {
            return;
        }

        adVersionRepository.save(AdVersion.builder()
            .ad(ad)
            .versionNumber(ad.getVersionNumber())
            .name(ad.getName())
            .description(ad.getDescription())
            .contentValue(ad.getContentFileName())
            .changedAt(ad.getLastChangeDate())
            .phaseName(ad.getCurrentPhase().getName())
            .build());
    }
}
