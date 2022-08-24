<%@ WebHandler Language="C#" Class="smilhandler" %>

using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.IO;
using System.Collections.Specialized;
using GenPlayList;
using System.Data.SqlClient;
using System.Data.Sql;
using System.Drawing;

public class smilhandler : IHttpHandler 
{

   public class Track
    {
        public Int32 dur;
        public Int32 volume;
        public string content;

        public Track(string C,Int32 D, Int32 V)
        {
            content = C;
            dur = D;
            volume = V;
        }

        
    };
    public class Tracks : List<Track>
    {
        public Int16 index;
    };


    public class Mix
    {
        public Int64 ruserid;
        public Int32 playlistid;
        public String userid;
        public Byte Weight;
        Int32 mixid;

        public Tracks tracks;

        public Mix(Int64 ruserid, Int32 playlistid, Byte weight, Int32 mixid)
        {
            this.ruserid = ruserid;
            this.playlistid = playlistid;
            this.Weight = weight;
            this.mixid = mixid;
        }
      

    };
    
    string TimeZoneById = "New Zealand Standard Time";
    string playlistname;
    GenPlayList.Playlist pl;
    string connectionString = System.Configuration.ConfigurationManager.ConnectionStrings["isbConnectionString"].ConnectionString;
    int UtcOffset;
    Color rssbackcolor = Color.Red;
    Color rssforcolor = Color.White;
    int rssspeed = 10;
    float rssbackgroundalpha=0.3F;
    int rssfontsize;
    int rsstop = 650;
    
    int debug = 0;
    int feedenabled = 0;
    int shutdown = 2;
    string RemoteUser = null;
    short ratioremote = 0;
    short ratiolocal = 100;
    string backgroundaudioplaylist;
    public smilhandler()
	{
        try
        {
            TimeZoneInfo cstZone = TimeZoneInfo.FindSystemTimeZoneById(TimeZoneById);
            UtcOffset = cstZone.BaseUtcOffset.Hours;
            TimeZoneInfo.AdjustmentRule[] adjustment = cstZone.GetAdjustmentRules();

            if (cstZone.IsDaylightSavingTime(DateTime.UtcNow)) UtcOffset = UtcOffset + adjustment[0].DaylightDelta.Hours;
           
        }
        catch (TimeZoneNotFoundException)
        {
            UtcOffset = 0;
        }

	}

