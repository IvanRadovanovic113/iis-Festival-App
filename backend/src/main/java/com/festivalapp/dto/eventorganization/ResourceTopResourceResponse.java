package com.festivalapp.dto.eventorganization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceTopResourceResponse {

    private String resourceName;
    private long requestCount;
}
