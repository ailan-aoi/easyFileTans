package com.mec.mfct.appserver;

import com.mec.mec_rmi.core.RmiClient;
import com.mec.mfct.ResourceOwner.ResourceOwner;
import com.mec.mfct.file.Resource;
import com.mec.mfct.mfctServer.IDefaultConnectConfig;
import com.mec.mfct.mfctServer.IRequestAction;

public class AppServer {
    private RmiClient rmiClient;
    private ResourceOwner owner;
    private String mfctIp;
    private int mfttPort;
    private boolean isJoinSenders;
    private String localHost;

    private IRequestAction requestAction;

    public AppServer() {
        rmiClient = new RmiClient();
        mfttPort = IDefaultConnectConfig.DEFAULT_APP_INTERACTIVE_PORT;
        rmiClient.setPort(mfttPort);
    }

    public void setmfctIp(String mfctIp) {
        this.mfctIp = mfctIp;
        rmiClient.setServerIp(mfctIp);
    }

    public void setmfctPort(int mfttPort) {
        this.mfttPort = mfttPort;
        rmiClient.setPort(mfttPort);
    }

    public void joinSenderServer(String localHost) {
        isJoinSenders = true;
        this.localHost = localHost;
    }

    public void alterResource(int resourceId, String absoultePartPath) {
        Resource resource = createResource(resourceId, absoultePartPath);
        if (requestAction == null) {
            this.requestAction = rmiClient.getProxy(IRequestAction.class);
        }
        requestAction.alterResource(resourceId, resource);
    }

    public void registry(int resourceId, String absoultePartPath) {
        registryPart(resourceId, createResource(resourceId, absoultePartPath));
    }

    private Resource createResource(int resourceId, String absoultePartPath) {
        Resource resource = new Resource();
        resource.scanResource(absoultePartPath);
        resource.setResourceId(resourceId);

        return resource;
    }

    private void registryPart(int resourceId, Resource resource) {
        if (requestAction == null) {
            this.requestAction = rmiClient.getProxy(IRequestAction.class);
        }
        boolean res = requestAction.registryResource(resourceId, resource);
        if (res) {
            System.out.println("资源注册成功!!");
            if (isJoinSenders) {
                res = requestAction.registryResourceOwner(resourceId, localHost);
                if (res) {
                    if (owner == null || !owner.isStartup()) {
                        System.out.println("owner注册成功");
                        owner = new ResourceOwner();
                        owner.addResource(resourceId, resource);
                        owner.setMfcfIp(mfctIp);
                        owner.setLocalHost(localHost);
                        owner.startup();
                    } else {
                        owner.addResource(resourceId, resource);
                    }
                } else {
                    System.out.println("owner注册失败");
                }
            }
        } else {
            System.out.println("资源注册失败！！");
        }
    }

}
