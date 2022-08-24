package com.isb.webplayer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public interface IPlaylistDownload{

    void ExpireTime(Date expiretime);
    Date ExpireTime();
    void ShutDownTime(int time);
    int ShutDownTime();
    void Start(URL url);
    ArrayList<mediacontent> MediaContentList();
    ArrayList<textcontent> TextContentList();

}
