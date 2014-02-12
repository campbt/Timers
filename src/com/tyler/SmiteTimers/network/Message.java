package com.tyler.SmiteTimers.network;

/**
 * Data class that stores info that comes in from messages
 */
public class Message {
    int id;
    int state;
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
        //this.id=id-1;
    }

}
