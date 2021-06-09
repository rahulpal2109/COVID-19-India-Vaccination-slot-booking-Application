package com.rahul.cowinapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Appointment {

    private Integer center_id;
    private Integer dose;
    private String session_id;
    private String slot;
    private List<String> beneficiaries;
}
