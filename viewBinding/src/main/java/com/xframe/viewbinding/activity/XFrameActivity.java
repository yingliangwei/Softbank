package com.xframe.viewbinding.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.xframe.viewbinding.util.ViewBindingUtil;

public class XFrameActivity<Binding extends ViewBinding> extends Activity implements Runnable {

    private Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBinding(getClass());
        if (binding != null) {
            setContentView(binding.getRoot());
        } else {
            Log.e("XFrameActivity", "布局转换失败");
        }
    }

    /**
     * @param c 布局变量实体类
     */
    public void setBinding(Class<?> c) {
        this.binding = ViewBindingUtil.inflate(c, getLayoutInflater());
    }

    @NonNull
    public Binding getBinding() {
        return binding;
    }

    @Override
    public void run() {

    }
}
