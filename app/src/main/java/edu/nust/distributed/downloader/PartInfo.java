package edu.nust.distributed.downloader;

import java.io.Serializable;

/**
 * Created by Toxin on 8/17/2015.
 */
public class PartInfo implements Serializable{
    public String Url;
    public long start;
    public long end;
}
