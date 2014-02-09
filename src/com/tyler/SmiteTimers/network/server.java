package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class server{
	ServerSocket servSocket;
	DataOutputStream dOut;
	DataInputStream dIn;
	int port;
	ServerThread serverThread = new ServerThread();
	public server(String ipAddr, int port)
	{
		//ServerSocket servSocket;
		this.port=port;
		
		/*try{
			servSocket = new ServerSocket(port);
			Socket fromClientSocket = servSocket.accept();
			dOut = new DataOutputStream(fromClientSocket.getOutputStream());
			dIn = new DataInputStream(fromClientSocket.getInputStream());
		}
		catch (IOException e)
		{
			
		}*/
	}
	public void sendReset (int y)
	{
		try
		{
					
			dOut.writeByte(y);
			dOut.flush();
		}
		catch(IOException e)
		{
			
		}
	}
	private class ServerThread extends Thread{
		private boolean running=true;
		@Override
		public void run(){
			while(running){
				try{
					servSocket = new ServerSocket(port);
					Socket fromClientSocket = servSocket.accept();
					dOut = new DataOutputStream(fromClientSocket.getOutputStream());
					dIn = new DataInputStream(fromClientSocket.getInputStream());
				}
				catch (IOException e)
				{
					
				}
			}
			
		}
	}
	
}