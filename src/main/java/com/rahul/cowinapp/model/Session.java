package com.rahul.cowinapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {

    private String session_id;
    private String date;
    private Integer available_capacity;
    private Integer available_capacity_dose1;
    private Integer available_capacity_dose2;
    private Integer min_age_limit;
    private String vaccine;
    private List<String> slots;


}
