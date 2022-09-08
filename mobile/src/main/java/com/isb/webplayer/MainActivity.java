package com.isb.webplayer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.hardware.Camera;
import android.location.Location;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;


import android.preference.EditTextPreference;
import android.provider.Settings;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.app.Activity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;


import java.io.File;
import java.io.FileInputStream;


import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;


import de.jockels.open.Environment2;
import de.jockels.open.NoSecondaryStorageException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.isb.webplayer.ui.login.LoginFragment;
import com.sprylab.android.widget.TextureVideoView;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import net.pubnative.AdvertisingIdClient;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.common.net.InternetDomainName;


public class MainActivity extends AppCompatActivity {


    int Debug = 0;
    WebCam uwc;
    AtomicInteger cameras = new AtomicInteger(-2);
    Identifiers ids = new Identifiers();

    private CameraPreview mPreview;
    private int PictureIndex = 1;

    private TextView tvmsg;
    private static SettingsFragment sf = null;
    private static LoginFragment lf=null;
    DownloadTask dwnltsk = null;
    private Paint mPaint;
    float mTextWidth;
    TextView tvMarq;
    private BroadcastReceiver hdmiMonitor = null;
    private boolean mPaused;

    int smiltextcontenindex = -1;
    Duration durThread;

    TextureVideoView mVideoView;
    WebView mWebView;
    WebView mwebViewInteractive;
    ImageView mImageView;
    DisplayArray m_disparray;
    String contentonmediadrive;
    File videofiles[];
    IPlaylistDownload dwnloadprms;
    String error;
    int videoplayfailcount = 0;
    int remotecontenindex = -1;
    // String email;
    //  String playlistname;
    String versionName;
    long r;

    //  private String android_id;
    private String AdID;
    //  private String latitude,longitude;
    private FusedLocationProviderClient mFusedLocationClient;
    spotify m_spotify;

    String baseurl;
    String DomainName;
    String title;
    String password;
    int menu_interactive_index = 0;
    private ArrayList<IInteractiveMenuInjection> PageFinished = new ArrayList<>();
  //  private ArrayList<IInteractiveMenuInjection> PageStarted = new ArrayList<>();
    IInteractiveMenuInjection imenulocal = new InteractiveMenuLocal();

    MotionEvent me=null;

    private final int interval = 30000; // 30 Second
    String interactiveurl = null;
    private Handler handler_interactive = new Handler();

    private Runnable runnable = new Runnable() {
        public void run() {

            if (mwebViewInteractive != null) mwebViewInteractive.loadUrl(interactiveurl);

        }
    };


    private class SSLTolerentWebViewClient extends WebViewClient {

        private Activity activity = null;

        public SSLTolerentWebViewClient(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            String url = request.getUrl().getPath();
            Log.d("Activity", "shouldOverrideUrlLoading url=" + url);
            if (url.indexOf(interactiveurl) > -1) return false;

            //   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //    activity.startActivity(intent);
            return false;
        }

