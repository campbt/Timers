package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.tyler.SmiteTimers.core.Timer;
public class Client {
	private ConnectionToServer server;
	private Socket socket1;
    private LinkedBlockingQueue<Message> messages;
    Map<Integer, Timer> timers; // Map of timer.id -> Timer object

	public Client(String ipAddr, int port, Collection<Timer> timers)
	{
        this.timers = new HashMap<Integer, Timer>();
        for(Timer timer: timers) {
            this.timers.put(timer.getId(), timer);
        }
		messages = new LinkedBlockingQueue<Message>();
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
						Message message = messages.take(); //Waits for a message to enter queue, then pops it.
                        if(Client.this.timers.containsKey(message.id)) {
                            Timer timer = Client.this.timers.get(message.id);
                            timer.setState(message.state);
                            timer.setTime(message.time);
                        } else {
                            // No idea what timer this goes to
                            // TODO Put log message
                            System.out.println("Have no timer for id: " + message.id);
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
			Thread read = new Thread(){  //Thread waits to receive message from server
				public void run(){
					while(true)
					{
						try
						{
							int id = dIn.readInt();
							int state = dIn.readInt();
							long time = dIn.readLong();

							messages.add(new Message(id, state, time));
						}
						catch(IOException e){
							
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
				dOut.writeInt(message.id);
				dOut.writeInt(message.state);
                dOut.writeDouble(message.time);
				dOut.flush();
			}
			catch (IOException e)
			{
				
			}
		}
	}
	
}
