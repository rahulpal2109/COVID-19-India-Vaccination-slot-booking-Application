package com.rahul.cowinapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Center {

    private Integer center_id;
    private String name;
    private String address;
    private Integer pincode;
    private String fee_type;
    private List<Session> sessions;
    private List<VaccineFees> vaccine_fees;
}
