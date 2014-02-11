package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Server{
	private ArrayList<ConnectionToClient> clientList;
    private LinkedBlockingQueue<Integer> messages;
    private ServerSocket serverSocket;
    
	public Server(int port)
	{
		
		clientList = new ArrayList<ConnectionToClient>();
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
				while (true)
				{
					try
					{
						Integer k = messages.take();
						sendReset(k.intValue());
					}
					catch(InterruptedException e)
					{
						
					}
				}
			}
		};
		messageHandling.start();		
	}
	
	public void sendReset (int y)
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
		
		ConnectionToClient (Socket socket) throws IOException {
			this.socket = socket;
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
							Integer v = new Integer(recieved);
							messages.add(v);
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
 		public void send(int y) throws IOException{ //sends int y to client
 			dOut.writeByte(y);
 			dOut.flush();			
 		}
	}
}