package com.rahul.cowinapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Slot {
    private Integer centerId;
    private String centerName;
    private String date;
    private String vaccine;
    private Integer availableCapacity;
    private int dose;
    private Integer minimumAgeLimit;
    private String slot;
    private String sessionId;
    private String feeType;
    private String feeAmount;
}
