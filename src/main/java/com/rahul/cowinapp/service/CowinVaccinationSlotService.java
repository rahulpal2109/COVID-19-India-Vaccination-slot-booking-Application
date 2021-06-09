package com.rahul.cowinapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.cowinapp.client.CowinClient;
import com.rahul.cowinapp.model.*;
import com.rahul.cowinapp.util.Constants;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@EnableAsync
public class CowinVaccinationSlotService {

    private String dateFormat = "dd-MM-yyyy";
    private String defpinCode = "700091";
    private String[] defBeneficiaries = {"94408057168110", "23741763327200", "99605014970780"};
    private String date = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    CowinClient cowinClient;

    @Qualifier("stateMap")
    @Autowired
    Map<String, State> stateMap;

    @Async
    public void process(String mobile, String pinCode, Integer districtId, Integer centerId, Integer dose, List<String> beneficiaries,
                        Boolean above45, String inputDate, String vaccine) throws Exception {
        System.out.println("Processing start: " + LocalDateTime.now());
        int count = 1;
        while(true) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
            }
            List<Center> centerList = getCentersFromCowin(pinCode, districtId, inputDate);
            List<Slot> slotList = getSlots(centerList, centerId, beneficiaries.size(), vaccine, inputDate, above45, dose);
            if (CollectionUtils.isEmpty(slotList)) {
                System.out.println("No slots found at: " + LocalDateTime.now());
                System.out.println();
                continue;
            }
            //System.out.println("Slots available at center: " + center);
            String response = selectSlot(mobile, dose, beneficiaries, true, slotList);
            if (!StringUtils.isEmpty(response)) {
                System.out.println(response);
                if (response.contains("Appointment booked successfully.")) {
                    System.out.println("----------- ");
                    break;
                }
                System.out.println();
            }
        }
    }

    public Map<String, Object> getCenters(String pinCode, Integer districtId) {
        return cowinClient.getSlots(pinCode, districtId, date, true);
    }

    public Map<String, Object> getBeneficiaries(String mobileNumber) throws Exception {
        Map<String, Object> beneficiaries = cowinClient.getBeneficiaries(mobileNumber, true);
        return beneficiaries;
    }

    private Center getCenter(Integer centerId, List<Center> centers) {
        //System.out.println("getCenter() - centerId: " + centerId);
        for (Center center: centers) {
            if (center.getCenter_id().intValue() == centerId.intValue()){
                return center;
            }
        }
        return null;
    }

    private Slot getSlotFromUserInput(List<Slot> slotList) throws Exception{
        if (CollectionUtils.isEmpty(slotList)) {
            return null;
        }
        Slot selectedSlot = null;
        System.out.println();
        System.out.println("Following Vaccination slot(s) is/are available - ");
        int index = 1;
        for (Slot slot: slotList) {
            System.out.println((index++) + " -> " + slot.getCenterName() + " , on: " + slot.getDate() + " , vaccine: "
                    + slot.getVaccine() + " , fee amount: " + slot.getFeeAmount());
        }
        System.out.println();
        System.out.println("Press Row Number and ENTER to book a slot. \nElse, press ENTER to skip.");
        System.out.println("Your response: ");
        try {
            String input = getUserInput();
            if (!StringUtils.isEmpty(input)) {
                index = Integer.parseInt(input);
                selectedSlot = slotList.get(index-1);
                System.out.println("Selected slot: " + selectedSlot.getCenterName() + " on " + selectedSlot.getDate() );
            } else {
                System.out.println("No valid input. Skipping.");
            }
        } catch (Exception e) {
            System.out.println("No valid input. Skipping.");
        }
        return selectedSlot;
    }

    private String getUserInput() throws IOException {
        String response = Strings.EMPTY;
        int waitSecs = Constants.USER_INPUT_WAIT_SECONDS; // wait x seconds at most

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < waitSecs * 1000 && !br.ready()) {
        }
        if (br.ready()) {
            response = br.readLine();
            System.out.println("You entered: " + response);
        } else {
            System.out.println("You did not enter data");
        }
        return response;
    }

    private void soundAlert() {
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(new ClassPathResource("./bell_notif.wav").getURL())) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            //clip.start();
            clip.loop(4);
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass() + " - " + e.getMessage());
        }
    }

    public String findSlotAndBookAppointment(String mobile, String pinCode, Integer districtId, Integer centerId,
            Integer dose, List<String> beneficiaries, Boolean above45, String inputDate, String vaccine) throws  Exception {
        System.out.println("bookAppointment start: " + LocalDateTime.now());
        List<Center> centersFromCowin = getCentersFromCowin(pinCode, districtId, inputDate);
        if (CollectionUtils.isEmpty(centersFromCowin)) {
            return "No centers or slots found.";
        }
        List<Slot> slotList = getSlots(centersFromCowin, centerId, beneficiaries.size(), vaccine, inputDate, above45, dose);
        String response = selectSlot(mobile, dose, beneficiaries, false, slotList);
        System.out.println(response);
        return response;
    }

    private List<Center> getCentersFromCowin(String pinCode, Integer districtId, String inputDate) throws IOException {
        List<Center> centers = Collections.emptyList();
        String startDate;
        if (StringUtils.isEmpty(inputDate)) {
            startDate = date;
        } else {
            startDate = inputDate;
        }
        Map<String, Object> slotResponse = cowinClient.getSlots(pinCode, districtId, startDate, true);

        /*ObjectMapper mapper = new ObjectMapper();
        final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        Map<String, Object> slotResponse = mapper.readValue(new ClassPathResource("./center.json").getFile(), type);*/

        if (!CollectionUtils.isEmpty(slotResponse)) {
            centers = mapper.convertValue(slotResponse.get("centers"), new TypeReference<List<Center>>() { });
        }
        return centers;
    }

    private String selectSlot(String mobile, Integer dose, List<String> beneficiaries, Boolean notify,
                              List<Slot> slotList) throws Exception {
        String response;
        Slot slot = null;
        if (CollectionUtils.isEmpty(slotList)) {
            response = "No centers or slots found.";
        } else {
            if (notify) {
                soundAlert();
            }
            if (slotList.size() > 1) {
                slot = getSlotFromUserInput(slotList);
            } else {
                slot = slotList.get(0);
            }
            response = createAppointment(slot, mobile, dose, beneficiaries);
        }
        return response;
    }

    private List<Slot> getSlots(List<Center> centerList, Integer centerId, int beneficiaryCount, String vaccine,
                                String inputDate, Boolean above45, Integer dose) {
        List<Slot> slots = new ArrayList<>();
        if (CollectionUtils.isEmpty(centerList)) {
            return slots;
        }
        if (!ObjectUtils.isEmpty(centerId)) {
            Center chosenCenter = getCenter(centerId, centerList);
            if (chosenCenter != null) {
                List<Slot> slotList = getSlotsFromCenterResponse(chosenCenter, centerId, beneficiaryCount, vaccine, inputDate, above45, dose);
                if (!CollectionUtils.isEmpty(slotList)) {
                    return slotList;
                }
            }
        }
        for (Center center: centerList) {
            slots.addAll(getSlotsFromCenterResponse(center, centerId, beneficiaryCount, vaccine, inputDate, above45, dose));
        }
        return slots;
    }

    private List<Slot> getSlotsFromCenterResponse(Center center, Integer centerId, int beneficiaryCount, String vaccine,
                                String inputDate, Boolean above45, int dose) {
        List<Slot> slotList = new ArrayList<>();
        if (center != null && !CollectionUtils.isEmpty(center.getSessions())) {
            for (Session session: center.getSessions()) {
                if (session.getAvailable_capacity().intValue() == 0) {
                    continue;
                }
                if (isValidSession(session, dose, above45, beneficiaryCount, vaccine)) {
                    List<String> availableSlots = session.getSlots();
                    if (CollectionUtils.isEmpty(availableSlots)) {
                        continue;
                    }
                    Slot slot = new Slot(center.getCenter_id(), center.getName(), session.getDate(), session.getVaccine(),
                            session.getAvailable_capacity(), dose, session.getMin_age_limit(),
                            availableSlots.get(availableSlots.size()-1), session.getSession_id(), center.getFee_type(), "0");
                    if (!CollectionUtils.isEmpty(center.getVaccine_fees()) && center.getVaccine_fees().get(0) != null) {
                        slot.setFeeAmount(center.getVaccine_fees().get(0).getFee());
                    }
                    if (!StringUtils.isEmpty(inputDate) && session.getDate().equals(inputDate) &&
                            centerId != null && centerId.intValue() == center.getCenter_id().intValue()) {
                        return Collections.singletonList(slot);
                    }
                    slotList.add(slot);
                }
            }
        }
        return slotList;
    }

    private boolean isValidSession(Session session, int dose, Boolean above45, int beneficiaryCount, String vaccine) {
        if (session == null) {
            return false;
        }
        if (session.getAvailable_capacity() != null && session.getAvailable_capacity() >= beneficiaryCount) {
            if (above45 != null) {
                if (above45) {
                    if (session.getMin_age_limit().intValue() != 45) {
                        return false;
                    }
                } else  {
                    if (session.getMin_age_limit().intValue() == 45) {
                        return false;
                    }
                }
            }

            if (!StringUtils.isEmpty(vaccine) && !vaccine.equalsIgnoreCase(session.getVaccine())) {
                return false;
            }

            if (dose == 1) {
                if (session.getAvailable_capacity_dose1() != null && session.getAvailable_capacity_dose1().intValue() >= beneficiaryCount) {
                    return true;
                }
            } else if (dose == 2) {
                if (session.getAvailable_capacity_dose2() != null && session.getAvailable_capacity_dose2().intValue() >= beneficiaryCount) {
                    return true;
                }
            }
        }
        return false;
    }

    private String createAppointment(Slot slot, String mobile, Integer dose, List<String> beneficiaries) throws Exception {
        if (slot == null) {
            return "No slot found for the given center/session.";
        }

        String response;
        String appointmentId = bookAppointment(mobile, slot.getCenterId(), dose, beneficiaries, slot.getSessionId(), slot.getSlot());
        if (!StringUtils.isEmpty(appointmentId)) {
            response = "Appointment booked successfully. Id: " + appointmentId;
        } else {
            response = "Appointment booking failed.";
        }
        return response;
    }

    private String bookAppointment(String mobile, Integer centerId, Integer dose, List<String> beneficiaries,
                                   String sessionId, String slot) throws Exception {
        Appointment appointment = new Appointment(centerId, dose, sessionId, slot, beneficiaries);
        System.out.println("Requesting to book appointment: " + appointment);
        return cowinClient.bookAppointment(appointment, mobile, true);
    }

    public Map<String, Object> getDistricts(String stateName) {
        State state = stateMap.get(stateName);
        if (state != null) {
            return cowinClient.getDistricts(state.getState_id());
        } else {
            return Collections.emptyMap();
        }
    }
}
