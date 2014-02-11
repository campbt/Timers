package com.tyler.SmiteTimers.network;

import java.util.Collection;

import com.tyler.SmiteTimers.core.Timer;
public class Network implements Timer.ToggleListener{
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
	public void sendReset(int y)
	{
		if(isServer){
			server.sendReset2(y);
		}
		else{
			client.sendReset(y);
		}
	}

	@Override
	public void timeToggled(int id){
		sendReset(id);
	}
}