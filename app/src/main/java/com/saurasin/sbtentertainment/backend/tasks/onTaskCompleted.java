package com.saurasin.sbtentertainment.backend.tasks;

/**
 * Created by saurasin on 5/26/17.
 */

public interface onTaskCompleted<Result> {
    void onTaskCompleted(Result result);
}
