package com.isb.webplayer;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import com.sprylab.android.widget.TextureVideoView;
import java.io.File;


public class DisplayVideoView implements IDisplay {

    TextureVideoView mVideoView;
    String contentonmediadrive;
    int videoplayfailcount=0;

    private OnEventListener<Integer> mCallBack;
    public Exception mException;
    private boolean mute=false;

    public DisplayVideoView(TextureVideoView _mVideoView,String _contentonmediadrive,OnEventListener callback){

        mVideoView=_mVideoView;
        mCallBack=callback;
        contentonmediadrive =  _contentonmediadrive;

        mVideoView.setShouldRequestAudioFocus(false);

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                return true;
            }
        });
        mVideoView.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //use a global variable to get the object

                mVideoView.start();

            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                mVideoView.stopPlayback();
               // playnextOnUiThread();
                mCallBack.onSuccess(result_code.play_next.getValue());

            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {


              //  tvmsg.setVisibility(View.VISIBLE);
              //  videoplayfailcount=2;
              //  mediacontent mc=dwnloadprms.mediacontentlst.get(remotecontenindex);
              //  settext("error playing file -- " + mc.url);
              //  Duration(10000);
                mException=new Exception("Media player error");

                mCallBack.onFailure(mException);
                return true;
            }
        });

        mVideoView.setVisibility(View.GONE);
    }

    @Override
    public int Display(final mediacontent mc) {
        if (mc.type == media_type.video) {

            boolean deletedlocalfile = false;
            File remotefile = new File(mc.url);
            File localfile = new File(contentonmediadrive + File.separator + remotefile.getName());
            long lmodified = -6;

            if (localfile.exists()) {
                lmodified = localfile.lastModified();
                if (mc.modified > lmodified) {
                    deletedlocalfile = localfile.delete();
                    mCallBack.onSuccess(result_code.play_next.getValue());
                    return result_code.play_next.getValue();
                } else {
                    mVideoView.setVideoPath(localfile.getAbsolutePath());
                    mVideoView.setVisibility(View.VISIBLE);
                    Log.d("Activity", "setVideoPath="+mc.url+" fc="+videoplayfailcount);

                    if(videoplayfailcount-- > -1){
                        // tvmsg.setVisibility(View.GONE);
                        mException=new Exception("Video Playback count");

                        mCallBack.onFailure(mException);
                    }
                    return result_code.success.getValue();
                }


            }
            else
            {
                if(videoplayfailcount++>0){
                    //   tvmsg.setVisibility(View.VISIBLE);


                }
                mCallBack.onSuccess(result_code.play_next_later.getValue());
                Log.d("Activity", "setVideoPath="+mc.url+" fc="+videoplayfailcount+" type="+mc.type+" exists="+localfile.exists());
                return result_code.play_next_later.getValue();

            }
        }
       Hide();
        return result_code.not_me.getValue();
    }
    @Override
    public void Mute() {
        mVideoView.Mute();
    }
    @Override
    public void Hide() {
        mVideoView.setVisibility(View.GONE);
    }
}
