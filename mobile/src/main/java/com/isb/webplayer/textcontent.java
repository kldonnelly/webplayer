package com.isb.webplayer;

public class textcontent {

    public textcontent(String desc){
        description=desc;
    }
    public textcontent(String desc,String d){
        description=desc;
        try
        {
            dur=Integer.parseInt(d.replace("s", ""))*1000;
        }
        catch(IllegalArgumentException e)
        {
            //	msg=e.getMessage();

        }
    }

    String description;
    long dur=100000;

}
