package com.isb.webplayer;


public interface OnEventListener<T> {
     void onSuccess(T result);
     void onFailure(Exception e);
}
