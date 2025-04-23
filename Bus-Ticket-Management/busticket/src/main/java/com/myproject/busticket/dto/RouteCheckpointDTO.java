package com.myproject.busticket.dto;

import com.myproject.busticket.enums.CheckpointType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteCheckpointDTO {
    private int checkpointId;
    private String placeName;
    private String address;
    private String province;
    private String city;
    private int checkpointOrder;
    private CheckpointType checkpointType;
}
