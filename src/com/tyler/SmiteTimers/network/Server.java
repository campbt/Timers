package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;

public class Server{
	private ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<Message> messages;
    private ServerSocket serverSocket;
    Map<Integer, Timer> timers; // Map of timer.id -> Timer object
    
	public Server(int port, Collection<Timer> timers)
	{
        this.timers = new HashMap<Integer, Timer>();
        for(Timer timer: timers) {
            this.timers.put(timer.getId(), timer);
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
		Thread messageHandling = new Thread()  //This thread waits for a new message to enter the queue from any client
		{ 
			public void run()
			{
				//MessageData k = new MessageData(0,"");
				while (true)
				{
					try
					{
						Message message = messages.take();
                        if(Server.this.timers.containsKey(message.id)) {
                            Timer timer = Server.this.timers.get(message.id);
                            timer.setState(message.state);
                            timer.setTime(message.time);
                        } else {
                            // No idea what timer this goes to
                            // TODO Put log message
                            System.out.println("Have no timer for id: " + message.id);
                        }
						sendMessage(message);
					}
					catch(InterruptedException e)
					{
						
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
			for(ConnectionToClient client : clientList)
			{	
				//String testAddress=client.socket.getInetAddress().toString();
				if(message.ip != null && !(client.socket.getInetAddress().toString().equals(message.ip))){
					client.send(message);
				}
			}
		}
		catch(IOException e)
		{
			
		}
	}

	private class ConnectionToClient{
		Socket socket;
		DataOutputStream dOut;
		DataInputStream dIn;
		
		ConnectionToClient (Socket socket2) throws IOException {
			socket = socket2;
			dOut = new DataOutputStream(this.socket.getOutputStream());
			dIn = new DataInputStream(this.socket.getInputStream());
			Thread read = new Thread() //This thread waits for a client to send a message, then adds it to the queue
			{
				public void run()
				{
					while(true)
					{
						try
						{
							int id = dIn.readInt();
							int state = dIn.readInt();
							long time = dIn.readLong();

							messages.add(new Message(id, state, time, socket.getInetAddress().toString()));
						}
						catch (IOException e)
						{
							
						}
					}
				}
			};
			read.setDaemon(true);
			read.start();
		}
		public void send(Message message) throws IOException	//sends int y to client
		{
			dOut.writeInt(message.id);
			dOut.writeInt(message.state);
			dOut.writeDouble(message.time);
			dOut.flush();			
 		}
	}
}
