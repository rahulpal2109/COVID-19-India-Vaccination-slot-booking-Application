package com.rahul.cowinapp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.rahul.cowinapp.model.Appointment;
import com.rahul.cowinapp.model.OTPValidationRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static com.rahul.cowinapp.util.Constants.*;

@Service
public class CowinClient {

    @Qualifier("customRestTemplate")
    @Autowired
    RestTemplate customRestTemplate;

    ObjectMapper objectMapper = new ObjectMapper();
    private String accessToken = null;
    private String txnId = null;
    private Instant startTime = null;

    public Map<String, Object> getSlots(String pinCode, Integer districtId, String date, boolean retry) {
        Map<String, Object> data = null;
        StringBuilder getSlotsUrl = new StringBuilder();
        try {
            getSlotsUrl.append(GET_SLOTS_URL);
            if (StringUtils.isEmpty(pinCode)) {
                getSlotsUrl.append("District?district_id=").append(districtId);
            } else {
                getSlotsUrl.append("Pin?pincode=").append(pinCode);
            }
            getSlotsUrl.append("&date=").append(date);
            //System.out.println("seng request to: " + getSlotsUrl);

            HttpEntity<String> entity = new HttpEntity<>("body", getHttpHeaders());

            ResponseEntity<String> response = customRestTemplate.exchange(getSlotsUrl.toString(), HttpMethod.GET,
                    entity, String.class);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Received data from COWIN");
                data = getObjectMapFromString(response);
                return data;
            } else {
                System.out.println("Failed to receive data from COWIN");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> getObjectMapFromString(ResponseEntity<String> response) throws JsonProcessingException {
        Map<String, Object> data;
        String responseBody = response.getBody();
        final MapType type = objectMapper.getTypeFactory().constructMapType(
                Map.class, String.class, Object.class);

        data = objectMapper.readValue(responseBody, type);
        return data;
    }

    public Map<String, Object> getBeneficiaries(String mobile, boolean retry) {
        Map<String, Object> data = null;
        //System.out.println("getBeneficiaries(): retry: " + retry);
        //System.out.println("send request to: " + BENEFICIARY_URL);
        try {
            HttpHeaders headers = getHttpHeaders();
            headers.set(AUTH, BEARER + getAccessToken(mobile));

            HttpEntity<String> entity = new HttpEntity<>("body", headers);
            ResponseEntity<String> response = customRestTemplate.exchange(BENEFICIARY_URL, HttpMethod.GET,
                    entity, String.class);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Received data from COWIN for beneficiaries");
                return getObjectMapFromString(response);
            } else {
                System.out.println("Failed to receive data from COWIN");
            }
        } catch (HttpClientErrorException e) {
            System.out.println("HttpClientErrorException");
            if (retry) {
                resetToken();
                getBeneficiaries(mobile, false);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    public void authenticateUser(String mobile) throws Exception{
        //System.out.println("authenticateUser()");
        resetToken();
        String txnId = requestOTP(mobile);
        if (StringUtils.isEmpty(txnId)) {
            txnId = this.txnId;
        }
        String token = getToken(txnId);
        if (!StringUtils.isEmpty(token)) {
            this.accessToken = token;
            this.txnId = txnId;
            this.startTime = Instant.now();
        } else {
            System.out.println("Authentication error");
            throw new Exception("Authentication error");
        }
    }

    private String getToken(String txnId) throws Exception {
        System.out.println("getToken(): txnId: " + txnId);
        String otp = getOTP();
        System.out.println("send request to Validate OTP: ");

        String otpHash = null;
        try {
            otpHash = genHash(otp);
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }

        if (StringUtils.isEmpty(otpHash)) {
            System.out.println("Error: generating otp hash. Terminating");
            return Strings.EMPTY;
        }

        OTPValidationRequest otpValidationRequest = new OTPValidationRequest(otpHash, txnId);
        String requestBody = objectMapper.writeValueAsString(otpValidationRequest);
        //System.out.println("requestBody: " + requestBody);

        HttpEntity entity = new HttpEntity(requestBody, getHttpHeaders());
        ResponseEntity<String> response = customRestTemplate.exchange(CONFIRM_OTP_URL, HttpMethod.POST, entity, String.class);
        if (response != null && response.getStatusCode().is2xxSuccessful()) {
            System.out.println("OTP validation successful.");
            return (String) getObjectMapFromString(response).get("token");
        } else {
            System.out.println("Failed to validate for OTP");
        }

        return Strings.EMPTY;
    }

    private String requestOTP(String mobile) {
        System.out.println("requestOTP(): " + mobile);
        try {
            System.out.println("send request to Generate OTP: ");

            String requestBody = "{\"mobile\":\"" + mobile +"\",\"secret\":\"" + SECRET + "\"}";
            //System.out.println("requestBody: " + requestBody);
            HttpEntity<String> entity = new HttpEntity<String>(requestBody, getHttpHeaders());

            ResponseEntity<String> response = customRestTemplate.exchange(REQUEST_OTP_URL, HttpMethod.POST, entity, String.class);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return (String) getObjectMapFromString(response).get("txnId");
            } else {
                System.out.println("Failed to request for OTP");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }

        return Strings.EMPTY;
    }

    public String getAccessToken(String mobile) throws Exception {
        if (!StringUtils.isEmpty(this.accessToken) && !sessionTimeout()) {
            return this.accessToken;
        }
        System.out.println("No access token found");
        authenticateUser(mobile);
        return this.accessToken;
    }

    private boolean sessionTimeout() {
        if (this.startTime == null) {
            resetToken();
            return true;
        }
        long timeElapsed = Duration.between(this.startTime, Instant.now()).toMinutes();
        if (timeElapsed > SESSION_TIMEOUT_MINS) {
            System.out.println("Session timeout. New session required.");
            resetToken();
            return true;
        }
        //System.out.println("Using existing session.");
        return false;
    }

    private String getOTP() {
        String otp = null;
        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            System.out.println("Enter OTP: ");
            otp = br.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }
        System.out.println("Returning OTP: " + otp);
        return otp;
    }

    public String genHash(String input)throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes());
        byte byteData[] = md.digest();
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public String bookAppointment(Appointment appointment, String mobile, boolean retry) throws Exception {
        //System.out.println("bookAppointment(): appointment: " + appointment);
        //System.out.println("send request to: " + BOOK_SLOT);
        try {
            HttpHeaders headers = getHttpHeaders();
            headers.set(AUTH, BEARER + getAccessToken(mobile));

            String requestBody = objectMapper.writeValueAsString(appointment);
            //System.out.println("requestBody: " + requestBody);

            HttpEntity<String> entity = new HttpEntity<String>(requestBody, headers);
            ResponseEntity<String> response = customRestTemplate.exchange(BOOK_SLOT, HttpMethod.POST, entity, String.class);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                return (String) getObjectMapFromString(response).get("appointment_confirmation_no");
            } else {
                System.out.println("Failed to book appointment.");
            }
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
            if (e.getMessage().contains(BOOKED_ERROR_MSG_COWIN)) {
                System.out.println(BOOKED_ERROR_MSG);
            } else if (e.getMessage().contains(DOSE2_ERROR_MSG_COWIN)) {
                System.out.println(DOSE2_ERROR_MSG);
            } else if (e.getMessage().contains(PAST_SESSION_MSG_COWIN)) {
                System.out.println(PAST_SESSION_MSG);
            } else if (e.getMessage().contains(INVALID_AGE_MSG_COWIN)) {
                System.out.println(INVALID_AGE_MSG);
            } else if (e.getMessage().contains(APPT_EXISTS_MSG_COWIN)) {
                System.out.println(APPT_EXISTS_MSG);
            } else if (e.getMessage().contains(INVALID_DATE_MSG_COWIN)) {
                System.out.println(INVALID_DATE_MSG);
            } else if (retry) {
                resetToken();
                bookAppointment(appointment, mobile, false);
            } else if (e.getMessage().contains(INVALID_OTP_MSG_COWIN)) {
                System.out.println(INVALID_OTP_MSG);
            } else {
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
            throw e;
        }
        return Strings.EMPTY;
    }

    private HttpHeaders getHttpHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", USER_AGENT);
        headers.set("Origin", "https://selfregistration.cowin.gov.in");
        headers.set("Referer", "https://selfregistration.cowin.gov.in");
        return headers;
    }

    public Map<String, Object> getDistricts(int stateId) {
        Map<String, Object> data = null;
        try {
            HttpEntity<String> entity = new HttpEntity<>("body", getHttpHeaders());

            ResponseEntity<String> response = customRestTemplate.exchange(GET_DISTRICTS + stateId, HttpMethod.GET,
                    entity, String.class);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Received data from COWIN");
                data = getObjectMapFromString(response);
                return data;
            } else {
                System.out.println("Failed to receive data from COWIN");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    private void resetToken() {
        System.out.println("Invalidating session.");
        this.startTime = null;
        this.accessToken = null;
        this.txnId = null;
    }
}
