/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.model;

/**
 * Created by saurasin on 1/26/17.
 */
public class ChildEntry {
    private String name;
    private String dob;
    
    public ChildEntry(final String n, final String d) {
        this.name = n;
        this.dob = d;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDOB() {
        return dob;
    }
}
