using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using WindowsMediaLib;
using System.Collections;
using System.Xml;
using System.Runtime.InteropServices;
using System.Web;
//using HtmlAgilityPack;


namespace GenPlayList
{

    public enum mediatype               // 
    {
        audio,            // 
        video,          // 
        html5,
        flash,
        image,
        widget
    };


   public static class  Utils
   {


       public static  int GetHeaderAttribute2(IWMHeaderInfo3 m_pHeaderInfo, string pwszName, out byte[] ppbValue)
       {
           AttrDataType wmtType;
        //   short cbLength = 0;
           UInt16 wAnyStream = 65535;
           ppbValue = null;
           short pwCount=0;
           short pwLangIndex=0;
           short[] pwIndices;
           StringBuilder pname=null;
    
           int pdwDataLength=0;
           short pwNameLen=0;

           m_pHeaderInfo. GetAttributeIndices((short)wAnyStream, pwszName, null, null, ref  pwCount);
           if (pwCount < 1) return 0;

           pwIndices = new short[pwCount];

           m_pHeaderInfo.GetAttributeIndices((short)wAnyStream, pwszName, null, pwIndices, ref  pwCount);


           m_pHeaderInfo.GetAttributeByIndexEx((short)wAnyStream, pwIndices[0], null, ref pwNameLen, out wmtType, out pwLangIndex, null, ref pdwDataLength);

           if (pdwDataLength < 1) return 0;

       
      

           ppbValue = new byte[pdwDataLength];

           m_pHeaderInfo.GetAttributeByIndexEx((short)wAnyStream, pwIndices[0], pname, ref pwNameLen, out wmtType, out pwLangIndex, ppbValue, ref pdwDataLength);


           return pdwDataLength;
       }

       public static short GetHeaderAttribute(IWMHeaderInfo3 m_pHeaderInfo, string pwszName, out byte[] ppbValue)
       {
           AttrDataType wmtType;
           short cbLength = 0;
           short wAnyStream = 0;
           ppbValue = null;

           //
           // Sanity check
           //
           if (null == m_pHeaderInfo)
           {
               return 0;
           }
           try
           {
               //
               // Get the count of bytes to be allocated for pbValue
               //
               m_pHeaderInfo.GetAttributeByName(ref wAnyStream,
                                                       pwszName,
                                                       out wmtType,
                                                       null,
                                                       ref cbLength);

               if (cbLength < 1) return 0;


               ppbValue = new byte[cbLength];



               //
               // Get the actual value
               //
               m_pHeaderInfo.GetAttributeByName(ref wAnyStream,
                                                       pwszName,
                                                       out wmtType,
                                                       ppbValue,
                                                       ref cbLength);
               return cbLength;
           }
           catch(Exception)
           {
               return 0;
           }
           
       }
       
      public static  int get_Marker(ref IWMHeaderInfo3 hinfo, string Name, ref long pVal, ref long duration)
       {

           byte[] pbValue = null;
           GetHeaderAttribute(hinfo, Constants.g_wszWMDuration, out pbValue);
           duration = BitConverter.ToInt64(pbValue, 0);


           //   WORD stream=0;
           //HRESULT hr;


           short cMarkers = 0;
           hinfo.GetMarkerCount(out cMarkers);

           StringBuilder wszName;
           short len = 0;

           //Name.MakeLower();
           long rtTime = 0;

           for (short iMarker = 0; iMarker < cMarkers; ++iMarker)
           {

               short req_len = 0;
               hinfo.GetMarker(iMarker, null, ref req_len, out rtTime);

               // Reallocate if necessary.
               if (len < req_len)
               {

                   len = req_len;
               }

               wszName = new StringBuilder(req_len);
               hinfo.GetMarker(iMarker, wszName, ref req_len, out rtTime);
               string MarkName = wszName.ToString();
               MarkName.ToLower();

               if (MarkName == Name)
               {
                   pVal = rtTime / 10000;
                   break;
               }
           }


           return 1;

       }
   }


    public class Playlist
    {
      
