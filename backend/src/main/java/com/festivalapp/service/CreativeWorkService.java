package com.festivalapp.service;

import com.festivalapp.dto.AdResponse;
import com.festivalapp.dto.CreativeAdUpdateRequest;
import com.festivalapp.model.Ad;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreativeWorkService {

    private final AdRepository adRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final AdVersionSnapshotService adVersionSnapshotService;

    private UserFestivalAssignment requireCreativeAssignment(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        if (assignment.getRole() != Role.PRODUCT_DESIGNER && assignment.getRole() != Role.TECHNICAL_SUPPORT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only product designers and technical support can edit ad content");
        }
        return assignment;
    }

    @Transactional(readOnly = true)
    public List<AdResponse> getAssignedAds(User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        return adRepository.findAllByCampaign_Festival_FestivalIdAndCurrentPhase_AssignedRoleOrderByLastChangeDateDescAdIdDesc(
                assignment.getFestival().getFestivalId(),
                assignment.getRole()
            ).stream()
            .map(AdResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdResponse getAd(Long adId, User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        ensureAdAccess(ad, assignment.getFestival().getFestivalId(), assignment.getRole());
        return AdResponse.from(ad);
    }

    @Transactional
    public AdResponse updateAd(Long adId, CreativeAdUpdateRequest request, User user) {
        UserFestivalAssignment assignment = requireCreativeAssignment(user);
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        ensureAdAccess(ad, assignment.getFestival().getFestivalId(), assignment.getRole());

        String nextName = request.getName().trim();
        String nextDescription = request.getDescription().trim();
        String nextContentValue = request.getContentValue().trim();

        boolean changed = !ad.getName().equals(nextName)
            || !ad.getDescription().equals(nextDescription)
            || !ad.getContentFileName().equals(nextContentValue);
        if (!changed) {
            return AdResponse.from(ad);
        }

        ad.setName(nextName);
        ad.setDescription(nextDescription);
        ad.setContentFileName(nextContentValue);
        ad.setLastChangeDate(LocalDate.now());
        ad.setVersionNumber(ad.getVersionNumber() + 1);
        Ad savedAd = adRepository.save(ad);
        adVersionSnapshotService.captureSnapshot(savedAd);
        return AdResponse.from(savedAd);
    }

    private void ensureAdAccess(Ad ad, Long festivalId, Role role) {
        if (!ad.getCampaign().getFestival().getFestivalId().equals(festivalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ad does not belong to your festival");
        }
        if (ad.getCurrentPhase().getAssignedRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This ad is not currently assigned to your role");
        }
    }
}
