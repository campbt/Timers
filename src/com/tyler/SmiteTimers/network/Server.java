package com.tyler.SmiteTimers.network;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;

public class Server{
	//private static final byte RESETTIMER = 1;
	private static final byte SENDMESSAGE = 1;
	private static final byte HEARTBEAT = 2;
	
	private ArrayList<ConnectionToClient> clientList;
	private LinkedBlockingQueue<Message> messages;
	private ServerSocket serverSocket;
	Map<Integer, Timer> timers; // Map of timer.id -> Timer object

    Writer writer = null;
    int messageNumber;
    
	public Server(int port, Collection<Timer> timers)
	{
		this.timers = new HashMap<Integer, Timer>();
        for(Timer timer: timers) {
            this.timers.put(timer.getId(), timer);
        }
		messageNumber=0;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("serverLog.txt"), "utf-8"));
		} 
		catch (IOException ex) {
		} 
		clientList = new ArrayList<ConnectionToClient>();
		messages = new LinkedBlockingQueue<Message>();
		try
		{
			serverSocket = new ServerSocket(port);
		}
		catch (IOException e)
		{		
		}
		Thread addClients = new Thread()  //This thread waits for new connections
		{
			public void run()
			{
				while(true)
				{
					try
					{
						Socket socket1 = serverSocket.accept(); //When a client attempts to connect, this accepts the connection
						writer.write("Connection established to" + socket1.getInetAddress().toString() + "\r\n");
						writer.flush();
						clientList.add(new ConnectionToClient(socket1)); //Adds new connection to list
					}
					catch(IOException e)
					{
						
					}
				}
			}
		};
		addClients.setDaemon(true);
		addClients.start();
		Thread sendHeartbeat = new Thread()
		{
			public void run()
			{
				while(true)
				{
						//sleep for 5 seconds, then send heart beat to all clients
					try
					{
						Thread.sleep(5000);
					}
					catch(InterruptedException e)
					{
						
					}
					for(ConnectionToClient client: Server.this.clientList)
					{
						try
						{
							writer.write("Sending heartbeat \r\n");
							writer.flush();
							client.send(HEARTBEAT, null);
						}
						catch(IOException e)
						{		
						}
					}
				}
			}
		};
		sendHeartbeat.start();
		Thread messageHandling = new Thread()  //This thread waits for a new message to enter the queue from any client
		{ 
			public void run()
			{
				while (true)
				{
					try
					{
						Message message = messages.take();
						try
						{
							messageNumber++;
							writer.write("Message number "+messageNumber+" received: " + message.id + " from " + message.ip + "\r\n");
							writer.flush();
						}
						catch (IOException e)
						{
							
						}
						if(Server.this.timers.containsKey(message.id)) 
						{
							 Timer timer = Server.this.timers.get(message.id);
	                         timer.setState(message.state);
	                         timer.setTime(message.time);
	                    } 
						else 
	                    {
	                            // No idea what timer this goes to
	                            // TODO Put log message
							try
							{
	                            writer.write("Have no timer for id: " + message.id + "\r\n");
	                            writer.flush();
							}
							catch(IOException e)
							{
								
							}
	                    }
						sendMessage(message);				
					}
					catch(InterruptedException e)
					{
						try
						{
							writer.write("Thread was interruped \r\n");
							writer.flush();
						}
						catch (IOException q){
						}
					}
				}
			}
		};
		messageHandling.start();		
	}
	
	public void sendMessage (Message message) //If reset is originating from a client, it sends through this method
	{
		try
		{
			byte actionToPerform = SENDMESSAGE;
			for(ConnectionToClient client : clientList)
			{	
				//if(message.ip != null && !(client.socket.getInetAddress().toString().equals(message.ip))){
				try{
					writer.write("Source: " +message.ip+"\r\n");
					writer.write("Destination: " + client.socket.getInetAddress().toString()+"\r\n");
					writer.flush();
				}
				catch(IOException e){
					
				}
				if(!(client.socket.getInetAddress().toString().equals(message.ip))){
					try{
						writer.write("Forwarding message to: "+ client.socket.getInetAddress().toString() + "\r\n");
						writer.flush();
					}
					catch(IOException e)
					{
						
					}
					client.send(actionToPerform,message);
				}
			}
		}
		catch(IOException e)
		{
			
		}
	}
	public int HowManyConnections(){
		return this.clientList.size(); 
	}
	private class ConnectionToClient{
		Socket socket;
		DataOutputStream dOut;
		DataInputStream dIn;
		
		ConnectionToClient (Socket socket2) throws IOException {
			socket = socket2;
			dOut = new DataOutputStream(this.socket.getOutputStream());
			dIn = new DataInputStream(this.socket.getInputStream());
			this.socket.setSoTimeout(30000);
			Thread read = new Thread() //This thread waits for a client to send a message, then adds it to the queue
			{
				private boolean running = true;
				public void run()
				{
					running=true;
					while(running)
					{
						try
						{
							byte actionToPerform = dIn.readByte();
							if(actionToPerform==SENDMESSAGE)
							{
								int id = dIn.readInt();
								int state = dIn.readInt();
								long time = dIn.readLong();

								writer.write("Client has sent message: "+ id +" with time " + time + "\r\n");
								writer.write("Client IP is: " + socket.getInetAddress().toString() +"\r\n");
								writer.flush();

								addMessage(new Message(id, state, time, socket.getInetAddress().toString()));
								writer.write("Size of messages = " + messages.size() + "\r\n");
								writer.flush();
							}
						}
						catch (IOException e)
						{
							try
							{
								writer.write("Connection to a client appears to be lost \r\n");
								writer.flush();
								running=false;
								Server.this.clientList.remove(ConnectionToClient.this);
							}
							catch(IOException q)
							{
								
							}
						}
					}
				}
			};
			read.setDaemon(true);
			read.start();
		}
		public synchronized void send(byte actionToPerform, Message message) throws IOException
		{
			if(actionToPerform == SENDMESSAGE){
				dOut.writeByte(SENDMESSAGE);
				dOut.writeInt(message.id);
				dOut.writeInt(message.state);
				dOut.writeLong(message.time);
				dOut.flush();
			}
			else if (actionToPerform == HEARTBEAT)
			{
				dOut.writeByte(HEARTBEAT);
				dOut.flush();
				writer.write("Heartbeat sent\r\n");
			}
 		}
	}
	public synchronized void addMessage(Message message){
		messages.add(message);
	}
}
