package com.festivalapp.service;

import com.festivalapp.dto.AdPhaseResponse;
import com.festivalapp.dto.AdReviewResponse;
import com.festivalapp.dto.AdResponse;
import com.festivalapp.dto.AdVersionDetailResponse;
import com.festivalapp.dto.AdVersionSummaryResponse;
import com.festivalapp.model.Ad;
import com.festivalapp.model.AdVersion;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.AdVersionRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdReviewService {

    private final AdRepository adRepository;
    private final AdVersionRepository adVersionRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    private Role requireReviewRole(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.FESTIVAL_MANAGER && assignment.getRole() != Role.FESTIVAL_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only festival managers and festival directors can review ads");
        }
        return assignment.getRole();
    }

    @Transactional(readOnly = true)
    public AdReviewResponse getAdReview(Long festivalId, Long adId, User user) {
        requireReviewRole(user);
        Ad ad = getAdWithinFestival(festivalId, adId);
        List<AdVersion> versions = adVersionRepository.findAllByAd_AdIdOrderByVersionNumberDesc(adId);

        List<AdVersionSummaryResponse> history = versions.isEmpty()
            ? List.of(new AdVersionSummaryResponse(ad.getVersionNumber(), ad.getLastChangeDate(), true))
            : versions.stream()
                .sorted(Comparator.comparing(AdVersion::getVersionNumber).reversed())
                .map(version -> AdVersionSummaryResponse.from(version, version.getVersionNumber().equals(ad.getVersionNumber())))
                .toList();

        return new AdReviewResponse(
            AdResponse.from(ad),
            ad.getAdType().getPhases().stream().map(AdPhaseResponse::from).toList(),
            history
        );
    }

    @Transactional(readOnly = true)
    public AdVersionDetailResponse getVersionDetail(Long festivalId, Long adId, Integer versionNumber, User user) {
        requireReviewRole(user);
        Ad ad = getAdWithinFestival(festivalId, adId);

        return adVersionRepository.findByAd_AdIdAndVersionNumber(adId, versionNumber)
            .map(version -> AdVersionDetailResponse.from(ad, version, version.getVersionNumber().equals(ad.getVersionNumber())))
            .orElseGet(() -> {
                if (ad.getVersionNumber().equals(versionNumber)) {
                    AdVersion syntheticVersion = AdVersion.builder()
                        .ad(ad)
                        .versionNumber(ad.getVersionNumber())
                        .name(ad.getName())
                        .description(ad.getDescription())
                        .contentValue(ad.getContentFileName())
                        .changedAt(ad.getLastChangeDate())
                        .phaseName(ad.getCurrentPhase().getName())
                        .build();
                    return AdVersionDetailResponse.from(ad, syntheticVersion, true);
                }
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad version not found");
            });
    }

    private Ad getAdWithinFestival(Long festivalId, Long adId) {
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to the selected festival");
        }
        return ad;
    }
}
