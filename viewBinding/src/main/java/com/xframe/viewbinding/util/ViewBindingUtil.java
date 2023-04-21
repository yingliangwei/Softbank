package com.xframe.viewbinding.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 类转换View实体类
 */
public class ViewBindingUtil {

    @SuppressWarnings("unchecked")
    @NonNull
    public static <Binding extends ViewBinding> Binding bind(Class<?> clazz, View rootView) {
        Class<?> bindingClass = getBindingClass(clazz);
        Binding binding = null;
        if (bindingClass != null) {
            try {
                Method method = bindingClass.getMethod("bind", View.class);
                binding = (Binding) method.invoke(null, rootView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return binding;
    }


    public static <Binding extends ViewBinding> Binding inflate(Class<?> clazz, LayoutInflater inflater) {
        return inflate(clazz, inflater, null);
    }

    public static <Binding extends ViewBinding> Binding inflate(Class<?> clazz, LayoutInflater inflater, ViewGroup root) {
        return inflate(clazz, inflater, root, false);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <Binding extends ViewBinding> Binding inflate(Class<?> clazz, LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        System.out.println(clazz.getName());
        Class<?> bindingClass = getBindingClass(clazz);
        Binding binding = null;
        if (bindingClass != null) {
            try {
                Method method = bindingClass.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
                binding = (Binding) method.invoke(null, inflater, root, attachToRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Objects.requireNonNull(binding);
    }

    private static Class<?> getBindingClass(Class<?> clazz) {
        Class<?> bindingClass = null;
        ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        Type[] types = Objects.requireNonNull(parameterizedType).getActualTypeArguments();
        for (Type type : types) {
            if (type instanceof Class<?>) {
                Class<?> temp = (Class<?>) type;
                if (ViewBinding.class.isAssignableFrom(temp)) {
                    bindingClass = temp;
                }
            }
        }
        return bindingClass;
    }
}
