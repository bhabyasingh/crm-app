package com.saurasin.sbtentertainment.backend.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saurasin on 5/20/17.
 */

public class LeadConstants {
    private String location;
    private String source;
    private final Map<String, String> locationToIdMap;
    
    private static volatile LeadConstants instance = new LeadConstants();
    
    private LeadConstants() {
        locationToIdMap = new HashMap<>();
        locationToIdMap.put("Elements - Nagawara", "44");
        locationToIdMap.put("Inorbit - Whitefield", "46");
    }
    
    public static LeadConstants getInstance() {
        return instance;
    }
    
    public void setLocation(final String aLocation) {
        location = aLocation;
    }
    
    public void setSource(final String aSource) {
        source = aSource;
    }
    
    public String getLocationId() {
        return locationToIdMap.get(location);
    }
    
    public String getSource() {
        return source;
    }
}