        float TotalDuration=3600;
      //  float AdvDuration=60;
   //     long TotalWeight;
   //     float TotalAdCount;
     //   float TotalTrackCount;
        public string title;
        public DateTime expire;
        public int shutdown=-1;
        public string BASE;
        public short volume=50;
        string separator="\\";
        public string http_video_stream_path="media";
        string client_music_path;
        public string http_audio_stream_path="media";
        public int UtcOffset = 12;
        public int scheduleid = 1;
        public string CrawlTextColorName = "white";
        public string CrawltextBackgroundColor="red";
        public string CrawlTextDur = "10s";
        public string CrawltextbackgroundOpacity = "20%";
		public string CrawlTextTopPos="500";
        public string BackGroundAudio;
        public int cps = 10;
        public int Debug = 0;

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool SetDllDirectory(string lpPathName);

        [DllImport("kernel32.dll", SetLastError = true)]
        public static extern int GetDllDirectory(int bufsize, StringBuilder buf);

  
       public List<Track> contents=new List<Track>();
       public List<marquee> marquees = new List<marquee>();

    

       public Playlist(string dlldir="",string TimeZoneById="New Zealand Standard Time")
       {
           try
           {
               TimeZoneInfo cstZone = TimeZoneInfo.FindSystemTimeZoneById(TimeZoneById);
               UtcOffset = cstZone.BaseUtcOffset.Hours;
               TimeZoneInfo.AdjustmentRule[] adjustment = cstZone.GetAdjustmentRules();

               if (cstZone.IsDaylightSavingTime(DateTime.UtcNow)) UtcOffset = UtcOffset + adjustment[0].DaylightDelta.Hours;
               expire = DateTime.UtcNow.AddHours(UtcOffset + 2);
              
           }
           catch (TimeZoneNotFoundException)
           {
               UtcOffset = 0;
           }


           if (dlldir.Length>1) SetDllDirectory(dlldir);    
      

        }

        public string DllDirectory
        {
            get
            {
                var dlldir = new StringBuilder(255);
                GetDllDirectory(255, dlldir);
                return dlldir.ToString();
            }

        }

      
        
