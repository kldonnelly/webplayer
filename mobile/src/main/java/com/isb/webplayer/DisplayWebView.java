package com.isb.webplayer;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DisplayWebView implements IDisplay {

    WebView mWebView;
    String contentonmediadrive;
    private OnEventListener<Integer> mCallBack;

    class JsObject {
        @JavascriptInterface
        public void end(String h)
        {
         //   playnextOnUiThread();
            mCallBack.onSuccess(result_code.play_next.getValue());

        }
    }

    public DisplayWebView(WebView _mWebView,String _contentonmediadrive,OnEventListener callback){
        mWebView=_mWebView;
        mCallBack=callback;
        contentonmediadrive=_contentonmediadrive;
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setBackgroundColor(Color.BLACK);
        mWebView.setVisibility(View.GONE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JsObject(), "external");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                // Handle the error
                Exception ex= new Exception(failingUrl+","+description);
                mCallBack.onFailure(ex);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                mWebView.loadUrl("javascript:(function() { document.getElementsByTagName('video')[0].play(); })()");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view,  WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
        mWebView.addJavascriptInterface(new JsObject(), "external");


        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress)
            {
                //  activity.setTitle("Loading...");
                //    activity.setProgress(progress * 100);

                //    if(progress == 100)
                //    activity.setTitle(R.string.app_name);
            }
        });



    }

    @Override
    public int Display(final mediacontent mc) {

        if(mc.type==media_type.swf || mc.type==media_type.html) //type==0 swf
        {

           return ViewHtml(mc.url);
        }
        else if(mc.type==media_type.widget){

           return ViewWidget(mc.url);
        }

        Hide();
        return result_code.not_me.getValue();
    }

    @Override
    public void Hide() {
        mWebView.setVisibility(View.GONE);
        try{
            mWebView.loadUrl("about:blank");
        }
        catch(Exception e){
            Log.d("Activity", e.getMessage());
        }

    }
    @Override
    public void Mute() {

    }
    private int ViewWidget(String url){



            File remotefile = new File(url);
            File localfile = new File(contentonmediadrive + File.separator + remotefile.getName());

            if (localfile.exists()) {
                //	Filename title= new Filename(remotefile.getName(),File.separator,'.');
                String title = FilenameUtils.removeExtension(localfile.getName());

                File localfolder = new File(contentonmediadrive + File.separator + title);

                if (localfolder.exists() && localfolder.isDirectory()) {

                    Collection<File> htmlFiles = FileUtils.listFiles(localfolder, new String[]{"html"}, true);

                    Iterator<File> itr = htmlFiles.iterator();
                    while (itr.hasNext()) {

                        mWebView.loadUrl("file://" + itr.next());
                        break;
                    }


                } else {

                    //    if(Debug>0)settext("unzip="+localfile.getParent());
                    unzipFunction(localfile.getAbsolutePath());
                    //   playnextOnUiThread();
                    mCallBack.onSuccess(result_code.play_next.getValue());
                    return result_code.play_next.getValue();


                }

            } else {
                //   playnextOnUiThread();
                mCallBack.onSuccess(result_code.play_next.getValue());
                return result_code.play_next.getValue();
            }
            mWebView.getSettings().setUseWideViewPort(false);
            // 29 July 2020   mVideoView.setVisibility(View.GONE);

            mWebView.setVisibility(View.VISIBLE);
            return result_code.success.getValue();

    }

    private  int ViewHtml(String url)
    {
        mWebView.getSettings().setUseWideViewPort(false);
        mWebView.loadUrl(url);
        mWebView.setVisibility(View.VISIBLE);
        return result_code.success.getValue();
    }


    private  void unzipFunction(String zipFile) {

        Filename n= new Filename(zipFile,File.separator, '.');

        String	destinationFolder=n.unzipfolder();

        //	File directory = new File(destinationFolder);
        Random r = new Random();

        File tempdir = new File(destinationFolder+"_"+Integer.toString(r.nextInt())+"_tmp");

        // buffer for read and write data to file
        byte[] buffer = new byte[2048];

        try {
            FileInputStream fInput = new FileInputStream(zipFile);
            ZipInputStream zipInput = new ZipInputStream(fInput);

            ZipEntry entry = zipInput.getNextEntry();

            while(entry != null){
                String entryName = entry.getName();
                File file = new File(tempdir + File.separator + entryName);

                //   settext("Unzip file " + entryName + " to " + file.getAbsolutePath());

                // create the directories of the zip directory
                if(entry.isDirectory()) {
                    File newDir = new File(file.getAbsolutePath());
                    if(!newDir.exists()) {
                        boolean success = newDir.mkdirs();
                        if(success == false) {
                            //    settext("Problem creating Folder");
                        }
                    }
                }
                else {

                    file.getParentFile().mkdirs();
                    FileOutputStream fOutput = new FileOutputStream(file);
                    int count = 0;
                    while ((count = zipInput.read(buffer)) > 0) {
                        // write 'count' bytes to the file output stream
                        fOutput.write(buffer, 0, count);
                    }
                    fOutput.close();
                }
                // close ZipEntry and take the next one
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }

            // close the last ZipEntry
            zipInput.closeEntry();

            zipInput.close();
            fInput.close();

            File directory = new File(destinationFolder);
            tempdir.renameTo(directory);


        } catch (IOException e) {
            //   error=e.getMessage();
        }

    }
}
