package com.isb.webplayer;

enum result_code {
    not_me(0),success(1), play_next(-1),play_next_later(-2);

    int e;
    result_code(int v){
        e=v;
    }
    public int getValue() { return e; }
}

public interface
IDisplay {

    int Display(final mediacontent mc);
    void Mute();
    void Hide();
}


