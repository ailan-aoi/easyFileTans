package com.mec.mfct.file;

public class Test {

    public static void main(String[] args) {
        UnreceivedSection section = new UnreceivedSection(100);
        section.receiveOneSection(0, 100);
        System.out.println(section.isFinished());
    }

}
