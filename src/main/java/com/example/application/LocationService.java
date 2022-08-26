package com.example.application;

import com.vaadin.flow.component.map.configuration.Coordinate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class LocationService {

    private HashMap<String, Coordinate> userLocations = new HashMap<>();

    public void updateLocation(String id, Coordinate coordinate) {
        userLocations.put(id, coordinate);
    }

    public Map<String, Coordinate> getLocations() {
        return Collections.unmodifiableMap(userLocations);
    }

    public void clear(String myId) {
        userLocations.remove(myId);
    }
}