        public string [] Tracks
        {
          
            set
            {
                put_Tracks(value);
               
            }

        }
        
        
        /*
        public string ClientAdPath
        {
            set
            {
                client_advertisement_path = value;
            }
        }
          */
        public string ClientMusicPath
        {
            set
            {
                client_music_path = value;
                if (client_music_path != null)
                {

                    if (client_music_path.Contains("\\"))
                    {
                     //   separator = "\\";
                    }
                    else if (client_music_path.Contains("/"))
                    {
                     //   separator = "/";
                    }
                }
            }
        }
        
       
        public float duration
        {
            get
            {
                return TotalDuration;
            }
            set
            {
                TotalDuration = value;
            }
        }
     

public string Getplaylistsmil(string TimeZoneById = "New Zealand Standard Time")
{
            XmlDocument pxmldoc;
            XmlElement seqnode;
            XmlElement smilnode;
            XmlElement headnode;
            XmlElement medianode;
            XmlElement layoutnode;
            XmlElement bodynode;
   //         XmlElement titlenode;
         //   XmlElement switchnode;
            XmlElement rssregionnode;
            XmlElement headmetabase; ;
            XmlElement parnode;
            XmlElement rootlayoutnode;

            XmlNode nodeout;
        

            pxmldoc= new XmlDocument();
	        smilnode= pxmldoc.CreateElement("smil");
            smilnode.SetAttribute("xmlns", "http://www.w3.org/ns/SMIL");
            smilnode.SetAttribute("version", "3.0");
            smilnode.SetAttribute("baseProfile", "Language");
            smilnode.SetAttribute("title", title);
            smilnode.SetAttribute("scheduleid", scheduleid.ToString());
            smilnode.SetAttribute("Debug",Debug.ToString());


	        nodeout=null;
	        nodeout= pxmldoc.AppendChild(smilnode);
	       

	        headnode=pxmldoc.CreateElement("head");
            nodeout = null;
	        nodeout=smilnode.AppendChild(headnode);

            headmetabase = pxmldoc.CreateElement("meta");
            headmetabase.SetAttribute("name","base");
            headmetabase.SetAttribute("content", BASE);
            headnode.AppendChild(headmetabase);
 
            headmetabase = pxmldoc.CreateElement("meta");
            headmetabase.SetAttribute("name", "Expires");
            headmetabase.SetAttribute("content", expire.ToOADate().ToString());
            headnode.AppendChild(headmetabase);

            headmetabase = pxmldoc.CreateElement("meta");
            headmetabase.SetAttribute("name", "now");
            headmetabase.SetAttribute("content", DateTime.UtcNow.AddHours(UtcOffset).ToOADate().ToString());
            headnode.AppendChild(headmetabase);

            headmetabase = pxmldoc.CreateElement("meta");
            headmetabase.SetAttribute("name", "shutdown");
            headmetabase.SetAttribute("content", shutdown.ToString());
            headnode.AppendChild(headmetabase);
         

            layoutnode=pxmldoc.CreateElement("layout");
            headnode.AppendChild(layoutnode);

            rootlayoutnode=pxmldoc.CreateElement("root-layout");
            rootlayoutnode.SetAttribute("width", "800");
            rootlayoutnode.SetAttribute("hieght", "600");
            layoutnode.AppendChild(rootlayoutnode);

             rssregionnode= pxmldoc.CreateElement("region");
             rssregionnode.SetAttribute("xml:id","rssfeed");
             rssregionnode.SetAttribute("left", "10");
             rssregionnode.SetAttribute("top", CrawlTextTopPos);
             rssregionnode.SetAttribute("width", "100%");
             rssregionnode.SetAttribute("height", "100%");
             rssregionnode.SetAttribute("textMode", "crawl");
             rssregionnode.SetAttribute("textColor", CrawlTextColorName);
             rssregionnode.SetAttribute("textFontWeight","bold");
             rssregionnode.SetAttribute("textFontSize", "xx-large");
             rssregionnode.SetAttribute("textConceal", "both");
             rssregionnode.SetAttribute("textBackgroundColor", CrawltextBackgroundColor);
             rssregionnode.SetAttribute("backgroundOpacity", CrawltextbackgroundOpacity);
             layoutnode.AppendChild(rssregionnode);


             
	  

	       bodynode= pxmldoc.CreateElement("body");
        //   bodynode.SetAttribute("systemComponent", "http://www.w3.org/1999/xhtml");
          
	       smilnode.AppendChild(bodynode);
           parnode= pxmldoc.CreateElement("par");
           nodeout = bodynode.AppendChild(parnode);
          
	       seqnode= pxmldoc.CreateElement("seq");
           seqnode.SetAttribute("repeatCount", "indefinite");
           nodeout = null;
           nodeout = parnode.AppendChild(seqnode);

           int size = contents.Count;


    for(short index=0; index<size; index++)
	{
        Track t = contents[index];

	Track tnext=null;
	Track tprev=null;
	bool nexttrackad=false;
	bool prevtrackad=false;
	if(index < size-1)
	{
        tnext = contents[index + 1];
		nexttrackad=tnext.ad;
	}

	if(index>0)
	{
        tprev = contents[index - 1];
		prevtrackad=tprev.ad;
	}
	


	
	medianode=null;
	//switchnode=null;
	//switchnode=pxmldoc.CreateElement("switch");
	//nodeout=seqnode.AppendChild(switchnode);
        
	if(t.type==mediatype.audio)medianode=pxmldoc.CreateElement("audio");
    else if (t.type == mediatype.video)medianode = pxmldoc.CreateElement("video");
    else if (t.type == mediatype.html5 || t.type == mediatype.widget) medianode = pxmldoc.CreateElement("ref");
    else if (t.type == mediatype.image)medianode = pxmldoc.CreateElement("img");
    else if (t.type == mediatype.flash)medianode = pxmldoc.CreateElement("animation"); 
    

	

    if(t.remotepath==null)medianode.SetAttribute("src",t.url.ToString());
    else medianode.SetAttribute("src",BASE+ t.remotepath);

    if (t.type == mediatype.html5)medianode.SetAttribute("type", "text/html");
    else if (t.type == mediatype.widget) medianode.SetAttribute("type", "application/widget");
  
   
    if (t.WMTitle!=null) medianode.SetAttribute("title", t.WMTitle);
    if (t.WMAuthor!=null) medianode.SetAttribute("author", t.WMAuthor);
    if (t.clip_begin > 0) medianode.SetAttribute("clipBegin", t.clip_begin.ToString());
    if (t.clip_end > 0) medianode.SetAttribute("clipEnd", t.clip_end.ToString());
    if (t.duration > 0) medianode.SetAttribute("dur", t.duration.ToString() + "s");
    try
    {
        if (t.modified != null) medianode.SetAttribute("modified", t.modified.ToFileTimeUtc().ToString());
      else medianode.SetAttribute("modified", "-1");
      medianode.SetAttribute("soundLevel", "20%");
      nodeout = null;
      nodeout = seqnode.AppendChild(medianode);
    }
    catch
    {
      //  medianode.SetAttribute("modified", "-2");
    }

 

 
	

	

	}
 //   parnode = pxmldoc.CreateElement("par");
 //   nodeout = bodynode.AppendChild(parnode);
    seqnode = pxmldoc.CreateElement("seq");
    seqnode.SetAttribute("repeatCount", "indefinite");
    //seqnode.SetAttribute("title", marquees[0].feedurl);
    nodeout = null;
    nodeout = parnode.AppendChild(seqnode);

    for (int mindex = 0; mindex < marquees.Count; mindex++)
    {
        for (int ii = 0; ii < marquees[mindex].rssitems.Count; ii++)
         {

            CrawlTextDur = marquees[mindex].rssitems[ii].duration.ToString() + "s";
            medianode = pxmldoc.CreateElement("smilText");
            medianode.SetAttribute("dur", CrawlTextDur);
            medianode.SetAttribute("region", "rssfeed");
    
            medianode.InnerText = marquees[mindex].rssitems[ii].description;
            seqnode.AppendChild(medianode);
        }
    }

    seqnode = null;
    seqnode = pxmldoc.CreateElement("seq");
    nodeout = null;
    nodeout = parnode.AppendChild(seqnode);

    medianode = pxmldoc.CreateElement("ref");
    nodeout = seqnode.AppendChild(medianode);
    medianode.SetAttribute("src",BackGroundAudio);


	return pxmldoc.OuterXml;



 }


public long ToUnixTimeSeconds(DateTime date)
{
    var epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
    return Convert.ToInt64((date - epoch).TotalSeconds);
}

public long ToUnixTimeMilliSeconds(DateTime date)
{
    var epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
    return Convert.ToInt64((date - epoch).TotalMilliseconds);
}

public string Getplaylistasx(string TimeZoneById="New Zealand Standard Time")
{
    XmlDocument			pxmldoc;
	XmlElement 			basenode;
	XmlElement 			asxnode;
	XmlElement 			refnode;
	XmlElement 			paramnode;
	XmlElement 			markernode;
	XmlElement 			titlenode;
	XmlElement 			endtime;

	XmlNode				nodeout;
	XmlNode				entrynode;
 



	pxmldoc = new XmlDocument();
//	pxmldoc->createNode(Type,L"ASX",L"",&asxnode);
	asxnode=pxmldoc.CreateElement("ASX");
	
	nodeout=null;
	nodeout=pxmldoc.AppendChild(asxnode);
	asxnode.SetAttribute("version","3.0");
	nodeout=null;

	titlenode=pxmldoc.CreateElement("TITLE");
	asxnode.AppendChild(titlenode);

    titlenode.InnerText = title;

	nodeout=null;

	paramnode=pxmldoc.CreateElement("PARAM");
	asxnode.AppendChild(paramnode);
	paramnode.SetAttribute("NAME","expires");
	paramnode.SetAttribute("VALUE",expire.ToOADate().ToString());

    nodeout = null;
    paramnode = pxmldoc.CreateElement("PARAM");
    asxnode.AppendChild(paramnode);
    paramnode.SetAttribute("NAME", "now");
    paramnode.SetAttribute("VALUE", DateTime.UtcNow.AddHours(UtcOffset).ToOADate().ToString());


	nodeout=null;
	basenode=pxmldoc.CreateElement("BASE");
	asxnode.AppendChild(basenode);
	basenode.SetAttribute("HREF",BASE);

	nodeout=null;
	paramnode=null;
	paramnode=pxmldoc.CreateElement("PARAM");
	asxnode.AppendChild(paramnode);
	paramnode.SetAttribute("NAME","volume");
	paramnode.SetAttribute("VALUE",volume.ToString());


//	int size=tracks_plus_ads.Count;
    int size = contents.Count;

	for(short index=0; index<size; index++)
	{
	//	Track t=tracks_plus_ads[index];
        Track t = contents[index];
        
		Track tnext=null;
		Track tprev=null;
       
		bool nexttrackad=false;
		bool prevtrackad=false;
        
		if(index < size-1)
		{
		//	tnext=tracks_plus_ads[index+1];
            tnext = contents[index + 1];
			nexttrackad=tnext.ad;
		}

		if(index>0)
		{
		//	tprev=tracks_plus_ads[index-1];
            tprev = contents[index - 1];
			prevtrackad=tprev.ad;
		}
	

		string FileName=t.Name;
	


		entrynode=null;
		entrynode=pxmldoc.CreateNode(XmlNodeType.Element,"ENTRY","");
		nodeout=null;
		asxnode.AppendChild(entrynode);
		nodeout=null;
		refnode=null;
		titlenode=null;
		titlenode=pxmldoc.CreateElement("TITLE");
		entrynode.AppendChild(titlenode);
		titlenode.InnerText=t.artist_title;
    //    titlenode.InnerText = t.fileName;
		nodeout=null;
		refnode=null;
		titlenode=null;

		refnode=pxmldoc.CreateElement("REF");
		entrynode.AppendChild(refnode);
        if (t.ad) refnode.SetAttribute("href", client_music_path + separator + FileName);
		else refnode.SetAttribute("href",client_music_path+separator+FileName);
	
		refnode=null;
		nodeout=null;
		refnode=pxmldoc.CreateElement("REF");
		entrynode.AppendChild(refnode);
		paramnode=null;
		nodeout=null;
		paramnode=pxmldoc.CreateElement("PARAM");
		nodeout=entrynode.AppendChild(paramnode);

		if(t.type== mediatype.audio)
		{
			paramnode.SetAttribute("NAME","directory");
			paramnode.SetAttribute("VALUE",http_audio_stream_path);
			refnode.SetAttribute("href",http_audio_stream_path+"/"+FileName);

		}
        else if (t.type == mediatype.video)
		{
			paramnode.SetAttribute("NAME","directory");
			paramnode.SetAttribute("VALUE",http_video_stream_path);
			refnode.SetAttribute("href",http_video_stream_path+"/"+FileName);

		}


		paramnode=null;
		nodeout=null;
		paramnode=pxmldoc.CreateElement("PARAM");
		entrynode.AppendChild(paramnode);
		paramnode.SetAttribute("NAME","AverageLevel");
		paramnode.SetAttribute("VALUE",t.AverageLevel.ToString());


		paramnode=null;
		nodeout=null;
		paramnode=pxmldoc.CreateElement("PARAM");
		entrynode.AppendChild(paramnode);
		paramnode.SetAttribute("NAME","FileName");
		paramnode.SetAttribute("VALUE",t.Name);

        if (tnext!=null && tnext.Name.ToLower().Contains("advert"))
        {
            paramnode = null;
            nodeout = null;
            paramnode = pxmldoc.CreateElement("PARAM");
            entrynode.AppendChild(paramnode);
            paramnode.SetAttribute("NAME", "ADVERTNEXT");
            paramnode.SetAttribute("VALUE", "TRUE");
        }




	if(t.ad)
	{
	//starttime=null;
	nodeout=null;
	endtime=null;

	nodeout=null;
	endtime=pxmldoc.CreateElement("DURATION");
	entrynode.AppendChild(endtime);
    TimeSpan g = TimeSpan.FromSeconds(t.duration);
	string ti=string.Format("{0}:{1}:{2}.0",g.Hours,g.Minutes,g.Seconds);
	endtime.SetAttribute("VALUE",ti);
	}



	if(t.StartMark)
	{
	markernode=null;
	nodeout=null;
	markernode=pxmldoc.CreateElement("STARTMARKER");
	entrynode.AppendChild(markernode);
	markernode.SetAttribute("NAME","Start");
	}
	if(t.EndMark)
	{
	nodeout=null;
	markernode=null;
	markernode=pxmldoc.CreateElement("ENDMARKER");
	entrynode.AppendChild(markernode);
	markernode.SetAttribute("NAME","End");
	}
	
	}



   return pxmldoc.OuterXml;
   


}

int put_Tracks(string[] newVal)
{
            String path;
	    for (short i = 0; i < newVal.Length; i++)
        {
		
		
		   path = newVal[i];
           if (path == null) break;
	
			Track t =new Track(path);
			if(t.duration>2)
			{
                contents.Add(t);
			}
 }
  
            
            return 1;
}

