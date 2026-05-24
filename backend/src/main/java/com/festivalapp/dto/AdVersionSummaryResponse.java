package com.festivalapp.dto;

import com.festivalapp.model.AdVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AdVersionSummaryResponse {

    private Integer versionNumber;
    private LocalDate changedAt;
    private boolean current;

    public static AdVersionSummaryResponse from(AdVersion version, boolean current) {
        return new AdVersionSummaryResponse(version.getVersionNumber(), version.getChangedAt(), current);
    }
}