    public void ProcessRequest(HttpContext ctx)
    {
        int playlistid = -1;
        string rplaylistid = "";
        string userid = "1";
        string ruserid = null;
        string feedid = "1";
        string feedindex = "0";
        string email = null;
        string User_Identity_Name = null;
        string playlistname="";
        int scheduleid=1;
        int rscheduleid = 1;
       
        DateTime expires = DateTime.UtcNow.AddHours(UtcOffset) + new TimeSpan(4, 0, 0);
        DateTime rexpires = DateTime.UtcNow.AddHours(UtcOffset) + new TimeSpan(4, 0, 0); 
      
        char[] delimiterChars = { '/', ' ', ',', '.', ':', '\t' };
        string username = ctx.Request.Path;
        username=username.Trim('/');
        SqlConnection connection = new SqlConnection(connectionString);
        connection.Open();
        string[] words = username.Split(delimiterChars);
        User_Identity_Name = words[0];

        NameValueCollection pColl = ctx.Request.Params;
        for (int pcollindex = 0; pcollindex < pColl.Count; pcollindex++)
        {
            string[] pValues = pColl.GetValues(pcollindex);
            if (pColl.GetKey(pcollindex) == "playlistid") int.TryParse( pValues[0],out playlistid);
            else if (pColl.GetKey(pcollindex) == "playlistname") playlistname = pValues[0];
            else if (pColl.GetKey(pcollindex) == "userid") userid = pValues[0];
            else if (pColl.GetKey(pcollindex) == "feedid") feedid = pValues[0];
            else if (pColl.GetKey(pcollindex) == "feedindex") feedindex = pValues[0];
            else if (pColl.GetKey(pcollindex) == "email") email = pValues[0];
            else if (pColl.GetKey(pcollindex) == "username") User_Identity_Name = pValues[0];
        }


        userid = GetUserID(connection, User_Identity_Name,email);
       
       if(playlistid<0) playlistid = GetPlayListid(connection, userid, playlistname);


       if (playlistid < 0)
       {
           scheduleid = GetScheduleid(connection, userid, ref expires);
           playlistid = GetPlaylistid(connection, userid, scheduleid);
       }
      
        
        
        ReadParams(connection, userid);

        if (RemoteUser != null && ratioremote >0 )
        {
            ruserid = GetUserID(connection, RemoteUser, RemoteUser);
            rscheduleid = GetScheduleid(connection, ruserid, ref rexpires);
            rplaylistid = GetPlaylistid(connection, ruserid, rscheduleid).ToString();
        }
		
         backgroundaudioplaylist= Getbackgroundaudioplaylist(connection, userid,scheduleid);
        
        ctx.Response.ContentType = "text/xml";

     //   Color rssbackcolor = Color.FromArgb(40, Color.Red);


        ColorConverter cc = new ColorConverter();

      //  ColorTranslator.ToHtml()

        pl = new GenPlayList.Playlist(ctx.Server.MapPath("sbin"));
        pl.BASE ="http://"+ctx.Request.Url.Host;
        pl.title = "playlistid=" + playlistid + " userid=" + userid.ToString() + " rplaylistid=" + rplaylistid + " remoteuser=" + RemoteUser + " ruserid=" + ruserid + " ratioremote=" + ratioremote.ToString() + " ratiolocal=" + ratiolocal.ToString();
        pl.scheduleid = scheduleid;
        pl.CrawlTextColorName =cc.ConvertToString(rssforcolor);
        pl.CrawltextbackgroundOpacity = (100*rssbackgroundalpha).ToString()+"%";
    //    pl.CrawltextBackgroundColor =cc.ConvertToString(rssbackcolor);
        pl.CrawltextBackgroundColor = ColorTranslator.ToHtml(rssbackcolor);
        pl.CrawlTextDur = rssspeed.ToString()+"s";
        pl.BackGroundAudio = backgroundaudioplaylist;
        pl.expire = expires;
        pl.Debug = debug;
        pl.shutdown = shutdown;
        pl.CrawlTextTopPos = rsstop.ToString();
        if (ruserid == null)
        {
            if (playlistid<99) readtracks(connection, userid, playlistid);
            else readmix(connection, userid, playlistid);
            
        }
        else readtracks(connection, userid, playlistid, ratiolocal, ruserid, rplaylistid, ratioremote);
      //  readtracks(connection, ruserid, rplaylistid);
           
       string feedurl = GetRssFeed(connection, userid, feedindex);
       connection.Close();

   //    ctx.Response.Write(feedurl);

       if (feedenabled>0)pl.marquees.Add(new Playlist.marquee(feedurl,rssspeed));
    
     
        ctx.Response.Write(pl.Getplaylistsmil());
    
    }

    private string  Getbackgroundaudioplaylist(SqlConnection connection,string userid,int scheduleid)
    {
      
        string audio=null;
        string queryString = "SELECT [audio] FROM [scheduled_audio] WHERE ([UserID] = " + userid + ") AND ([scheduleid] = '" + scheduleid+"')";
        SqlCommand command = new SqlCommand(queryString, connection);

        SqlDataReader reader = command.ExecuteReader();

        while (reader.HasRows && reader.Read())
        {
            if (reader.IsDBNull(0)) break;

            audio = reader.GetString(0);
         
        }

        // Call Close when done reading.
        reader.Close();


        return audio;
    }
    private int GetPlayListid(SqlConnection connection,string userid,string playlistname)
    {
        int playlistid = -1;
        if (playlistname.Length < 2) return -2;

        string queryString = "SELECT [playlistid] FROM [playlists] WHERE ([UserID] = " + userid + ") AND ([playlistname] = '" + playlistname+"')";
        SqlCommand command = new SqlCommand(queryString, connection);

        SqlDataReader reader = command.ExecuteReader();
     
        while (reader.Read())
        {
            if (reader.IsDBNull(0)) break;

            playlistid = reader.GetInt32(0);
         
        }

        // Call Close when done reading.
        reader.Close();


        return playlistid;
    }

    
    static Int32 GetTracks(Tracks tracks, SqlConnection connection, string ruserid, int playlistid)
    {
        Int32 count = 0;
        tracks.index = 0;
        string queryStringLocal = "SELECT [content],[duration],[Volume] FROM [animations] Where ([Row] > 0) AND ([UserID] =" + ruserid + ") AND ([playlistid] = " + playlistid + ") ORDER BY Row";

        SqlCommand command = new SqlCommand(queryStringLocal, connection);

        SqlDataReader readerL = command.ExecuteReader();
        while (readerL.Read())
        {
            if (readerL.IsDBNull(0) || readerL.IsDBNull(1)) continue;
            tracks.Add(new Track(readerL.GetString(0), readerL.GetInt32(1), readerL.GetInt32(2)));
            count++;
        }
        readerL.Close();
        return count;
    }
    
