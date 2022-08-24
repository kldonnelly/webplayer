package com.isb.webplayer;

class mediacontent implements IMediaContent{

    public mediacontent(String f,media_type t)
    {
        type=t;
        url=f;
        msg="no duration";
        modified=-2;

    }
    public mediacontent(String f,String d,String mod,media_type t)
    {
        type=t;
        url=f;
        msg=d;
        modified=-3;
        try
        {
            dur=Double.parseDouble(d.replace("s", ""))*1000.0;
            modified=(Long.parseLong(mod)-116444736000000000L)/10000;

        }
        catch(IllegalArgumentException e)
        {
            modified=-4;
            msg=e.getMessage();

        }


    }
    public mediacontent(String f,String d,media_type t)
    {
        type=t;
        url=f;
        msg=d;
        modified=-5;
        try
        {
            dur=Integer.parseInt(d.replace("s", ""))*1000;

        }
        catch(IllegalArgumentException e)
        {

            msg=e.getMessage();

        }


    }



    NamedBitmap nb;
    String url;
    media_type type;
    double dur=100000;
    long modified=-1;
    String msg="null";
    int errorcode=0;


}
