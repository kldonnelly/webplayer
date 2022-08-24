package nz.co.isb.webplayer;

import android.util.Log;

import org.junit.Test;

import com.isb.webplayer.IPlaylistDownload;
import com.isb.webplayer.OnEventListener;
import com.isb.webplayer.RemoteDownloadSMILPlaylist;

import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class PlaylistDownloadTest{
    @Test
    public void DownloadTest(){

         IPlaylistDownload dl=null;
        final IPlaylistDownload finalDl = dl;
        dl = new RemoteDownloadSMILPlaylist(null, new OnEventListener<Integer>(){

            @Override
            public void onSuccess(Integer result) {
                if(finalDl !=null)
                Log.d(TAG, "onSuccess: "+ finalDl.MediaContentList().get(0));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        String url="http://signage.isb.co.nz/smilhandler.ashx?email=ctvcfsouth@gmail.com";
        try {
            URL playlist = new URL(url);
            dl.Start(playlist);
        }
        catch (MalformedURLException e) {

        }
    }
}
