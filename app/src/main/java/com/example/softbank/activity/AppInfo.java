package com.example.softbank.activity;

import org.json.JSONException;
import org.json.JSONObject;

public class AppInfo {
    String label;
    String packageName;

    String versionName;

    String appName;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setLabel(String toString) {
        this.label = toString;
    }

    public void setPackage_name(String packageName) {
        this.packageName = packageName;
    }

    public  String getJson() throws JSONException {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("label",label);
        jsonObject.put("packageName",packageName);
        jsonObject.put("versionName",versionName);
        jsonObject.put("appName",appName);
        return jsonObject.toString();
    }
}
