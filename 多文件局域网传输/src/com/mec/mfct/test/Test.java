package com.mec.mfct.test;

import org.apache.log4j.Logger;

public class Test {

    public static void main(String[] args) {
//        Logger registry = Logger.getLogger("registry");
        Logger login = Logger.getLogger("login");

        login.info("测试已经写入的文件");
        login.trace("严重错误");
        login.debug("这里出现了问题");
        login.error("出现了错误");
    }

}
