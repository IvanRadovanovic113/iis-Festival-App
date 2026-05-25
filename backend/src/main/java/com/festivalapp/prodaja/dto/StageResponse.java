package com.festivalapp.prodaja.dto;

import com.festivalapp.model.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StageResponse {

    private Long stageId;
    private String name;
    private Integer capacity;
    private String location;
    private Long festivalId;
    private String festivalName;

    public static StageResponse from(Stage stage) {
        StageResponse r = new StageResponse();
        r.stageId = stage.getStageId();
        r.name = stage.getName();
        r.capacity = stage.getCapacity();
        r.location = stage.getLocation();
        r.festivalId = stage.getFestival().getFestivalId();
        r.festivalName = stage.getFestival().getName();
        return r;
    }
}
