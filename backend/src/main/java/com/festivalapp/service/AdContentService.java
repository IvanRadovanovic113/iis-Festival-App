package com.festivalapp.service;

import com.festivalapp.model.Ad;
import com.festivalapp.model.AdVersion;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.repository.AdVersionRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdContentService {

    private final AdRepository adRepository;
    private final AdVersionRepository adVersionRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final FileStorageService fileStorageService;

    public FileContentPayload loadCurrentAdContent(Long adId, User user) {
        Ad ad = adRepository.findById(adId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"));
        ensureCurrentAdAccess(ad, user);
        if (ad.getContentStoragePath() == null || ad.getContentMimeType() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad does not have stored binary content");
        }
        return new FileContentPayload(
            fileStorageService.loadAsResource(ad.getContentStoragePath()),
            ad.getContentMimeType(),
            ad.getContentOriginalFileName()
        );
    }

    public FileContentPayload loadVersionContent(Long adVersionId, User user) {
        AdVersion version = adVersionRepository.findById(adVersionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad version not found"));
        ensureReviewAccess(user);
        if (version.getContentStoragePath() == null || version.getContentMimeType() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad version does not have stored binary content");
        }
        return new FileContentPayload(
            fileStorageService.loadAsResource(version.getContentStoragePath()),
            version.getContentMimeType(),
            version.getContentOriginalFileName()
        );
    }

    private void ensureCurrentAdAccess(Ad ad, User user) {
        Role role = ensureKnownRole(user);
        if (role == Role.FESTIVAL_MANAGER || role == Role.FESTIVAL_DIRECTOR) {
            return;
        }
        if (ad.getCurrentPhase().getAssignedRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This ad is not currently assigned to your role");
        }
        if (!isAllowedContentType(role, ad.getAdType().getContentType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This ad content type is not assigned to your role");
        }
    }

    private void ensureReviewAccess(User user) {
        Role role = ensureKnownRole(user);
        if (role != Role.FESTIVAL_MANAGER && role != Role.FESTIVAL_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only festival managers and festival directors can access version files");
        }
    }

    private Role ensureKnownRole(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        return assignment.getRole();
    }

    private boolean isAllowedContentType(Role role, String contentType) {
        Set<String> designerTypes = Set.of("Text", "Image");
        Set<String> supportTypes = Set.of("Audio", "Video");
        return switch (role) {
            case PRODUCT_DESIGNER -> designerTypes.contains(contentType);
            case TECHNICAL_SUPPORT -> supportTypes.contains(contentType);
            default -> false;
        };
    }

    public record FileContentPayload(Resource resource, String mimeType, String originalFileName) {
    }
}