   public class content
    {
        public content(Uri url)
        {
            this.url = url;

        }


        public content(string path)
        {
            if (path == null) return;
            Boolean isfile = false;

            try
            {
                url = new Uri(path);
                if (url.Scheme == "http")
                {
                    type = mediatype.html5;

                }
                Name = url.AbsolutePath;
                isfile=url.IsFile;

            }
            catch(Exception e)
            {
                err = e.Message;
            }




            if (isfile)
           {

               string DirectoryName = Path.GetDirectoryName(path);
               string fileName = Path.GetFileName(path);

               DirectoryInfo di1 = new DirectoryInfo(DirectoryName);
               FileInfo[] fileinfos;
               FileInfo fileinfo=null;
               try
               {
                   fileinfos = di1.GetFiles(fileName, SearchOption.TopDirectoryOnly);
                   if (fileinfos.Length < 1 || fileinfos[0].Length < 1) return;
                  fileinfo = fileinfos[0];
               }  
               catch (Exception e)
               {
                   err = e.Message;
                }

             
   
             

               if (fileinfo==null || !fileinfo.Exists || fileinfo.Length < 100) return;

               Name = fileinfo.Name;
               filepath = fileinfo.FullName;
               length = fileinfo.Length;
               modified = fileinfo.LastWriteTime;
               string Extension = fileinfo.Extension.ToLower();

               if (Extension == ".wmv" || Extension == ".mp4" || Extension == ".divx" || Extension == ".mkv" || Extension == ".mov")
               {
                   type = mediatype.video;

               }
               else if (Extension == ".html" || Extension == ".htm")
               {
                   type = mediatype.html5;
               }
               else if (Extension == ".jpg" || Extension == ".png")
               {
                   type = mediatype.image;
               }
               else if (Extension == ".swf")
               {
                   type = mediatype.flash;
               }
               else if (Extension == ".wgt")
               {
                   type = mediatype.widget;
               }
           }
            /*
           if (type == mediatype.html5)
           {
               HtmlWeb web = new HtmlWeb();
               HtmlDocument doc = web.Load(path);
               docinnertext = doc.DocumentNode.InnerHtml;
               HtmlNode metaTag = doc.DocumentNode.Descendants("title").SingleOrDefault();
               Name=metaTag.InnerText;
           
              
           }
            */
           remotepath = ReverseMapPath(filepath);
        }

