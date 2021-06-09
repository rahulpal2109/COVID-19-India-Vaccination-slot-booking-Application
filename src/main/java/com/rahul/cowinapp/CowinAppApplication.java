package com.rahul.cowinapp;

import com.rahul.cowinapp.service.CowinVaccinationSlotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CowinAppApplication {

    static CowinVaccinationSlotService service = new CowinVaccinationSlotService();

    public static void main(String[] args) {
        SpringApplication.run(CowinAppApplication.class, args);
    }

}
