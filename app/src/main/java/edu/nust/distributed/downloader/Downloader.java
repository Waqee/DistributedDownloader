package edu.nust.distributed.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Toxin on 8/18/2015.
 */
public class Downloader{
    private long mStartByte;
    private long mEndByte;
    private URL mURL;
    private int BUFFER_SIZE = 16384;

    public Downloader(long startin, long endin, URL urli)
    {
        mStartByte = startin;
        mEndByte = endin;
        mURL = urli;
    }

    public void run() {
        BufferedInputStream in = null;
        File file = null;

        try {
            // open Http connection to URL
            HttpURLConnection conn = (HttpURLConnection)mURL.openConnection();

            // set the range of byte to download
            String byteRange = mStartByte + "-" + mEndByte;
            conn.setRequestProperty("Range", "bytes=" + byteRange);
            System.out.println("bytes=" + byteRange);

            // connect to server
            conn.connect();


            // get the input stream
            in = new BufferedInputStream(conn.getInputStream());

            // open the output file and seek to the start location
            file = new File("/sdcard/Downloader","Temp " + mStartByte);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            byte data[] = new byte[BUFFER_SIZE];
            int numRead;
            while((numRead = in.read(data,0,BUFFER_SIZE)) != -1)
            {
                // write to buffer
                bos.write(data,0,numRead);
                bos.flush();
                // increase the startByte for resume later
                mStartByte += numRead;
            }

        } catch (IOException e) {

        } finally {

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
        }

    }
}
