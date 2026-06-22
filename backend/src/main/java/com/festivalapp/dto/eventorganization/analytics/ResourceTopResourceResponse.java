package com.festivalapp.dto.eventorganization.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceTopResourceResponse {

    private String resourceName;
    private long requestCount;
}
