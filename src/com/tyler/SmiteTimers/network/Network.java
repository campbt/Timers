package com.tyler.SmiteTimers.network;

import java.util.Collection;

import com.tyler.SmiteTimers.core.Timer;
public class Network implements Timer.StateChangedListener {
	boolean isServer;
	Server server;
	Client client;
	
	public Network(int port, Collection<Timer> timers){
		server = new Server(port,timers);
		isServer=true;
	}
	public Network(String ip, int port, Collection<Timer> timers){
		client = new Client(ip, port, timers);
		isServer=false;
	}
	public void sendMessage(Message message)
	{
		if(isServer){
			server.sendMessage(message);
		} else {
			client.sendMessage(message);
		}
	}

	@Override
	public void stateChanged(Timer timer){
		sendMessage(new Message(timer.getId(), timer.getState(), timer.getTime()));
	}
}
