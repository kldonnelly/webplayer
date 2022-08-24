package com.isb.webplayer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//import org.apache.commons.io.FileUtils;

import android.os.AsyncTask;
import android.os.StatFs;
import android.util.Log;
import android.widget.TextView;


public class DownloadTask extends AsyncTask<String, Integer, Void>{
 

  //  TextView mytext;
    File localdir;
    private OnEventListener<Integer[]> mCallBack;
    String err="no message";
    String contentonmediadrive;
    public DownloadTask(String localdir,OnEventListener callback){
      //  this.context = context;
      //Create the notification object from NotificationHelper class
    //	mytext=tv;
        mCallBack=callback;
    	this.localdir=new File(localdir);
        contentonmediadrive=localdir;
    }

    void StartDownLoadTask(ArrayList<mediacontent> mediacontentlist) {
        Log.d("Activity", "StartDownLoadTask");

        ArrayList<String> downloadstringArray = new ArrayList<String>();

        for (int index = 0; index < mediacontentlist.size(); index++) {
            File remotefile = new File(mediacontentlist.get(index).url);

            File localfile = new File(contentonmediadrive + File.separator + remotefile.getName());

            if (mediacontentlist.get(index).type == media_type.video || mediacontentlist.get(index).type == media_type.image || mediacontentlist.get(index).type == media_type.widget) {
                boolean local_exists = localfile.exists();

                long local_lastModified = localfile.lastModified();
                long remote_lastModified = mediacontentlist.get(index).modified;


                if (local_exists && (remote_lastModified > local_lastModified)) {
                    //   settext("deleting  "+localfile.getAbsolutePath());
                    Log.d("PLAYLIST", "deleting file>>" + localfile.getAbsolutePath());

                    Filename fn = new Filename(localfile.getAbsolutePath(), File.separator, '.');

                    File dir = new File(fn.unzipfolder());

                    Log.d("PLAYLIST", "deleting folder>>" + dir.getAbsolutePath());


                    //	FileUtils.deleteQuietly(dir);
                    DownloadTask.deleteRecursive(dir);


                    local_exists = !localfile.delete();
                }


                if (!local_exists) downloadstringArray.add(mediacontentlist.get(index).url);
            }


        }

        if(downloadstringArray.size()>0)execute(downloadstringArray.toArray(new String[downloadstringArray.size()]));

    }
    @Override
	protected void onPreExecute(){
        //Create the notification in the statusbar
      
    }
    
    
  static  public void deleteRecursive(File fileOrDirectory) {

    	   if (fileOrDirectory.isDirectory()) {
    	       for (File child : fileOrDirectory.listFiles()) {
    	          deleteRecursive(child);
    	       }
    	   }

    	   fileOrDirectory.delete();
    	 }
    
   
    
    
    @Override
    protected Void doInBackground(String... aurl) {
        //This is where we would do the actual download stuff
        //for now I'm just going to loop for 10 seconds
        // publishing progress every second

        int count;
        long AvailableBlocks,UsableSpace,TotalSpace;
   		StatFs statFs = new StatFs(localdir.getPath());
   	//	AvailableBlocks=statFs.getAvailableBlocksLong();
   		
   		
        File root = localdir.getParentFile();

        UsableSpace=localdir.getUsableSpace();
        TotalSpace=localdir.getTotalSpace();
        err=String.format(" UsableSpace %d TotalSpace %d\n", UsableSpace,TotalSpace);
        File[] dirs=  localdir.listFiles();


        if(dirs!=null && dirs.length>0) {
            Arrays.sort(dirs, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {

                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });


            for (int i = 0; i < dirs.length; i++) {

                if (dirs[i].isDirectory() && dirs[i].getPath().contains("tmp")) {
                    //  FileUtils.deleteQuietly(dirs[i]);
                    deleteRecursive(dirs[i]);
                }

                err = err + dirs[i].getName() + " " + dirs[i].lastModified() + "\n";


            }
        }

	      
        
        File[] tempfiles=root.listFiles();
        
   if(tempfiles !=null) {

       for (int i = 0; i < tempfiles.length; i++) {
           try {
               if (!tempfiles[i].isDirectory()) tempfiles[i].delete();
           } catch (Exception e) {
               err = e.getMessage();
           }

       }
   }
        File newfile;
        try {

        for(int index=0; index < aurl.length; index++)	
        {
        
        String fileName = new File(aurl[index]).getName();

        newfile=new File(localdir+File.separator+fileName);
     
      
        if(newfile.exists())continue;
        URL url2=null;
        
        try
        {
        URL url= new URL(aurl[index]);
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),url.getPath(),  url.getQuery(), url.getRef());
       
       String urlStr=uri.toASCIIString();
        
       url2= new URL(urlStr);
         
        }
        catch(MalformedURLException m )
        {
        	continue;
        	
        }

        URLConnection conexion = url2.openConnection();
        conexion.connect();
        
        int lenghtOfFile = conexion.getContentLength();

       InputStream  instream=url2.openStream();
      
        
       BufferedInputStream input = new BufferedInputStream(instream);

        
        File tempFile = File.createTempFile(fileName, ".tmp",root);
        
      //  Log.d("Tempfile=%s",tempFile.getPath());
        FileOutputStream output = new FileOutputStream(tempFile);
        byte data[] = new byte[2048];
        

        long total = 0;
        	count=0;
            while ((count = input.read(data)) != -1) {
                total += count;
                if (isCancelled()) break;
              //  Log.d("%Percentage%"+a,""+(int)((total*100)/lenghtOfFile));
                publishProgress(aurl.length,index,(int)((total*100)/lenghtOfFile));
                
                output.write(data, 0, count);
            }
	
            output.flush();
            output.close();
            input.close();
            instream.close();
            
            if (isCancelled()){
            	
            	Log.d("DownloadTask", "tempFile.delete()="+tempFile.delete());	
            	return null;
            }
            tempFile.renameTo(newfile);
          
            
        }
        
        } 
        catch (Exception e) 
        {
        	err=e.getMessage();
        }

       
        return null;
    }
    @Override
	protected void onProgressUpdate(Integer... progress) {
        //This method runs on the UI thread, it receives progress updates
        //from the background thread and publishes them to the status bar
    //	mytext.setText(String.format("%d/%d %d %%", progress[1],progress[0],progress[2]));
        mCallBack.onSuccess(progress);
    	//mytext.setText(progress[0]);
    }
    @Override
	protected void onPostExecute(Void result)    {
//    	mytext.setText(err);
      
    }
    
    @Override
    protected void onCancelled ()
    {
    	
    }
   
}
