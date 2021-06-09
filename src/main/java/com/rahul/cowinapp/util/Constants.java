package com.rahul.cowinapp.util;

public class Constants {

    public static final long SESSION_TIMEOUT_MINS = 15L;
    public static final int USER_INPUT_WAIT_SECONDS = 20;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36";
    public static final String SECRET = "U2FsdGVkX19QcxyXVDgIUaGUc93t/JuByP0f1pUMJJ4DK1iFFS4UAwYss0wzRQdwo+aWvTgRBK7TKXdEwnVjSA==";
    public static final String GET_DISTRICTS = "https://cdn-api.co-vin.in/api/v2/admin/location/districts/";
    public static final String GET_SLOTS_URL = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarBy";
    public static final String REQUEST_OTP_URL = "https://cdn-api.co-vin.in/api/v2/auth/generateMobileOTP";
    public static final String CONFIRM_OTP_URL = "https://cdn-api.co-vin.in/api/v2/auth/validateMobileOtp";
    //public static final String REQUEST_OTP_URL = "https://cdn-api.co-vin.in/api/v2/auth/generateOTP";
    //public static final String CONFIRM_OTP_URL = "https://cdn-api.co-vin.in/api/v2/auth/confirmOTP";
    public static final String BOOK_SLOT = "https://cdn-api.co-vin.in/api/v2/appointment/schedule";
    public static final String BENEFICIARY_URL = "https://cdn-api.co-vin.in/api/v2/appointment/beneficiaries";

    public static final String AUTH = "Authorization";
    public static final String BEARER = "Bearer ";

    public static final String BOOKED_ERROR_MSG_COWIN = "center is completely booked";
    public static final String BOOKED_ERROR_MSG = "Appointment booking failed as vaccination center is completely booked";
    public static final String DOSE2_ERROR_MSG_COWIN = "must be vaccinated with first dose";
    public static final String DOSE2_ERROR_MSG = "Appointment booking failed as Dose 1 is still pending.";
    public static final String PAST_SESSION_MSG_COWIN = "vaccination session is already complete";
    public static final String PAST_SESSION_MSG = "Appointment booking failed as vaccination session is already complete.";
    public static final String INVALID_AGE_MSG_COWIN = "Minimum age criteria is 45 years";
    public static final String INVALID_AGE_MSG = "Appointment booking failed as Minimum age criteria is 45 years for this center.";
    public static final String INVALID_OTP_MSG_COWIN = "Invalid OTP";
    public static final String INVALID_OTP_MSG = "Invalid OTP";
    public static final String APPT_EXISTS_MSG_COWIN = "An active first dose appointment already exists for";
    public static final String APPT_EXISTS_MSG = "Appointment booking failed as an active first dose appointment already exists.";
    public static final String INVALID_DATE_MSG_COWIN = "Appointments are allowed only for future dates.";
    public static final String INVALID_DATE_MSG = "Appointment booking failed as appointments are allowed only for future dates.";


}