    void readmix(SqlConnection connection, string userid, int mixnum = 100)
    {
        string queryString = "SELECT ruserid,playlistid,weight,mixid FROM mixes  where (weight > 0) AND (userid =" + userid + ")  AND (mixnum=" + mixnum + ")";

        List<Mix> MixList = new List<Mix>();

        SqlCommand command = new SqlCommand(queryString, connection);


        SqlDataReader reader = command.ExecuteReader();
        while (reader.Read())
        {
            Int64 ruserid = reader.GetInt64(0);
            Int32 playlistid = reader.GetInt32(1);
            Byte weight = reader.GetByte(2);
            Int32 mixid = reader.GetInt32(3);
            Mix ml = new Mix(ruserid, playlistid, weight, mixid);
            MixList.Add(ml);


        }
        reader.Close();

        for (int i = 0; i < MixList.Count; i++)
        {
            Int64 ruserid = MixList[i].ruserid;
            Int32 playlistid = MixList[i].playlistid;
            Mix g = MixList.Find(f => f.ruserid.Equals(ruserid) && f.playlistid.Equals(playlistid));
            if (g != null && g.tracks != null) MixList[i].tracks = g.tracks;
            else
            {
                Tracks tracks = new Tracks();
                tracks.index = 0;
                GetTracks(tracks, connection, ruserid.ToString(), playlistid);
                MixList[i].tracks = tracks;
            }

        }

        int totalcount = 0;
        int mainlistsize = MixList[0].tracks.Count;

        while (MixList[0].tracks.index < mainlistsize && totalcount++ < 100)
        {
            for (int i = 0; i < MixList.Count; i++)
            {

                int count = 0;
                for (int ii = 0; ii < MixList[i].tracks.Count; ii++)
                {

                    if (count++ >= MixList[i].Weight) break;
                    if (MixList[i].tracks.index >= MixList[i].tracks.Count) MixList[i].tracks.index = 0;
                    pl.contents.Add(new Playlist.Track(MixList[i].tracks[MixList[i].tracks.index].content, MixList[i].tracks[MixList[i].tracks.index].dur));
                    MixList[i].tracks.index++;
                    if (MixList[0].tracks.index >= mainlistsize) break;

                    totalcount++;
                }


            }
        }

    }
    private string GetRssFeed(SqlConnection connection,string userid,string feedindex)
    {
     //   string queryString = "SELECT [url],[enabled] FROM [Feeds] WHERE (UserID = " + userid + ") AND ([Index] = " + feedindex+")";
        string queryString = "SELECT [url],[enabled] FROM [Feeds] WHERE (UserID = " + userid + ")";
        SqlCommand command = new SqlCommand(queryString, connection);
     
        SqlDataReader reader = command.ExecuteReader();
        // Call Read before accessing data. 
        string feedurl="http://rss.nzherald.co.nz/rss/xml/nzhrsscid_000000001.xml";
        while (reader.Read())
        {
            if (reader.IsDBNull(0)) break;
            feedurl = reader.GetString(0);
            feedenabled = reader.GetInt32(1);
            if (feedurl.Length > 5 && feedenabled>0) break;
        }

        // Call Close when done reading.
        reader.Close();
        return feedurl;

    }

