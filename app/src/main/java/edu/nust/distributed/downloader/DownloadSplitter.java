package edu.nust.distributed.downloader;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;


/**
 * Created by Toxin on 8/17/2015.
 */
public class DownloadSplitter implements Runnable{
    protected Socket clientSocket = null;
    static protected String DownloadUrl = "";
    static protected boolean Complete = false;
    static protected long Length ;
    static protected long nextpart;
    static protected long partLength = 5000000;

    static public void Initialize(String Url) throws IOException
    {
        DownloadUrl = Url;

        URL oracle = new URL(DownloadUrl);

        HttpURLConnection yc = (HttpURLConnection) oracle.openConnection();

        try {
            // retrieve file size from Content-Length header field
            Length = Long.parseLong(yc.getHeaderField("Content-Length"));
        } catch (NumberFormatException nfe) {
        }

        nextpart = 0;
    }


    public DownloadSplitter(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public void run() {

        long startTime = System.currentTimeMillis();
        try {

            byte[] temp = new byte[10];
            byte[] buffer = new byte[4096];
            int bytesRead;
            InputStream is = null;
            OutputStream os = null;

            for(int i=0;i<10;i++)
                temp[i]=(byte)255;

            if(clientSocket!=null) {
                 is = clientSocket.getInputStream();

                 os = clientSocket.getOutputStream();
            }

            while(true) {

                PartInfo part = new PartInfo();

                part.Url = DownloadUrl;
                synchronized (this) {
                    if(Complete)
                        part.start = -1;
                    else
                        part.start = nextpart;
                    if(nextpart+partLength>=Length)
                    {
                        Complete = true;
                        part.end = Length - 1;
                    }
                    else {
                        nextpart += partLength;
                        part.end = part.start + partLength - 1;
                    }
                }

                if(clientSocket!=null) {
                    os.write(serialize(part));
                    os.flush();
                }
                else {
                    Log.d("ASD", "starting part " + part.start + " " + (System.currentTimeMillis() - startTime));
                    Downloader downloader = new Downloader(part.start, part.end, new URL(part.Url));
                    downloader.run();
                    Log.d("ASD", "ended part " + part.start + " " + (System.currentTimeMillis() - startTime));
                }

                if(clientSocket!=null) {
                    File file = new File("/sdcard/Downloader","Temp " + part.start);
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);


                    long total = 0;

                    while(true) {
                        bytesRead = is.read(buffer, 0, buffer.length);

                        bos.write(buffer, 0,bytesRead);
                        bos.flush();

                        total+=bytesRead;

                        if(total==part.end-part.start+1)
                            break;
                    }

                }




                if(Complete)
                {
                    Log.d("ASD", "Completed");
                    break;
                }

            }

        }

        catch (IOException e) {
        }

    }
}
