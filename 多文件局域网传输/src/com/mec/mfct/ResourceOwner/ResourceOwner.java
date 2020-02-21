package com.mec.mfct.ResourceOwner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.mec.mec_rmi.core.RmiClient;
import com.mec.mfct.file.Resource;
import com.mec.mfct.file.Sender;
import com.mec.mfct.file.SenderTask;
import com.mec.mfct.mfctServer.IDefaultConnectConfig;
import com.mec.mfct.mfctServer.IRequestAction;
import com.mec.mfct.util.ServerThread;
import com.my.util.core.TickTick;

public class ResourceOwner {
    private ServerThread mfctClient;
    private RmiClient healthCheckClient;
    private TickTick tick;
    private Sender sender;
    private int mfcfPort;
    private int healthCheckPort;
    private String mfcfIp;
    private String localHost;
    private boolean resourceIsExist;

    private Map<Integer, Resource> resources;

    public ResourceOwner() {
        mfcfPort = IDefaultConnectConfig.DEFAULT_DISTRIBUTE_PORT;
        this.healthCheckPort = IDefaultConnectConfig.DEFAULT_APP_INTERACTIVE_PORT;

        resources = new HashMap<>();
    }

    public void setMfcfPort(int mfcfPort) {
        this.mfcfPort = mfcfPort;
    }

    public void setMfcfIp(String mfcfIp) {
        this.mfcfIp = mfcfIp;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public void addResource(int resourceId, Resource resource) {
        resources.put(resourceId, resource);
    }

    public void startup() {
        init();
        tick.startup();
        mfctClient.startup();
    }

    private void init() {
        sender = new Sender();
        mfctClient = new ServerThread(mfcfPort) {
            @Override
            public void doSomething(Socket socket) {
                try {
                    ObjectInputStream dis = new ObjectInputStream(socket.getInputStream());
                    SenderTask task = (SenderTask)dis.readObject();
                    Resource resource = resources.get(task.getResourceId());
                    sender.setResource(resource);
                    sender.setSenderTask(task);
                    sender.startup();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        healthCheckClient = new RmiClient();
        healthCheckClient.setServerIp(mfcfIp);
        healthCheckClient.setPort(healthCheckPort);
        IRequestAction requestAction = healthCheckClient.getProxy(IRequestAction.class);

        tick = new TickTick() {
            @Override
            public void doSomething() {
                Set<Integer> sets = resources.keySet();
                for (int resourceId : sets) {
                    resourceIsExist = resources.get(resourceId).checkAllFileExist();
                    if (!resourceIsExist) {
                        continue;
                    }
                    System.out.println("resourceIsExist" + resourceIsExist);
                    requestAction.healthCheck(resourceId, localHost, resourceIsExist);
                }
            }
        }.setTiming(IDefaultConnectConfig.DEFAULT_FLASHTIME);
    }

    public boolean isStartup() {
        return tick.isActive();
    }

    public void stop() {
        mfctClient.stopAll();
        tick.stop();
    }
}
