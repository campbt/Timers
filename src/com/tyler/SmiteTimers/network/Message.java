package com.tyler.SmiteTimers.network;

/**
 * Data class that stores info that comes in from messages
 */
public class Message {
    byte actionToPerform;
	int id;
    int state;
    long initialTime;
    long time;
    String ip;
    
    public Message(int id, int state, long time) {
        this.id = id;
        this.state = state;
        this.time = time;
    }

    public Message(int id, int state, long time, String ip) {
        this.id = id;
        this.state = state;
        this.time = time;
        this.ip = ip;
    }
    public Message(byte actionToPerform, int id, int state, long time, String ip) {
        this.actionToPerform = actionToPerform;
    	this.id = id;
        this.state = state;
        this.time = time;
        this.ip = ip;
    }
    public Message(byte actionToPerform, int id, int state, long initialTime, long time) {
        this.actionToPerform = actionToPerform;
    	this.id = id;
        this.state = state;
        this.initialTime = initialTime;
        this.time = time;
    }
    public Message(byte actionToPerform, int id, int state, long initialTime, long time, String ip) {
        this.actionToPerform = actionToPerform;
    	this.id = id;
        this.state = state;
        this.initialTime = initialTime;
        this.time = time;
        this.ip = ip;
    }

}
