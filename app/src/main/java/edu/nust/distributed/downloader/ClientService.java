/*
 WiFi Direct File Transfer is an open source application that will enable sharing 
 of data between Android devices running Android 4.0 or higher using a WiFi direct
 connection without the use of a separate WiFi access point.This will enable data 
 transfer between devices without relying on any existing network infrastructure. 
 This application is intended to provide a much higher speed alternative to Bluetooth
 file transfers. 

 Copyright (C) 2012  Teja R. Pitla
 Contact: teja.pitla@gmail.com

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 */




package edu.nust.distributed.downloader;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;



import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

public class ClientService extends IntentService {

	private boolean serviceEnabled;
	
	private int port;
	private String fileToSend;
	private ResultReceiver clientResult;
	private WifiP2pDevice targetDevice;
	private WifiP2pInfo wifiInfo;
	
	public ClientService() {
		super("ClientService");
		serviceEnabled = true;
		
	}

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		port = ((Integer) intent.getExtras().get("port")).intValue();
		clientResult = (ResultReceiver) intent.getExtras().get("clientResult");	
		//targetDevice = (WifiP2pDevice) intent.getExtras().get("targetDevice");	
		wifiInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");	
		
		if(!wifiInfo.isGroupOwner)
		{	
			//targetDevice.
			//signalActivity(wifiInfo.isGroupOwner + " Transfering file " + fileToSend.getName() + " to " + wifiInfo.groupOwnerAddress.toString()  + " on TCP Port: " + port );
			
			InetAddress targetIP = wifiInfo.groupOwnerAddress;
			
			Socket clientSocket = null;
			OutputStream os = null;

			 
			try {
				
				clientSocket = new Socket(targetIP, port);
				os = clientSocket.getOutputStream();

				InputStream is = clientSocket.getInputStream();


				
				signalActivity("About to start handshake");

				



				byte[] buffer = new byte[4096];
				int bytesRead;
			    
//			    FileInputStream fis = new FileInputStream(fileToSend);
//			    BufferedInputStream bis = new BufferedInputStream(fis);
			   // long BytesToSend = fileToSend.length();
				while(true) {
					bytesRead = is.read(buffer, 0, buffer.length);

					PartInfo part = (PartInfo) deserialize(buffer);

					signalActivity("part " + part.start);
					if (part.start == -1)
						break;

					//Toast.makeText(getApplicationContext(),"hey" + part.start,Toast.LENGTH_LONG);
					Downloader downloader = new Downloader(part.start, part.end, new URL(part.Url));
					downloader.run();

					File file = new File("/sdcard/Downloader","Temp " + part.start);

					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);

					while(true)
					{

						bytesRead = bis.read(buffer, 0, buffer.length);

						if(bytesRead == -1)
						{
							break;
						}

						//BytesToSend = BytesToSend - bytesRead;
						os.write(buffer,0, bytesRead);
						os.flush();
					}

//					FileOutputStream fos = new FileOutputStream(file);
//					BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//					while (true) {
//						bytesRead = is.read(buffer, 0, buffer.length);
//						if (bytesRead == -1) {
//							break;
//						}
//						bos.write(buffer, 0, bytesRead);
//						bos.flush();
//
//					}


				}

				signalActivity("Completed");
				clientSocket.close();
				
			} catch (IOException e) {
				signalActivity(e.getMessage());
			}
			catch(Exception e)
			{
				signalActivity(e.getMessage());

			}
			
		}
		else
		{
			signalActivity("This device is a group owner, therefore the IP address of the " +
					"target device cannot be determined. File transfer cannot continue");
		}
		
	
		clientResult.send(port, null);
	}

	private byte[] getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
							return inetAddress.getAddress();
						}
						//return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
					}
				}
			}
		} catch (SocketException ex) {
			//Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		} catch (NullPointerException ex) {
			//Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
		}
		return null;
	}

	private String getDottedDecimalIP(byte[] ipAddr) {
		//convert to dotted decimal notation:
		String ipAddrStr = "";
		for (int i=0; i<ipAddr.length; i++) {
			if (i > 0) {
				ipAddrStr += ".";
			}
			ipAddrStr += ipAddr[i]&0xFF;
		}
		return ipAddrStr;
	}
	

	public void signalActivity(String message)
	{
		Bundle b = new Bundle();
		b.putString("message", message);
		clientResult.send(port, b);
	}
	
	
	public void onDestroy()
	{
		serviceEnabled = false;
		
		//Signal that the service was stopped 
		//serverResult.send(port, new Bundle());
		
		stopSelf();
	}

}