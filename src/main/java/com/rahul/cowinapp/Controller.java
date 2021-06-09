package com.rahul.cowinapp;

import com.rahul.cowinapp.model.State;
import com.rahul.cowinapp.service.CowinVaccinationSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {

    private String dateFormat = "dd-MM-yyyy";
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
    private List<String> vaccineTypes = Arrays.asList("COVISHIELD", "COVAXIN", "SPUTNIK V");

    @Autowired
    private CowinVaccinationSlotService service;

    @Qualifier("stateMap")
    @Autowired
    Map<String, State> stateMap;

    @Autowired
    BuildProperties buildProperties;

    @GetMapping("/app")
    public ResponseEntity getAppDetails() {
        Map<String, String> response = new HashMap<>();
        response.put("Application name", buildProperties.getName());
        response.put("Application version", buildProperties.getVersion());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/districts")
    public ResponseEntity start(@RequestParam String state) {
        if (state == null || !stateMap.containsKey(state)) {
            return new ResponseEntity("Invalid State name. \nValid values are: " + stateMap.keySet(), HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> response = service.getDistricts(state);
        System.out.println("Returning response ----------- ");
        System.out.println();
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/startProcess")
    public ResponseEntity start(@RequestParam String mobileNumber, @RequestParam(required = false) String pinCode,
            @RequestParam(required = false) Integer districtId, @RequestParam(required = false) Integer center,
            @RequestParam Integer dose, @RequestParam Boolean above45, @RequestParam(required = false) String date,
            @RequestParam(required = false) String vaccine, @RequestBody List<String> beneficiaries) throws Exception {
        ResponseEntity validationResponse = validate(mobileNumber, pinCode, districtId, center, dose, above45,date,vaccine,beneficiaries);
        if (validationResponse != null) {
            return validationResponse;
        }
        service.process(mobileNumber, pinCode, districtId, center, dose, beneficiaries, above45.booleanValue(), date, vaccine);
        System.out.println("Returning response ----------- ");
        System.out.println();
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/centers")
    public ResponseEntity getCenters(@RequestParam(required = false) String pinCode, @RequestParam(required = false) Integer districtId) {
        if (StringUtils.isEmpty(pinCode) && districtId == null) {
            return new ResponseEntity("Either Pin Code or District Id is required", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.isEmpty(pinCode) && districtId != null) {
            return new ResponseEntity("Either Pin Code or District Id should be blank", HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> response = service.getCenters(pinCode, districtId);
        System.out.println("Returning response ----------- ");
        System.out.println();
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @PostMapping("/bookAppointment")
    public ResponseEntity bookAppointment(@RequestParam String mobileNumber, @RequestParam(required = false)  String pinCode,
            @RequestParam(required = false) Integer districtId, @RequestParam(required = false) Integer center,
            @RequestParam Integer dose, @RequestParam Boolean above45, @RequestParam(required = false) String date,
            @RequestParam(required = false) String vaccine, @RequestBody List<String> beneficiaries) throws Exception {
        ResponseEntity validationResponse = validate(mobileNumber,pinCode,districtId,center,dose,above45,date,vaccine,beneficiaries);
        if (validationResponse != null) {
            return validationResponse;
        }
        String response = service.findSlotAndBookAppointment(mobileNumber, pinCode, districtId, center, dose, beneficiaries, above45, date, vaccine);
        System.out.println("Returning response ----------- ");
        System.out.println();
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/beneficiaries")
    ResponseEntity getBeneficiaries(@RequestParam String mobileNumber) throws Exception {
        if (StringUtils.isEmpty(mobileNumber) || mobileNumber.length() != 10) {
            return new ResponseEntity("Invalid Mobile number", HttpStatus.BAD_REQUEST);
        }
        Map<String, Object> response = service.getBeneficiaries(mobileNumber);
        System.out.println("Returning response ----------- ");
        System.out.println();
        return new ResponseEntity(response, HttpStatus.OK);
    }

    private ResponseEntity validate(String mobileNumber, String pinCode, Integer districtId, Integer center, Integer dose, Boolean above45,
                                    String date, String vaccine, List<String> beneficiaries) {
        String errorText = null;
        if (StringUtils.isEmpty(mobileNumber) || mobileNumber.length() != 10) {
            errorText = "Invalid mobile number";
        } else if (StringUtils.isEmpty(pinCode) && districtId == null) {
            errorText = "Either Pin Code or District Id is required";
        } else if (!StringUtils.isEmpty(pinCode) && districtId != null) {
            errorText = "Either Pin Code or District Id should be blank";
        } /*else if (StringUtils.isEmpty(center)) {
            System.out.println("Missing center id");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }*/ else if (CollectionUtils.isEmpty(beneficiaries)) {
            errorText = "Missing beneficiaries";
        } else if (beneficiaries.size()>4) {
            errorText = "Max 4 beneficiaries allowed";
        } else if (dose != 1 && dose != 2) {
            errorText = "Invalid dose";
        } else if (above45 == null) {
            errorText = "Invalid boolean value for above45";
        } else if (dose == 2 && (StringUtils.isEmpty(vaccine) || !vaccineTypes.contains(vaccine.toUpperCase()))) {
            errorText = "Invalid Vaccine";
        } else if (!StringUtils.isEmpty(date)) {
            try {
                LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);
            } catch (Exception e) {
                errorText = "Invalid date format. Correct format is: " + dateFormat;
            }
        }

        if (!StringUtils.isEmpty(errorText)) {
            System.out.println(errorText);
            return new ResponseEntity(errorText, HttpStatus.BAD_REQUEST);
        }
        System.out.println("Mobile: " + mobileNumber);
        System.out.println("Pin Code: " + pinCode);
        System.out.println("Center: " + center);
        System.out.println("Dose: " + dose);
        System.out.println("Above 45: " + above45);
        System.out.println("Beneficiaries: " + beneficiaries);
        System.out.println();
        return null;
    }

}
