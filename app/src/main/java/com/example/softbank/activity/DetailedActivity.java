package com.example.softbank.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.softbank.BindingActivity;
import com.example.softbank.R;
import com.example.softbank.databinding.GcdabdedejoBinding;
import com.example.softbank.storage.Storage;

public class DetailedActivity extends BindingActivity<GcdabdedejoBinding> {
    private int position;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        if (position != -1 && Storage.packageNames.size() != 0) {
            AppInfo appInfo = Storage.packageNames.get(position);
            initData(appInfo);
            packageName = appInfo.getPackageName();
            initClick();
        }
    }

    private void initClick() {
        getBinding().remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall(packageName);
                Storage.packageNames.remove(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences s = getSharedPreferences("packageManager", Context.MODE_PRIVATE);
        if (Storage.packageNames.size() == 0) {
            SharedPreferences.Editor editor = s.edit();
            editor.clear();
            editor.apply();
        }
    }

    private void uninstall(String packageName) {
        Intent intent = new Intent();
        Uri uri = Uri.parse("package:" + packageName);//获取删除包名的URI
        intent.setAction(Intent.ACTION_DELETE);//设置我们要执行的卸载动作
        intent.setData(uri);//设置获取到的URI
        startActivity(intent);
    }

    private void initData(AppInfo appInfo) {
        getBinding().name.setText(appInfo.getAppName());
        String result = getString(R.string.dajejcdeefo1) + " " + appInfo.getVersionName();//对应xml中定义的123顺序
        getBinding().version.setText(result);
        getBinding().pkgTv.setText(appInfo.getPackageName());
    }
}