    private void ReadParams(SqlConnection connection, string userid)
    {
        string queryString = "SELECT [Parameter],[Value] FROM [configuration] Where [UserID]=" + userid;
        string Paramname = "";
        string Paramvalue = "";
     //   float alpha=0.4F;
        ColorConverter cc = new ColorConverter();
        SqlCommand command = new SqlCommand(queryString, connection);
        SqlDataReader reader = command.ExecuteReader();


        // Call Read before accessing data. 
        while (reader.Read())
        {

            if (reader.IsDBNull(1)) continue;

            if (reader.IsDBNull(0)) continue;

            Paramname = reader.GetString(0);
            Paramname.Trim();
            Paramname = Paramname.ToLower();
            Paramvalue = reader.GetString(1);
            Paramvalue.Trim();


            if (Paramname.Equals("rssbackgroundcolor"))
            {
                rssbackcolor = Color.FromArgb((int)(rssbackgroundalpha * 255), (Color)cc.ConvertFromString(Paramvalue));
             //   rssbackcolor = (Color)cc.ConvertFromString(Paramvalue);
            

            }
            else if (Paramname.Equals("rssfontcolor"))
            {
                rssforcolor = (Color)cc.ConvertFromString(Paramvalue);
            }
            else if (Paramname.Equals("rssfontsize"))
            {

                Int32.TryParse(Paramvalue, out rssfontsize);
            
            }
            else if (Paramname.Equals("debug"))
            {

                Int32.TryParse(Paramvalue, out debug);

            }
            else if (Paramname.Equals("rssbackgroundalpha"))
            {
                float.TryParse(Paramvalue, out rssbackgroundalpha);

                rssbackcolor = Color.FromArgb((int)(rssbackgroundalpha * 255), rssbackcolor);
            }
            else if (Paramname.Equals("rssspeed"))
            {

                Int32.TryParse(Paramvalue, out rssspeed);

            }
            else if (Paramname.Equals("rsstop"))
            {

                Int32.TryParse(Paramvalue, out rsstop);

            }
            else if (Paramname.Equals("remoteuser"))
            {

                RemoteUser = Paramvalue;

            }
            else if (Paramname.Equals("ratioremote"))
            {

                Int16.TryParse(Paramvalue, out ratioremote);

            }
            else if (Paramname.Equals("ratiolocal"))
            {

                Int16.TryParse(Paramvalue, out ratiolocal);

            }
            else if (Paramname.Equals("shutdown"))
            {

                Int32.TryParse(Paramvalue, out shutdown);

            }
            
        }

        // Call Close when done reading.
        reader.Close();


    }
    private string GetUserID(SqlConnection connection,string name=null,string email=null)
    {

        string queryString=null;
        string emailqueryString = null;

        long userid=1;
        SqlDataReader reader;

        if (email != null)
        {
            emailqueryString = "SELECT UserID FROM dbo.Users WHERE (email LIKE N'%" + email + "%')";
            SqlCommand command = new SqlCommand(emailqueryString, connection);


            reader = command.ExecuteReader();
            if (reader.HasRows)
            {
                // Call Read before accessing data. 
                while (reader.Read())
                {
                    if (reader.IsDBNull(0)) break;
                    userid = reader.GetInt64(0);
                }

                // Call Close when done reading.
              
            }

            reader.Close();
        }
        else if (name != null)
        {
            queryString = "SELECT UserID FROM dbo.Users WHERE (User_Identity_Name LIKE N'%" + name + "%')";
            SqlCommand command = new SqlCommand(queryString, connection);


            reader = command.ExecuteReader();
            if (reader.HasRows)
            {
                // Call Read before accessing data. 
                while (reader.Read())
                {
                    if (reader.IsDBNull(0)) break;
                    userid = reader.GetInt64(0);
                }

                // Call Close when done reading.
              
            }
            reader.Close();
        }


      
        return userid.ToString();
    }

    private int GetScheduleid(SqlConnection connection, string userid, ref DateTime expires)
    {
        TimeSpan myTimeNow = DateTime.UtcNow.AddHours(UtcOffset).TimeOfDay;
        DayOfWeek dow= DateTime.UtcNow.AddHours(UtcOffset).DayOfWeek;
    

        TimeSpan qp = new TimeSpan(4, 0, 0);
        TimeSpan qe = new TimeSpan(4, 0, 0);
        int scheduleid=-1;
        int scheduleide = -1;
        int scheduleidp = -1;

        string queryStringParticularDay = "SELECT dbo.scheduler.scheduleid,dbo.scheduler.start, dbo.scheduler.duration FROM dbo.scheduler WHERE (dbo.scheduler.UserId = " + userid + ") AND (DayOfWeek=" + ((int)dow).ToString() + ") ORDER BY dbo.scheduler.start";
        string queryStringEverDay = "SELECT dbo.scheduler.scheduleid,dbo.scheduler.start, dbo.scheduler.duration FROM dbo.scheduler WHERE (dbo.scheduler.UserId = " + userid + ") ORDER BY dbo.scheduler.start";
        
        
    
        SqlCommand commandp = new SqlCommand(queryStringParticularDay, connection);

        SqlDataReader readerp = commandp.ExecuteReader();
        if (readerp.HasRows)
        {
            // Call Read before accessing data. 
            while (readerp.Read())
            {

                if (scheduleidp < 0) scheduleide = readerp.GetInt32(0);
                if (readerp.IsDBNull(1)) continue;
                qp = readerp.GetTimeSpan(1);

                if (qp > myTimeNow)
                {
                    expires = DateTime.UtcNow.AddHours(UtcOffset) + (qp - myTimeNow);
                    break;
                }

                scheduleidp = readerp.GetInt32(0);

            }
        }
        readerp.Close();

        SqlCommand commande = new SqlCommand(queryStringEverDay, connection);
        SqlDataReader readere = commande.ExecuteReader();

        if (readere.HasRows)
        {
            
            while (readere.Read())
            {
                if (scheduleide < 0) scheduleide = readere.GetInt32(0);
                if (readere.IsDBNull(1)) continue;
                qe = readere.GetTimeSpan(1);

                if (qe > myTimeNow)
                {
                    expires = DateTime.UtcNow.AddHours(UtcOffset) + (qe - myTimeNow);
                    break;
                }
            scheduleide = readere.GetInt32(0);

            }
        }

       
        // Call Close when done reading.
        readere.Close();
        if (scheduleidp>0 && qp < qe) scheduleid = scheduleidp;
        else scheduleid = scheduleide;
      


        return scheduleid;

    }

