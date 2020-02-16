package com.vil.vil_bot.helpers;

import android.app.ActivityManager;
import android.content.Context;

import java.util.HashMap;
import java.util.List;

public class Helper {

    public static HashMap<String, String> languages = new HashMap<>();

    static {
        languages.put("english", "en-IN");
        languages.put("marathi", "mr-IN");
        languages.put("hindi", "hi-IN");
        languages.put("gujarati", "gu-IN");
        languages.put("kannada", "kn-IN");
        languages.put("punjabi", "pa-IN");
        languages.put("malyalam", "ml-IN");
        languages.put("tamil", "ta-IN");
        languages.put("telugu", "te-IN");
        languages.put("bengali", "bn-IN");
    }


    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
