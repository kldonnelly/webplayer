package com.isb.webplayer;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import android.graphics.Color;
import android.widget.TextView;

public class RemoteDownloadSMILPlaylist  extends AsyncTask<URL, Integer, Integer> implements IPlaylistDownload{

    Document doc=null;
   // IPlaylistDownload m_params;
   private   ArrayList<mediacontent> mediacontentlst;
    private  ArrayList<textcontent> textcontentlst;
    private Date expires;
    private int shutdown=-1;

    String error;
    TextView tvmsg;
    Date now;

    String title;
    TextView tvMarq;
    DownloadTask dwnltsk;
    int textcontenindex=-1;

    String contentonmediadrive;
    private OnEventListener<Integer> mCallBack;
    public Exception mException;

    public RemoteDownloadSMILPlaylist( String contentonmediadrive ,OnEventListener callback)
    {
        mCallBack = callback;
        this.contentonmediadrive=  contentonmediadrive;
        Calendar rightNow=Calendar.getInstance();

        expires= rightNow.getTime();

        rightNow.add(Calendar.HOUR, 1);

        expires=rightNow.getTime();

        textcontentlst = new ArrayList<>();
        mediacontentlst = new ArrayList<>();
     //   m_params = params;

    }

    @Override
    protected Integer doInBackground(URL... urls) {
        // TODO Auto-generated method stub
        doc=getDocParser(urls[0]);
        if(doc!=null)return 1;
        if(mException==null)mException = new Exception("Unknown error");
        mCallBack.onFailure(mException);
        return -6;
    }