    private int GetPlaylistid(SqlConnection connection, string userid,int scheduleid)
    {
        string queryString = "SELECT playlistid FROM dbo.scheduled_playlists WHERE (scheduleid =" + scheduleid.ToString()+ ") AND (userid =" + userid + ")";
        int playlistid = 1; 
        SqlCommand command = new SqlCommand(queryString, connection);
        SqlDataReader reader = command.ExecuteReader();
        if (reader.Read())
        {
            if (!reader.IsDBNull(0))playlistid = reader.GetInt32(0);

        }

        // Call Close when done reading.
        reader.Close();


        return playlistid;
    }

    private void readtracks(SqlConnection connection, string userid, int playlistid, short ratiolocal, string ruserid, string rplaylistid, short ratioremote)
    {

        string queryStringLocal = "SELECT [content],[duration],[Volume] FROM [animations] Where ([Row] > 0) AND ([UserID] =" + userid + ") AND ([playlistid] = " + playlistid + ") ORDER BY Row";
        string queryStringRemote = "SELECT [content],[duration],[Volume] FROM [animations] Where ([Row] > 0) AND ([UserID] =" + ruserid + ") AND ([playlistid] = " + rplaylistid + ") ORDER BY Row";
        SqlCommand command = new SqlCommand(queryStringLocal, connection);
        List<Track> lstL = new List<Track>();
        SqlDataReader readerL = command.ExecuteReader();
        while (readerL.Read())
        {
            if (readerL.IsDBNull(0)) break;
            lstL.Add(new Track(readerL.GetString(0), readerL.GetInt32(1), readerL.GetInt32(2)));

        }
        
        readerL.Close();
        command = null;
        
        command = new SqlCommand(queryStringRemote, connection);
        SqlDataReader readerR=null;
   //     try
    //    {

            readerR = command.ExecuteReader();
       
       
    
        int dur;
        // Call Read before accessing data. 
        short indexremote=0;
   
        short i = 0;
        if (!readerR.HasRows) return;
        
        while (readerR.Read())
        {
            if (readerR.IsDBNull(0)) break;
            string trackR = readerR.GetString(0);
            dur = 0;
            if (readerR.IsDBNull(1)) pl.contents.Add(new Playlist.Track(trackR));
            else
            {
                dur = readerR.GetInt32(1);
                pl.contents.Add(new Playlist.Track(trackR, dur));
            }
            
            if (++indexremote < ratioremote) continue;
            indexremote = 0;

            for (int indexlocal = 0; indexlocal < ratiolocal; indexlocal++)
            {
                   if(i >= lstL.Count)i = 0;
                   pl.contents.Add(new Playlist.Track(lstL[i].content, lstL[i].dur));
                   i++;
             
            }
        
         }
      
      
      //  }
     //   catch (Exception ee)
     //   {

          
     //   }
        if (readerR!=null) readerR.Close();
        
    }

    
    
    private void readtracks(SqlConnection connection,string userid, int playlistid)
    {
      
        string queryString = "SELECT [content],[duration],[Volume] FROM [animations] Where ([Row] > 0) AND ([UserID] =" + userid + ") AND ([playlistid] = "+playlistid+") ORDER BY Row";

     
            SqlCommand command = new SqlCommand(queryString, connection);
         

            SqlDataReader reader = command.ExecuteReader();
            int dur;
            // Call Read before accessing data. 
            while (reader.Read())
            {
               if (reader.IsDBNull(0)) break;
               string  track = reader.GetString(0);
               dur = 0;
               if (reader.IsDBNull(1)) pl.contents.Add(new Playlist.Track(track)); 
               else
               {
                   dur = reader.GetInt32(1);
                   pl.contents.Add(new Playlist.Track(track, dur));
               }
           
               
            }

            // Call Close when done reading.
            reader.Close();
        
    }

    public bool IsReusable
    {
        get
        {
            return true;
        }
    }

}
