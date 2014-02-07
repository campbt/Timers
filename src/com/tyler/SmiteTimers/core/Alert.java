package com.tyler.SmiteTimers.core;

import com.tyler.SmiteTimers.core.Timer.TimeUpdatedListener;

/**
 *  Stores a time (time in milli from timer's end that this alert should be triggered
 */
public class Alert implements TimeUpdatedListener {

    AlertTriggeredListener listener;
    long timeToTrigger;

    public Alert(long timeToTrigger, AlertTriggeredListener callback) {
        this.timeToTrigger = timeToTrigger;
        this.listener = callback;
    }

    public void timeUpdated(long timeInMilli) {
        if(timeInMilli == timeToTrigger) {
            // Trigger alert       
            this.listener.alertTriggered(this);
        }
    }

    public interface AlertTriggeredListener {
        public void alertTriggered(Alert alert);
    }

}
