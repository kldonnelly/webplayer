package com.isb.webplayer;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;



public class PersistJobService extends JobService {

    private static final String YOUR_APP_PACKAGE_NAME = "com.isb.webplayer";


    void ManageActivity(ActivityManager activityManager)
    {


        Calendar rightNow=Calendar.getInstance();

        int  now = rightNow.get(Calendar.HOUR_OF_DAY);

        if (now < 4 ) return;
        try{
            // The first in the list of RunningTasks is always the foreground task.
            ActivityManager.RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);
            //   List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
            String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();

            Log.d("PersistService","Foreground Task " + foregroundTaskPackageName);

            // Check foreground app: If it is not in the foreground... bring it!
            if (!foregroundTaskPackageName.equals(YOUR_APP_PACKAGE_NAME)){
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(YOUR_APP_PACKAGE_NAME);
                Log.d("PersistService", "Starting");
                LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(LaunchIntent);
            }
        }
        catch(Exception ex)
        {
            Log.d("PersistService", ex.getMessage());
        }


    }

    @Override
    public boolean onStartJob(JobParameters params) {

        final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
      try{
          ManageActivity(activityManager);
          Util.scheduleJob(getApplicationContext(),90); // reschedule the job
      }
      catch(Exception ex){

      }


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
