package com.sr105.wirelessdetails;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtraAppInfo {
    private final String TAG = ExtraAppInfo.class.getSimpleName();

    private final Context mContext;

    public ExtraAppInfo(@NonNull Context context) {
        mContext = context;
    }

    /**
     * Get the build time of the current application. Android
     * doesn't store this, but since APKs are zip files, we can
     * grab the time of the classes.dex file which should be rebuilt
     * whenever the application is rebuilt.
     * From: http://stackoverflow.com/a/7608719/47078
     *
     * @return Date when current application was built
     */
    public Date buildTime()
    {
        return buildTime(apkPath());
    }

    public Date buildTime(String apkPath)
    {
        Date date = null;
        try
        {
            ZipFile zipFile = new ZipFile(apkPath);
            ZipEntry zipEntry = zipFile.getEntry("classes.dex");
            date = new java.util.Date(zipEntry.getTime());
            zipFile.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return date;
    }

    public String apkPath()
    {
        try
        {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), 0);
            return applicationInfo.sourceDir;
        }
        catch (Exception e)
        {
            Log.e(TAG, "", e);
        }
        return null;
    }
}
