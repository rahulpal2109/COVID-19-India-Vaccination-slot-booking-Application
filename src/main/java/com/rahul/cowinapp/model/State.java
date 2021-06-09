package com.rahul.cowinapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class State {
    private int state_id;
    private String state_name;
}
