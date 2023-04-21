package com.xframe.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageManagerUtil {
    //获取版本号
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager packagemanager = context.getPackageManager();
            PackageInfo info = packagemanager.getPackageInfo(context.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return versionCode;

    }


    //获取版本名
    public static String getApkName(Context context) {
        String versionName = "0";
        try {
            PackageManager packagemanager = context.getPackageManager();
            PackageInfo info = packagemanager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return versionName;
    }
}