        @Override

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }
    }


    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void LocalOpenDocument() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {

            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            File file = new File(contentonmediadrive + File.separator + "playlist.xml");
            FileInputStream _stream = new FileInputStream(file);
            //	FileInputStream _stream=openFileInput (contentonmediadrive+File.separator+"playlist.xml");
            doc = dBuilder.parse(_stream);

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }


        String nodename = "no name";
        Element rootnode;
        NodeList seqnodes;
        NodeList media;
        NodeList metanodes;
        NodeList regionnodes;
        Node src;
        Node dur;
        Node mod;
        Node type;

        rootnode = doc.getDocumentElement();
        NamedNodeMap attrs = rootnode.getAttributes();
        if (attrs.getLength() == 0) {
            if (Debug > 0) tvmsg.setText("Failed to root node attributes");
            return;
        }

        Node debug = attrs.getNamedItem("Debug");

        if (debug != null) Debug = Integer.parseInt(debug.getNodeValue());

        if (Debug == 0) tvmsg.setVisibility(View.GONE);
        Node Title = attrs.getNamedItem("title");
        title = Title.getNodeValue();

        metanodes = doc.getElementsByTagName("meta");


        dwnloadprms.MediaContentList().clear();
        dwnloadprms.TextContentList().clear();


        regionnodes = doc.getElementsByTagName("region");

        for (int index = 0; index < regionnodes.getLength(); index++) {
            attrs = regionnodes.item(index).getAttributes();
            Node textColor = attrs.getNamedItem("textColor");
            attrs.getNamedItem("textFontSize");
            Node textBackGroundColor = attrs.getNamedItem("textBackgroundColor");
            tvMarq.setTextColor(Color.parseColor(textColor.getNodeValue()));
            int bk = Color.parseColor(textBackGroundColor.getNodeValue());

            Node backgroundOpacity = attrs.getNamedItem("backgroundOpacity");
            float alpha = Float.parseFloat(backgroundOpacity.getNodeValue().replace("%", ""));
            alpha = alpha / 100;
            tvMarq.setBackgroundColor(adjustAlpha(bk, alpha));

        }


        seqnodes = doc.getElementsByTagName("seq");
        media = seqnodes.item(0).getChildNodes();

        for (int index = 0; index < media.getLength(); index++) {
            attrs = media.item(index).getAttributes();

            nodename = media.item(index).getNodeName();

            src = attrs.getNamedItem("src");
            dur = attrs.getNamedItem("dur");
            mod = attrs.getNamedItem("modified");
            type = attrs.getNamedItem("type");

            if (nodename.toLowerCase().contains("animation")) {
                if (dur != null)
                    dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), dur.getNodeValue(), media_type.swf));
                else
                    dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), media_type.swf));
            } else if (nodename.toLowerCase().contains("video")) {

                if (dur != null && mod != null)
                    dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), dur.getNodeValue(), mod.getNodeValue(), media_type.video));
                else
                    dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), media_type.video));
            } else if (nodename.toLowerCase().contains("img")) {

                if (dur != null && mod != null)
                    dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), dur.getNodeValue(), mod.getNodeValue(), media_type.image));
                else
                    dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), media_type.html));

            } else if (nodename.toLowerCase().contains("ref") && type != null) {

                if (type.getNodeValue().contains("html")) {
                    if (dur != null)
                        dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), dur.getNodeValue(), media_type.html));
                    else
                        dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), media_type.html));
                } else if (type.getNodeValue().contains("widget")) {
                    if (dur != null)
                        dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), dur.getNodeValue(), media_type.widget));
                    else
                        dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), media_type.widget));
                }

            } else {

                dwnloadprms.MediaContentList().add(new mediacontent(src.getNodeValue(), media_type.none));
            }


        }

        media = seqnodes.item(1).getChildNodes();
        //	tv.setText("got seq 2 number="+ media.getLength());
        for (int index = 0; index < media.getLength(); index++) {
            attrs = media.item(index).getAttributes();

            nodename = media.item(index).getNodeName();
            dur = attrs.getNamedItem("dur");
            if (nodename.toLowerCase().contains("smiltext")) {

                String desc = media.item(index).getTextContent();
                if (dur != null)
                    dwnloadprms.TextContentList().add(new textcontent(desc, dur.getNodeValue()));
                else dwnloadprms.TextContentList().add(new textcontent(desc));
            }
        }


        if (dwnloadprms.MediaContentList().size() > 1) {
            playnextOnUiThread();
        }


        if (dwnloadprms.TextContentList().size() > 0) {
            smiltextcontenindex = -1;
            textcontent stx = dwnloadprms.TextContentList().get(++smiltextcontenindex);
            tvMarq.setVisibility(View.VISIBLE);
            tvMarq.setText(stx.description);
            crawltext(stx.dur);
        }

    }

    @Override
    protected void onStart() {

        Intent hdmiStatus = registerReceiver(hdmiMonitor, new IntentFilter("android.intent.action.HDMI_PLUGGED"));
        ids.bHdmiSwitchSet = hdmiStatus.getBooleanExtra("state", false);
        Log.d("Activity", "Starting HdmiSwitchSet=" + ids.bHdmiSwitchSet);
        super.onStart();


        if (getLastLocation() < 1) {
            r = RemoteRefreshSmil();
            Log.d("Activity", "RemoteRefreshSmil r=" + r + " No Location");
        }


        try {
            if (m_spotify != null) m_spotify.start(this);
        } catch (Exception e) {
            Log.d("Activity", "Spotify " + e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
        Log.d("Activity", "Stopping");
        if (m_spotify != null) m_spotify.Stop();
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X-", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    public void setHideyBar() {

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;


        // Navigation bar hiding:  Backwards compatible to ICS.

        newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        // Status bar hiding: Backwards compatible to Jellybean

        newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;


        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.

        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);


    }

    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        File contentonmediadrivetemp = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "media");
        contentonmediadrivetemp.mkdirs();
        File contentonsecmediadrivetemp = null;


        try {
            contentonsecmediadrivetemp = new File(Environment2.getSecondaryExternalStorageDirectory().getPath() + File.separator + "media");
            contentonsecmediadrivetemp.mkdirs();
        } catch (NoSecondaryStorageException e) {

            e.printStackTrace();
        }


        if (contentonsecmediadrivetemp != null && contentonsecmediadrivetemp.exists()) {
            contentonmediadrive = contentonsecmediadrivetemp.getAbsolutePath();
            videofiles = contentonsecmediadrivetemp.listFiles();

        } else if (contentonmediadrivetemp.exists()) {
            contentonmediadrive = contentonmediadrivetemp.getAbsolutePath();
            videofiles = contentonmediadrivetemp.listFiles();

        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        tvmsg = this.findViewById(R.id.textViewdebug);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }


        getSupportActionBar().hide();

        if (shouldAskPermissions()) {
            askPermissions();
        }

        try {
            versionName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mwebViewInteractive = this.findViewById(R.id.webviewinteractive);

        mwebViewInteractive.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                me=event;
                handler_interactive.removeCallbacksAndMessages(null);
                handler_interactive.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler_interactive.postDelayed(runnable, interval);
                Log.d("Activity", "interactive clicked");
                return false;
            }
        });

        setHideyBar();

        ids.playlist = "";

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ids.playlist = sharedPref.getString("playlist", "none");

        ids.email = "ctvtestdrive@gmail.com";
        String email2 = sharedPref.getString("email", ids.email);

        interactiveurl = sharedPref.getString("interactiveurl", null);
        //460dp height 1920 × 1080 1.777 activity_main.xml

        float h = convertPixelsToDp((float) 607.5, this);

        Log.d("Activity", "607.5 pixels = " + h + "dp");
        ViewGroup screen = findViewById(R.id.screen);
        final ProgressBar simpleProgressBar = (ProgressBar) this.findViewById(R.id.progressBar);

        if (interactiveurl != null && !interactiveurl.isEmpty()) {


            IInteractiveMenuInjection imenuremote = new InteractiveMenuRemote(interactiveurl + "/inject.js");
            //   Log.d("Activity", "inject remote="+imenuremote.getjavascript());
            PageFinished.add(imenulocal);
            PageFinished.add(imenuremote);

         //   imenuremote = new InteractiveMenuRemote(interactiveurl + "/pagestarded.js");
            //   Log.d("Activity", "inject remote="+imenuremote.getjavascript());
            //PageStarted.add(imenulocal);
           // PageStarted.add(imenuremote);


            screen.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 607));

            mwebViewInteractive.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    Log.d("Activity", "Progress = " + newProgress);
                    if(simpleProgressBar!=null) {
                        simpleProgressBar.setProgress(newProgress);
                        if(newProgress<20)simpleProgressBar.setVisibility(View.VISIBLE);
                        else  if(newProgress>90)simpleProgressBar.setVisibility(View.GONE);
                    }

                }
            });

            mwebViewInteractive.setWebViewClient(new SSLTolerentWebViewClient(this) {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    // Handle the error
                    simpleProgressBar.setVisibility(View.VISIBLE);
                    Log.d("Activity", "Error Url description = " + description);

                }



                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {

                    Log.d("Activity", "url=" + url);
                    if(simpleProgressBar!=null)simpleProgressBar.setVisibility(View.VISIBLE);
                    if(me!=null && url.length() > 5 && interactiveurl.length() > 5 && url.contains(interactiveurl) != true){

                   //     String loading = PageStarted.get(0).getjavascript(0, 0, "loading...");
                  //      String temp = PageStarted.get(1).getjavascript(0, 0, "loading...");

                    //    if (temp != null && temp.contains("loading")) loading = temp;
                     //   Log.d("Activity", "inject=" + loading);
                     //   view.loadUrl("javascript:(function() {" + loading + "})()");

                    }
                    super.onPageStarted(view, url, favicon);
                }


                @Override
                public void onPageFinished(WebView view, String url) {
                    if (url.length() > 5 && interactiveurl.length() > 5 && url.contains(interactiveurl) != true) {
                        Log.d("Activity", "interactive Url=" + url);
                        Log.d("Activity", "Home Url=" + interactiveurl);
                        String inject = PageFinished.get(0).getjavascript();
                        String temp = PageFinished.get(1).getjavascript();

                        if (temp != null) inject = temp;
                        Log.d("Activity", "inject=" + inject);

                        view.loadUrl("javascript:(function() {" + inject + "})()");
                        m_disparray.MuteDisplay();
                        handler_interactive.removeCallbacksAndMessages(null);
                        handler_interactive.postAtTime(runnable, System.currentTimeMillis() + interval);
                        handler_interactive.postDelayed(runnable, interval);
                    }
                    else  m_disparray.UnMuteDisplay();

                    if(simpleProgressBar!=null)simpleProgressBar.setVisibility(View.GONE);
                    super.onPageFinished(view, url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view,  WebResourceRequest request) {

                    Log.d("Activity", "requesturl=" + request.getUrl());
                    return false;
                }


            });


            mwebViewInteractive.getSettings().setJavaScriptEnabled(true);
            WebSettings settings = mwebViewInteractive.getSettings();
            settings.setDomStorageEnabled(true);

            mwebViewInteractive.loadUrl(interactiveurl);
            mwebViewInteractive.setVisibility(View.VISIBLE);

        } else {
            mwebViewInteractive.setVisibility(View.GONE);


            screen.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

        }


        if (email2.contains("@")) ids.email = email2.trim();
        else {
            Account[] accounts = AccountManager.get(this).getAccounts();
            if (accounts.length > 0) ids.email = accounts[0].name.trim();
        }

        baseurl = sharedPref.getString("baseurl", null);
        try {
            DomainName=InternetDomainName.from(new URL(baseurl).getHost()).topPrivateDomain().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        password = sharedPref.getString("password","i$b$ignage");
        boolean debug = sharedPref.getBoolean("debug", false);

        if (debug) Debug = 3;

        mVideoView = this.findViewById(R.id.videoView1);

        m_disparray = new DisplayArray();

        DisplayVideoView dvv = new DisplayVideoView(mVideoView, contentonmediadrive, new OnEventListener<Integer>() {

            @Override
            public void onSuccess(Integer result) {

                if (result == result_code.play_next.getValue()) {
                    playnextOnUiThread();
                    if (videoplayfailcount-- < 0 && Debug == 0) tvmsg.setVisibility(View.GONE);
                } else if (result == result_code.play_next_later.getValue()) {
                    Duration(10000);
                    if (videoplayfailcount++ > 0) {
                        tvmsg.setVisibility(View.VISIBLE);

                    }
                }
                Log.d("Activity", "Video play success = " + result);
            }

            @Override
            public void onFailure(Exception e) {
                //  settext(e.getMessage());
                // tvmsg.setVisibility(View.VISIBLE);
                Log.d("Activity", "Video play failed=" + e.getMessage());
                Duration(10000);
            }
        });

        m_disparray.Add(dvv);

        ids.android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        ids.mac_address = getMacAddr();
        Log.d("Activity", "ANDROID_ID=" + ids.android_id);

        uwc = new WebCam(this, cameras);

        if (Debug < 1) tvmsg.setVisibility(View.GONE);
        settext(cameras.toString() + " cameras");


        String uploadUrl = sharedPref.getString("uploadUrl", null);

        Log.d("Activity", "Upload Url=" + uploadUrl);

        if (cameras.get() > 0) uwc.Initialize(uploadUrl, this.ids);


        mWebView = this.findViewById(R.id.webView1);
        File dir = this.getCacheDir();
        mWebView.getSettings().setAppCachePath(dir.getAbsolutePath());

        DisplayWebView dwv = new DisplayWebView(mWebView, contentonmediadrive, new OnEventListener<Integer>() {

            @Override
            public void onSuccess(Integer result) {

            }

            @Override
            public void onFailure(Exception e) {

                Duration(50000);
                Log.d("Activity", e.getMessage());

            }
        });
        m_disparray.Add(dwv);

        tvMarq = this.findViewById(R.id.textView1);


        tvMarq.setTextColor(Color.WHITE);
        tvMarq.setBackgroundColor(Color.RED);
        tvmsg.setTextColor(Color.RED);

        mImageView = this.findViewById(R.id.imageView1);

        DisplayImageView div = new DisplayImageView(mImageView, contentonmediadrive, new OnEventListener<Integer>() {

            @Override
            public void onSuccess(Integer result) {

                if (result == result_code.play_next_later.getValue()) Duration(10000);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        m_disparray.Add(div);

        //29 July 2020     mImageView.setVisibility(View.GONE);

        AdvertisingIdClient.getAdvertisingId(this, new AdvertisingIdClient.Listener() {

            @Override
            public void onAdvertisingIdClientFinish(AdvertisingIdClient.AdInfo adInfo) {
                // Callback when process is over
                AdID = adInfo.getId();
                Log.d("Activity", "AdvertisingIdClient=" + AdID);
            }

            @Override
            public void onAdvertisingIdClientFail(Exception exception) {
                // Callback when process fails
            }
        });


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1);
        mPaint.setStrokeCap(Paint.Cap.ROUND);


        //   m_spotify= new spotify();


    }


    private void CheckShutdown() {

        if (dwnloadprms.ShutDownTime() < 0) return;

        Calendar rightNow = Calendar.getInstance();

        int now = rightNow.get(Calendar.HOUR_OF_DAY) * 60 + rightNow.get(Calendar.MINUTE);

        int diff = now - dwnloadprms.ShutDownTime();

        if ((diff > 0) && (diff < 10)) {
            //  this.finishAffinity();
            CloseApp();
            //  Process.killProcess( Process.myPid());
            //  System.exit(0);

        }


    }

    private void playnextx() {
        CheckShutdown();

        if (dwnloadprms.ExpireTime().compareTo(new Date()) < 0) {

            RemoteRefreshSmil();
            return;

        }

        if (Debug > 0)
            settext("exp=" + dwnloadprms.ExpireTime().toString() + "\nnow=" + new Date().toString() + "\nemail=" + ids.email + "\nDebug=" + Integer.valueOf(Debug).toString() + "\n" + contentonmediadrive + "\nversion " + versionName + "\nplaylistname=" + ids.playlist + "\nBaseUrl=" + baseurl + "\nrefreshsmil=" + Long.toString(r) + "\nerror=" + error + "\nshutdown=" + Integer.toString(dwnloadprms.ShutDownTime()) + "\nandroidid=" + ids.android_id + "\nAddID=" + AdID);
        //    if(Debug>2)tvmsg.setText("exp="+dwnloadprms.expires.toString()+"\nnow="+new Date().toString()+"\nemail="+email+"\nDebug="+Integer.valueOf(Debug).toString()+"\ntitle="+title+"\nandroidid="+android_id+"\nAddID="+AdID);


        if (durThread != null) durThread.interrupt();

        if (++remotecontenindex > dwnloadprms.MediaContentList().size() - 1) {
            remotecontenindex = 0;
            if (PictureIndex++ > dwnloadprms.MediaContentList().size() - 1) PictureIndex = 0;
        }

        try {


            mediacontent mc = dwnloadprms.MediaContentList().get(remotecontenindex);

            Log.d("MainActivity", mc.url + ", Dur = " + mc.dur);

            if (mc.type != media_type.video) Duration((int) mc.dur);

            m_disparray.ShowDisplay(mc);
        } catch (Exception e) {
          //  Log.d("MainActivity", e.getMessage());
            Duration((int) 10000);
        }


        if (remotecontenindex == PictureIndex) {


            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                // Do something after 5s = 5000ms
                uwc.TakePictures();
            }, 2000);

        }


    /*
        if(m_spotify != null) {

            m_spotify.Resume();

            if (dwnloadprms.backgroundaudio != null && m_spotify.GetUri() != null) {

                String[] values = dwnloadprms.backgroundaudio.split(":");
                int index = values.length - 1;

                if (!values[index].equals(m_spotify.GetUri())) {

                    Log.d("MainActivity", values[index] + "," + m_spotify.GetUri());
                    m_spotify.SetURI(dwnloadprms.backgroundaudio);
                }
            }
        }

     */
    }


    public void playnextOnUiThread() {

        runOnUiThread(() -> {
            // UI code goes here
            playnextx();
        });


    }

    public void LocalOpenDocumentUIThread() {

        runOnUiThread(() -> {
            // UI code goes here
            LocalOpenDocument();
        });
    }

    /**
     *
     */

    void StartDownLoadTask(ArrayList<mediacontent> mediacontentlist) {
        Log.d("Activity", "StartDownLoadTask");

        ArrayList<String> downloadstringArray = new ArrayList<>();

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


        if (downloadstringArray.size() > 0) {
            Log.d("Activity", "StartDownLoadTask size=" + downloadstringArray.size() + " Drive=" + contentonmediadrive);


            dwnltsk = new DownloadTask(contentonmediadrive, new OnEventListener<Integer[]>() {

                @Override
                public void onSuccess(Integer[] result) {
                    tvmsg.setText(String.format("%d/%d %d %%", result[1], result[0], result[2]));
                }

                @Override
                public void onFailure(Exception e) {

                }
            });
            dwnltsk.execute(downloadstringArray.toArray(new String[downloadstringArray.size()]));
        }

    }

    long RemoteRefreshSmil() {
        dwnloadprms = new RemoteDownloadSMILPlaylist(contentonmediadrive, new OnEventListener<Integer>() {
            @Override
            public void onSuccess(Integer result) {


                Log.d("RemoteRefreshSmil", "succeeded result=" + result + dwnloadprms.MediaContentList().size() + " items");


                playnextOnUiThread();
                setHideyBar();
                getSupportActionBar().hide();
                mWebView.clearCache(true);

                StartDownLoadTask(dwnloadprms.MediaContentList());


                if (dwnloadprms.TextContentList().size() > 0) {

                    smiltextcontenindex = -1;
                    textcontent stx = dwnloadprms.TextContentList().get(++smiltextcontenindex);
                    tvMarq.setVisibility(View.VISIBLE);
                    tvMarq.setText(stx.description);
                    Log.d("RemoteRefreshSmil", "crawl text on message=" + stx.description);
                    crawltext(stx.dur);
                }


            }

            @Override
            public void onFailure(Exception e) {

                Log.d("RemoteRefreshSmil", "failed opening local document e=" + e.getMessage());


                LocalOpenDocumentUIThread();


                //  Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

//        dwnloadprms.tvmsg=tvmsg;

        //   dwnloadprms.tvMarq=tvMarq;
        //    dwnloadprms.contentonmediadrive=contentonmediadrive;

        URL playlist;
        int r1 = 0;
        videoplayfailcount = 0;

        error = "none";
        smiltextcontenindex = -1;

        String boxid = ids.mac_address;

        if (boxid.length() < 2) boxid = ids.android_id;

        String url;

        if (ids.location != null) {
            url = baseurl + "?email=" + ids.email + "&playlistname=" + ids.playlist.replace(" ", "%20") + "&error=" + error.replace(" ", "%20") + "&id=" + boxid + "&hdmi=" + ids.bHdmiSwitchSet + "&latitude=" + ids.location.getLatitude() + "&longitude=" + ids.location.getLongitude();

        } else {
            url = baseurl + "?email=" + ids.email + "&playlistname=" + ids.playlist.replace(" ", "%20") + "&error=" + error.replace(" ", "%20") + "&id=" + boxid + "&hdmi=" + ids.bHdmiSwitchSet;

        }

        Log.d("RemoteRefreshSmil", "Connecting to Server url=" + url);
        try {


            playlist = new URL(url);

            dwnloadprms.Start(playlist);


        } catch (MalformedURLException e) {
            r1 = -1;
            e.printStackTrace();
        }


        return r1;

    }

    public void toggleHidyBar() {


        if (getSupportActionBar().isShowing()) getSupportActionBar().hide();
        else getSupportActionBar().show();
    }


    public void settext(final String text) {

        runOnUiThread(() -> {


            if(tvmsg!=null)tvmsg.setText(text);

        });
    }

    public void Duration(final int timeout) {

        if (durThread != null) {
            durThread.interrupt();
            durThread = null;
        }

        durThread = new Duration(timeout, () -> playnextOnUiThread());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getActionMasked()) {
            case (MotionEvent.ACTION_DOWN):
                if (Debug > 0) settext("Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE):
                if (Debug > 0) settext("Action was MOVE");
                toggleHidyBar();
                return true;
            case (MotionEvent.ACTION_UP):
                if (Debug > 0) settext("Action was UP");
                //  toggleHidyBar();
                //  setHideyBar();
                return true;
            case (MotionEvent.ACTION_CANCEL):
                if (Debug > 0) settext("Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                if (Debug > 0) settext("Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return super.onTouchEvent(event);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    static Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            String filePath = msg.getData().getString("file"); // You can change this according to your requirement.

        }
    };


    /**
     * Get activity instance from desired context.
     */


    public static class SettingsFragment extends PreferenceFragmentCompat {


        public static Activity getActivity(Context context) {
            if (context == null) return null;
            if (context instanceof Activity) return (Activity) context;
            if (context instanceof ContextWrapper)
                return getActivity(((ContextWrapper) context).getBaseContext());
            return null;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {


            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference button = findPreference(getString(R.string.hide_button));
            button.setOnPreferenceClickListener(preference -> {
                //code for what you want it to do
                Activity ac = getActivity(preference.getContext());

                if (ac != null) {
                    View viewf = ac.findViewById(R.id.pref_container);
                    viewf.setVisibility(View.GONE);
                }
                if (sf != null) {
                    getFragmentManager().beginTransaction()
                            .hide(sf)
                            .commit();
                }
                if (lf != null) {
                    getFragmentManager().beginTransaction()
                            .hide(lf)
                            .commit();
                }
                return true;
            });

            Preference editpassword = findPreference("password");
            /*
            EditTextPreference pref=(EditTextPreference)editpassword;

            editpassword.setOnBindEditTextListener(
                    new EditTextPreference.OnBindEditTextListener() {
                        @Override
                        public void onBindEditText(@NonNull EditText editText) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    });
            */
            editpassword.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue.toString().length() > 4) {
                    //

                    return true;
                }
                 else{
                    // invalid you can show invalid message
                    Toast.makeText(getContext().getApplicationContext(), "Password Too Short", Toast.LENGTH_SHORT).show();
                    return false;
                }


            });

        }

    }

    private void CloseApp() {
        Log.d("Activity", "Closing");
        settext("Closing");

        if(lf!=null ){
            getSupportFragmentManager().beginTransaction().remove(lf).commitAllowingStateLoss();
            lf=null;
        }

        if(sf!=null ){
            getSupportFragmentManager().beginTransaction().remove(sf).commitAllowingStateLoss();
            sf=null;
        }

        ViewGroup viewf = findViewById(R.id.pref_container);
        if(viewf!=null)viewf.removeAllViews();



     //   if(sf!=null)getSupportFragmentManager().beginTransaction().remove(sf).commitAllowingStateLoss();

        if (dwnltsk != null) {
            dwnltsk.cancel(false);

            tvmsg.setVisibility(View.VISIBLE);

            Log.d("Activity", "cancel download");

            try {

                dwnltsk.get();
            } catch (InterruptedException | ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        Log.d("Activity", "finishAffinity");
        try {
            this.finishAffinity();

            if (mWebView.getParent() != null) {
                ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            }


            mWebView.removeAllViews();
            mWebView.destroy();
        } catch (Exception e) {
            Log.d("Activity", e.getMessage());
        }


    }


    void StartCameraPreview() {
        StringBuilder err = new StringBuilder();
        Camera cam = uwc.getCameraInstance(0, err);


        if (cam != null) {
            mPreview = new CameraPreview(this, cam);

            FrameLayout preview = findViewById(R.id.pref_container);
            preview.addView(mPreview);
            preview.setVisibility(View.VISIBLE);
            mPreview.setVisibility(View.VISIBLE);
        }

    }


    void MenuTakePicture() {
        //   SurfaceView surfaceTexture = new SurfaceView(this);
        Log.d("Camera", "Menu TakePicture");

        uwc.ids.SaveLocal = true;
        uwc.TakePictures(0);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        View viewf;
        switch (item.getItemId()) {

            case R.id.action_close:

                CloseApp();

                return true;
            case R.id.action_debug:
                Debug = 2;
                tvmsg.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_settings:

                viewf = this.findViewById(R.id.pref_container);
                viewf.setVisibility(View.VISIBLE);

                if (sf == null) {
                    sf = new SettingsFragment();

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.pref_container, sf)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .show(sf)
                            .commit();

                }
                return true;
            case R.id.action_login:
                viewf = this.findViewById(R.id.pref_container);
                viewf.setVisibility(View.VISIBLE);
                if (lf == null) {
                    lf = new LoginFragment();
                    Bundle lfargs = new Bundle();
                    lfargs.putString("password", password);
                    lfargs.putString("DomainName",DomainName);
                    lf.setArguments(lfargs);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.pref_container, lf)
                            .commit();
                } else {

                    getSupportFragmentManager().beginTransaction()
                            .show(lf)
                            .commit();

                }
                return true;
            case R.id.action_hide:

                toggleHidyBar();
                //  setHideyBar();
                viewf = this.findViewById(R.id.pref_container);
                if (viewf != null) viewf.setVisibility(View.GONE);

                return true;
            case R.id.action_camera:
                StartCameraPreview();
                return true;

            case R.id.action_picture:
                MenuTakePicture();
                return true;

            case 10:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void crawltext(long dur) {

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);

        int xDest = dm.widthPixels;


        mPaint.setTextSize(tvMarq.getTextSize());
        mPaint.setTypeface(tvMarq.getTypeface());

        mTextWidth = mPaint.measureText(tvMarq.getText().toString());

        TranslateAnimation anim = new TranslateAnimation(xDest, -mTextWidth, 0, 0);
        anim.setDuration(dur);
        anim.setInterpolator(new LinearInterpolator());
        //   anim.setRepeatCount(100);
        anim.setFillAfter(true);
        tvMarq.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                try {
                    textcontent stx = dwnloadprms.TextContentList().get(smiltextcontenindex++);
                    tvMarq.setText(stx.description);
                    if (smiltextcontenindex >= dwnloadprms.TextContentList().size() - 1)
                        smiltextcontenindex = 0;
                    crawltext(stx.dur);
                } catch (Exception e) {
                    Log.d("Activity", e.getMessage());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    int getLastLocation() {

        Log.d("Activity", "Build Version=" + Build.VERSION.SDK_INT);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d("Activity", "no location permission");
                return 0;
            }
        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Task task = mFusedLocationClient.getLastLocation();


        task.addOnCompleteListener((OnCompleteListener<Location>) task1 -> {
            if (task1.isSuccessful()) {
                // Task completed successfully
                Location location = task1.getResult();
                if (location != null) {
                    // Logic to handle location object
                    ids.location = location;
                    String msg = "Updated Location: " +
                            ids.location.getLatitude() + "," +
                            ids.location.getLongitude();
                    Log.d("Activity", msg);
                } else {
                    Log.d("Activity", "Location null");
                }

            } else {
                // Task failed with an exception
                Exception exception = task1.getException();
                Log.d("Activity", exception.getMessage());
            }

            r = RemoteRefreshSmil();
            Log.d("Activity", "RemoteRefreshSmil r=" + r);


        });
        return 1;

    }


}
