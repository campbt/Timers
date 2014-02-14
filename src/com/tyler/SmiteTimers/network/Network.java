package com.tyler.SmiteTimers.network;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;

import com.tyler.SmiteTimers.core.Timer;
public class Network implements Timer.StateChangedListener {
	boolean isServer;
	Server server;
	Client client;
	Writer writer;
	
	public Network(int port, Collection<Timer> timers){
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("networkLog.txt"), "utf-8"));
		} 
		catch (IOException ex) {
		}
		server = new Server(port,timers);
		try {
			writer.write("Running as server\r\n");
			writer.flush();
		}catch(IOException e)
		{
			
		}
		isServer=true;
	}
	public Network(String ip, int port, Collection<Timer> timers){
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("networkLog.txt"), "utf-8"));
		} 
		catch (IOException ex) {
		}
		try {
			writer.write("Running as client\r\n");
			writer.flush();
		}catch(IOException e)
		{
			
		}
		client = new Client(ip, port, timers);
		isServer=false;
	}
	public void sendMessage(Message message)
	{
		if(isServer){
			try{
				writer.write("Having server send message: "+message.id+"\r\n");
				writer.flush();
			}
			catch(IOException e){
				
			}
			server.sendMessage(message,null);
		} else {
			try{
				writer.write("Having client send message: "+message.id+"\r\n");
				writer.flush();
			}
			catch(IOException e){
				
			}
			client.sendMessage(message);
		}
	}
	public boolean isConnected()
	{
		if(this.isServer)
		{
			return true;
		}
		else
		{
			return client.isConnected();
		}
	}
	public int HowManyConnections(){
		if(this.isServer){
			return server.HowManyConnections();
		}
		else
		{
			return (client.isConnected() ? 1:0);
		}
	}
	@Override
	public void stateChanged(Timer timer){
		try{
			writer.write("State change detected\r\n");
			writer.flush();
		}catch(IOException e)
		{
		}
		sendMessage(new Message(timer.getId(), timer.getState(), timer.getTime()));
	}
}
