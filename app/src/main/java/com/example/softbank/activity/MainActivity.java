package com.example.softbank.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.util.Log;

import com.example.softbank.BindingActivity;
import com.example.softbank.databinding.DbdhgefdejrBinding;

public class MainActivity extends BindingActivity<DbdhgefdejrBinding> {
    private final String tag = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermission(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            initTime();
        }
    }

    private void initTime() {
        CountDownTimer timer = new CountDownTimer(1000L * 3, 1000L) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        };
        timer.start();
    }

    //默认短信
    void initPermission(Activity activity) {
        String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
        //获取手机当前设置的默认短信应用的包名
        String packageName = activity.getPackageName();
        if (defaultSmsApp == null) {
            initTime();
            Log.e(tag, "defaultSmsApp null");
            return;
        }
        if (!defaultSmsApp.equals(packageName)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName);
            startActivityForResult(intent, 10);
        } else {
            initTime();
        }
    }
}
