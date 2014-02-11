package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
	private ConnectionToServer server;
	private Socket socket1;
    private LinkedBlockingQueue<Integer> messages;
	public Client(String ipAddr, int port)
	{
		try
		{
			socket1 = new Socket(InetAddress.getByName(ipAddr),port);
			server = new ConnectionToServer(socket1);
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
						Integer p = messages.take(); //Waits for a message to enter queue, then pops it.
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
	public void sendReset (int y)
	{
		server.sendReset(y);
	}
	
	private class ConnectionToServer{
		DataInputStream dIn;
		DataOutputStream dOut;
		Socket socket;
		
		ConnectionToServer(Socket socket) throws IOException{
			this.socket=socket;
			dOut=new DataOutputStream(this.socket.getOutputStream());
			dIn=new DataInputStream(this.socket.getInputStream());
			Thread read = new Thread(){  //Thread waits to receive message from server
				public void run(){
					while(true)
					{
						try
						{
							int y = dIn.readInt();
							Integer q = new Integer(y);
							messages.add(q);
						}
						catch(IOException e){
							
						}
					}
				}
				
			};
			read.setDaemon(true);
			read.start();
			
		}
		public void sendReset(int y)
		{
			try
			{
				dOut.writeByte(y);
				dOut.flush();
			}
			catch (IOException e)
			{
				
			}
		}
	}
	
}