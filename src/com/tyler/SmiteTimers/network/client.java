package com.tyler.SmiteTimers.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class client {
	DataOutputStream dOut;
	DataInputStream dIn;
	Socket socket1;
	public client(String ipAddr, int port)
	{
		//Socket socket1;
		try
		{
			socket1 = new Socket(InetAddress.getByName(ipAddr),port);
			dOut = new DataOutputStream(socket1.getOutputStream());
			dIn = new DataInputStream(socket1.getInputStream());			
		}
		catch (IOException e)
		{
			
		}
		
		
	}
	public void sendReset (int y){
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