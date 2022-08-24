package com.isb.webplayer;

import java.util.ArrayList;
import java.util.Iterator;

public class DisplayArray {

    private ArrayList<IDisplay> Displays = new ArrayList<IDisplay>();
    private boolean Mute=false;

  public  DisplayArray(){

  }

   long Add(IDisplay display){
       Displays.add(display);
       return 0;
    }

    void MuteDisplay(){
        Mute=true;
        Iterator<IDisplay> it = Displays.iterator();
        while(it.hasNext()) {
            IDisplay d = it.next();
            d.Mute();
        }
  }
  void UnMuteDisplay(){
      Mute=false;
  }

    int ShowDisplay(final mediacontent mc){

        Iterator<IDisplay> it = Displays.iterator();
        while(it.hasNext()) {
            IDisplay d = it.next();
            int e = d.Display(mc);
            if(Mute)d.Mute();
        }
        return 1;

    }
}
