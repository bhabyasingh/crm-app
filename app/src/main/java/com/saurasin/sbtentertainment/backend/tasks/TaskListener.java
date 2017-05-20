/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

/**
 * Created by saurasin on 5/12/17.
 */

public interface TaskListener {
    void onTaskResult(boolean status);
}
