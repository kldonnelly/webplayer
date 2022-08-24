package com.isb.webplayer;


public class Duration extends Thread {

    private DurationComplete mdelegate;

    public Duration(long dur,DurationComplete delegate)
    {
        d=dur;
        mdelegate=delegate;
        start();
    }
    long d;


    @Override
    public void run(){
        try {
            sleep(d);
            mdelegate.DurationFinish();

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }
}