        public string ReverseMapPath(string path)
        {
            if (HttpContext.Current!=null)
            {
                string appPath = HttpContext.Current.Server.MapPath("~");
                string res = string.Format("/{0}", path.Replace(appPath, "").Replace("\\", "/"));
                return res.Trim().ToLower();
            }
            return null;
        }
        public DateTime modified;
        public Uri url;
        public string filepath = null;
        public string Name;
        public string remotepath;
        public long length=0;
        public mediatype type;
        public string html5videotagsrc;
        public string docinnertext;
        public string err;
    }

   public class marquee
   {
       public marquee(string varFeedUrl,int cps=10)
       {
            XmlDocument feedDocument = new XmlDocument();
            RSSItem rssitem = new RSSItem();
            try
            {
                if (varFeedUrl.Length > 3) feedDocument.Load(varFeedUrl);
            }
            catch
            {
                rssitem.description = varFeedUrl;
                if (rssitem.description.Length > 100) rssitem.duration = rssitem.description.Length/cps;
                else rssitem.duration = 11;

              
                rssitems.Add(rssitem);
                return;
            }
            feedurl = varFeedUrl;
            //Create a XmlNamespaceManager for our namespace.
            XmlNamespaceManager manager =new XmlNamespaceManager(feedDocument.NameTable);
           //Add the RSS namespace to the manager.
            manager.AddNamespace("rss", "http://purl.org/rss/1.0/");


            XmlNode imagenode = feedDocument.SelectSingleNode("/rss/channel/image",manager);
            logourl = "";
         //   if (imagenode!=null) logourl = imagenode.SelectSingleNode("url", manager).InnerText;



            //Get the title node out of the RSS document
            XmlNode titleNode =feedDocument.SelectSingleNode("/rss/channel/title", manager);
            //Get the article nodes
            XmlNodeList articleNodes =feedDocument.SelectNodes("/rss/channel/item", manager);

            //Loop through the articles and extract
            // their data.
         //   listBox1.Items.Clear();

            foreach (XmlNode articleNode in articleNodes)
            {
                //Get the article's title.
                string title =
                articleNode.SelectSingleNode("title", manager).InnerText;

                //Get the article's link
                string link =
                articleNode.SelectSingleNode("link", manager).InnerText;

                //Get the article's description
                string description =
                articleNode.SelectSingleNode("description", manager).InnerText;

                XmlNode enclosure_node = articleNode.SelectSingleNode("enclosure", manager);

                string enclosure_url = "";

                if (enclosure_node != null) enclosure_url = enclosure_node.Attributes["url"].Value;

                if (description.Length > 100) rssitem.duration = description.Length/cps;
                else rssitem.duration = 11;

                rssitem.title = title;
                rssitem.description = description;
                rssitem.enclosere_url = enclosure_url;
                rssitems.Add(rssitem);
            
            }
       }

