package com.rahul.cowinapp.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.rahul.cowinapp.model.State;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfiguration {

    private final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

    @Bean(name="customRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate(getRequestFactory());
    }

    private ClientHttpRequestFactory getRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setReadTimeout(TIMEOUT);
        factory.setConnectTimeout(TIMEOUT);
        factory.setConnectionRequestTimeout(TIMEOUT);
        return factory;
    }

    @Bean(name="stateMap")
    public Map<String, State> getStateMap() throws IOException {
        Map<String, State> stateMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        final MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        Map<String, Object> data = mapper.readValue(new ClassPathResource("states.json").getInputStream(), type);
        List<State> stateList = mapper.convertValue(data.get("states"), new TypeReference<List<State>>() { });
        if (!CollectionUtils.isEmpty(stateList)) {
            for (State state: stateList) {
                stateMap.put(state.getState_name(), state);
            }
        }
        return stateMap;
    }
}