    /**
     * @param playlist
     * @return
     */
    Document getDocParser(URL playlist) {

        Document doc=null;
        HttpURLConnection urlConnection=null;
        try{


            urlConnection=(HttpURLConnection)playlist.openConnection();
            Log.d("Activity", "Connect Timeout="+urlConnection.getConnectTimeout());


            if(urlConnection.getResponseCode()== HttpURLConnection.HTTP_OK)
            {

                InputStream urlInputStream =  urlConnection.getInputStream();
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(urlInputStream);
                doc.getDocumentElement().normalize();
            }


        }
        catch (ParserConfigurationException e)
        {
            mException = e;
            error=e.getMessage();

        }
        catch (IOException e)
        {
            mException = e;
            error= e.getMessage();
        }
        catch (SAXException e)
        {
            mException = e;
            error= e.getMessage();
        }
        finally
        {
            if(urlConnection!=null)urlConnection.disconnect();
        }



        return doc;
    }


    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onPostExecute(Integer result) {

        if(doc==null || result<0)
        {

            //	tvmsg.setText("Failed to get document result="+ result.toString());
            return;
        }



        String nodename="no name";
        Element rootnode;
        NodeList seqnodes;
        NodeList media;
        NodeList metanodes;
        NodeList regionnodes;
        Node src;
        Node dur;
        Node mod;
        Node type;

        rootnode=doc.getDocumentElement();
        NamedNodeMap attrs=rootnode.getAttributes();
        if(attrs.getLength()==0)
        {
         //   if(m_params.Debug>0)tvmsg.setText("Failed to root node attributes");
            return;
        }

        Node debug=attrs.getNamedItem("Debug");

     //   if(debug!=null && m_params.Debug<2)m_params.Debug=Integer.parseInt(debug.getNodeValue());

        Node userid=attrs.getNamedItem("userid");
     //   if(userid!=null)m_params.Userid=userid.getNodeValue();




     //   if(m_params.Debug==0)tvmsg.setVisibility(View.GONE);
     //   else tvmsg.setVisibility(View.VISIBLE);

        Node Title=attrs.getNamedItem("title");
        title=Title.getNodeValue();

        metanodes=doc.getElementsByTagName("meta");


        MediaContentList().clear();
        TextContentList().clear();



        for(int index=0; index< metanodes.getLength(); index++)
        {
            attrs=metanodes.item(index).getAttributes();
            Node name=attrs.getNamedItem("name");
            Node content=attrs.getNamedItem("content");
            OleAutomationDateUtil jj= new OleAutomationDateUtil();

            if(name.getNodeValue().toLowerCase().contains("expires"))
            {
                ExpireTime(jj.fromOADate(Double.parseDouble(content.getNodeValue())));

            }
            else if(name.getNodeValue().toLowerCase().contains("now"))
            {
                now=jj.fromOADate(Double.parseDouble(content.getNodeValue()));

            }
            else if(name.getNodeValue().toLowerCase().contains("shutdown"))
            {
               ShutDownTime(Integer.parseInt(content.getNodeValue()));

            }
        }

        regionnodes=doc.getElementsByTagName("region");

        for(int index=0; index< regionnodes.getLength(); index++)
        {
            attrs=regionnodes.item(index).getAttributes();
            Node textColor=attrs.getNamedItem("textColor");
            attrs.getNamedItem("textFontSize");
            Node textBackGroundColor=attrs.getNamedItem("textBackgroundColor");
//            tvMarq.setTextColor(Color.parseColor(textColor.getNodeValue()));
            int bk=Color.parseColor(textBackGroundColor.getNodeValue());

            Node backgroundOpacity=attrs.getNamedItem("backgroundOpacity");
            float alpha=Float.parseFloat(backgroundOpacity.getNodeValue().replace("%", ""));
            alpha=alpha/100;
        //    tvMarq.setBackgroundColor(adjustAlpha(bk,alpha));

        }





        seqnodes=doc.getElementsByTagName("seq");
        media =seqnodes.item(0).getChildNodes();
        //	tv.setText("got seq 1 number="+ media.getLength());

        for(int index=0; index< media.getLength(); index++)
        {
            attrs=media.item(index).getAttributes();

            nodename=media.item(index).getNodeName();

            src= attrs.getNamedItem("src");
            dur=attrs.getNamedItem("dur");
            mod=attrs.getNamedItem("modified");
            type=attrs.getNamedItem("type");

            if(nodename.toLowerCase().contains("animation")){
                if(dur!=null)MediaContentList().add(new mediacontent(src.getNodeValue(),dur.getNodeValue(),media_type.swf));
                else MediaContentList().add(new mediacontent(src.getNodeValue(),media_type.swf));
            }
            else if(nodename.toLowerCase().contains("video")){

                if(dur!=null && mod!=null)MediaContentList().add(new mediacontent(src.getNodeValue(),dur.getNodeValue(),mod.getNodeValue(),media_type.video));
                else MediaContentList().add(new mediacontent(src.getNodeValue(),media_type.video));
            }
            else if(nodename.toLowerCase().contains("img")){

                if(dur!=null && mod!=null )MediaContentList().add(new mediacontent(src.getNodeValue(),dur.getNodeValue(),mod.getNodeValue(),media_type.image));
                else MediaContentList().add(new mediacontent(src.getNodeValue(),media_type.image));

            }
            else if(nodename.toLowerCase().contains("ref")){

                if(type.getNodeValue().toLowerCase().contains("html"))
                {
                    if(dur!=null)MediaContentList().add(new mediacontent(src.getNodeValue(),dur.getNodeValue(),media_type.html));
                    else MediaContentList().add(new mediacontent(src.getNodeValue(),media_type.html));
                }
                else if(type.getNodeValue().toLowerCase().contains("widget"))
                {
                    if(dur!=null && mod!=null)MediaContentList().add(new mediacontent(src.getNodeValue(),dur.getNodeValue(),mod.getNodeValue(),media_type.widget));
                    else MediaContentList().add(new mediacontent(src.getNodeValue(),media_type.widget));
                }
            }

        }

        media =seqnodes.item(1).getChildNodes();
        //	tv.setText("got seq 2 number="+ media.getLength());
        for(int index=0; index< media.getLength(); index++)
        {
            attrs=media.item(index).getAttributes();

            nodename=media.item(index).getNodeName();
            dur=attrs.getNamedItem("dur");
            if(nodename.toLowerCase().contains("smiltext")){

                String desc=media.item(index).getTextContent();
                if(dur!=null)TextContentList().add(new textcontent(desc,dur.getNodeValue()));
                else TextContentList().add(new textcontent(desc));
            }
        }


        if(seqnodes.getLength()>2) {
            media = seqnodes.item(2).getChildNodes();

            if (media != null) {
                attrs = media.item(0).getAttributes();
                src = attrs.getNamedItem("src");
             //   m_params.backgroundaudio= src.getNodeValue();


            }
        }

     //   StartDownLoadTask();

        if(MediaContentList().size()>1)
        {
            mCallBack.onSuccess(result);
            SaveDocument(doc);

        }
        else
        {
            if(mException==null)mException = new Exception("no media content");
            mCallBack.onFailure(mException);

        }

    }



    public void SaveDocument(Document doc){

        try{


            TransformerFactory transformerfactory=
                    TransformerFactory.newInstance();
            Transformer transformer=
                    transformerfactory.newTransformer();


            DOMSource source=new DOMSource(doc);
            File file = new File(contentonmediadrive+File.separator+"playlist.xml");
            FileOutputStream _stream = new FileOutputStream(file);
            // FileOutputStream _stream=openFileOutput(contentonmediadrive+File.separator+"playlist.xml", MODE_WORLD_WRITEABLE);
            StreamResult result=new StreamResult(_stream);
            transformer.transform(source, result);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    @Override
    public void ExpireTime(Date expiretime) {
        expires=expiretime;
    }

    @Override
    public Date ExpireTime() {
        return expires;
    }

    @Override
    public void ShutDownTime(int time) {
        shutdown=time;
    }

    @Override
    public int ShutDownTime() {
        return shutdown;
    }

    @Override
    public void Start(URL url) {
    this.execute(url);
    }

    @Override
    public ArrayList<mediacontent> MediaContentList() {
        return mediacontentlst;
    }

    @Override
    public ArrayList<textcontent> TextContentList() {
        return textcontentlst;
    }
}
