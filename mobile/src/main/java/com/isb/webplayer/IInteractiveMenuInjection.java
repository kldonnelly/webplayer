package com.isb.webplayer;

public interface IInteractiveMenuInjection {
   String getjavascript();
   String getjavascript(int x,int y,String text);
   String getjavascript(boolean muted);
}
