package com.mec.mfct.test;

import com.mec.mfct.appserver.AppServer;

public class AppServerTest {

    public static void main(String[] args) {
        AppServer appServer = new AppServer();
        appServer.setmfctIp("192.168.1.19");
        appServer.joinSenderServer("192.168.1.19");
        appServer.registry(1,  "D:\\ProgrammeStudy\\temp");
    }

}
