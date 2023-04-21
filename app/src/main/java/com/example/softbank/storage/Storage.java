package com.example.softbank.storage;

import com.example.softbank.activity.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    //储存危险app
    public static List<AppInfo> packageNames = new ArrayList<>();

    public static boolean isApp(String packageName) {
        for (AppInfo appInfo : packageNames) {
            if (appInfo.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
