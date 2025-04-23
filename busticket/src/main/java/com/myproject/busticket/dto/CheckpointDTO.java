package com.myproject.busticket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckpointDTO {
    private int checkpointId;
    private String placeName;
    private String address;
    private String province;
    private String city;
    private String region;
    private String phone;
}
