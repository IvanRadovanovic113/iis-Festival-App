package com.festivalapp.controller;

import com.festivalapp.model.User;
import com.festivalapp.service.AdContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ad-content")
@RequiredArgsConstructor
public class AdContentController {

    private final AdContentService adContentService;

    @GetMapping("/ads/{adId}")
    public ResponseEntity<Resource> getCurrentAdContent(
        @PathVariable Long adId,
        @AuthenticationPrincipal User user
    ) {
        AdContentService.FileContentPayload payload = adContentService.loadCurrentAdContent(adId, user);
        return buildResourceResponse(payload);
    }

    @GetMapping("/ad-versions/{adVersionId}")
    public ResponseEntity<Resource> getVersionContent(
        @PathVariable Long adVersionId,
        @AuthenticationPrincipal User user
    ) {
        AdContentService.FileContentPayload payload = adContentService.loadVersionContent(adVersionId, user);
        return buildResourceResponse(payload);
    }

    private ResponseEntity<Resource> buildResourceResponse(AdContentService.FileContentPayload payload) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (payload.mimeType() != null && !payload.mimeType().isBlank()) {
            mediaType = MediaType.parseMediaType(payload.mimeType());
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline().filename(payload.originalFileName() == null ? "content.bin" : payload.originalFileName()).build().toString()
            )
            .body(payload.resource());
    }
}
