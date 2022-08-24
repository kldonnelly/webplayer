package com.isb.webplayer;


import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class DisplayImageView implements IDisplay {

    ImageView mImageView;
    String contentonmediadrive;
    private OnEventListener<Integer> mCallBack;

    public DisplayImageView(ImageView _mImageView, String _contentonmediadrive,OnEventListener callback){
        mImageView = _mImageView;
        mCallBack=callback;
        mImageView.setVisibility(View.GONE);
        contentonmediadrive =  _contentonmediadrive;
    }



    @Override
    public int Display(final mediacontent mc) {

    if(mc.type==media_type.image) {
        File remotefile = new File(mc.url);
        File localfile = new File(contentonmediadrive + File.separator + remotefile.getName());

        if (localfile.exists()) {

            if (mc.nb == null) mc.nb = new NamedBitmap(localfile.getAbsolutePath());
            if (mc.nb != null && mc.nb.nbitmap != null) mImageView.setImageBitmap(mc.nb.nbitmap);
            else mImageView.setImageURI(Uri.parse(localfile.getAbsolutePath()));

            mImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            mCallBack.onSuccess(result_code.play_next_later.getValue());
            return result_code.play_next_later.getValue();
        }

        return result_code.success.getValue();
    }
       Hide();
        return result_code.not_me.getValue();
    }
    @Override
    public void Mute() {

    }
    @Override
    public void Hide() {
        mImageView.setVisibility(View.GONE);
    }
}
