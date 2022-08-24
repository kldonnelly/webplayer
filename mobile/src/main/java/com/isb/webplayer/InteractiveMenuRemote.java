package com.isb.webplayer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class InteractiveMenuRemote extends AsyncTask<String, String, String> implements IInteractiveMenuInjection {

    HttpURLConnection urlConnection = null;
    String content = null;

    InteractiveMenuRemote(String url) {

        execute(url);

    }

    @Override
    protected String doInBackground(String... args) {

        String surl = args[0];
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(surl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }


        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {

        content = result;
        Log.d("Activity", "remote javascript=" + content);
    }

    @Override
    public String getjavascript() {

        if (!content.contains("html") && content.contains("createElement")) {

            return content;
        } else return null;
    }
    @Override
    public String getjavascript(boolean muted) {

        if (!content.contains("html") && content.contains("createElement")) {

            return content + "document.getElementsByTagName('video')[0].muted=true;";
        } else return null;
    }

    @Override
    public String getjavascript(int x, int y, String text) {

        if (!content.contains("html") && content.contains("createElement")) {

            return String.format(content, text, x, y);
        } else return null;

    }
}