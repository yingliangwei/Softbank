package com.example.softbank.socket.listener;


import com.example.softbank.socket.SocketManage;

public interface OnConnection {
    void success(SocketManage manage);

    default void error(SocketManage manage) {
    }
}
