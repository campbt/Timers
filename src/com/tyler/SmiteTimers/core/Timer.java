package com.tyler.SmiteTimers.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic timers that can be passed Alerts and will trigger alerts when the timer is reached
 * Operates on seconds
 */
public class Timer {

    private static final long DEFAULT_ALERT_INCREMENT = 1000; // 1 seconds
    private static final int STATE_STOPPED = 0;
    private static final int STATE_COUNTING_DOWN = 1;

    private final long timerLength; // Duration of timer in milliseconds
    private long time; // Current amount of time left on timer
    private long alertIncrement; // Amount of time to wait in between TimeUpdated calls

    private int id;
    private int state;

    private Set<TimeUpdatedListener> timeUpdatedListeners = new HashSet<TimeUpdatedListener>();
    private Set<StateChangedListener> toggleListeners = new HashSet<StateChangedListener>();
    private TimerThread timerThread = new TimerThread();

    public Timer(long time) {
        this(time, DEFAULT_ALERT_INCREMENT);
    }

    public Timer(long time, long alertIncrement) {
        assert(time % alertIncrement == 0); //  The alert increment needs to evenly go into the total time of the timer

        this.timerLength = time;
        this.time = time;
        this.alertIncrement = alertIncrement;
    }

    /**
     * Starts timer if it is stopped and resets timer if it is running
     */
    public void toggle() {
        if(this.state == Timer.STATE_STOPPED) {
            this.setState(Timer.STATE_COUNTING_DOWN);
        } else {
            this.setState(Timer.STATE_STOPPED);
        }
        alertStateChangedListeners();
    }

    public void start() {
        this.timerThread.start();
    }

    public void reset() {
        this.time = timerLength;
        this.timerThread.turnOff();

        alertListners();
        this.timerThread = new TimerThread();
    }

    public void addAlert(Alert alert) {
        this.addTimeUpdatedListener(alert);
        alert.timeUpdated(this.time);
    }

    public void alertListners() {
        for(TimeUpdatedListener listener: this.timeUpdatedListeners) {
            listener.timeUpdated(this.time);
        }
    }
    
    public void alertStateChangedListeners() {
        for(StateChangedListener listener: this.toggleListeners) {
            listener.stateChanged(this);
        }
    }

    public long getTime() {
        return time;
    }

    public void setTime(long timeInMilli) {
        this.time = timeInMilli;
        alertListners();
    }

    public long getTimerLength() {
        return timerLength;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        if(!(this.state==state))
        {
        	this.state = state;
        	if(this.state == STATE_COUNTING_DOWN) {
        		this.start();
        	} else {
        		this.reset();
        	}
        	//alertStateChangedListeners();
        }
    }

    public void addTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListeners.add(listener);
    }

    public void removeTimeUpdatedListener(TimeUpdatedListener listener) {
        timeUpdatedListeners.remove(listener);
    }
    
    public void addStateChangedListener(StateChangedListener listener) {
        toggleListeners.add(listener);
    }

    public void removeStateChangedListener(StateChangedListener listener) {
        toggleListeners.remove(listener);
    }

    public interface TimeUpdatedListener {
        public void timeUpdated(long timeInMilli);
    }
    public interface StateChangedListener {
        public void stateChanged(Timer timer);
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
                // Update time and alert listeners that has a change has occurred
                if(this.running) {
                    Timer.this.time -= Timer.this.alertIncrement;
                    Timer.this.alertListners();
                }
                try {
                    Thread.sleep(Timer.this.alertIncrement);
                } catch(InterruptedException e) {

                }
            }
        }

    }

}
