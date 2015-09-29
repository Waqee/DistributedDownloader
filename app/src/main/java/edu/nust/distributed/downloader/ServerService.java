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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;



import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

public class ServerService extends IntentService {

	private boolean serviceEnabled;
	
	private int port;
	private File saveLocation;
	private ResultReceiver serverResult;
	
	public ServerService() {
		super("ServerService");
		serviceEnabled = true;
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		

		
		port = ((Integer) intent.getExtras().get("port")).intValue();

		serverResult = (ResultReceiver) intent.getExtras().get("serverResult");
		
		
		//signalActivity("Starting to download");




		String fileName = "";
		
        ServerSocket welcomeSocket = null;
        Socket socket = null;
                      
		try {
			

			
				welcomeSocket = new ServerSocket(port);
				


				if(DownloadSplitter.Complete)
					serverResult.send(port, null);
				
				//Listen for incoming connections on specified port
				//Block thread until someone connects 
				socket = welcomeSocket.accept();



					try {
						String url =(String) intent.getExtras().get("url").toString();
						DownloadSplitter.Initialize(url);
					} catch (IOException io) {
						Log.d("Exceptoin", "this");
					}


					new Thread(
							new DownloadSplitter(
									socket)
					).start();
					new Thread(
							new DownloadSplitter(
									null)
					).start();


					while(!DownloadSplitter.Complete)
						;

			signalActivity("Completed");

			
	    
		} catch (IOException e) {
			signalActivity(e.getMessage());
			
			
		}
		catch(Exception e)
		{
			signalActivity(e.getMessage());

		}
			
		//Signal that operation is complete

		
		
		
	
		
	}
	

	public void signalActivity(String message)
	{
		Bundle b = new Bundle();
		b.putString("message", message);
		serverResult.send(port, b);
	}
	
	
	public void onDestroy()
	{
		serviceEnabled = false;
		
		//Signal that the service was stopped 
		//serverResult.send(port, new Bundle());
		
		stopSelf();
	}

}