       public struct RSSItem
       {
           public string title;
           public string description;
           public string enclosere_url;
           public int duration;

       }
       public string feedurl;
       public List<RSSItem> rssitems = new List<RSSItem>();
       public string logourl;
   
     
   }

   public class Track : content
    {

      public Track(string path,int duration,int AverageLevel):base(path)
       {
            AverageLevel=AverageLevel;
            this.duration = duration;
            writewminfo(path);
       }

       
       public Track(string path,int duration):base(path)
       {
           this.duration = duration;
           writewminfo(path);
       }

       public Track(string path) : base(path)
       {


           if (type != mediatype.video) return;

           artist_title = Path.GetFileNameWithoutExtension(filepath);
           writewminfo(path);


       }

       void writewminfo(string path)
       {

           err = "";
           IWMSyncReader r;
         try
           {

               WMUtils.WMCreateSyncReader(IntPtr.Zero, Rights.Playback, out r);
               r.Open(filepath);
               IWMHeaderInfo3 hi = (IWMHeaderInfo3)r;
               byte[] pbValue = null;

               long val = 0;
               TimeVal = 0;
               Utils.get_Marker(ref hi, "Start", ref val, ref TimeVal);
               if (val > 1)
               {
                    StartMark = true;
                     clip_begin = val / 10000000;
               }

               val = 0;
               TimeVal = 0;
               Utils.get_Marker(ref hi, "End", ref val, ref TimeVal);
               if (val > 1)
               {
                   EndMark = true;
                   clip_end = val / 10000000;
               }

               duration = (double)TimeVal / 10000000.0;

            

               if (Utils.GetHeaderAttribute(hi, Constants.g_wszWMAuthor, out pbValue) > 2)
               {
                   WMAuthor = Encoding.Unicode.GetString(pbValue).TrimEnd('\0');
                   artist_title = WMAuthor;
                   if (pbValue != null) pbValue = null;
               }



               if (Utils.GetHeaderAttribute(hi, Constants.g_wszWMTitle, out pbValue) > 2)
               {
                   WMTitle = Encoding.Unicode.GetString(pbValue).TrimEnd('\0'); ;

                   artist_title = artist_title + "," + WMTitle;
                   if (pbValue != null) pbValue = null;
               }



               if (Utils.GetHeaderAttribute(hi, Constants.g_wszAverageLevel, out pbValue) > 1)
               {
                   Int32 d = BitConverter.ToInt32(pbValue, 0);
                   if (d > 40) AverageLevel = d;
                   if (pbValue != null) pbValue = null;

               }

               if (Utils.GetHeaderAttribute(hi, Constants.g_wszWMWMADRCAverageReference, out pbValue) > 1)
               {
                   Int32 d2 = BitConverter.ToInt32(pbValue, 0);
                   if (d2 > 100) AverageLevel = d2;
                   if (pbValue != null) pbValue = null;
               }


               if (Utils.GetHeaderAttribute2(hi, Constants.g_wszWMWMADRCAverageReference, out pbValue) > 1)
               {
                   Int32 d3 = BitConverter.ToInt32(pbValue, 0);
                   if (d3 > 100) AverageLevel = d3;
                   if (pbValue != null) pbValue = null;
               }


               r.Close();



           }
           catch (Exception e)
           {
               err = e.Message;
           }

       }

    

        
        
        public bool ad=false;
        public string artist_title=null;
        public long AverageLevel=3999;
        public double duration=10;
        public bool EndMark=false;
      //  public string filepath=null;
        public bool selected=false;
        public bool StartMark=false;
        public string WMTitle;
        public string WMAuthor;
        long TimeVal;
       public long clip_begin=0;
        public long clip_end=0;
     
    //    public string fileName;
 }


  
    }
}
