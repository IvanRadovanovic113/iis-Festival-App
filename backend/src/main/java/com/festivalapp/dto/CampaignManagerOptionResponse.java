package com.festivalapp.dto;

import com.festivalapp.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampaignManagerOptionResponse {

    private Long userId;
    private String username;
    private String email;

    public static CampaignManagerOptionResponse from(User user) {
        CampaignManagerOptionResponse response = new CampaignManagerOptionResponse();
        response.userId = user.getId();
        response.username = user.getUsername();
        response.email = user.getEmail();
        return response;
    }
}
