package com.example.softbank.socket.task;

import com.example.softbank.socket.SocketManage;

public class HeartbeatTask extends Thread {
    private int size = 3;
    private final SocketManage socketManage;
    private boolean isRun;

    public HeartbeatTask(SocketManage socketManage) {
        this.socketManage = socketManage;
        isRun = false;
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    @Override
    public void run() {
        while (true) {
            if (isRun) {
                break;
            }
            if (size <= 0) {
                // System.out.println("HeartbeatTask SocketChannel");
                socketManage.close();
                break;
            }

            if (socketManage.getSocketChannel() != null && socketManage.getSocketChannel().isConnected()) {
                boolean is = socketManage.print("0");
                if (is) {
                    try {
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        socketManage.close();
                        break;
                    }
                    if (socketManage.isHeartbeatTask()) {
                        // System.out.println("心跳成功");
                        size = 3;
                        socketManage.setHeartbeatTask(false);
                    } else {
                        size = size - 1;
                        //System.out.println("心跳异常次数" + size);
                    }
                }
            } else {
                // System.out.println("断开连接");
                socketManage.close();
                break;
            }
        }
    }
}
