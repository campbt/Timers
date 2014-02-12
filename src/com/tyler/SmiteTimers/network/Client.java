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
import java.net.SocketException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;
public class Client {
	private static final byte RESETTIMER=1;
	private static final byte SENDMESSAGE=1;
	private static final byte HEARTBEAT=2;
	private static final byte RECONNECT=4;
	
	private ConnectionToServer server;
	private Socket socket1;
    private LinkedBlockingQueue<Message> messages;
    Map<Integer, Timer> timers; // Map of timer.id -> Timer object
    
    Writer writer = null;

	public Client(String ipAddr, int port, Collection<Timer> timers)
	{
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
		try
		{
			socket1 = new Socket(InetAddress.getByName(ipAddr),port);
			server = new ConnectionToServer(socket1);
			writer.write("Connection established to server \r\n");
			writer.flush();
		}
		catch (IOException e)
		{
		}
		Thread messageHandling = new Thread()  //Thread handles received messages from server
		{
			public void run()
			{
				while(true)
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
		};
		messageHandling.setDaemon(true);
		messageHandling.start();
		
		
	}
	public void sendMessage(Message message)
	{
		try
		{
			writer.write("Sending message to server with id: " + message.id +"\r\n");
			writer.flush();
		}
		catch(IOException e)
		{
			
		}
		server.sendMessage(message);
	}
	
	private class ConnectionToServer{
		DataInputStream dIn;
		DataOutputStream dOut;
		Socket socket;
		
		ConnectionToServer(Socket socket) throws IOException{
			this.socket=socket;
			dOut=new DataOutputStream(this.socket.getOutputStream());
			dIn=new DataInputStream(this.socket.getInputStream());
			this.socket.setSoTimeout(30000);
			Thread read = new Thread(){  //Thread waits to receive message from server
				public void run(){
					while(true)
					{
						try
						{
							byte actionToPerform = dIn.readByte();//Will time out after 30 seconds.  Determines what action to perform.
							if(actionToPerform == RESETTIMER)
							{
								try
								{
								
									int id = dIn.readInt();
									int state = dIn.readInt();
									long time = dIn.readLong();
									writer.write("Sending message id: " + id + " with time " +time+"\r\n");
									writer.flush();
									messages.add(new Message(id, state, time));
								}
								catch(IOException e)
								{
							
								}
							}
						}
						catch(SocketException e)//Will get called if readByte() times out.  Probably means connection was lost.
						{
							//TODO: run reset connection
							try
							{
								writer.write("Connection to server appears to be lost \r\n");
								writer.flush();
							}
							catch(IOException q)
							{
								
							}
						}
						catch(IOException e)
						{							
						}
					}
				}
			};
			read.setDaemon(true);
			read.start();
			
		}
		public void sendMessage(Message message)
		{
			try
			{
				dOut.writeByte(SENDMESSAGE);
				dOut.writeInt(message.id);
				dOut.writeInt(message.state);
                dOut.writeLong(message.time);
				dOut.flush();
			}
			catch (IOException e)
			{
				
			}
		}
	}
	
}
