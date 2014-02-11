package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;

public class Server{
	private ArrayList<ConnectionToClient> clientList;
    //private LinkedBlockingQueue<Integer> messages;
    private LinkedBlockingQueue<MessageData> messages2;
    private ServerSocket serverSocket;
    Collection<Timer> timerList;
    
	public Server(int port, Collection<Timer> timers)
	{
		timerList=timers;
		clientList = new ArrayList<ConnectionToClient>();
		messages2 = new LinkedBlockingQueue<MessageData>();
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
						MessageData k = messages2.take();
						for(Timer timerInstance : timerList)
						{
							if(timerInstance.getId()==k.message)
							{
								timerInstance.networkToggle();
							}
						}
						sendReset(k);
					}
					catch(InterruptedException e)
					{
						
					}
				}
			}
		};
		messageHandling.start();		
	}
	
	public void sendReset (MessageData y) //If reset is originating from a client, it sends through this method
	{
		try
		{
			for(ConnectionToClient client : clientList)
			{	
				//String testAddress=client.socket.getInetAddress().toString();
				if(!(client.socket.getInetAddress().toString().equals(y.ipAddress))){
					client.send(y.message);
				}
			}
		}
		catch(IOException e)
		{
			
		}
	}

	public void sendReset2 (int y) //If reset is originating from server, it sends through this method
	{
		try
		{
			for(ConnectionToClient client : clientList)
			{
				client.send(y);
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
							int recieved = dIn.readInt();
							//Integer v = new Integer(recieved);
							messages2.add(new MessageData(recieved,socket.getInetAddress().toString()));
							//messages.add(v);
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
		public void send(int y) throws IOException	//sends int y to client
		{
			dOut.writeInt(y);
			dOut.flush();			
 		}
	}
}