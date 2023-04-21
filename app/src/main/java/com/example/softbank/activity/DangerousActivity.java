package com.example.softbank.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.softbank.BindingActivity;
import com.example.softbank.R;
import com.example.softbank.adapter.DangerousAdapter;
import com.example.softbank.databinding.HejgahbcebpBinding;
import com.example.softbank.recycler.ItemListenter;
import com.example.softbank.storage.Storage;

public class DangerousActivity extends BindingActivity<HejgahbcebpBinding> implements ItemListenter {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        DangerousAdapter dangerousAdapter = new DangerousAdapter(this, this, Storage.packageNames);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        getBinding().recyclerview.setLayoutManager(manager);
        getBinding().recyclerview.setAdapter(dangerousAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Storage.packageNames.size() == 0) {
            SharedPreferences s = getSharedPreferences("packageManager", Context.MODE_PRIVATE);
            if (Storage.packageNames.size() == 0) {
                SharedPreferences.Editor editor = s.edit();
                editor.clear();
                editor.apply();
            }
            finish();
        }
    }

    @Override
    public void onItemClick(int id, int position) {
        if (id == R.id.remove) {
            System.out.println("数量" + Storage.packageNames.size());
            //删除点击
            AppInfo appInfo = Storage.packageNames.get(position);
            System.out.println(appInfo.packageName);
            uninstall(appInfo.packageName);
            Storage.packageNames.remove(position);
        } else if (id == R.id.info) {
            Intent intent = new Intent(this, DetailedActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }


    private void uninstall(String packageName) {
        Intent intent = new Intent();
        Uri uri = Uri.parse("package:" + packageName);//获取删除包名的URI
        intent.setAction(Intent.ACTION_DELETE);//设置我们要执行的卸载动作
        intent.setData(uri);//设置获取到的URI
        startActivity(intent);

    }
}
