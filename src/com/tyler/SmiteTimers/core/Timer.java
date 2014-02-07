package com.tyler.SmiteTimers.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic timers that can be passed Alerts and will trigger alerts when the timer is reached
 * Operates on seconds
 */
public class Timer {

    private static final long DEFAULT_ALERT_INCREMENT = 1000; // 1 seconds

    final long timerLength; // Duration of timer in milliseconds
    long time; // Current amount of time left on timer
    long alertIncrement; // Amount of time to wait in between TimeUpdated calls

    Set<TimeUpdatedListener> timeUpdatedListeners = new HashSet<TimeUpdatedListener>();
    TimerThread timerThread = new TimerThread();

    public Timer(long time) {
        this(time, DEFAULT_ALERT_INCREMENT);
    }

    public Timer(long time, long alertIncrement) {
        assert(time % alertIncrement == 0); //  The alert increment needs to evenly go into the total time of the timer

        this.timerLength = time;
        this.time = time;
        this.alertIncrement = alertIncrement;
    }

    public void start() {
        this.timerThread.start();
    }

    public void reset() {
        time = timerLength;
        this.timerThread.turnOff();
    }

    public void addAlert(Alert alert) {
        this.addTimeUpdatedListener(alert);
    }

    public void alertListners() {
        for(TimeUpdatedListener listener: this.timeUpdatedListeners) {
            listener.timeUpdated(this.time);
        }
    }

    public void addTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListeners.add(listener);
    }

    public void removeTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListeners.remove(listener);
    }

    public interface TimeUpdatedListener {
        public void timeUpdated(long timeInMilli);
    }

    private class TimerThread extends Thread {
        private boolean running = true;

        public void turnOff() {
            this.running = false;
        }

        @Override
        public void run() {
            this.running = true;
            while(this.running && time > 0) {
                try {
                    Thread.sleep(Timer.this.alertIncrement);
                } catch(InterruptedException e) {

                }
                // Update time and alert listeners that has a change has occured
                Timer.this.time -= Timer.this.alertIncrement;
                Timer.this.alertListners();
            }
        }

    }

}
