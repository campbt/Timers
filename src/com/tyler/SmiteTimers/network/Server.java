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
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;

public class Server{
	private ArrayList<ConnectionToClient> clientList;
    //private LinkedBlockingQueue<Integer> messages;
	private LinkedBlockingQueue<MessageData> messages2;
    private ServerSocket serverSocket;
    Collection<Timer> timerList;
    Writer writer = null;
    int messageNumber;
   // private PrintWriter writer;// = new PrintWriter("Serverlog.txt", "UTF-8");
    
	public Server(int port, Collection<Timer> timers)
	{
		//try{
	//		writer= new PrintWriter("Serverlog.txt","UTF-8");
		//}
		//catch(UnsupportedEncodingException e){
			
	//	}
		messageNumber=0;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("serverLog.txt"), "utf-8"));
		} 
		catch (IOException ex) {
		  // report
		} 
		finally 
		{
		   try 
		   {
			   //writer.close();}
		   }
		   catch (Exception ex) 
		   {
			   
		   }
		}
		//writer.write("")
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
						socket1.setKeepAlive(true);
						writer.write("Connection established to" + socket1.getInetAddress().toString() + "\r\n");
						//writer.close();
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
						try
						{
							messageNumber++;
							writer.write("Message number "+messageNumber+" received: " + k.message + " from " + k.ipAddress + "\r\n");
							writer.flush();
						}
						catch (IOException e)
						{
							
						}
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
						try{
							writer.write("Thread was interruped");
						}
						catch (IOException q){
						}
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
					try{
						writer.write("Forwarding message to: "+ client.socket.getInetAddress().toString() + "\r\n");
						writer.flush();
					}
					catch(IOException e)
					{
						
					}
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
				try
				{
					writer.write("Message originating from server. Sending to " + client.socket.getInetAddress().toString() + "the message: " + y + "\r\n");
					writer.flush();
				}
				catch (IOException e)
				{
					
				}
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
							try
							{
								writer.write("Client has sent message: "+recieved +"\r\n");
								writer.flush();
							}
							catch(IOException e){
								
							}
							addMessage(new MessageData(recieved,socket.getInetAddress().toString()));
							//messages.add(v);
							try
							{
								writer.write("Size of messages2 = " + messages2.size() + "\r\n");
								writer.flush();
							}
							catch(IOException e){
								
							}
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
	public synchronized void addMessage(MessageData data){
		messages2.add(data);
	}
}