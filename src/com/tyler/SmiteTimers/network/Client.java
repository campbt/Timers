package com.tyler.SmiteTimers.network;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;
public class Client {
	private static final byte RESETTIMER=1;
	private static final byte SENDMESSAGE=1;
	private static final byte HEARTBEAT=2;
	private static final byte BUILDTIMERLIST = 3;
	//private static final byte RECONNECT=4;
	
	private ConnectionToServer server;
	private Socket socket1;
    private LinkedBlockingQueue<Message> messages;
    private MessageHandlingThread messageHandling = new MessageHandlingThread();
    private String ipAddr;
    private int port;
    private boolean isConnected;
    Map<Integer, Timer> timers; // Map of timer.id -> Timer object
    
    Writer writer = null;

	public Client(String ipAddr, int port, Collection<Timer> timers)
	{
        this.ipAddr=ipAddr;
        this.port=port;
		this.timers = new HashMap<Integer, Timer>();
        for(Timer timer: timers) {
            this.timers.put(timer.getId(), timer);
        }
		messages = new LinkedBlockingQueue<Message>();
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("clientLog.txt"), "utf-8"));
		} 
		catch (IOException ex) {
		} 
		Thread startConnectionThread = new Thread()
		{
			boolean running=true;
			public void run(){
				while(running)
				{
					try
					{
						Client.this.socket1 = new Socket(InetAddress.getByName(Client.this.ipAddr),Client.this.port);
						Client.this.server = new ConnectionToServer(Client.this.socket1);
						Client.this.messageHandling.start();
						writer.write("Connection established to server \r\n");
						writer.flush();
						for(Timer timer : Client.this.timers.values())
						{
							Message message = new Message(BUILDTIMERLIST,timer.getId(),timer.getState(),timer.getTimerLength(),timer.getTime());
							Client.this.server.sendMessage(BUILDTIMERLIST, message);
						}
					}
					catch (IOException e)
					{
					}
				}
			}
		};
		startConnectionThread.start();
	}
	public boolean isConnected(){
		return this.isConnected;
	}
	private void resetConnection()
	{
		this.socket1=null;
		this.server=null;
		this.messageHandling.turnOff();
		boolean running = true;
		while(running)
		{
			try
			{
				this.socket1 = new Socket(InetAddress.getByName(this.ipAddr),this.port);
				this.server = new ConnectionToServer(this.socket1);
				running=false;
				this.messageHandling = new MessageHandlingThread();
				this.messageHandling.start();
				this.isConnected=true;
				writer.write("Connection established to server \r\n");
				writer.flush();
			}
			catch (IOException e)
			{
				try
				{
					Thread.sleep(3000);
				}
				catch(InterruptedException q)
				{
					
				}
			}
		}
	}
	public void sendMessage(Message message)
	{
		try
		{
			writer.write("Sending message to server with id: " + message.id +"\r\n");
			writer.flush();
			server.sendMessage(SENDMESSAGE,message);
		}
		catch(IOException e)
		{
			
		}
	}
	private class MessageHandlingThread extends Thread
	{
		private boolean running=true;
		public void turnOff(){
			running=false;
		}
		@Override
		public void run()
		{
			running = true;
			while(running)
			{
				try
				{
					Message message = messages.take(); //Waits for a message to enter queue, then pops it.
                    if(Client.this.timers.containsKey(message.id)) {
                        Timer timer = Client.this.timers.get(message.id);
                        timer.setState(message.state);
                        timer.setTime(message.time);
                    } else {
                        // No idea what timer this goes to
                        // TODO Put log message
                    	try
                    	{
                    		writer.write("Have no timer for id: " + message.id);
                    		writer.flush();
                    	}
                    	catch(IOException e)
                    	{
                    	}
                    }
				}
				catch(InterruptedException e)
				{
					
				}
			}
		}
	}
	private class ConnectionToServer{
		DataInputStream dIn;
		DataOutputStream dOut;
		Socket socket;
		
		ConnectionToServer(Socket socket) throws IOException{
			this.socket=socket;
			dOut=new DataOutputStream(this.socket.getOutputStream());
			dIn=new DataInputStream(this.socket.getInputStream());
			this.socket.setSoTimeout(15000);
			Thread read = new Thread(){  //Thread waits to receive message from server
				private boolean running = true;
				public void run(){
					running = true;
					while(running)
					{
						try
						{
							byte actionToPerform = dIn.readByte();//Will time out after 15 seconds.  Determines what action to perform.
							if(actionToPerform == RESETTIMER)
							{
									int id = dIn.readInt();
									int state = dIn.readInt();
									long time = dIn.readLong();
									writer.write("Message received id: " + id + " with time " +time+"\r\n");
									writer.flush();
									messages.add(new Message(id, state, time));
							}
						}
						catch(IOException e)
						{	
							try
							{
								writer.write("Connection to server appears to be lost \r\n");
								writer.flush();
							}
							catch(IOException q)
							{
								
							}
							running=false;
							Client.this.isConnected=false;
							Client.this.resetConnection();
						}
					}
				}
			};
			read.setDaemon(true);
			read.start();
			Thread sendHeartbeat = new Thread()
			{
				public void run()
				{
					while(true)
					{
						try
						{
							Thread.sleep(5000);
						}
						catch(InterruptedException e)
						{
							
						}
						
						try
						{
							writer.write("Sending heartbeat \r\n");
							writer.flush();
							ConnectionToServer.this.sendMessage(HEARTBEAT, null);
						}
						catch(IOException e)
						{		
						}
						
					}
				}
			};
			sendHeartbeat.setDaemon(true);
			sendHeartbeat.start();	
		}
		
		public synchronized void sendMessage(byte actionToPerform, Message message) throws IOException
		{
			if(actionToPerform==SENDMESSAGE)
			{
				dOut.writeByte(SENDMESSAGE);
				dOut.writeInt(message.id);
				dOut.writeInt(message.state);
				dOut.writeLong(message.time);
				dOut.flush();
			}
			else if(actionToPerform==HEARTBEAT)
			{
				dOut.writeByte(HEARTBEAT);
				dOut.flush();
			}
			else if(actionToPerform==BUILDTIMERLIST)
			{
				dOut.writeByte(BUILDTIMERLIST);
				dOut.writeInt(message.id);
				dOut.writeInt(message.state);
				dOut.writeLong(message.initialTime);
				dOut.writeLong(message.time);
				dOut.flush();
			}
		}
	}
	
}
